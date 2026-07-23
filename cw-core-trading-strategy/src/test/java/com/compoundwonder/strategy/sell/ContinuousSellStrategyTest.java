package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContinuousSellStrategyTest {

    @Test
    void keepsHolidayRulesInTheUnifiedContinuousStrategy() {
        ContinuousSellStrategy strategy = new ContinuousSellStrategy();

        AtomicInteger highTurnoverRule = new AtomicInteger();
        assertTrue(strategy.evaluateOrderBook(
                holidayMarket(56, 2, 5), ruleRecord(highTurnoverRule)));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_TURNOVER,
                highTurnoverRule.get());

        AtomicInteger highBoardRule = new AtomicInteger();
        assertTrue(strategy.evaluateOrderBook(
                holidayMarket(5, 3, 6), ruleRecord(highBoardRule)));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_BOARD,
                highBoardRule.get());
    }

    @Test
    void identifiesBreakBoardNextOpenSellWithoutLevelTwoData() {
        assertTrue(BreakBoardNextOpenSellPolicy.shouldSellAtNextOpen(11));
        assertTrue(BreakBoardNextOpenSellPolicy.shouldSellAtNextOpen(12));
        assertTrue(BreakBoardNextOpenSellPolicy.shouldSellAtNextOpen(13));
        assertFalse(BreakBoardNextOpenSellPolicy.shouldSellAtNextOpen(3));
        assertFalse(BreakBoardNextOpenSellPolicy.shouldSellAtNextOpen(null));
    }

    @Test
    void topLevelSellNeverReadsTheHistoricalBuyMode() {
        TradeMarketState market = (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getTradeMode")) {
                        throw new AssertionError("卖出链路不应读取买入 tradeMode");
                    }
                    return defaultValue(method.getReturnType());
                });
        TradeRuleRecord record = (TradeRuleRecord) Proxy.newProxyInstance(
                TradeRuleRecord.class.getClassLoader(),
                new Class<?>[]{TradeRuleRecord.class},
                (proxy, method, args) -> null);

        assertFalse(new TradeStrategyDispatcher().evaluateSell(market, record));
    }

    private static TradeMarketState holidayMarket(
            double turnover, int nextTradingDay, int lbcs) {
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getInitialMarketValue" -> 100_000;
                    case "getTurnoverRate" -> turnover;
                    case "getStatus" -> 1;
                    case "getLbcs" -> lbcs;
                    case "getTime" -> 100_000_000;
                    case "getNextTradingDay" -> nextTradingDay;
                    case "getEmaSealTrend" -> -1;
                    case "getLastPrice", "getLimitUpPrice" -> 1_000;
                    case "getIncrease" -> 10D;
                    case "getLastLimitUptime" -> 94_000_000;
                    case "getLimitUpBuyAmount" -> 10_000L;
                    case "getChangePercent" -> 0D;
                    case "getSymbol" -> "600000";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static TradeRuleRecord ruleRecord(AtomicInteger ruleCode) {
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

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        return null;
    }
}
