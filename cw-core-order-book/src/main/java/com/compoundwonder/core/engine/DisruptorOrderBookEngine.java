package com.compoundwonder.core.engine;

import com.compoundwonder.core.processor.TickEventShangHaiHandler;
import com.compoundwonder.core.processor.TickEventShenZhenHandler;
import com.compoundwonder.core.service.OrderBookRepository;
import com.compoundwonder.core.service.OrderExecutionGateway;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.util.SymbolUtil;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.EventHandlerIdentity;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * 沪深主板订单簿的统一运行引擎。
 *
 * <p>主要职责：</p>
 * <ol>
 *     <li>分别创建并管理上海、深圳两个 Disruptor。</li>
 *     <li>把订单簿注册到对应交易所 Handler 的私有数组中。</li>
 *     <li>根据证券代码把 Tick 路由到正确的 RingBuffer。</li>
 *     <li>在回测结束时等待已发布 Tick 全部消费完成。</li>
 *     <li>在两次回测之间清理订单簿和回放进度。</li>
 * </ol>
 *
 * <p>该类只负责订单簿运行机制，不负责读取 Parquet、查询数据库或模拟账户成交。
 * Spring Bean、线程策略和等待策略由 backtest 或实盘应用配置。</p>
 *
 * <p>一次回测的推荐调用顺序：</p>
 * <pre>
 * start()
 * registerOrderBook(...)
 * publish(...) // 重复发布 Tick
 * awaitProcessed(...)
 * 读取订单簿和规则结果
 * reset()
 * </pre>
 */
public final class DisruptorOrderBookEngine implements AutoCloseable {

    /** 上海主板对应的 Disruptor 和 Handler 下标。 */
    private static final int SHANGHAI_HANDLER = 0;

    /** 深圳主板对应的 Disruptor 和 Handler 下标。 */
    private static final int SHENZHEN_HANDLER = 1;

    /**
     * 回测编排层使用的订单簿索引。
     * Handler 处理 Tick 时不会查询这里，而是访问各自的私有订单簿数组。
     */
    private final OrderBookRepository repository;

    /** 下标 0 为上海 Disruptor，下标 1 为深圳 Disruptor。 */
    private final Disruptor<TickData>[] disruptors;

    /** 与 disruptors 下标一一对应，用于注册订单簿和查询消费序号。 */
    private final EventHandlerIdentity[] handlers;

    /**
     * 两个 RingBuffer 最近一次发布的序号。
     * awaitProcessed() 根据该序号判断本轮回放是否已经消费完成。
     */
    private final long[] lastPublishedSequences = {-1L, -1L};

    /** 防止重复启动或重复关闭 Disruptor。 */
    private boolean started;

    /**
     * 创建沪深两个订单簿处理通道并绑定对应 Handler，但不会立即启动线程。
     *
     * @param repository 回测编排和结果查询使用的订单簿仓库
     * @param executionGateway 策略触发后的交易动作出口，回测与实盘提供不同实现
     * @param ringBufferSize RingBuffer 容量，必须是 2 的幂
     * @param threadNamePrefix 消费线程名称前缀，便于日志和性能诊断
     * @param producerType 生产者模式；回测通常为 SINGLE，实盘可按行情线程模型配置
     * @param waitStrategyFactory 等待策略工厂；沪深 Disruptor 各创建一个独立实例
     * @throws IllegalArgumentException 当 RingBuffer 容量不是 2 的幂时抛出
     */
    @SuppressWarnings("unchecked")
    public DisruptorOrderBookEngine(OrderBookRepository repository,
                                    OrderExecutionGateway executionGateway,
                                    int ringBufferSize,
                                    String threadNamePrefix,
                                    ProducerType producerType,
                                    Supplier<WaitStrategy> waitStrategyFactory) {
        if (Integer.bitCount(ringBufferSize) != 1) {
            throw new IllegalArgumentException("ringBufferSize must be a power of two");
        }
        this.repository = repository;
        this.disruptors = new Disruptor[2];
        this.handlers = new EventHandlerIdentity[]{
                new TickEventShangHaiHandler(executionGateway),
                new TickEventShenZhenHandler(executionGateway)
        };
        disruptors[SHANGHAI_HANDLER] = createDisruptor(
                ringBufferSize, threadNamePrefix + "sh-", producerType, waitStrategyFactory.get());
        disruptors[SHENZHEN_HANDLER] = createDisruptor(
                ringBufferSize, threadNamePrefix + "sz-", producerType, waitStrategyFactory.get());
        disruptors[SHANGHAI_HANDLER].handleEventsWith((TickEventShangHaiHandler) handlers[SHANGHAI_HANDLER]);
        disruptors[SHENZHEN_HANDLER].handleEventsWith((TickEventShenZhenHandler) handlers[SHENZHEN_HANDLER]);
    }

    /**
     * 创建单个交易所的 Disruptor。
     *
     * <p>线程设置为 daemon，避免应用异常退出时仅因订单簿线程而阻止 JVM 结束；
     * 正常关闭仍应调用 close()，让队列中的事件得到处理。</p>
     *
     * @param ringBufferSize RingBuffer 容量
     * @param threadName 该交易所消费线程名称前缀
     * @param producerType 生产者类型
     * @param waitStrategy 消费者等待策略
     * @return 尚未启动的 Disruptor
     */
    private Disruptor<TickData> createDisruptor(int ringBufferSize, String threadName,
                                                ProducerType producerType, WaitStrategy waitStrategy) {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, threadName + "1");
            thread.setDaemon(true);
            return thread;
        };
        return new Disruptor<>(TickData::new, ringBufferSize, threadFactory,
                producerType, waitStrategy);
    }

    /**
     * 启动上海和深圳两个 Disruptor 消费线程。
     *
     * <p>该方法幂等，重复调用不会重复启动线程。Spring 配置通过 initMethod 调用它，
     * 单元测试或非 Spring 环境也可以显式调用。</p>
     */
    public synchronized void start() {
        if (started) {
            return;
        }
        for (Disruptor<TickData> disruptor : disruptors) {
            disruptor.start();
        }
        started = true;
    }

    /**
     * 将一条 Tick 发布到对应主板交易所的 RingBuffer。
     *
     * <p>RingBuffer 中的 TickData 会循环复用，因此这里复制字段而不是保存 source 引用。
     * 发布前必须先通过 registerOrderBook() 注册该证券的订单簿，否则 Handler 会忽略该 Tick。</p>
     *
     * @param source 从 DuckDB、实时行情或其他数据源解析出的 Tick
     * @throws IllegalStateException 当引擎尚未启动时抛出
     * @throws IllegalArgumentException 当证券不属于 60 或 00 主板范围时抛出
     */
    public void publish(TickData source) {
        ensureStarted();
        int handlerIndex = mainBoardHandlerIndex(source.symbolId);
        RingBuffer<TickData> ringBuffer = disruptors[handlerIndex].getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            copy(source, ringBuffer.get(sequence));
        } finally {
            lastPublishedSequences[handlerIndex] = sequence;
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 注册本轮回测需要处理的订单簿。
     *
     * <p>订单簿会同时保存到两处：</p>
     * <ul>
     *     <li>对应交易所 Handler 的私有数组：供 Tick 热路径按数组下标直接访问。</li>
     *     <li>OrderBookRepository：供回测编排层在回放结束后查询结果。</li>
     * </ul>
     *
     * <p>两处保存的是同一个 OrderBook 实例，不会复制订单簿状态。</p>
     *
     * @param symbolId 经过 SymbolUtil 转换的内部证券编号，如 600000 对应 1600000
     * @param orderBook 已完成当日参考昨收价、流通股本等基础数据初始化的订单簿
     * @throws IllegalArgumentException 当证券不属于沪深主板时抛出
     */
    public void registerOrderBook(int symbolId, OrderBook orderBook) {
        int handlerIndex = mainBoardHandlerIndex(symbolId);
        repository.put(symbolId, orderBook);
        if (handlerIndex == SHANGHAI_HANDLER) {
            ((TickEventShangHaiHandler) handlers[handlerIndex]).registerOrderBook(symbolId, orderBook);
        } else {
            ((TickEventShenZhenHandler) handlers[handlerIndex]).registerOrderBook(symbolId, orderBook);
        }
    }

    /**
     * 等待本轮已经发布的所有 Tick 被对应 Handler 消费完成。
     *
     * <p>该方法以两个 RingBuffer 最近发布序号为目标，不依赖固定 sleep，
     * 因而回测结果不会受机器速度或数据量影响。调用成功后才可以读取最终订单簿、
     * 规则记录或调用 reset()。</p>
     *
     * @param timeout 最长等待时间，沪深两个通道共用同一个截止时间
     * @throws IllegalStateException 当引擎未启动或等待超时时抛出
     */
    public void awaitProcessed(Duration timeout) {
        ensureStarted();
        long deadline = System.nanoTime() + timeout.toNanos();
        for (int i = 0; i < disruptors.length; i++) {
            long targetSequence = lastPublishedSequences[i];
            if (targetSequence < 0) {
                continue;
            }
            while (disruptors[i].getSequenceValueFor(handlers[i]) < targetSequence) {
                if (System.nanoTime() >= deadline) {
                    throw new IllegalStateException("Timed out waiting for order book replay to finish");
                }
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
            }
        }
    }

    /**
     * 取出上海、深圳 Handler 本轮已经提交的规则记录并清空缓冲区。
     *
     * <p>必须在 awaitProcessed() 成功之后调用，避免消费线程仍在写入缓冲区。
     * 返回 DTO 副本后，底层 RuleRecord 对象会继续留在缓冲区中供下一轮回测复用。</p>
     *
     * @return 本轮沪深两个 Handler 的规则触发记录
     */
    public List<RuleRecordDTO> drainRuleRecords() {
        List<RuleRecordDTO> result = new ArrayList<>();
        drainRuleRecords(((TickEventShangHaiHandler) handlers[SHANGHAI_HANDLER]).getRuleRecords(), result);
        drainRuleRecords(((TickEventShenZhenHandler) handlers[SHENZHEN_HANDLER]).getRuleRecords(), result);
        return result;
    }

    /**
     * 将单个 Handler 的规则缓冲区转换成 DTO，并重置缓冲区写入位置。
     */
    private void drainRuleRecords(RuleRecordBuffer buffer, List<RuleRecordDTO> target) {
        RuleRecord[] records = buffer.records();
        for (int i = 0; i < buffer.size(); i++) {
            target.add(records[i].toDTO());
        }
        buffer.clear();
    }

    /**
     * 清理本轮回测状态，为下一轮回测复用当前 Disruptor。
     *
     * <p>该方法清空沪深 Handler 私有订单簿数组、外部订单簿索引和发布序号，
     * 但不会销毁或重启 Disruptor 线程。必须在 awaitProcessed() 成功之后调用，
     * 否则尚未处理的 Tick 可能找不到订单簿。</p>
     */
    public void reset() {
        ((TickEventShangHaiHandler) handlers[SHANGHAI_HANDLER]).reset();
        ((TickEventShenZhenHandler) handlers[SHENZHEN_HANDLER]).reset();
        repository.clear();
        for (int i = 0; i < lastPublishedSequences.length; i++) {
            lastPublishedSequences[i] = -1L;
        }
    }

    /**
     * 获取指定交易所的底层 Disruptor。
     *
     * <p>该方法主要用于兼容现有 DisruptorManager；一般回测代码应优先使用 publish()，
     * 避免绕过本类维护的已发布序号。</p>
     *
     * @param handlerIndex 0 表示上海，1 表示深圳
     * @return 对应交易所的 Disruptor
     * @throws IllegalArgumentException 当下标不是 0 或 1 时抛出
     */
    public Disruptor<TickData> disruptor(int handlerIndex) {
        if (handlerIndex < 0 || handlerIndex >= disruptors.length) {
            throw new IllegalArgumentException("handlerIndex must be 0 or 1");
        }
        return disruptors[handlerIndex];
    }

    /**
     * 将内部证券编号转换成当前引擎使用的两个主板 Handler 下标。
     *
     * @param symbolId 内部证券编号
     * @return 0 表示上海主板，1 表示深圳主板
     * @throws IllegalArgumentException 当证券属于创业板、科创板或其他未支持市场时抛出
     */
    private int mainBoardHandlerIndex(int symbolId) {
        int handlerIndex = SymbolUtil.getHandlerIndex(symbolId);
        if (handlerIndex != SHANGHAI_HANDLER && handlerIndex != SHENZHEN_HANDLER) {
            throw new IllegalArgumentException("Only Shanghai and Shenzhen main-board symbols are supported: " + symbolId);
        }
        return handlerIndex;
    }

    /**
     * 校验需要消费线程参与的操作是否在引擎启动后执行。
     *
     * @throws IllegalStateException 当引擎尚未启动时抛出
     */
    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("Order book engine has not been started");
        }
    }

    /**
     * 把外部 Tick 内容复制进 RingBuffer 预分配对象。
     *
     * <p>time2 和 time3 属于本轮处理耗时标记，发布新事件时必须清零，
     * 防止 RingBuffer 对象复用后残留上一条事件的数据。</p>
     *
     * @param source 外部解析得到的 Tick
     * @param target RingBuffer 中预分配且会循环复用的 Tick
     */
    private void copy(TickData source, TickData target) {
        target.symbolId = source.symbolId;
        target.time = source.time;
        target.orderId = source.orderId;
        target.price = source.price;
        target.quantity = source.quantity;
        target.amount = source.amount;
        target.buyerOrderId = source.buyerOrderId;
        target.sellerOrderId = source.sellerOrderId;
        target.dataType = source.dataType;
        target.direction = source.direction;
        target.type = source.type;
        target.time1 = source.time1;
        target.time2 = 0L;
        target.time3 = 0L;
    }

    /**
     * 关闭沪深两个 Disruptor。
     *
     * <p>正常情况下最多等待每个通道 5 秒完成队列消费；超时后执行 halt，
     * 防止应用关闭过程无限阻塞。该方法幂等，并由 Spring Bean 的 destroyMethod 调用。</p>
     */
    @Override
    public synchronized void close() {
        if (!started) {
            return;
        }
        for (Disruptor<TickData> disruptor : disruptors) {
            try {
                disruptor.shutdown(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                disruptor.halt();
            }
        }
        started = false;
    }
}
