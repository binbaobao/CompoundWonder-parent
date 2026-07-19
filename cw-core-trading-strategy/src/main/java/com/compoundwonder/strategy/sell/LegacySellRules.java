package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.legacy.LegacyAveragePriceSellEvaluator;
import com.compoundwonder.strategy.sell.legacy.LegacyOrderBookSellEvaluator;

/**
 * 场景拆分期间的既有卖出规则兼容入口。
 *
 * <p>当前各板高与市值场景先完整复用拆分前的卖出规则，确保只改变代码归属，
 * 不改变规则编号、优先级、阈值和记录内容。后续根据回测结果修改某个场景时，
 * 直接替换对应场景类的四个显式方法，不需要修改总分发器。</p>
 */
public final class LegacySellRules {

    private LegacySellRules() {
    }

    public static boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return LegacyOrderBookSellEvaluator.evaluate(market, record);
    }

    public static boolean evaluateAveragePrice(int calculateIndex, TradeMarketState market,
                                               TradeRuleRecord record) {
        return LegacyAveragePriceSellEvaluator.evaluate(calculateIndex, market, record);
    }
}
