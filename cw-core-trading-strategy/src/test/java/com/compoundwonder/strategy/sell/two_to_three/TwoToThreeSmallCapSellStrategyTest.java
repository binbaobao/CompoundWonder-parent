package com.compoundwonder.strategy.sell.two_to_three;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwoToThreeSmallCapSellStrategyTest {

    @Test
    void keepsHighTurnoverBreakFromXinriSharesOn2025_06_12() {
        Map<String, Object> values = baseValues(94_175, 2);
        values.put("getTime", 93_750_530);
        values.put("getLastPrice", 1_546);
        values.put("getLimitUpBuyAmount", 999L);
        values.put("getStatus", 15);
        values.put("getChangePercent", -3.5D);
        values.put("getEmaSealTrend", -1);
        values.put("getTurnoverRate", 30.00279145730223D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new TwoToThreeSmallCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK, rule.ruleCode);
        assertEquals(1_546, rule.price);
    }

    @Test
    void holdsPositivePricePullbackFromChuhuanTechnologyOn2025_12_23() {
        int index = 10;
        Map<String, Object> values = baseValues(80_000, 2);
        values.put("getTime", 93_559_530);
        values.put("getIncrease", 3.937823834196891D);
        values.put("getMinutePriceAt", (IntUnaryOperator) i -> switch (i) {
            case 7 -> 3_030;
            case 8 -> 3_020;
            case 9 -> 3_010;
            default -> 3_006;
        });

        CapturedRule rule = new CapturedRule();

        assertFalse(new TwoToThreeSmallCapSellStrategy().evaluateAveragePrice(index, market(values), rule));
        assertEquals(0, rule.ruleCode);
    }

    @Test
    void acceptsSmallDivergenceFromHuafengSharesOn2025_02_25() {
        int index = 10;
        Map<String, Object> values = baseValues(80_000, 2);
        values.put("getTime", 93_259_760);
        values.put("getIncrease", -1.7919075144508672D);
        values.put("getMinutePriceAt", (IntUnaryOperator) i -> switch (i) {
            case 7 -> 1_720;
            case 8 -> 1_710;
            case 9 -> 1_700;
            default -> 1_696;
        });

        CapturedRule rule = new CapturedRule();

        assertFalse(new TwoToThreeSmallCapSellStrategy().evaluateAveragePrice(index, market(values), rule));
        assertEquals(0, rule.ruleCode);
    }

    @Test
    void keepsAverageWeakeningFromShuangqiangTechnologyOn2025_12_05() {
        int index = 10;
        Map<String, Object> values = baseValues(80_000, 2);
        values.put("getTime", 93_259_970);
        values.put("getIncrease", -7.171685811859671D);
        values.put("getAveragePriceAt", (IntUnaryOperator) i -> switch (i) {
            case 7 -> 3_030;
            case 8 -> 3_020;
            case 9 -> 3_010;
            default -> 3_000;
        });
        values.put("getMinutePriceAt", (IntUnaryOperator) i -> 2_975);

        CapturedRule rule = new CapturedRule();

        assertTrue(new TwoToThreeSmallCapSellStrategy().evaluateAveragePrice(index, market(values), rule));
        assertEquals(RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING, rule.ruleCode);
        assertEquals(2_975, rule.price);
    }

    @Test
    void keepsLowOpenWeaknessFromZhuangyuanPastureOn2025_12_19() {
        int index = 10;
        Map<String, Object> values = baseValues(80_000, 2);
        values.put("getTime", 93_259_970);
        values.put("getOpenIncrease", -5.5D);
        values.put("getIncrease", -5.07655116841257D);
        values.put("getMinutePriceAt", (IntUnaryOperator) i -> 1_177);

        CapturedRule rule = new CapturedRule();

        assertTrue(new TwoToThreeSmallCapSellStrategy().evaluateAveragePrice(index, market(values), rule));
        assertEquals(RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING, rule.ruleCode);
        assertEquals(1_177, rule.price);
    }

    private static Map<String, Object> baseValues(int marketValue, int lbcs) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "600000");
        values.put("getInitialMarketValue", marketValue);
        values.put("getLbcs", lbcs);
        return values;
    }

    private static TradeMarketState market(Map<String, Object> values) {
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value instanceof IntUnaryOperator operator) {
                        return operator.applyAsInt((Integer) args[0]);
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
        private int ruleCode;
        private int price;

        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
            this.ruleCode = ruleCode;
            this.price = price;
        }
    }
}
