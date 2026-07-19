package com.compoundwonder.strategy.firstboard.trade;

import com.compoundwonder.strategy.TradeMarketState;
import com.compoundwonder.strategy.TradeRuleRecord;

/**
 * 卖出条件统一入口。
 *
 * <p>保留 Handler 现有调用方式，具体规则按数据来源分别交给涨停盘口和均价走势评估器。</p>
 */
public final class ConditionEvaluatorSell {

    private ConditionEvaluatorSell() {
    }

    /**
     * 评估涨停状态、封单变化和换手率相关的卖出条件。
     *
     * @param orderBook 当前 Handler 私有订单簿
     * @param ruleRecord 本轮待填充的规则记录
     * @return 命中任意卖出规则时返回 {@code true}
     */
    public static boolean evaluate(TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
        return LimitUpSellEvaluator.evaluate(orderBook, ruleRecord);
    }

    /**
     * 评估分钟价格与均价走势相关的卖出条件。
     *
     * @param calculateIndex 当前分钟采样位置
     * @param orderBook 当前 Handler 私有订单簿
     * @param ruleRecord 本轮待填充的规则记录
     * @return 命中任意卖出规则时返回 {@code true}
     */
    public static boolean averagePriceSellStrategy(int calculateIndex, TradeMarketState orderBook,
                                                   TradeRuleRecord ruleRecord) {
        return AveragePriceSellEvaluator.evaluate(calculateIndex, orderBook, ruleRecord);
    }
}

