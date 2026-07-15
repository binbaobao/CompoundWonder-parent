package com.compoundwonder.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(800, orderBook.getTotalBuyVolume());
        assertEquals(200, orderBook.getTotalSellVolume());
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
        assertNull(orderBook.getIdIndex().get(1));
        assertNull(completed.getPrevious());
        assertNull(completed.getNext());
        assertSame(secondBuy, orderBook.getPriceLevel(1000).getBuyHead());
        assertEquals(500, orderBook.getBuyQuantity(1000));
        assertEquals(500, orderBook.getTotalBuyVolume());
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
        assertEquals(400, orderBook.getBuyQuantity(1000));
        assertEquals(400, orderBook.getTotalBuyVolume());
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
