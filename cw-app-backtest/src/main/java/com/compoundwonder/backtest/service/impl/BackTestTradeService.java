package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.DisruptorOrderBookEngine;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.PriceLevel;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.compoundwonder.util.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 单股票历史订单簿回测编排服务。
 *
 * <p>负责准备订单簿、驱动可替换的数据源、等待 Disruptor 消费完成并返回规则记录；
 * 不负责 Parquet 或 ClickHouse 的具体读取实现。</p>
 */
@Slf4j
@Service
public class BackTestTradeService {

    private final DisruptorOrderBookEngine orderBookEngine;
    private final BacktestTickDataSource tickDataSource;
    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final BacktestOrderExecutionGateway executionGateway;
    private final Duration replayTimeout;

    public BackTestTradeService(DisruptorOrderBookEngine orderBookEngine,
                                BacktestTickDataSource tickDataSource,
                                StockDailyService stockDailyService,
                                StockTradeCalendarService stockTradeCalendarService,
                                StockEmotionCycleDailyService stockEmotionCycleDailyService,
                                BacktestOrderExecutionGateway executionGateway,
                                @Value("${backtest.replay-timeout-seconds:120}") long replayTimeoutSeconds) {
        this.orderBookEngine = orderBookEngine;
        this.tickDataSource = tickDataSource;
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
        this.executionGateway = executionGateway;
        this.replayTimeout = Duration.ofSeconds(replayTimeoutSeconds);
    }

    /**
     * 执行一次单股票、单交易日回测。
     *
     * <p>Disruptor 在应用启动时已经长期运行。该方法只清理并注册本轮订单簿，
     * 不启动或关闭 Disruptor。方法加锁是为了防止多个 HTTP 请求共享同一套 Handler
     * 私有数组时互相覆盖。</p>
     *
     * @param date 回测交易日，格式 yyyy-MM-dd
     * @param stockCode 60 或 00 开头的六位主板代码
     * @param direction 1 表示测试买入规则，2 表示测试卖出规则
     * @return 本轮对应方向触发的规则记录；买入回放同时返回集合竞价撤单记录
     */
    public List<RuleRecordDTO> backTest(String date, String stockCode, int direction) {
        validateDirection(direction);
        validateStockCode(stockCode);
        LocalDate tradeDate = LocalDate.parse(date);
        BacktestReplayMode mode = direction == 1 ? BacktestReplayMode.BUY : BacktestReplayMode.SELL;
        return replay(tradeDate, stockCode, mode, null).records().stream()
                .filter(record -> matchesDirection(record, direction))
                .toList();
    }

    /**
     * 按指定场景执行一次单股票、单交易日订单簿回放。
     *
     * <p>{@link BacktestReplayMode#BUY_AFTER_TIME} 仍会从当天第一条 Tick 开始构建订单簿，
     * 只是到达允许时间后的第一条 Tick 前才打开买入监控。</p>
     */
    public synchronized BacktestReplayResult replay(LocalDate tradeDate, String stockCode,
                                                     BacktestReplayMode mode, Integer allowedAfterTime) {
        validateStockCode(stockCode);
        validateReplayMode(mode, allowedAfterTime);
        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        OrderBook orderBook = buildOrderBook(tradeDate, stockCode, mode);

        orderBookEngine.reset();
        executionGateway.clear();
        orderBookEngine.registerOrderBook(symbolId, orderBook);

        boolean replayStarted = false;
        boolean processed = false;
        try {
            replayStarted = true;
            Consumer<TickData> consumer = replayConsumer(symbolId, mode, allowedAfterTime);
            long tickCount = tickDataSource.replay(tradeDate, stockCode, consumer);
            if (tickCount == 0) {
                throw new IllegalStateException(tradeDate + " 没有找到股票 " + stockCode + " 的 Level2 Tick");
            }

            orderBookEngine.awaitProcessed(replayTimeout);
            processed = true;
            logLimitUpBuyQueue(orderBook);
            List<RuleRecordDTO> records = orderBookEngine.drainRuleRecords();
            fillLastOrderTime(records.stream()
                    .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_BUY)
                            .equals(record.getActionType()))
                    .toList(), orderBook);
            BacktestReplayResult result = new BacktestReplayResult(
                    tradeDate, stockCode, orderBook.getSecurityName(), mode, records,
                    orderBook.getTransactionStatus(), orderBook.getLastPriceOrderTime(),
                    orderBook.getLimitUpPrice(), orderBook.getLastPrice(), tickCount);
            log.info("回测完成 date={}, stockCode={}, mode={}, tickCount={}, ruleCount={}, orderBook={}",
                    tradeDate, stockCode, mode, tickCount, records.size(), orderBook);
            return result;
        } finally {
            if (replayStarted && !processed) {
                awaitPartiallyPublishedTicks();
            }
            orderBookEngine.reset();
            executionGateway.clear();
        }
    }

    private Consumer<TickData> replayConsumer(int symbolId, BacktestReplayMode mode,
                                              Integer allowedAfterTime) {
        if (mode != BacktestReplayMode.BUY_AFTER_TIME) {
            return orderBookEngine::publish;
        }
        AtomicBoolean monitoringOpened = new AtomicBoolean(false);
        return tick -> {
            if (!monitoringOpened.get() && tick.time > allowedAfterTime) {
                TickData control = new TickData();
                control.symbolId = symbolId;
                control.time = tick.time;
                control.dataType = 3;
                control.type = 1;
                orderBookEngine.publish(control);
                monitoringOpened.set(true);
            }
            orderBookEngine.publish(tick);
        };
    }

    /**
     * 按回放结束时的最新成交价，从该价位委托队头补充规则记录的最后委托时间。
     */
    static void fillLastOrderTime(List<RuleRecordDTO> records, OrderBook orderBook) {
        int lastOrderTime = orderBook.getLastPriceOrderTime();
        if (lastOrderTime == 0) {
            return;
        }
        for (RuleRecordDTO record : records) {
            record.setLastOrderTime(lastOrderTime);
        }
    }

    /**
     * 回放完成后打印一次涨停价买方委托队列，便于与旧 List 订单簿结果核对。
     */
    private void logLimitUpBuyQueue(OrderBook orderBook) {
        if (orderBook.getStatus() % 2 != 1) {
            return;
        }

        PriceLevel limitUpLevel = orderBook.getPriceLevel(orderBook.getLimitUpPrice());
        if (limitUpLevel == null || limitUpLevel.getBuyHead() == null) {
            log.warn("股票 {} 状态为涨停，但涨停价 {} 没有买方委托队列",
                    orderBook.getSymbol(), orderBook.getLimitUpPrice());
            return;
        }

        TickNode head = limitUpLevel.getBuyHead();
        TickNode tail = limitUpLevel.getBuyTail();
        int printedCount = 0;
        StringBuilder quantities = new StringBuilder();
        for (TickNode node = head; node != null && printedCount < 200; node = node.getNext()) {
            if (printedCount > 0 && printedCount % 10 != 0) {
                quantities.append(',');
            }
            quantities.append(node.getQuantity());
            printedCount++;
            if (printedCount % 10 == 0) {
                quantities.append(System.lineSeparator());
            }
        }

        log.info("股票 {} 涨停封单共计：{} 单，共计：{} 股，"
                        + "队首委托(剩余数量:{},时间:{},订单号:{})，"
                        + "队尾委托(剩余数量:{},时间:{},订单号:{})",
                orderBook.getSymbol(), limitUpLevel.getBuyOrderCount(), limitUpLevel.getBuyQuantity(),
                head.getQuantity(), head.getTime(), head.getOrderId(),
                tail.getQuantity(), tail.getTime(), tail.getOrderId());
        log.info("股票 {} 涨停封单前 {} 单（队首到队尾）：{}{}",
                orderBook.getSymbol(), printedCount, System.lineSeparator(), quantities);
    }

    /**
     * 使用回测当天日 K 的参考昨收价初始化订单簿价格区间。
     *
     * <p>当天日 K 提供 prevClose 和流通股本；名称和所有策略统计仍使用回测日前的数据。</p>
     */
    OrderBook buildOrderBook(LocalDate tradeDate, String stockCode, int direction) {
        validateDirection(direction);
        return buildOrderBook(tradeDate, stockCode,
                direction == 1 ? BacktestReplayMode.BUY : BacktestReplayMode.SELL);
    }

    OrderBook buildOrderBook(LocalDate tradeDate, String stockCode, BacktestReplayMode mode) {
        List<StockDailyEntity> dailyRows = stockDailyService.list(
                Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, stockCode)
                        .le(StockDailyEntity::getTradeDate, tradeDate)
                        .orderByDesc(StockDailyEntity::getTradeDate)
                        .last("LIMIT 201"));
        if (dailyRows.isEmpty() || !tradeDate.equals(dailyRows.get(0).getTradeDate())) {
            throw new IllegalArgumentException(tradeDate + " 没有股票 " + stockCode + " 的当日日 K 数据");
        }

        StockDailyEntity currentDaily = dailyRows.get(0);
        List<StockDailyEntity> history = dailyRows.stream()
                .filter(daily -> daily.getTradeDate() != null && daily.getTradeDate().isBefore(tradeDate))
                .limit(200)
                .toList();
        if (history.isEmpty()) {
            throw new IllegalArgumentException(tradeDate + " 之前没有股票 " + stockCode + " 的日 K 数据");
        }
        StockDailyEntity previousDaily = history.get(0);
        if (currentDaily.getPrevClose() == null || currentDaily.getPrevClose() <= 0) {
            throw new IllegalStateException("回测当日日 K 的 prevClose 缺失: " + stockCode + " " + tradeDate);
        }
        if (currentDaily.getFloatShares() == null || currentDaily.getFloatShares() <= 0) {
            throw new IllegalStateException("回测当日日 K 的流通股本缺失: " + stockCode + " " + tradeDate);
        }
        long circulation = currentDaily.getFloatShares();
        long maxVolume = history.stream()
                .map(StockDailyEntity::getVolume)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        OrderBook orderBook = new OrderBook(
                stockCode, circulation, currentDaily.getPrevClose(), maxVolume);
        orderBook.setSecurityName(previousDaily.getStockName());
        orderBook.setDate(tradeDate.toString());
        orderBook.setLbcs(Math.max(0, valueOrZero(previousDaily.getConsecutiveLimitUpDays())));
        orderBook.setYesterdayTurnover(valueOrZero(previousDaily.getTurnoverRate()));
        orderBook.setInitialMarketValue(calculateInitialMarketValue(previousDaily, orderBook.getLbcs()));
        orderBook.setMaxHs(maxVolume * 100.0 / circulation);
        orderBook.setTransactionStatus(initialTransactionStatus(mode));
        if (mode == BacktestReplayMode.SELL) {
            initializeSellHistory(orderBook, history, tradeDate, stockCode);
        }
        return orderBook;
    }

    private int initialTransactionStatus(BacktestReplayMode mode) {
        return switch (mode) {
            case BUY -> 1;
            case SELL -> -1;
            case OVERNIGHT_BUY -> 2;
            case BUY_AFTER_TIME -> 0;
        };
    }

    /**
     * 按旧实盘持仓初始化口径补充卖出规则依赖的最近三日日 K 统计。
     */
    private void initializeSellHistory(OrderBook orderBook, List<StockDailyEntity> history,
                                       LocalDate tradeDate, String stockCode) {
        if (history.size() < 3) {
            throw new IllegalArgumentException(tradeDate + " 之前股票 " + stockCode
                    + " 的日 K 不足 3 个交易日，无法初始化卖出订单簿");
        }

        StockDailyEntity yesterday = history.get(0);
        StockDailyEntity twoDaysAgo = history.get(1);
        StockDailyEntity threeDaysAgo = history.get(2);
        double yesterdayTurnover = valueOrZero(yesterday.getTurnoverRate());
        double twoDaysAgoTurnover = valueOrZero(twoDaysAgo.getTurnoverRate());
        double threeDaysAgoTurnover = valueOrZero(threeDaysAgo.getTurnoverRate());

        orderBook.setTwoDaysTurnover((yesterdayTurnover + twoDaysAgoTurnover) / 2);
        orderBook.setThreeDaysTurnover(
                (yesterdayTurnover + twoDaysAgoTurnover + threeDaysAgoTurnover) / 3);
        orderBook.setOneWordLimitUp(countConsecutiveOneWordLimitUps(
                yesterday, twoDaysAgo, threeDaysAgo));

        Integer averageLimitUpHeight = stockEmotionCycleDailyService
                .queryRecentAverageLimitUpHeight(tradeDate.minusDays(15), tradeDate);
        if (averageLimitUpHeight == null) {
            throw new IllegalStateException(tradeDate + " 之前 15 日没有市场最高连板数据");
        }
        orderBook.setAverageLimitUpHeight(averageLimitUpHeight);

        LocalDate nextTradeDay = stockTradeCalendarService.findNextTradeDay(tradeDate);
        if (nextTradeDay == null) {
            throw new IllegalStateException(tradeDate + " 之后没有交易日历数据");
        }
        orderBook.setNextTradingDay((int) ChronoUnit.DAYS.between(tradeDate, nextTradeDay) - 1);
    }

    private int countConsecutiveOneWordLimitUps(StockDailyEntity... dailyRows) {
        int count = 0;
        for (StockDailyEntity daily : dailyRows) {
            if (valueOrZero(daily.getKlineState()) != 3) {
                break;
            }
            count++;
        }
        return count;
    }

    private int calculateInitialMarketValue(StockDailyEntity previousDaily, int consecutiveLimitUpDays) {
        if (previousDaily.getFloatMarketCap() == null) {
            return 0;
        }
        return (int) Math.round(previousDaily.getFloatMarketCap()
                / Math.pow(1.1, consecutiveLimitUpDays));
    }

    private boolean matchesDirection(RuleRecordDTO record, int direction) {
        Integer actionType = record.getActionType();
        return direction == 1
                ? Integer.valueOf(RuleConstant.TRADING_MODE_BUY).equals(actionType)
                    || Integer.valueOf(RuleConstant.TRADING_MODE_CANCEL).equals(actionType)
                : Integer.valueOf(RuleConstant.TRADING_MODE_SELL).equals(actionType);
    }

    private void validateDirection(int direction) {
        if (direction != 1 && direction != 2) {
            throw new IllegalArgumentException("direction 只能是 1（买入）或 2（卖出）");
        }
    }

    private void validateReplayMode(BacktestReplayMode mode, Integer allowedAfterTime) {
        if (mode == null) {
            throw new IllegalArgumentException("回放模式不能为空");
        }
        if (mode == BacktestReplayMode.BUY_AFTER_TIME && allowedAfterTime == null) {
            throw new IllegalArgumentException("卖出后买入回放必须提供允许买入时间");
        }
    }

    private void validateStockCode(String stockCode) {
        if (stockCode == null || !stockCode.matches("(?:60|00)\\d{4}")) {
            throw new IllegalArgumentException("当前回测只支持 60/00 沪深主板股票: " + stockCode);
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0D : value;
    }

    /**
     * 数据源中途失败时，先等待已经发布的 Tick 消费完成，再清理 Handler 私有数组。
     */
    private void awaitPartiallyPublishedTicks() {
        try {
            orderBookEngine.awaitProcessed(replayTimeout);
        } catch (RuntimeException exception) {
            log.warn("等待部分回测 Tick 消费完成失败，准备清理本轮状态", exception);
        }
    }
}
