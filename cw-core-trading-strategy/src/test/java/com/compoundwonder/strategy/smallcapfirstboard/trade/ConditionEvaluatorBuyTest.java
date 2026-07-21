package com.compoundwonder.strategy.smallcapfirstboard.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorBuyTest {

    @Test
    void rejectsNormalRuleFourteenWithoutCompletedSevenPercentMinute() {
        assertFalse(ConditionEvaluatorBuy.evaluate(
                market(Map.of("getMinutePriceAt", (IntUnaryOperator) index -> 0)),
                new CapturedRule()));
    }

    @Test
    void keepsNormalRuleFourteenAfterCompletedSevenPercentMinute() {
        CapturedRule rule = new CapturedRule();
        TradeMarketState market = market(Map.of(
                "getMinutePriceAt", (IntUnaryOperator) index -> index == 9 ? 1_070 : 0));

        assertTrue(ConditionEvaluatorBuy.evaluate(market, rule));
        assertEquals(14, rule.ruleCode);
    }

    @Test
    void keepsMorningLongSealAfterAfternoonReseal() {
        CapturedRule rule = new CapturedRule();
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 140_100_000);
        overrides.put("getLastLimitUptime", 140_100_000);
        overrides.put("getStatus", 3);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> index == 1 ? 1_100 : 0);

        assertTrue(ConditionEvaluatorBuy.evaluate(market(overrides), rule));
        assertEquals(14, rule.ruleCode);
    }

    private static TradeMarketState market(Map<String, Object> overrides) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "600000");
        values.put("getStatus", 0);
        values.put("getTime", 94_000_000);
        values.put("getClosePrice", 1_000);
        values.put("getLastPrice", 1_100);
        values.put("getLimitUpPrice", 1_100);
        values.put("getIncrease", 10D);
        values.put("getOpenIncrease", 0D);
        values.put("getLowPriceIncrease", 0D);
        values.put("getAmplitude", 5D);
        values.put("getTurnoverRate", 20D);
        values.put("getMaxHs", 30D);
        values.put("getInitialMarketValue", 90_000);
        values.put("getLimitUpBuyAmount", 600L);
        values.put("getLastLimitUptime", 94_000_000);
        values.putAll(overrides);

        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value instanceof IntUnaryOperator function) {
                        return function.applyAsInt((Integer) args[0]);
                    }
                    if (value != null) return value;
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

        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
            this.ruleCode = ruleCode;
        }
    }
}
