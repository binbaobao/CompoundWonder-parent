package com.compoundwonder.core.processor;

import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.common.strategy.trade.TradeTriggerType;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.StrategyExecutionSession;
import com.compoundwonder.core.engine.TickData;

/**
 * 上海集合竞价事件执行编排。
 *
 * <p>策略模块负责参数计算、规则判断、日志和规则记录；本类只负责维护相邻快照状态、
 * 调用交易执行网关并推进订单簿交易状态。对象随上海 Handler 常驻，不在热路径创建对象。</p>
 */
final class ShanghaiAuctionEventProcessor {

    private final UnifiedTradeExecutor tradeExecutor;
    ShanghaiAuctionEventProcessor(UnifiedTradeExecutor tradeExecutor) {
        this.tradeExecutor = tradeExecutor;
    }

    /**
     * 处理一张上海早盘或尾盘集合竞价快照。
     *
     * <p>买入与撤单只依据事件到达前的交易状态。某张快照刚触发买入时，不能再使用
     * 同一张快照立刻撤单；从下一张快照开始，挂单必须持续满足封单绝对强度。</p>
     *
     * @param marketSession 共享盘口及本股全部独立策略会话
     * @param event 当前三秒快照
     * @param recordTime Handler 已推进的市场时间
     * @param ruleRecordBuffer Handler 私有预分配规则记录缓冲区
     */
    void process(OrderBookSession marketSession, TickData event, int recordTime,
                 RuleRecordBuffer ruleRecordBuffer) {
        OrderBook orderBook = marketSession.orderBook();
        if (event.time > ConstantUtil.TIME_920) {
            orderBook.updateLowestPrice(event.price, marketSession.spec());
        }

        for (StrategyExecutionSession strategySession : marketSession.strategySessions()) {
            TradeTriggerType triggerType = event.time < ConstantUtil.TIME_930
                    ? TradeTriggerType.OPENING_AUCTION : TradeTriggerType.CLOSING_AUCTION;
            if (!strategySession.template().supports(triggerType)) continue;
            int transactionStatus = strategySession.executionState().transactionStatus();
            // 先保存事件到达前状态，保证本快照的新买单不会被同一快照立刻撤销。
            if (event.time < ConstantUtil.TIME_930) {
                processMorningAuction(strategySession, event, recordTime,
                        transactionStatus, ruleRecordBuffer);
            } else {
                processClosingAuction(strategySession, event, recordTime,
                        transactionStatus, ruleRecordBuffer);
            }
        }
    }

    /** 处理上海早盘集合竞价买入和挂单后的撤单。 */
    private void processMorningAuction(StrategyExecutionSession session, TickData event, int recordTime,
                                      int transactionStatus,
                                      RuleRecordBuffer ruleRecordBuffer) {
        long previousBuyVolume = session.executionState().recordShanghaiAuctionBuyVolume(
                event.buyerOrderId);

        if (transactionStatus == 1) {
            RuleRecord ruleRecord = ruleRecordBuffer.nextRecord(session);
            // 调用当前模式上海集合竞价买入规则。
            if (session.template().shanghaiOpeningAuctionBuy().evaluateBuy(
                    session, event, previousBuyVolume, recordTime, ruleRecord)) {
                // 网关异常时不推进状态、不提交规则，由引擎异常处理器统一停用本股。
                tradeExecutor.submitBuy(session, event.symbolId, event.time);
                session.executionState().transactionStatus(2);
                ruleRecordBuffer.commit();
            }
            return;
        }

        if (transactionStatus == 2
                && event.time < ConstantUtil.TIME_920
                && event.time > ConstantUtil.TIME_91530) {
            RuleRecord ruleRecord = ruleRecordBuffer.nextRecord(session);
            // 调用当前模式上海集合竞价撤单规则；价格规则优先于封单绝对强度规则。
            if (session.template().shanghaiOpeningAuctionBuy().evaluateCancel(
                    session, event, recordTime, ruleRecord)) {
                tradeExecutor.submitCancel(session);
                session.executionState().transactionStatus(1);
                ruleRecordBuffer.commit();
            }
        }
    }

    /** 处理上海尾盘集合竞价卖出；不改写早盘快照买量基准。 */
    private void processClosingAuction(StrategyExecutionSession session, TickData event, int recordTime,
                                      int transactionStatus,
                                      RuleRecordBuffer ruleRecordBuffer) {
        if (transactionStatus != -1
                || event.time < ConstantUtil.TIME_1459
                || event.time >= ConstantUtil.TIME_1500) {
            return;
        }

        RuleRecord ruleRecord = ruleRecordBuffer.nextRecord(session);
        // 调用上海尾盘集合竞价卖出规则。
        if (!session.template().closingAuctionSell().evaluateShanghai(
                session, event, recordTime, ruleRecord)) {
            return;
        }

        tradeExecutor.submitSell(session, session.getLimitDownPrice());
        session.executionState().transactionStatus(-2);
        ruleRecordBuffer.commit();
    }
}
