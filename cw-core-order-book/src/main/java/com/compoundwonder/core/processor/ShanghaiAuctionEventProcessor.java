package com.compoundwonder.core.processor;

import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.TickData;

/**
 * 上海集合竞价事件执行编排。
 *
 * <p>策略模块负责参数计算、规则判断、日志和规则记录；本类只负责维护相邻快照状态、
 * 调用交易执行网关并推进订单簿交易状态。对象随上海 Handler 常驻，不在热路径创建对象。</p>
 */
final class ShanghaiAuctionEventProcessor {

    private final OrderExecutionGateway executionGateway;
    private final TradeDecisionService tradeDecisionService;

    ShanghaiAuctionEventProcessor(OrderExecutionGateway executionGateway,
                                  TradeDecisionService tradeDecisionService) {
        this.executionGateway = executionGateway;
        this.tradeDecisionService = tradeDecisionService;
    }

    /**
     * 处理一张上海早盘或尾盘集合竞价快照。
     *
     * <p>买入与撤单只依据事件到达前的交易状态。某张快照刚触发买入时，不能再使用
     * 同一张快照立刻撤单；从下一张快照开始，挂单必须持续满足封单绝对强度。</p>
     *
     * @param orderBook Handler 私有订单簿
     * @param event 当前三秒快照
     * @param recordTime Handler 已推进的市场时间
     * @param transactionStatus 事件到达前的交易状态
     * @param ruleRecordBuffer Handler 私有预分配规则记录缓冲区
     * @return 处理后的交易状态
     */
    int process(OrderBook orderBook, TickData event, int recordTime,
                int transactionStatus, RuleRecordBuffer ruleRecordBuffer) {
        if (event.time > ConstantUtil.TIME_920) {
            orderBook.updateLowestPrice(event.price);
        }

        if (event.time < ConstantUtil.TIME_930) {
            return processMorningAuction(orderBook, event, recordTime,
                    transactionStatus, ruleRecordBuffer);
        }
        return processClosingAuction(orderBook, event, recordTime,
                transactionStatus, ruleRecordBuffer);
    }

    /** 处理上海早盘集合竞价买入和挂单后的撤单。 */
    private int processMorningAuction(OrderBook orderBook, TickData event, int recordTime,
                                      int transactionStatus,
                                      RuleRecordBuffer ruleRecordBuffer) {
        long previousBuyVolume = orderBook.recordShanghaiAuctionBuyVolume(
                event.buyerOrderId);

        if (transactionStatus == 1) {
            RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
            // 调用当前模式上海集合竞价买入规则。
            if (tradeDecisionService.evaluateShanghaiAuctionBuy(
                    orderBook, event, previousBuyVolume, recordTime, ruleRecord)) {
                executionGateway.buy(orderBook.getDate(), event.symbolId,
                        orderBook.getLimitUpPrice(), event.time);
                orderBook.setTransactionStatus(2);
                ruleRecordBuffer.commit();
                return 2;
            }
            return transactionStatus;
        }

        if (transactionStatus == 2
                && event.time < ConstantUtil.TIME_920
                && event.time > ConstantUtil.TIME_91530) {
            RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
            // 调用当前模式上海集合竞价撤单规则；价格规则优先于封单绝对强度规则。
            if (tradeDecisionService.evaluateShanghaiAuctionCancel(
                    orderBook, event, recordTime, ruleRecord)) {
                executionGateway.cancel(orderBook.getSymbol());
                orderBook.setTransactionStatus(1);
                ruleRecordBuffer.commit();
                return 1;
            }
        }
        return transactionStatus;
    }

    /** 处理上海尾盘集合竞价卖出；不改写早盘快照买量基准。 */
    private int processClosingAuction(OrderBook orderBook, TickData event, int recordTime,
                                      int transactionStatus,
                                      RuleRecordBuffer ruleRecordBuffer) {
        if (transactionStatus != -1
                || event.time < ConstantUtil.TIME_1459
                || event.time >= ConstantUtil.TIME_1500) {
            return transactionStatus;
        }

        RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
        // 调用上海尾盘集合竞价卖出规则。
        if (!tradeDecisionService.evaluateShanghaiClosingAuctionSell(
                orderBook, event, recordTime, ruleRecord)) {
            return transactionStatus;
        }

        executionGateway.sell(orderBook.getSymbol(),
                orderBook.getLimitDownPrice(), orderBook.getLimitDownPrice());
        orderBook.setTransactionStatus(-2);
        ruleRecordBuffer.commit();
        return -2;
    }
}
