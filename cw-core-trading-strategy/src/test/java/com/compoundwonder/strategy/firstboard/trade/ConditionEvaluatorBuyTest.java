package com.compoundwonder.strategy.firstboard.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorBuyTest {

    @Test
    void rejectsContinuousFirstBoardEntryBelowTwelvePercentTurnover() {
        assertFalse(ConditionEvaluatorBuy.evaluate(market(11.99D), new CapturedRule()));
    }

    @Test
    void keepsContinuousFirstBoardEntryAtTwelvePercentTurnover() {
        assertTrue(ConditionEvaluatorBuy.evaluate(market(12.00D), new CapturedRule()));
    }

    @Test
    void rejectsNormalEntryWhenSevenPercentWasReachedLessThanEightMinutesAgo() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 94_000_000);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> index == 4 ? 1_071 : 0);

        assertFalse(ConditionEvaluatorBuy.evaluate(
                market(12.00D, overrides), new CapturedRule()));
    }

    @Test
    void keepsNormalEntryWhenSevenPercentWasReachedAtLeastEightMinutesAgo() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 94_000_000);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> index == 2 ? 1_071 : 0);

        assertTrue(ConditionEvaluatorBuy.evaluate(
                market(12.00D, overrides), new CapturedRule()));
    }

    @Test
    void keepsDirectLimitUpEntryWithoutCompletedSevenPercentMinute() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 94_000_000);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> 0);

        assertTrue(ConditionEvaluatorBuy.evaluate(
                market(12.00D, overrides), new CapturedRule()));
    }

    @Test
    void rejectsLargeOrderRuleWhenSevenPercentWasReachedLessThanEightMinutesAgo() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 94_000_000);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> index == 4 ? 1_071 : 0);
        overrides.put("getLargestBuyOrderPrice", 1_000);
        overrides.put("getLargestBuyOrderQuantity", 888_800);

        assertFalse(ConditionEvaluatorBuy.evaluate(
                market(12.00D, overrides), new CapturedRule()));
    }

    @Test
    void keepsLargeOrderRuleWhenSevenPercentWasReachedAtLeastEightMinutesAgo() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 94_000_000);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> index == 2 ? 1_071 : 0);
        overrides.put("getLargestBuyOrderPrice", 1_000);
        overrides.put("getLargestBuyOrderQuantity", 888_800);

        assertTrue(ConditionEvaluatorBuy.evaluate(
                market(12.00D, overrides), new CapturedRule()));
    }

    @Test
    void keepsDirectLargeOrderLimitUpWithoutCompletedSevenPercentMinute() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("getTime", 94_000_000);
        overrides.put("getMinutePriceAt", (IntUnaryOperator) index -> 0);
        overrides.put("getLargestBuyOrderPrice", 1_000);
        overrides.put("getLargestBuyOrderQuantity", 888_800);

        assertTrue(ConditionEvaluatorBuy.evaluate(
                market(12.00D, overrides), new CapturedRule()));
    }

    private static TradeMarketState market(double turnoverRate) {
        return market(turnoverRate, Map.of());
    }

    private static TradeMarketState market(double turnoverRate, Map<String, Object> overrides) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "600000");
        values.put("getStatus", 0);
        values.put("getLbcs", 1);
        values.put("getTime", 93_255_150);
        values.put("getClosePrice", 1_000);
        values.put("getLastPrice", 1_000);
        values.put("getLimitUpPrice", 1_000);
        values.put("getOpenIncrease", 0D);
        values.put("getLowPriceIncrease", 0D);
        values.put("getAmplitude", 5D);
        values.put("getTurnoverRate", turnoverRate);
        values.put("getMaxVolume", 30_000_000L);
        values.put("getCirculation", 100_000_000L);
        values.put("getInitialMarketValue", 125_000);
        values.put("getLimitUpBuyAmount", 600L);
        values.put("getLastLimitUptime", 93_255_150);
        values.putAll(overrides);
        if (overrides.containsKey("getTime") && !overrides.containsKey("getLastLimitUptime")) {
            values.put("getLastLimitUptime", overrides.get("getTime"));
        }

        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value instanceof IntUnaryOperator function) {
                        return function.applyAsInt((Integer) args[0]);
                    }
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
        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
        }
    }
}
