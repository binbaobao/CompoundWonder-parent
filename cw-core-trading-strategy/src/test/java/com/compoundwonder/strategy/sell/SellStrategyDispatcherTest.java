package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

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
}
