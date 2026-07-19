package com.compoundwonder.strategy.firstboard.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/**
 * 普通首板模式 09:31 后的连续竞价打板场景。
 * 当前复用该模式已有完整规则，后续回测调参只需修改本场景。
 */
final class ContinuousLimitUpBuyEvaluator {
    private ContinuousLimitUpBuyEvaluator() {
    }
    static boolean evaluate(TradeMarketState market, TradeRuleRecord record) {
        return ConditionEvaluatorBuy.evaluate(market, record);
    }
}
