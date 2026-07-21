package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import com.compoundwonder.strategy.sell.common.CommonSellStrategy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellStrategyDispatcherTest {

    @Test
    void routesYesterdayBoardHeightToItsOwnSellScene() {
        SellStrategyDispatcher dispatcher = new SellStrategyDispatcher();

        assertEquals(SellScene.TWO_TO_THREE, dispatcher.resolveScene(2));
        assertEquals(SellScene.THREE_TO_FOUR, dispatcher.resolveScene(3));
        assertEquals(SellScene.FOUR_TO_FIVE, dispatcher.resolveScene(4));
        assertEquals(SellScene.FIVE_TO_SIX, dispatcher.resolveScene(5));
        assertEquals(SellScene.SIX_TO_SEVEN, dispatcher.resolveScene(6));
        assertEquals(SellScene.SEVEN_TO_EIGHT, dispatcher.resolveScene(7));
        assertEquals(SellScene.EIGHT_TO_NINE, dispatcher.resolveScene(8));
        assertEquals(SellScene.HIGH_BOARD, dispatcher.resolveScene(9));
        assertEquals(SellScene.HIGH_BOARD, dispatcher.resolveScene(12));
    }

    @Test
    void rejectsBoardHeightsOutsideThePositionSellScope() {
        SellStrategyDispatcher dispatcher = new SellStrategyDispatcher();

        assertEquals(SellScene.UNSUPPORTED, dispatcher.resolveScene(0));
        assertEquals(SellScene.UNSUPPORTED, dispatcher.resolveScene(1));
    }

    @Test
    void splitsSellRulesAtTheUnifiedStartupMarketValueBoundary() {
        assertEquals(SellMarketCapBand.SMALL_CAP, SellMarketCapBand.from(119_998));
        assertEquals(SellMarketCapBand.NORMAL_CAP, SellMarketCapBand.from(119_999));
        assertEquals(SellMarketCapBand.NORMAL_CAP, SellMarketCapBand.from(120_000));
    }

    @Test
    void routesEveryBoardHeightAndMarketValueBandToAnIndependentStrategy() {
        SellStrategyDispatcher dispatcher = new SellStrategyDispatcher();

        assertStrategy(dispatcher, 2, 119_998, "TwoToThreeSmallCapSellStrategy");
        assertStrategy(dispatcher, 2, 119_999, "TwoToThreeNormalCapSellStrategy");
        assertStrategy(dispatcher, 3, 119_998, "ThreeToFourSmallCapSellStrategy");
        assertStrategy(dispatcher, 3, 119_999, "ThreeToFourNormalCapSellStrategy");
        assertStrategy(dispatcher, 4, 119_998, "FourToFiveSmallCapSellStrategy");
        assertStrategy(dispatcher, 4, 119_999, "FourToFiveNormalCapSellStrategy");
        assertStrategy(dispatcher, 5, 119_998, "FiveToSixSmallCapSellStrategy");
        assertStrategy(dispatcher, 5, 119_999, "FiveToSixNormalCapSellStrategy");
        assertStrategy(dispatcher, 6, 119_998, "SixToSevenSmallCapSellStrategy");
        assertStrategy(dispatcher, 6, 119_999, "SixToSevenNormalCapSellStrategy");
        assertStrategy(dispatcher, 7, 119_998, "SevenToEightSmallCapSellStrategy");
        assertStrategy(dispatcher, 7, 119_999, "SevenToEightNormalCapSellStrategy");
        assertStrategy(dispatcher, 8, 119_998, "EightToNineSmallCapSellStrategy");
        assertStrategy(dispatcher, 8, 119_999, "EightToNineNormalCapSellStrategy");
        assertStrategy(dispatcher, 9, 119_998, "HighBoardSmallCapSellStrategy");
        assertStrategy(dispatcher, 12, 119_999, "HighBoardNormalCapSellStrategy");
    }

    @Test
    void keepsHolidayRulesInTheCommonFinalStrategy() {
        CommonSellStrategy strategy = new CommonSellStrategy();

        AtomicInteger highTurnoverRule = new AtomicInteger();
        assertTrue(strategy.evaluateOrderBook(holidayMarket(56, 2, 5), ruleRecord(highTurnoverRule)));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_TURNOVER, highTurnoverRule.get());

        AtomicInteger highBoardRule = new AtomicInteger();
        assertTrue(strategy.evaluateOrderBook(holidayMarket(5, 3, 6), ruleRecord(highBoardRule)));
        assertEquals(RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_BOARD, highBoardRule.get());
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
    void topLevelSellDispatchNeverReadsTheHistoricalBuyMode() {
        TradeMarketState market = (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getLbcs")) {
                        return 1;
                    }
                    if (method.getName().equals("getTradeMode")) {
                        throw new AssertionError("卖出链路不应读取买入 tradeMode");
                    }
                    throw new AssertionError("低于 2 板时不应读取其他订单簿字段: " + method.getName());
                });
        TradeRuleRecord record = (TradeRuleRecord) Proxy.newProxyInstance(
                TradeRuleRecord.class.getClassLoader(),
                new Class<?>[]{TradeRuleRecord.class},
                (proxy, method, args) -> null);

        assertFalse(new TradeStrategyDispatcher().evaluateSell(market, record));
    }

    private static void assertStrategy(SellStrategyDispatcher dispatcher, int yesterdayBoardHeight,
                                       long initialMarketValue, String expectedClassName) {
        BoardSellStrategy strategy = dispatcher.resolveStrategy(yesterdayBoardHeight, initialMarketValue);
        assertEquals(expectedClassName, strategy.getClass().getSimpleName());
    }

    private static TradeMarketState holidayMarket(double turnover, int nextTradingDay, int lbcs) {
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
                    case "getMinAveragePrice" -> 1_000;
                    case "getMinAveragePriceIncrease" -> 0D;
                    case "getSymbol" -> "600000";
                    default -> throw new AssertionError("公共假期规则不应读取字段: " + method.getName());
                });
    }

    private static TradeRuleRecord ruleRecord(AtomicInteger ruleCode) {
        return (TradeRuleRecord) Proxy.newProxyInstance(
                TradeRuleRecord.class.getClassLoader(),
                new Class<?>[]{TradeRuleRecord.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("fill")) {
                        ruleCode.set((Integer) args[1]);
                        return null;
                    }
                    throw new AssertionError("规则记录不应调用方法: " + method.getName());
                });
    }
}
