package com.compoundwonder.core.engine;

import com.compoundwonder.core.service.CacheService;
import com.compoundwonder.core.service.OrderExecutionGateway;
import com.compoundwonder.util.SymbolUtil;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisruptorOrderBookEngineTest {

    private DisruptorOrderBookEngine engine;

    @AfterEach
    void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    void replaysMainBoardTickIntoRegisteredOrderBook() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("600000");
        OrderBook orderBook = new OrderBook("600000", 1_000_000L, 10.00, 500_000L);

        engine = new DisruptorOrderBookEngine(repository, new NoOpOrderExecutionGateway(), 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        TickData trade = new TickData();
        trade.symbolId = symbolId;
        trade.dataType = 2;
        trade.time = 93000000;
        trade.price = 1001;
        trade.quantity = 100;
        trade.amount = 100_100;
        engine.publish(trade);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(100, orderBook.getVolume());
        assertEquals(100_100, orderBook.getTurnover());
        assertEquals(1001, orderBook.getLastPrice());
        assertEquals(List.of(), engine.drainRuleRecords());
    }

    @Test
    void resetRemovesHandlerPrivateOrderBooks() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("000001");
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        engine = new DisruptorOrderBookEngine(repository, new NoOpOrderExecutionGateway(), 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        engine.publish(order(symbolId, 1, 1000, 300, (byte) 1));
        engine.awaitProcessed(Duration.ofSeconds(1));
        assertEquals(300, orderBook.getBuyQuantity(1000));

        engine.reset();

        assertEquals(0, orderBook.getIdIndex().size());
        assertEquals(0, orderBook.getBuyQuantity(1000));
        assertEquals(0, orderBook.getTotalBuyVolume());

        TickData trade = new TickData();
        trade.symbolId = symbolId;
        trade.dataType = 2;
        trade.time = 93000000;
        trade.price = 1001;
        trade.quantity = 100;
        trade.amount = 100_100;
        engine.publish(trade);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(0, orderBook.getVolume());
    }

    @Test
    void reconstructsIndependentShenzhenQueuesAtTheSamePrice() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("000001");
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        engine = new DisruptorOrderBookEngine(repository, new NoOpOrderExecutionGateway(), 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        TickData buy = order(symbolId, 1, 1000, 300, (byte) 1);
        TickData sell = order(symbolId, 2, 1000, 200, (byte) 2);
        engine.publish(buy);
        engine.publish(sell);
        engine.awaitProcessed(Duration.ofSeconds(1));

        PriceLevel level = orderBook.getPriceLevel(1000);
        assertEquals(1, level.getBuyHead().getOrderId());
        assertEquals(2, level.getSellHead().getOrderId());
        assertEquals(300, level.getBuyQuantity());
        assertEquals(200, level.getSellQuantity());

        TickData trade = new TickData();
        trade.symbolId = symbolId;
        trade.dataType = 2;
        trade.type = 0;
        trade.time = 92500000;
        trade.price = 1000;
        trade.quantity = 200;
        trade.amount = 200_000;
        trade.buyerOrderId = 1;
        trade.sellerOrderId = 2;
        engine.publish(trade);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(100, orderBook.getBuyQuantity(1000));
        assertEquals(0, orderBook.getSellQuantity(1000));
        assertEquals(100, orderBook.getTotalBuyVolume());
        assertEquals(0, orderBook.getTotalSellVolume());
    }

    @Test
    void rejectsNonMainBoardSymbol() {
        CacheService repository = new CacheService();
        engine = new DisruptorOrderBookEngine(repository, new NoOpOrderExecutionGateway(), 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();

        TickData tick = new TickData();
        tick.symbolId = SymbolUtil.fastSymbolToInt("300001");

        assertThrows(IllegalArgumentException.class, () -> engine.publish(tick));
    }

    private TickData order(int symbolId, int orderId, int price, int quantity, byte direction) {
        TickData order = new TickData();
        order.symbolId = symbolId;
        order.dataType = 1;
        order.time = 91900000;
        order.orderId = orderId;
        order.price = price;
        order.quantity = quantity;
        order.direction = direction;
        return order;
    }

    private static final class NoOpOrderExecutionGateway implements OrderExecutionGateway {
        @Override
        public void buy(String date, int symbol, int price, int time) {
        }

        @Override
        public void sell(String symbol, int price, int limitDownPrice) {
        }

        @Override
        public void quickSell(String symbol, int price, int limitDownPrice) {
        }

        @Override
        public void cancel(String symbol) {
        }

        @Override
        public void enableFirstLimitUpTradingMode(String symbol) {
        }
    }
}
