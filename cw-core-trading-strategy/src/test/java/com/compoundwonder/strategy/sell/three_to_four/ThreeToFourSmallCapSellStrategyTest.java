package com.compoundwonder.strategy.sell.three_to_four;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreeToFourSmallCapSellStrategyTest {

    @Test
    void keepsHighTurnoverBreakFromNanjingPortOn2025_05_16() {
        Map<String, Object> values = baseValues(110_633, 3);
        values.put("getTime", 100_055_630);
        values.put("getLastPrice", 1_023);
        values.put("getLimitUpBuyAmount", 2_042L);
        values.put("getStatus", 39);
        values.put("getTurnoverRate", 54.38365946976607D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new ThreeToFourSmallCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK, rule.ruleCode);
        assertEquals(1_023, rule.price);
    }

    @Test
    void keepsOneWordWeakeningFromDechuangEnvironmentOn2025_01_17() {
        Map<String, Object> values = baseValues(60_950, 3);
        values.put("getTime", 93_220_920);
        values.put("getLastPrice", 1_052);
        values.put("getLimitUpPrice", 1_052);
        values.put("getLimitUpBuyAmount", 2_020L);
        values.put("getStatus", 1);
        values.put("getChangePercent", -3.89D);
        values.put("getTurnoverRate", 3.007023498079144D);
        values.put("getTwoDaysTurnover", 20D);
        values.put("getYesterdayTurnover", 20D);
        values.put("getOneWordLimitUp", 1);
        values.put("getLastSealAmount", 2_020L);
        values.put("getAmplitude", 1D);
        values.put("getOpenIncrease", 9D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new ThreeToFourSmallCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING, rule.ruleCode);
        assertEquals(1_052, rule.price);
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
