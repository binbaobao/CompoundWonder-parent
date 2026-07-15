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
        engine.reset();

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
    void rejectsNonMainBoardSymbol() {
        CacheService repository = new CacheService();
        engine = new DisruptorOrderBookEngine(repository, new NoOpOrderExecutionGateway(), 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();

        TickData tick = new TickData();
        tick.symbolId = SymbolUtil.fastSymbolToInt("300001");

        assertThrows(IllegalArgumentException.class, () -> engine.publish(tick));
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
