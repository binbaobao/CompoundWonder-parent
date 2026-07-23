package com.compoundwonder.core.processor;

import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.orderbook.TradeOrderIntent;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.common.strategy.trade.TradeTriggerType;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.StrategyExecutionSession;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.core.engine.TradeExecutionState;
import lombok.extern.slf4j.Slf4j;

/**
 * 所有策略会话共用的交易执行流水线。
 *
 * <p>交易所 Handler 只负责更新共享盘口和准备交易所特有的行情参数；本类按固定顺序
 * 对每个策略会话执行模板、推进独立状态并输出带策略来源标识的订单意图。</p>
 *
 * <p>所有下单路径都先调用网关，再推进状态并提交规则记录。同步回测网关抛出异常时，
 * 当前信号不会被标成已下单；异常处理器随后整股停用，避免半提交状态继续交易。</p>
 */
@Slf4j
final class UnifiedTradeExecutor {
    private final OrderExecutionGateway executionGateway;

    UnifiedTradeExecutor(OrderExecutionGateway executionGateway) {
        this.executionGateway = executionGateway;
    }

    void processControl(OrderBookSession marketSession, TickData event) {
        for (StrategyExecutionSession session : marketSession.strategySessions()) {
            TradeExecutionState state = session.executionState();
            int status = state.transactionStatus();
            if (status >= 0 && status != event.type) {
                state.transactionStatus(event.type);
                log.info("修改股票 {} 策略会话 {} 监控状态，由 {} -> {}",
                        event.symbolId, session.key().sessionId(), status, event.type);
            } else if (status < 0) {
                // 卖出监控下，控制事件 0 只停用；其他值沿用旧链路立即发出快速卖单。
                if (event.type == 0) {
                    state.disable();
                } else {
                    submitQuickSell(session, marketSession.orderBook().getLastPrice());
                }
            }
        }
    }

    void processAveragePriceSell(OrderBookSession marketSession, int calculateIndex,
                                 TickData event, RuleRecordBuffer records) {
        if (calculateIndex < 3) return;
        for (StrategyExecutionSession session : marketSession.strategySessions()) {
            if (!session.template().supports(TradeTriggerType.MINUTE_CLOSE)
                    || !session.executionState().isSellMonitoring()
                    || !session.template().averagePriceSell().evaluate(
                    calculateIndex, session, records.nextRecord(session))) {
                continue;
            }
            submitQuickSell(session, event.price);
            session.executionState().beginSellOrder();
            records.commit();
        }
    }

    void disableWeakFirstBoardSessions(OrderBookSession marketSession, int marketTime) {
        if (!shouldDisableWeakFirstBoard(
                marketTime, marketSession.orderBook().getStatus())) {
            return;
        }
        for (StrategyExecutionSession session : marketSession.strategySessions()) {
            if (session.executionState().isBuyMonitoring() && session.getLbcs() == 1) {
                session.executionState().disable();
            }
        }
    }

    /**
     * 只关闭 10 点仍从未触板的首板任务。偶数状态不等于弱势未触板：状态 2、4 等
     * 表示已经炸过板，后续仍可能回封，必须继续监控。
     */
    static boolean shouldDisableWeakFirstBoard(int marketTime, int limitUpStatus) {
        return marketTime >= ConstantUtil.TIME_1000 && limitUpStatus == 0;
    }

    void processContinuous(OrderBookSession marketSession, TickData event,
                           RuleRecordBuffer records) {
        OrderBook orderBook = marketSession.orderBook();
        for (StrategyExecutionSession session : marketSession.strategySessions()) {
            if (!session.template().supports(TradeTriggerType.CONTINUOUS_TICK)) continue;
            TradeExecutionState state = session.executionState();
            if (state.isBuyMonitoring() && event.time >= ConstantUtil.TIME_931
                    && session.template().continuousBuy()
                    .shouldEnableFirstBoardTradingMode(session)) {
                submitEnableFirstBoardMode(session);
            }
            if (state.isBuyMonitoring()
                    && session.template().continuousBuy().isTimeAllowed(session, event.time)
                    && session.template().continuousBuy().evaluate(
                    session, records.nextRecord(session))) {
                // 网关成功返回后再落状态和规则记录，保持一条信号的提交原子性。
                submitBuy(session, event.symbolId, orderBook.getTime());
                state.beginBuyOrder();
                records.commit();
                log.info("打板股票 {} sessionId={} 触发单号={}",
                        event.symbolId, session.key().sessionId(), event.orderId);
            }
            if (state.isSellMonitoring()
                    && session.template().continuousSell().evaluate(
                    session, records.nextRecord(session))) {
                // 卖出与买入使用相同的“网关 -> 状态 -> 规则记录”顺序。
                submitQuickSell(session, orderBook.getLastPrice());
                state.beginSellOrder();
                records.commit();
                log.info("卖出股票 {} sessionId={} 触发单号={}",
                        event.symbolId, session.key().sessionId(), event.orderId);
            }
        }
    }

    void submitBuy(StrategyExecutionSession session, int symbolId, int time) {
        executionGateway.execute(TradeOrderIntent.buy(
                session.key().sessionId(), session.key().strategyId(), session.getDate(),
                symbolId, session.getSymbol(), session.getLimitUpPrice(), time));
    }

    void submitSell(StrategyExecutionSession session, int price) {
        executionGateway.execute(TradeOrderIntent.sell(
                session.key().sessionId(), session.key().strategyId(), session.getSymbol(),
                price, session.getLimitDownPrice()));
    }

    void submitQuickSell(StrategyExecutionSession session, int price) {
        executionGateway.execute(TradeOrderIntent.quickSell(
                session.key().sessionId(), session.key().strategyId(), session.getSymbol(),
                price, session.getLimitDownPrice()));
    }

    void submitCancel(StrategyExecutionSession session) {
        executionGateway.execute(TradeOrderIntent.cancel(
                session.key().sessionId(), session.key().strategyId(), session.getSymbol()));
    }

    private void submitEnableFirstBoardMode(StrategyExecutionSession session) {
        executionGateway.execute(TradeOrderIntent.enableFirstBoardMode(
                session.key().sessionId(), session.key().strategyId(), session.getSymbol()));
    }
}
