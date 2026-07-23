package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 连续竞价卖出不再按昨日板数和启动市值预分派后的场景回归。
 */
class UnifiedContinuousSellRegressionTest {

    @Test
    void highBoardGapShrinkingRuleCanMatchYesterdaySixBoard() {
        TradeMarketState market = market(Map.ofEntries(
                Map.entry("getSymbol", "600743"),
                Map.entry("getLbcs", 6),
                Map.entry("getInitialMarketValue", 193_457),
                Map.entry("getTime", 93_248_710),
                Map.entry("getLastPrice", 353),
                Map.entry("getIncrease", 9.97D),
                Map.entry("getOpenIncrease", 9.97D),
                Map.entry("getYesterdayTurnover", 6.4258D),
                Map.entry("getTurnoverRate", 6.8959D),
                Map.entry("getChangePercent", -6.27D),
                Map.entry("getLimitUpBuyAmount", 231L)
        ));
        AtomicInteger ruleCode = new AtomicInteger();

        assertTrue(new TradeStrategyDispatcher().evaluateSell(market, record(ruleCode)));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING, ruleCode.get());
    }

    @Test
    void fiftyPercentTurnoverRuleIsNotBlockedByNormalCapBand() {
        TradeMarketState market = market(Map.ofEntries(
                Map.entry("getSymbol", "603318"),
                Map.entry("getLbcs", 3),
                Map.entry("getInitialMarketValue", 191_455),
                Map.entry("getTime", 131_200_000),
                Map.entry("getLastPrice", 1_000),
                Map.entry("getIncrease", 10D),
                Map.entry("getTurnoverRate", 52D),
                Map.entry("getStatus", 2)
        ));
        AtomicInteger ruleCode = new AtomicInteger();

        assertTrue(new TradeStrategyDispatcher().evaluateSell(market, record(ruleCode)));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK, ruleCode.get());
    }

    @Test
    void modelThreeProfitProtectionDoesNotLeakIntoNormalCapPosition() {
        TradeMarketState market = market(Map.ofEntries(
                Map.entry("getSymbol", "603178"),
                Map.entry("getLbcs", 2),
                Map.entry("getInitialMarketValue", 146_093),
                Map.entry("getTime", 93_301_870),
                Map.entry("getClosePrice", 1_916),
                Map.entry("getIncrease", -4.33D),
                Map.entry("getMinutePriceAt", new int[]{2_000, 1_990, 1_980, 1_970, 1_960}),
                Map.entry("getAveragePriceAt", new int[]{2_000, 2_000, 2_000, 2_000, 2_000})
        ));

        assertFalse(new TradeStrategyDispatcher().evaluateAveragePriceSell(
                4, market, record(new AtomicInteger())));
    }

    private static TradeMarketState market(Map<String, Object> facts) {
        Map<String, Object> values = new HashMap<>(facts);
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    if (values.containsKey(method.getName())) {
                        Object value = values.get(method.getName());
                        if (value instanceof int[] indexedValues) {
                            return indexedValues[(Integer) args[0]];
                        }
                        return value;
                    }
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) return false;
                    if (type == int.class) return 0;
                    if (type == long.class) return 0L;
                    if (type == double.class) return 0D;
                    return null;
                });
    }

    private static TradeRuleRecord record(AtomicInteger ruleCode) {
        return (TradeRuleRecord) Proxy.newProxyInstance(
                TradeRuleRecord.class.getClassLoader(),
                new Class<?>[]{TradeRuleRecord.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("fill")) {
                        ruleCode.set((Integer) args[1]);
                    }
                    return null;
                });
    }
}
