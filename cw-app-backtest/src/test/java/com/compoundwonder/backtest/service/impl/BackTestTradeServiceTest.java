package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.core.engine.DisruptorOrderBookEngine;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.core.service.CacheService;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class BackTestTradeServiceTest {

    private DisruptorOrderBookEngine engine;

    @AfterEach
    void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    void replaysTicksThroughReplaceableDataSource() {
        StockDailyEntity currentDaily = currentDaily();
        StockDailyEntity previousDaily = previousDaily();
        StockDailyEntity maxVolumeDaily = new StockDailyEntity();
        maxVolumeDaily.setTradeDate(LocalDate.of(2026, 7, 13));
        maxVolumeDaily.setVolume(800_000L);
        StockDailyService dailyService = (StockDailyService) Proxy.newProxyInstance(
                StockDailyService.class.getClassLoader(),
                new Class<?>[]{StockDailyService.class},
                (proxy, method, args) -> {
                    if ("list".equals(method.getName())) {
                        return List.of(currentDaily, previousDaily, maxVolumeDaily);
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });

        BacktestTickDataSource dataSource = (tradeDate, stockCode, consumer) -> {
            TickData tick = new TickData();
            tick.symbolId = 1_600_000;
            tick.dataType = 2;
            tick.time = 93_000_000;
            tick.price = 1_001;
            tick.quantity = 100;
            tick.amount = 100_100;
            consumer.accept(tick);
            return 1;
        };
        BacktestOrderExecutionGateway gateway = new BacktestOrderExecutionGateway();
        engine = new DisruptorOrderBookEngine(new CacheService(), gateway, 1024,
                "test-backtest-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        BackTestTradeService service = new BackTestTradeService(
                engine, dataSource, dailyService, null, null, gateway, 1);

        assertEquals(List.of(), service.backTest("2026-07-15", "600000", 1));
    }

    @Test
    void initializesReferencePricesFromReplayDayPrevClose() {
        StockDailyEntity currentDaily = currentDaily();
        StockDailyEntity previousDaily = previousDaily();
        previousDaily.setIsSt(true);
        StockDailyEntity maxVolumeDaily = new StockDailyEntity();
        maxVolumeDaily.setTradeDate(LocalDate.of(2026, 7, 13));
        maxVolumeDaily.setVolume(800_000L);
        StockDailyService dailyService = (StockDailyService) Proxy.newProxyInstance(
                StockDailyService.class.getClassLoader(),
                new Class<?>[]{StockDailyService.class},
                (proxy, method, args) -> {
                    if ("list".equals(method.getName())) {
                        return List.of(currentDaily, previousDaily, maxVolumeDaily);
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
        BackTestTradeService service = new BackTestTradeService(
                null, null, dailyService, null, null, new BacktestOrderExecutionGateway(), 1);

        OrderBook orderBook = service.buildOrderBook(LocalDate.of(2026, 7, 15), "600000", 1);

        assertEquals(950, orderBook.getClosePrice());
        assertEquals(1045, orderBook.getLimitUpPrice());
        assertEquals(855, orderBook.getLimitDownPrice());
        assertEquals(1_100_000L, orderBook.getCirculation());
        assertEquals(800_000L, orderBook.getMaxVolume());
        assertEquals(800_000L * 100D / 1_100_000L, orderBook.getMaxHs());
        assertEquals(15D, orderBook.getYesterdayTurnover());
        assertEquals(1, orderBook.getLbcs());
    }

    @Test
    void initializesSellHistoryUsingRealTradeCalculationRules() {
        StockDailyEntity currentDaily = currentDaily();
        StockDailyEntity yesterday = previousDaily();
        yesterday.setKlineState(3);

        StockDailyEntity twoDaysAgo = previousDaily();
        twoDaysAgo.setTradeDate(LocalDate.of(2026, 7, 13));
        twoDaysAgo.setTurnoverRate(30D);
        twoDaysAgo.setVolume(800_000L);
        twoDaysAgo.setKlineState(3);

        StockDailyEntity threeDaysAgo = previousDaily();
        threeDaysAgo.setTradeDate(LocalDate.of(2026, 7, 10));
        threeDaysAgo.setTurnoverRate(45D);
        threeDaysAgo.setVolume(600_000L);
        threeDaysAgo.setKlineState(3);

        StockDailyService dailyService = dailyService(
                List.of(currentDaily, yesterday, twoDaysAgo, threeDaysAgo));
        StockTradeCalendarService calendarService = tradeCalendarService(LocalDate.of(2026, 7, 20));
        StockEmotionCycleDailyService emotionCycleService = emotionCycleService(4);
        BackTestTradeService service = new BackTestTradeService(
                null, null, dailyService, calendarService, emotionCycleService,
                new BacktestOrderExecutionGateway(), 1);

        OrderBook orderBook = service.buildOrderBook(
                LocalDate.of(2026, 7, 15), "600000", 2);

        assertEquals(-1, orderBook.getTransactionStatus());
        assertEquals(15D, orderBook.getYesterdayTurnover());
        assertEquals(22.5D, orderBook.getTwoDaysTurnover());
        assertEquals(30D, orderBook.getThreeDaysTurnover());
        assertEquals(3, orderBook.getOneWordLimitUp());
        assertEquals(4, orderBook.getAverageLimitUpHeight());
        assertEquals(4, orderBook.getNextTradingDay());
    }

    @Test
    void rejectsUnknownDirectionBeforeReadingData() {
        BackTestTradeService service = new BackTestTradeService(
                null, null, null, null, null, new BacktestOrderExecutionGateway(), 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.backTest("2026-07-15", "600000", 3));
    }

    @Test
    void buyReplayReturnsCancellationRecordsForFrontendDisplay() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 15);
        RuleRecordDTO buy = new RuleRecordDTO();
        buy.setActionType(RuleConstant.TRADING_MODE_BUY);
        buy.setSymbol("001388");
        RuleRecordDTO cancel = new RuleRecordDTO();
        cancel.setActionType(RuleConstant.TRADING_MODE_CANCEL);
        cancel.setSymbol("001388");
        BackTestTradeService service = new BackTestTradeService(
                null, null, null, null, null, new BacktestOrderExecutionGateway(), 1) {
            @Override
            public synchronized BacktestReplayResult replay(LocalDate date, String stockCode,
                                                             BacktestReplayMode mode,
                                                             Integer allowedAfterTime) {
                return new BacktestReplayResult(
                        date, stockCode, "测试股票", mode, List.of(buy, cancel),
                        1, 0, 0, 0, 2);
            }
        };

        List<RuleRecordDTO> records = service.backTest("2026-07-15", "001388", 1);

        assertEquals(List.of(buy, cancel), records);
    }

    @Test
    void fillsLastOrderTimeFromFirstOrderAtLastTradePrice() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        orderBook.setLastPrice(1_000);
        orderBook.addOrder(order(2, 1_000, 93_000_200, (byte) 2));
        orderBook.addOrder(order(1, 1_000, 93_000_100, (byte) 1));
        RuleRecordDTO record = new RuleRecordDTO();

        BackTestTradeService.fillLastOrderTime(List.of(record), orderBook);

        assertEquals(93_000_100, record.getLastOrderTime());
    }

    @Test
    void fillsLastOrderTimeFromBuyQueueWhenShenzhenPriceHasBothDirections() {
        OrderBook orderBook = new OrderBook("000001", 1_000_000L, 10.00, 500_000L);
        orderBook.setLastPrice(1_000);
        orderBook.addOrder(order(1, 1_000, 93_000_100, (byte) 2));
        orderBook.addOrder(order(2, 1_000, 93_000_600, (byte) 1));
        RuleRecordDTO record = new RuleRecordDTO();

        BackTestTradeService.fillLastOrderTime(List.of(record), orderBook);

        assertEquals(93_000_600, record.getLastOrderTime());
    }

    @Test
    void opensBuyMonitoringOnlyAfterConfiguredTimeWhileRebuildingWholeOrderBook() {
        StockDailyService dailyService = dailyService(List.of(
                currentDaily(), previousDaily(), historicalDaily(LocalDate.of(2026, 7, 13), 800_000L)));
        BacktestTickDataSource dataSource = (tradeDate, stockCode, consumer) -> {
            consumer.accept(tradeTick(1_600_000, 92_500_000, 1_000));
            consumer.accept(tradeTick(1_600_000, 93_000_000, 1_001));
            consumer.accept(tradeTick(1_600_000, 93_000_001, 1_002));
            return 3;
        };
        BacktestOrderExecutionGateway gateway = new BacktestOrderExecutionGateway();
        engine = new DisruptorOrderBookEngine(new CacheService(), gateway, 1024,
                "test-backtest-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        BackTestTradeService service = new BackTestTradeService(
                engine, dataSource, dailyService, null, null, gateway, 1);

        BacktestReplayResult result = service.replay(
                LocalDate.of(2026, 7, 15), "600000", BacktestReplayMode.BUY_AFTER_TIME, 93_000_000);

        assertEquals(3, result.tickCount());
        assertEquals(1, result.finalTransactionStatus());
        assertEquals(1_002, result.lastPrice());
    }

    @Test
    void initializesOvernightReplayWithPendingBuyStatus() {
        StockDailyService dailyService = dailyService(List.of(
                currentDaily(), previousDaily(), historicalDaily(LocalDate.of(2026, 7, 13), 800_000L)));
        BacktestTickDataSource dataSource = (tradeDate, stockCode, consumer) -> {
            consumer.accept(tradeTick(1_600_000, 91_500_000, 1_000));
            return 1;
        };
        BacktestOrderExecutionGateway gateway = new BacktestOrderExecutionGateway();
        engine = new DisruptorOrderBookEngine(new CacheService(), gateway, 1024,
                "test-backtest-", ProducerType.SINGLE, YieldingWaitStrategy::new);
        engine.start();
        BackTestTradeService service = new BackTestTradeService(
                engine, dataSource, dailyService, null, null, gateway, 1);

        BacktestReplayResult result = service.replay(
                LocalDate.of(2026, 7, 15), "600000", BacktestReplayMode.OVERNIGHT_BUY, null);

        assertEquals(2, result.finalTransactionStatus());
    }

    private TickNode order(int orderId, int price, int time, byte direction) {
        TickNode node = new TickNode();
        node.setOrderId(orderId);
        node.setPrice(price);
        node.setTime(time);
        node.setQuantity(100);
        node.setDirection(direction);
        return node;
    }

    private TickData tradeTick(int symbolId, int time, int price) {
        TickData tick = new TickData();
        tick.symbolId = symbolId;
        tick.dataType = 2;
        tick.time = time;
        tick.price = price;
        tick.quantity = 100;
        tick.amount = price;
        return tick;
    }

    private StockDailyEntity historicalDaily(LocalDate tradeDate, long volume) {
        StockDailyEntity daily = previousDaily();
        daily.setTradeDate(tradeDate);
        daily.setVolume(volume);
        return daily;
    }

    private StockDailyService dailyService(List<StockDailyEntity> dailyRows) {
        return (StockDailyService) Proxy.newProxyInstance(
                StockDailyService.class.getClassLoader(),
                new Class<?>[]{StockDailyService.class},
                (proxy, method, args) -> {
                    if ("list".equals(method.getName())) {
                        return dailyRows;
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockTradeCalendarService tradeCalendarService(LocalDate nextTradeDay) {
        return (StockTradeCalendarService) Proxy.newProxyInstance(
                StockTradeCalendarService.class.getClassLoader(),
                new Class<?>[]{StockTradeCalendarService.class},
                (proxy, method, args) -> {
                    if ("findNextTradeDay".equals(method.getName())) {
                        return nextTradeDay;
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockEmotionCycleDailyService emotionCycleService(int averageLimitUpHeight) {
        return (StockEmotionCycleDailyService) Proxy.newProxyInstance(
                StockEmotionCycleDailyService.class.getClassLoader(),
                new Class<?>[]{StockEmotionCycleDailyService.class},
                (proxy, method, args) -> {
                    if ("queryRecentAverageLimitUpHeight".equals(method.getName())) {
                        return averageLimitUpHeight;
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockDailyEntity previousDaily() {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setStockCode("600000");
        daily.setStockName("浦发银行");
        daily.setTradeDate(LocalDate.of(2026, 7, 14));
        daily.setClosePrice(10D);
        daily.setFloatShares(1_000_000L);
        daily.setVolume(500_000L);
        daily.setFloatMarketCap(100_000D);
        daily.setTurnoverRate(15D);
        daily.setConsecutiveLimitUpDays(1);
        return daily;
    }

    private StockDailyEntity currentDaily() {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setStockCode("600000");
        daily.setStockName("浦发银行");
        daily.setIsSt(false);
        daily.setTradeDate(LocalDate.of(2026, 7, 15));
        daily.setPrevClose(9.50D);
        daily.setClosePrice(10.45D);
        daily.setFloatShares(1_100_000L);
        daily.setVolume(9_000_000L);
        daily.setTurnoverRate(80D);
        daily.setConsecutiveLimitUpDays(2);
        return daily;
    }

}
