package com.compoundwonder.core.processor.evaluator;

import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionStrategyDispatcherTest {

    private final TradeStrategyDispatcher dispatcher = new TradeStrategyDispatcher();

    @Test
    void dispatchesEveryAuctionRuleThroughAllThreeIndependentModes() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook("000001", 100_000L, 10.00, 50_000L);
            orderBook.setTradeMode(tradeMode);
            int limitUpPrice = orderBook.getLimitUpPrice();

            assertEquals(2, dispatcher.evaluateShanghaiAuctionBuy(
                    orderBook, 91_900_000, limitUpPrice, limitUpPrice,
                    10_000, 1_000, 5_000, 1_000));
            assertEquals(1, dispatcher.evaluateShanghaiAuctionCancel(
                    orderBook, limitUpPrice - 1, limitUpPrice,
                    10_000, 1_000, 5_000));
            assertEquals(2, dispatcher.evaluateShanghaiAuctionCancel(
                    orderBook, limitUpPrice, limitUpPrice,
                    5_000, 1_000, 5_000));

            assertEquals(6, dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook, (byte) 1, limitUpPrice, limitUpPrice,
                    900_001, 5_000, 500, 4_000, 2_000, 100_000));
            assertEquals(7, dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook, (byte) 2, limitUpPrice, limitUpPrice,
                    1, 5_001, 500, 5_000, 100, 100_000));
            assertEquals(2, dispatcher.evaluateShenzhenAuctionCancel(
                    orderBook, 4_000, 500, 5_000));
            assertEquals(1, dispatcher.evaluateShenzhenSnapshotAuctionCancel(
                    orderBook, limitUpPrice - 1, limitUpPrice));

            assertTrue(dispatcher.evaluateClosingAuctionSell(
                    orderBook, limitUpPrice - 1, limitUpPrice, 10_000, 1_000));
            assertTrue(dispatcher.evaluateClosingAuctionSell(
                    orderBook, limitUpPrice, limitUpPrice, 1_000, 2_000));

            OrderBook controlBook = new OrderBook("000001", 100_000L, 10.00, 50_000L);
            controlBook.setTradeMode(tradeMode);
            controlBook.setLbcs(2);
            controlBook.updatePrice(0, 0, 1_050, 93_100_000);
            controlBook.updatePrice(0, 0, 990, 93_800_000);
            assertTrue(dispatcher.shouldEnableFirstBoardTradingMode(controlBook));
            assertTrue(dispatcher.isContinuousBuyTimeAllowed(controlBook, 142_959_999));
            assertFalse(dispatcher.isContinuousBuyTimeAllowed(controlBook, 143_000_000));
        }
    }
}
