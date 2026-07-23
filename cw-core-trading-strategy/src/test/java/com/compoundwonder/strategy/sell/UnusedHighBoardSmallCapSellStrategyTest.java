package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UnusedHighBoardSmallCapSellStrategyTest {

    @Test
    void sevenToEightHasNoBaselineInternalSellRule() {
        assertCleared(7);
    }

    @Test
    void eightToNineHasNoBaselineInternalSellRule() {
        assertCleared(8);
    }

    @Test
    void higherBoardsHaveNoBaselineInternalSellRule() {
        assertCleared(9);
    }

    private static void assertCleared(int lbcs) {
        ContinuousSellStrategy strategy = new ContinuousSellStrategy();
        TradeMarketState market = legacySealWeakeningMarket(lbcs);
        TradeRuleRecord record = (actionType, ruleCode, symbol, time, price, increase, remark) -> { };

        assertFalse(strategy.evaluateOrderBook(market, record));
        assertFalse(strategy.evaluateAveragePrice(20, market, record));
    }

    private static TradeMarketState legacySealWeakeningMarket(int lbcs) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "600000");
        values.put("getInitialMarketValue", 80_000);
        values.put("getLbcs", lbcs);
        values.put("getTurnoverRate", 5D);
        values.put("getLastPrice", 1_000);
        values.put("getLimitUpPrice", 1_000);
        values.put("getLimitUpBuyAmount", 12_000L);
        values.put("getLastSealAmount", 20_000L);
        values.put("getChangePercent", -2D);

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
}
