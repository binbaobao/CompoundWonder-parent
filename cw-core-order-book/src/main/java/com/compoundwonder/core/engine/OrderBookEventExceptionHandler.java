package com.compoundwonder.core.engine;

import com.compoundwonder.core.service.OrderBookRepository;
import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 隔离单条行情事件的处理异常，避免一个股票的异常终止整个市场的消费线程。
 *
 * <p>事件处理失败后会暂停该股票的全部策略会话，但不会停止 Disruptor。异常从 Handler
 * 边界抛出时已经没有可靠的策略会话标识，因此必须整股失效，不能只暂停主会话后让其他
 * 模式在缺失一次事件的盘口上继续下单。后续行情仍会更新其他股票的订单簿。</p>
 */
@Slf4j
final class OrderBookEventExceptionHandler implements ExceptionHandler<TickData> {

    private final OrderBookRepository repository;

    OrderBookEventExceptionHandler(OrderBookRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handleEventException(Throwable ex, long sequence, TickData event) {
        if (event == null) {
            log.error("订单簿事件处理异常，事件为空，sequence={}", sequence, ex);
            return;
        }

        event.time3 = System.nanoTime();
        OrderBookSession session = repository.get(event.symbolId);
        if (session != null) {
            // 异常处理器运行在对应 Handler 的消费线程中，不会与该订单簿的正常写入并发。
            for (StrategyExecutionSession strategySession : session.strategySessions()) {
                strategySession.executionState().disable();
            }
        }
        log.error("订单簿事件处理异常，已暂停该股票交易并继续消费，sequence={}, symbolId={}, dataType={}, "
                        + "time={}, orderId={}, buyerOrderId={}, sellerOrderId={}",
                sequence, event.symbolId, event.dataType, event.time, event.orderId,
                event.buyerOrderId, event.sellerOrderId, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("订单簿消费线程启动异常", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("订单簿消费线程关闭异常", ex);
    }
}
