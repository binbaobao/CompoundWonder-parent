package com.compoundwonder.strategy.sell.five_to_six;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiveToSixSmallCapSellStrategyTest {

    @Test
    void keepsGapShrinkingFromWangliSecurityOn2026_03_11() {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "605268");
        values.put("getInitialMarketValue", 80_000);
        values.put("getLbcs", 5);
        values.put("getTime", 93_209_830);
        values.put("getLastPrice", 1_855);
        values.put("getLimitUpBuyAmount", 5_301L);
        values.put("getOpenIncrease", 10.02D);
        values.put("getTurnoverRate", 2.4944055D);
        values.put("getYesterdayTurnover", 5.5056D);
        values.put("getChangePercent", -5.1D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new FiveToSixSmallCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING, rule.ruleCode);
        assertEquals(1_855, rule.price);
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
