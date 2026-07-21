package com.compoundwonder.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class OrderBookPriceLevelTest {

    @Test
    void maintainsIndependentBuyAndSellQueuesAtTheSamePrice() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        TickNode firstBuy = node(1, 1000, 300, (byte) 1);
        TickNode secondBuy = node(2, 1000, 500, (byte) 1);
        TickNode firstSell = node(3, 1000, 200, (byte) 2);

        orderBook.addOrder(firstBuy);
        orderBook.addOrder(secondBuy);
        orderBook.addOrder(firstSell);

        PriceLevel level = orderBook.getPriceLevel(1000);
        assertSame(firstBuy, level.getBuyHead());
        assertSame(secondBuy, level.getBuyTail());
        assertSame(secondBuy, firstBuy.getNext());
        assertSame(firstBuy, secondBuy.getPrevious());
        assertSame(firstSell, level.getSellHead());
        assertSame(firstSell, level.getSellTail());
        assertEquals(800, level.getBuyQuantity());
        assertEquals(200, level.getSellQuantity());
        assertEquals(2, level.getBuyOrderCount());
        assertEquals(1, level.getSellOrderCount());
        assertEquals(800L, orderBook.getTotalBuyVolume());
        assertEquals(200L, orderBook.getTotalSellVolume());
    }

    @Test
    void partialAndFullTradesKeepQueueAndAggregatesConsistent() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        TickNode firstBuy = node(1, 1000, 300, (byte) 1);
        TickNode secondBuy = node(2, 1000, 500, (byte) 1);
        orderBook.addOrder(firstBuy);
        orderBook.addOrder(secondBuy);

        assertNull(orderBook.applyTrade(1, 100));
        assertEquals(200, firstBuy.getQuantity());
        assertEquals(700, orderBook.getBuyQuantity(1000));

        TickNode completed = orderBook.applyTrade(1, 300);

        assertSame(firstBuy, completed);
        assertFalse(orderBook.containsOrder(1));
        assertNull(completed.getPrevious());
        assertNull(completed.getNext());
        assertSame(secondBuy, orderBook.getPriceLevel(1000).getBuyHead());
        assertEquals(1, orderBook.getPriceLevel(1000).getBuyOrderCount());
        assertEquals(500, orderBook.getBuyQuantity(1000));
        assertEquals(500L, orderBook.getTotalBuyVolume());
    }

    @Test
    void cancellationUnlinksMiddleNodeInConstantTime() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        TickNode firstBuy = node(1, 1000, 100, (byte) 1);
        TickNode middleBuy = node(2, 1000, 200, (byte) 1);
        TickNode lastBuy = node(3, 1000, 300, (byte) 1);
        orderBook.addOrder(firstBuy);
        orderBook.addOrder(middleBuy);
        orderBook.addOrder(lastBuy);

        TickNode cancelled = orderBook.cancelOrder(2);

        assertSame(middleBuy, cancelled);
        assertSame(lastBuy, firstBuy.getNext());
        assertSame(firstBuy, lastBuy.getPrevious());
        assertNull(middleBuy.getPrevious());
        assertNull(middleBuy.getNext());
        assertEquals(2, orderBook.getPriceLevel(1000).getBuyOrderCount());
        assertEquals(400, orderBook.getBuyQuantity(1000));
        assertEquals(400L, orderBook.getTotalBuyVolume());
    }

    @Test
    void supportsAggregateQuantitiesBeyondIntegerRange() {
        OrderBook orderBook = new OrderBook("000001", 10_000_000_000L, 10.00, 5_000_000_000L);

        orderBook.addOrder(node(1, 1000, 1_500_000_000, (byte) 1));
        orderBook.addOrder(node(2, 1000, 1_500_000_000, (byte) 1));

        assertEquals(3_000_000_000L, orderBook.getBuyQuantity(1000));
        assertEquals(3_000_000_000L, orderBook.getTotalBuyVolume());
    }

    @Test
    void rejectsInvalidAndDuplicateOrdersWithoutThrowing() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);

        assertEquals(OrderBook.AddOrderResult.ADDED,
                orderBook.addOrder(node(1, 1000, 100, (byte) 1)));
        assertEquals(OrderBook.AddOrderResult.DUPLICATE,
                orderBook.addOrder(node(1, 1000, 200, (byte) 1)));
        assertEquals(OrderBook.AddOrderResult.INVALID_PRICE,
                orderBook.addOrder(node(2, 2000, 100, (byte) 1)));
        assertEquals(OrderBook.AddOrderResult.INVALID_DIRECTION,
                orderBook.addOrder(node(3, 1000, 100, (byte) 3)));
        assertEquals(1, orderBook.getActiveOrderCount());
    }

    @Test
    void reusesEmptyPriceLevelDuringTheTradingDay() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        orderBook.addOrder(node(1, 1000, 100, (byte) 1));
        PriceLevel firstLevel = orderBook.getPriceLevel(1000);

        orderBook.cancelOrder(1);
        orderBook.addOrder(node(2, 1000, 200, (byte) 1));

        assertSame(firstLevel, orderBook.getPriceLevel(1000));
    }

    @Test
    void exitsLimitUpStateUsingCurrentQueueAmount() {
        OrderBook orderBook = new OrderBook("000001", 10_000_000L, 10.00, 5_000_000L);
        orderBook.addOrder(node(1, orderBook.getLimitUpPrice(), 1_000_000, (byte) 1));
        orderBook.updatePrice(0, 0, orderBook.getLimitUpPrice(), 93_000_000);

        orderBook.updateLimitUpStatus();
        assertEquals(1, orderBook.getStatus());

        orderBook.applyTrade(1, 950_000);
        orderBook.updateLimitUpStatus();

        assertEquals(2, orderBook.getStatus());
        assertEquals(0L, orderBook.getLimitUpBuyAmount());
    }

    @Test
    void confirmsWeakeningOnlyAfterFiveConsecutiveNegativeEmaChanges() {
        OrderBook orderBook = limitUpOrderBook(1_500_000);

        for (int i = 0; i < 4; i++) {
            orderBook.applyTrade(1, 100_000);
            orderBook.updateLimitUpStatus();
            assertEquals(0, orderBook.getEmaSealTrend());
        }

        orderBook.applyTrade(1, 100_000);
        orderBook.updateLimitUpStatus();

        assertEquals(-1, orderBook.getEmaSealTrend());

        orderBook.addOrder(node(2, orderBook.getLimitUpPrice(), 500_000, (byte) 1));
        orderBook.updateLimitUpStatus();

        assertEquals(0, orderBook.getEmaSealTrend());
    }

    @Test
    void confirmsStrengtheningOnlyAfterFiveConsecutivePositiveEmaChanges() {
        OrderBook orderBook = limitUpOrderBook(1_000_000);

        for (int i = 0; i < 4; i++) {
            orderBook.addOrder(node(i + 2, orderBook.getLimitUpPrice(), 100_000, (byte) 1));
            orderBook.updateLimitUpStatus();
            assertEquals(0, orderBook.getEmaSealTrend());
        }

        orderBook.addOrder(node(6, orderBook.getLimitUpPrice(), 100_000, (byte) 1));
        orderBook.updateLimitUpStatus();

        assertEquals(1, orderBook.getEmaSealTrend());
    }

    @Test
    void returnsNeutralForMixedEmaChangesAndResetsAfterBreakBoard() {
        OrderBook orderBook = limitUpOrderBook(1_500_000);

        for (int i = 0; i < 4; i++) {
            orderBook.applyTrade(1, 100_000);
            orderBook.updateLimitUpStatus();
        }
        orderBook.addOrder(node(2, orderBook.getLimitUpPrice(), 500_000, (byte) 1));
        orderBook.updateLimitUpStatus();
        assertEquals(0, orderBook.getEmaSealTrend());

        orderBook.applyTrade(1, 100_000);
        orderBook.updateLimitUpStatus();
        orderBook.updatePrice(0, 0, orderBook.getLimitUpPrice() - 1, 93_100_000);
        orderBook.updateLimitUpStatus();

        assertEquals(0, orderBook.getEmaSealTrend());
    }

    @Test
    void freezesThePreviousMinuteAverageBeforeUpdatingTheHistoricalMinimum() {
        OrderBook orderBook = new OrderBook("000001", 10_000_000L, 10.00, 5_000_000L);

        orderBook.updateMinuteAveragePrice(0, 1_005, 93_000_000);
        orderBook.updateMinuteAveragePrice(0, 990, 93_030_000);
        assertEquals(0, orderBook.getMinAveragePrice());

        orderBook.updateMinuteAveragePrice(1, 995, 93_100_000);
        assertEquals(990, orderBook.getMinAveragePrice());
        assertEquals(-1D, orderBook.getMinAveragePriceIncrease());

        orderBook.updateMinuteAveragePrice(1, 980, 93_130_000);
        assertEquals(990, orderBook.getMinAveragePrice());

        orderBook.updateMinuteAveragePrice(2, 1_000, 93_200_000);
        assertEquals(980, orderBook.getMinAveragePrice());
        assertEquals(-2D, orderBook.getMinAveragePriceIncrease());
    }

    @Test
    void recognizesMinuteChangeWhenMorningCloseAndAfternoonOpenShareAnArrayIndex() {
        OrderBook orderBook = new OrderBook("000001", 10_000_000L, 10.00, 5_000_000L);

        orderBook.updateMinuteAveragePrice(120, 995, 113_000_000);
        orderBook.updateMinuteAveragePrice(120, 985, 130_000_000);

        assertEquals(995, orderBook.getMinAveragePrice());
        assertEquals(985, orderBook.avgPrice[120]);
    }

    private OrderBook limitUpOrderBook(int initialQuantity) {
        OrderBook orderBook = new OrderBook("000001", 10_000_000L, 10.00, 5_000_000L);
        orderBook.addOrder(node(1, orderBook.getLimitUpPrice(), initialQuantity, (byte) 1));
        orderBook.updatePrice(0, 0, orderBook.getLimitUpPrice(), 93_000_000);
        orderBook.updateLimitUpStatus();
        orderBook.updateLimitUpStatus();
        return orderBook;
    }

    private TickNode node(int orderId, int price, int quantity, byte direction) {
        TickNode node = new TickNode();
        node.setOrderId(orderId);
        node.setPrice(price);
        node.setQuantity(quantity);
        node.setDirection(direction);
        return node;
    }
}
