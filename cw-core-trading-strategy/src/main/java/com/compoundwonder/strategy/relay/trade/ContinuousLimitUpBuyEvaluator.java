package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/**
 * 连板模式 09:31 后的连续竞价打板场景。
 *
 * <p>当前完整复用该模式已有的买入规则和规则编号。后续根据回测调整连板打板时，
 * 只修改本入口或其委托的规则类，不影响两个交易所的集合竞价场景。</p>
 */
final class ContinuousLimitUpBuyEvaluator {
    private ContinuousLimitUpBuyEvaluator() {
    }

    static boolean evaluate(TradeMarketState market, TradeRuleRecord record) {
        return ConditionEvaluatorBuy.evaluate(market, record);
    }
}
