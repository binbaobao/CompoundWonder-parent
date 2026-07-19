package com.compoundwonder.core.engine;

import com.compoundwonder.core.service.CacheService;
import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.constant.ConstantUtil;
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

    private static final TradeDecisionService DECISIONS = new TestTradeDecisionService();

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

        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
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
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        engine.publish(order(symbolId, 1, 1000, 300, (byte) 1));
        engine.awaitProcessed(Duration.ofSeconds(1));
        assertEquals(300, orderBook.getBuyQuantity(1000));

        engine.reset();

        assertEquals(0, orderBook.getActiveOrderCount());
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
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
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
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();

        TickData tick = new TickData();
        tick.symbolId = SymbolUtil.fastSymbolToInt("300001");

        assertThrows(IllegalArgumentException.class, () -> engine.publish(tick));
    }

    @Test
    void treatsShanghaiNineThirtySnapshotAsContinuousMarketData() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("600000");
        OrderBook orderBook = new OrderBook("600000", 1_000_000L, 10.00, 500_000L);
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        TickData snapshot = snapshot(symbolId, ConstantUtil.TIME_930, 1001);
        engine.publish(snapshot);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(1001, orderBook.price[0]);
    }

    @Test
    void closingAuctionSellMovesShanghaiToSellPendingOnlyOnce() {
        assertClosingAuctionSellOnlyOnce("600000");
    }

    @Test
    void closingAuctionSellMovesShenzhenToSellPendingOnlyOnce() {
        assertClosingAuctionSellOnlyOnce("000001");
    }

    @Test
    void shanghaiRejectsOutOfRangePriceWhileShenzhenUsesCurrentPrice() {
        CacheService repository = new CacheService();
        int shSymbolId = SymbolUtil.fastSymbolToInt("600000");
        int szSymbolId = SymbolUtil.fastSymbolToInt("000001");
        OrderBook shOrderBook = new OrderBook("600000", 1_000_000L, 10.00, 500_000L);
        OrderBook szOrderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        shOrderBook.setLastPrice(1000);
        szOrderBook.setLastPrice(1000);
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(shSymbolId, shOrderBook);
        engine.registerOrderBook(szSymbolId, szOrderBook);

        TickData shOrder = order(shSymbolId, 1, 2000, 300, (byte) 1);
        shOrder.type = 2;
        TickData szAboveLimit = order(szSymbolId, 1, 2000, 300, (byte) 1);
        szAboveLimit.type = 2;
        TickData szBelowLimit = order(szSymbolId, 2, 800, 200, (byte) 1);
        szBelowLimit.type = 2;
        engine.publish(shOrder);
        engine.publish(szAboveLimit);
        engine.publish(szBelowLimit);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(0, shOrderBook.getActiveOrderCount());
        assertEquals(2, szOrderBook.getActiveOrderCount());
        assertEquals(500, szOrderBook.getBuyQuantity(1000));
    }

    @Test
    void shenzhenZeroPriceMarketOrderUsesCurrentOrderBookPrice() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("000001");
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        orderBook.setLastPrice(1000);
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        TickData marketOrder = order(symbolId, 1, 0, 300, (byte) 1);
        marketOrder.type = 1;
        engine.publish(marketOrder);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(1, orderBook.getActiveOrderCount());
        assertEquals(300, orderBook.getBuyQuantity(1000));
    }

    @Test
    void shanghaiIntradaySellRuleStartsAtNineThirtyOne() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("600000");
        OrderBook orderBook = new OrderBook("600000", 1_000_000L, 10.00, 500_000L);
        orderBook.setInitialMarketValue(120_000);
        RecordingOrderExecutionGateway gateway = new RecordingOrderExecutionGateway();
        engine = new DisruptorOrderBookEngine(repository, gateway, DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        engine.publish(trade(symbolId, 92_800_000, 900));
        engine.publish(trade(symbolId, 92_900_000, orderBook.getLimitUpPrice()));
        TickData limitUpBuy = order(symbolId, 1, orderBook.getLimitUpPrice(), 1_000_000, (byte) 1);
        limitUpBuy.type = 2;
        engine.publish(limitUpBuy);
        engine.awaitProcessed(Duration.ofSeconds(1));
        assertEquals(1, orderBook.getStatus());
        orderBook.setTransactionStatus(-1);

        engine.publish(trade(symbolId, ConstantUtil.TIME_930, orderBook.getLimitUpPrice()));
        engine.awaitProcessed(Duration.ofSeconds(1));
        assertEquals(0, gateway.quickSellCount);
        assertEquals(-1, orderBook.getTransactionStatus());

        engine.publish(trade(symbolId, ConstantUtil.TIME_931, orderBook.getLimitUpPrice()));
        engine.awaitProcessed(Duration.ofSeconds(1));
        assertEquals(1, gateway.quickSellCount);
        assertEquals(-2, orderBook.getTransactionStatus());
    }

    @Test
    void largeAuctionThresholdDoesNotOverflow() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("600000");
        OrderBook orderBook = new OrderBook(
                "600000", 100_000_000_000L, 10.00, 100_000_000_000L);
        engine = new DisruptorOrderBookEngine(repository, new RecordingOrderExecutionGateway(), DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        engine.publish(snapshot(symbolId, 91_600_000, orderBook.getLimitUpPrice()));
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(0, orderBook.getTransactionStatus());
    }

    @Test
    void handlerContinuesAfterUnexpectedEventException() {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("600000");
        OrderBook orderBook = new OrderBook("600000", 1_000_000L, 10.00, 500_000L);
        orderBook.setTransactionStatus(-1);
        RecordingOrderExecutionGateway gateway = new RecordingOrderExecutionGateway();
        gateway.failSell = true;
        engine = new DisruptorOrderBookEngine(repository, gateway, DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        engine.publish(snapshot(symbolId, ConstantUtil.TIME_1459, 1000));
        engine.awaitProcessed(Duration.ofSeconds(1));

        TickData trade = new TickData();
        trade.symbolId = symbolId;
        trade.dataType = 2;
        trade.time = ConstantUtil.TIME_1500;
        trade.price = 1000;
        trade.quantity = 100;
        trade.amount = 100_000;
        engine.publish(trade);
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(0, orderBook.getTransactionStatus());
        assertEquals(100, orderBook.getVolume());
    }

    private void assertClosingAuctionSellOnlyOnce(String symbol) {
        CacheService repository = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt(symbol);
        OrderBook orderBook = new OrderBook(symbol, 1_000_000L, 10.00, 500_000L);
        orderBook.setTransactionStatus(-1);
        RecordingOrderExecutionGateway gateway = new RecordingOrderExecutionGateway();
        engine = new DisruptorOrderBookEngine(repository, gateway, DECISIONS, 1024,
                "test-order-book-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        engine.registerOrderBook(symbolId, orderBook);

        engine.publish(snapshot(symbolId, ConstantUtil.TIME_1459, 1000));
        engine.publish(snapshot(symbolId, ConstantUtil.TIME_1459 + 1, 1000));
        engine.awaitProcessed(Duration.ofSeconds(1));

        assertEquals(-2, orderBook.getTransactionStatus());
        assertEquals(1, gateway.sellCount);
    }

    private TickData snapshot(int symbolId, int time, int price) {
        TickData snapshot = new TickData();
        snapshot.symbolId = symbolId;
        snapshot.dataType = 4;
        snapshot.time = time;
        snapshot.price = price;
        snapshot.orderId = 100_000;
        snapshot.buyerOrderId = 100;
        snapshot.sellerOrderId = 200;
        return snapshot;
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

    private TickData trade(int symbolId, int time, int price) {
        TickData trade = new TickData();
        trade.symbolId = symbolId;
        trade.dataType = 2;
        trade.time = time;
        trade.price = price;
        return trade;
    }

    private static final class RecordingOrderExecutionGateway implements OrderExecutionGateway {

        private int sellCount;
        private int quickSellCount;
        private boolean failSell;

        @Override
        public void buy(String date, int symbol, int price, int time) {
        }

        @Override
        public void sell(String symbol, int price, int limitDownPrice) {
            sellCount++;
            if (failSell) {
                throw new IllegalStateException("test sell failure");
            }
        }

        @Override
        public void quickSell(String symbol, int price, int limitDownPrice) {
            quickSellCount++;
        }

        @Override
        public void cancel(String symbol) {
        }

        @Override
        public void enableFirstLimitUpTradingMode(String symbol) {
        }
    }

    /** 订单簿单元测试只验证事件和执行边界，不依赖具体策略模块。 */
    private static final class TestTradeDecisionService implements TradeDecisionService {

        @Override
        public boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateSell(TradeMarketState market, TradeRuleRecord record) {
            return true;
        }

        @Override
        public boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                                TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateCancel(TradeMarketState market) {
            return false;
        }

        @Override
        public boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
            return false;
        }

        @Override
        public boolean isContinuousBuyTimeAllowed(TradeMarketState market, int time) {
            return true;
        }

        @Override
        public boolean evaluateShanghaiAuctionBuy(TradeMarketState market,
                                                  AuctionMarketEvent event,
                                                  int recordTime,
                                                  TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateShanghaiAuctionCancel(TradeMarketState market,
                                                     AuctionMarketEvent event,
                                                     int recordTime,
                                                     TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateShenzhenAuctionBuy(TradeMarketState market,
                                                 AuctionMarketEvent event,
                                                 int recordTime,
                                                 long limitUpBuyVolume,
                                                 long totalSellVolume,
                                                 TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateShenzhenAuctionCancel(TradeMarketState market,
                                                    AuctionMarketEvent event,
                                                    int recordTime,
                                                    long limitUpBuyVolume,
                                                    long totalSellVolume,
                                                    TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                                            AuctionMarketEvent event,
                                                            int recordTime,
                                                            TradeRuleRecord record) {
            return false;
        }

        @Override
        public boolean evaluateShanghaiClosingAuctionSell(TradeMarketState market,
                                                          AuctionMarketEvent event,
                                                          int recordTime,
                                                          TradeRuleRecord record) {
            return true;
        }

        @Override
        public boolean evaluateShenzhenClosingAuctionSell(TradeMarketState market,
                                                          AuctionMarketEvent event,
                                                          int recordTime,
                                                          TradeRuleRecord record) {
            return true;
        }
    }
}
