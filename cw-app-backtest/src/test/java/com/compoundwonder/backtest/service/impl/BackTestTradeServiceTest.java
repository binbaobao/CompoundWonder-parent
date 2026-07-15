package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.core.engine.DisruptorOrderBookEngine;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.core.service.CacheService;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
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
        BackTestTradeService service = new BackTestTradeService(engine, dataSource, dailyService, gateway, 1);

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
                null, null, dailyService, new BacktestOrderExecutionGateway(), 1);

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
    void rejectsUnknownDirectionBeforeReadingData() {
        BackTestTradeService service = new BackTestTradeService(
                null, null, null, new BacktestOrderExecutionGateway(), 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.backTest("2026-07-15", "600000", 3));
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
