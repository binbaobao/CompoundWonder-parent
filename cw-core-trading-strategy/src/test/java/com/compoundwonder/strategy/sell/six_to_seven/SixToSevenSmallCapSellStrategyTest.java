package com.compoundwonder.strategy.sell.six_to_seven;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.ContinuousSellStrategy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SixToSevenSmallCapSellStrategyTest {

    @Test
    void keepsGapShrinkingFromLanfengBiochemicalOn2025_09_30() {
        Map<String, Object> values = baseValues("002513", 70_000);
        values.put("getTime", 93_101_870);
        values.put("getLastPrice", 1_040);
        values.put("getLimitUpBuyAmount", 229L);
        values.put("getOpenIncrease", 8.99D);
        values.put("getTurnoverRate", 8.2889564D);
        values.put("getYesterdayTurnover", 30.9359D);
        values.put("getChangePercent", -14.43D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new ContinuousSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING, rule.ruleCode);
        assertEquals(1_040, rule.price);
    }

    @Test
    void keepsLowTurnoverBreakFromGuofangGroupOn2025_04_14() {
        Map<String, Object> values = baseValues("601086", 77_662);
        values.put("getTime", 93_121_540);
        values.put("getLastPrice", 928);
        values.put("getLimitUpBuyAmount", 418L);
        values.put("getTurnoverRate", 16.42345D);
        values.put("getThreeDaysTurnover", 7.7849D);
        values.put("getChangePercent", -2.09D);
        values.put("getLastSealAmount", 418L);

        CapturedRule rule = new CapturedRule();

        assertTrue(new ContinuousSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_LOW_TURNOVER, rule.ruleCode);
        assertEquals(928, rule.price);
    }

    @Test
    void sellsMarketHeightBreakFromXingyeSharesOn2025_01_27() {
        Map<String, Object> values = baseValues("603928", 64_154);
        values.put("getTime", 94_858_850);
        values.put("getLastPrice", 1_597);
        values.put("getLimitUpBuyAmount", 1_732L);
        values.put("getTurnoverRate", 29.261810552833257D);
        values.put("getThreeDaysTurnover", 10.661666666666667D);
        values.put("getChangePercent", -3.37D);
        values.put("getLastSealAmount", 1_732L);
        values.put("getAverageLimitUpHeight", 6);

        CapturedRule rule = new CapturedRule();

        assertTrue(new ContinuousSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_LOW_TURNOVER, rule.ruleCode);
        assertEquals(1_597, rule.price);
        assertTrue(rule.remark.contains("近 15 日平均高度 6 板"));
        assertTrue(rule.remark.contains("高度差 1 板"));
    }

    private static Map<String, Object> baseValues(String symbol, int marketValue) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", symbol);
        values.put("getInitialMarketValue", marketValue);
        values.put("getLbcs", 6);
        return values;
    }

    private static TradeMarketState market(Map<String, Object> values) {
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value != null) {
                        return value;
                    }
                    return switch (method.getReturnType().getName()) {
                        case "int" -> 0;
                        case "long" -> 0L;
                        case "double" -> 0D;
                        case "boolean" -> false;
                        default -> null;
                    };
                });
    }

    private static final class CapturedRule implements TradeRuleRecord {
        private int ruleCode;
        private int price;
        private String remark;

        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
            this.ruleCode = ruleCode;
            this.price = price;
            this.remark = remark;
        }
    }
}
