package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.StockWatchingTask;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HistoricalBacktestTradeServiceImplTest {

    @Test
    void createsPositionFromFillableOvernightOrderAndSavesDailyEquity() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask task = watchingTask(9L, "600001", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(date -> List.of(task));
        BackTestTradeService replayService = new FakeReplayService(request -> new BacktestReplayResult(
                tradeDate, "600001", "测试股票", request.mode(),
                List.of(), 2, 91_501_001, 1_000, 1_000, 10));

        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replayService, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(daily("600001", tradeDate, 10D)), Runnable::run);

        BacktestRun result = service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNotNull(write.newPosition());
        assertEquals(9_900, write.newPosition().getQuantity());
        assertEquals(new BigDecimal("99000.00"), write.newPosition().getBuyAmount());
        assertEquals(new BigDecimal("13.86"), write.newPosition().getBuyFee());
        assertEquals(1, write.buyRule().getRuleCode());
        assertEquals(new BigDecimal("99986.14"), write.dailyRecord().getTotalAsset());
        assertEquals(new BigDecimal("99986.14"), persistence.finalAsset);
        assertEquals(new BigDecimal("-0.00013860"), persistence.totalReturnRate);
        assertEquals(BacktestPersistenceService.COMPLETED, result.getStatus());
    }

    @Test
    void discardsOvernightCancelReplayRecordsAndKeepsOnlySelectedBuy() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask overnightTask = watchingTask(1L, "600001", tradeDate);
        StockWatchingTask selectedTask = watchingTask(2L, "000001", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(
                date -> List.of(overnightTask, selectedTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.mode() == BacktestReplayMode.OVERNIGHT_BUY) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 91_952_000, 0),
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 100_000_000, 100_000_600)));
            }
            if ("000001".equals(request.symbol())) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 100_000_000, 100_000_101)));
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of());
        });
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(daily("000001", tradeDate, 10D)), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertEquals("000001", write.buyRule().getSymbol());
        assertEquals(RuleConstant.TRADING_MODE_BUY, write.buyRule().getActionType());
        assertEquals(2L, write.buyTask().getId());
    }

    @Test
    void skipsNonPositiveKlineStateCandidatesButStillReplaysTheOvernightOrder() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask overnightTask = watchingTask(1L, "600001", tradeDate);
        StockWatchingTask zeroStateTask = watchingTask(2L, "000001", tradeDate);
        StockWatchingTask negativeStateTask = watchingTask(3L, "600002", tradeDate);
        StockWatchingTask positiveStateTask = watchingTask(4L, "000002", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(date ->
                List.of(overnightTask, zeroStateTask, negativeStateTask, positiveStateTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.mode() == BacktestReplayMode.OVERNIGHT_BUY) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 91_952_000, 0)));
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of(
                    rule(RuleConstant.TRADING_MODE_BUY, request.symbol(),
                            100_000_000, 100_000_101)));
        });
        StockDailyEntity overnightDaily = daily("600001", tradeDate, 10D);
        overnightDaily.setKlineState(0);
        StockDailyEntity zeroStateDaily = daily("000001", tradeDate, 10D);
        zeroStateDaily.setKlineState(0);
        StockDailyEntity negativeStateDaily = daily("600002", tradeDate, 10D);
        negativeStateDaily.setKlineState(-1);
        StockDailyEntity positiveStateDaily = daily("000002", tradeDate, 10D);
        positiveStateDaily.setKlineState(11);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(
                        List.of(overnightDaily, zeroStateDaily, negativeStateDaily),
                        positiveStateDaily), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertEquals("000002", write.buyRule().getSymbol());
        assertEquals(2, replay.requests.size());
        assertEquals("600001", replay.requests.get(0).symbol());
        assertEquals(BacktestReplayMode.OVERNIGHT_BUY, replay.requests.get(0).mode());
        assertEquals("000002", replay.requests.get(1).symbol());
        assertEquals(BacktestReplayMode.BUY_AFTER_TIME, replay.requests.get(1).mode());
    }

    @Test
    void sellsHoldingThenReplaysCandidatesOnlyAfterSellTime() {
        LocalDate firstDate = LocalDate.of(2026, 7, 13);
        LocalDate secondDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask firstTask = watchingTask(1L, "600001", firstDate);
        StockWatchingTask secondTask = watchingTask(2L, "000001", secondDate);
        FakePersistenceService persistence = new FakePersistenceService(date ->
                date.equals(firstDate) ? List.of(firstTask) : List.of(secondTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.date().equals(firstDate)) {
                return new BacktestReplayResult(firstDate, "600001", "测试股票",
                        request.mode(), List.of(), 2, 91_501_001, 1_000, 1_000, 10);
            }
            if (request.mode() == BacktestReplayMode.SELL) {
                return result(secondDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_SELL, request.symbol(), 100_000_000, 0, 1_100)));
            }
            return result(secondDate, request.symbol(), request.mode(), List.of(
                    rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 100_000_001, 100_000_102)));
        });
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(firstDate, secondDate)),
                stockDailyService(
                        daily("600001", firstDate, 10D),
                        daily("000001", secondDate, 10D)), Runnable::run);

        service.runRange(firstDate, secondDate);

        BacktestDayWrite secondDay = persistence.savedDays.get(1);
        assertEquals("600001", secondDay.sellRule().getSymbol());
        assertEquals("000001", secondDay.buyRule().getSymbol());
        assertEquals(2, secondDay.previousPosition().getStatus());
        assertEquals(100_000_000, replay.requests.get(2).allowedAfterTime());
    }

    @Test
    void keepsHoldingAndDoesNotReplayBuyCandidatesWhenNoSellRuleMatches() {
        LocalDate firstDate = LocalDate.of(2026, 7, 13);
        LocalDate secondDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask firstTask = watchingTask(1L, "600001", firstDate);
        StockWatchingTask secondTask = watchingTask(2L, "000001", secondDate);
        FakePersistenceService persistence = new FakePersistenceService(date ->
                date.equals(firstDate) ? List.of(firstTask) : List.of(secondTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.date().equals(firstDate)) {
                return new BacktestReplayResult(firstDate, "600001", "测试股票",
                        request.mode(), List.of(), 2, 91_501_001, 1_000, 1_000, 10);
            }
            return result(secondDate, request.symbol(), request.mode(), List.of());
        });
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(firstDate, secondDate)),
                stockDailyService(
                        daily("600001", firstDate, 10D),
                        daily("600001", secondDate, 10.5D)), Runnable::run);

        service.runRange(firstDate, secondDate);

        BacktestDayWrite secondDay = persistence.savedDays.get(1);
        assertNotNull(secondDay.previousPosition());
        assertEquals(1, secondDay.previousPosition().getStatus());
        assertEquals(2, secondDay.previousPosition().getHoldingTradeDays());
        assertNull(secondDay.sellRule());
        assertNull(secondDay.buyRule());
        assertNull(secondDay.newPosition());
        assertEquals(2, replay.requests.size());
        assertEquals(BacktestReplayMode.SELL, replay.requests.get(1).mode());
    }

    @Test
    void sellsAtNextTradingDayOpenWithoutReplayWhenBuyDayBrokeLimitUp() {
        LocalDate buyDate = LocalDate.of(2026, 3, 13);
        LocalDate sellDate = LocalDate.of(2026, 3, 16);
        StockWatchingTask task = watchingTask(6_762L, "001260", buyDate);
        FakePersistenceService persistence = new FakePersistenceService(date ->
                date.equals(buyDate) ? List.of(task) : List.of());
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.mode() == BacktestReplayMode.OVERNIGHT_BUY) {
                return new BacktestReplayResult(buyDate, "001260", "坤泰股份",
                        request.mode(), List.of(), 2, 91_501_001, 2_698, 2_342, 10);
            }
            throw new IllegalArgumentException("2026-03-16 没有找到股票 001260 的 Level2 Tick");
        });
        StockDailyEntity buyDayForValuation = daily("001260", buyDate, 23.42D);
        buyDayForValuation.setOpenPrice(26.98D);
        buyDayForValuation.setKlineState(12);
        StockDailyEntity sellDay = daily("001260", sellDate, 21.85D);
        sellDay.setOpenPrice(21.69D);
        sellDay.setKlineState(0);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(buyDate, sellDate)),
                stockDailyService(buyDayForValuation, sellDay), Runnable::run);

        service.runRange(buyDate, sellDate);

        BacktestDayWrite secondDay = persistence.savedDays.get(1);
        assertEquals(1, replay.requests.size());
        assertEquals(RuleConstant.SELL_BACKTEST_LIMIT_UP_BREAK_NEXT_OPEN,
                secondDay.sellRule().getRuleCode());
        assertEquals(93_000_000, secondDay.sellRule().getTime());
        assertEquals(2_169, secondDay.sellRule().getPrice());
        assertEquals(new BigDecimal("80253.00"), secondDay.previousPosition().getSellAmount());
        assertEquals(2, secondDay.previousPosition().getStatus());
    }

    private StockTradeCalendarService calendarService(List<LocalDate> tradeDates) {
        return (StockTradeCalendarService) Proxy.newProxyInstance(
                StockTradeCalendarService.class.getClassLoader(),
                new Class<?>[]{StockTradeCalendarService.class},
                (proxy, method, args) -> {
                    if ("findTradeDays".equals(method.getName())) {
                        return tradeDates;
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockDailyService stockDailyService(StockDailyEntity... dailyRows) {
        return stockDailyService(List.of(), dailyRows);
    }

    private StockDailyService stockDailyService(List<StockDailyEntity> nonBuyableRows,
                                                StockDailyEntity... dailyRows) {
        Deque<StockDailyEntity> rows = new ArrayDeque<>(List.of(dailyRows));
        return (StockDailyService) Proxy.newProxyInstance(
                StockDailyService.class.getClassLoader(),
                new Class<?>[]{StockDailyService.class},
                (proxy, method, args) -> {
                    if ("list".equals(method.getName())) {
                        return nonBuyableRows;
                    }
                    if ("getOne".equals(method.getName())) {
                        return rows.removeFirst();
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockWatchingTask watchingTask(long id, String symbol, LocalDate tradeDate) {
        StockWatchingTask task = new StockWatchingTask();
        task.setId(id);
        task.setStockCode(symbol);
        task.setStockName("测试股票");
        task.setTradeDate(tradeDate);
        task.setTradeMode(1);
        task.setLimitUpScore(90);
        return task;
    }

    private StockDailyEntity daily(String symbol, LocalDate tradeDate, double closePrice) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setStockCode(symbol);
        daily.setTradeDate(tradeDate);
        daily.setClosePrice(closePrice);
        daily.setAdjustFactor(1D);
        daily.setKlineState(1);
        return daily;
    }

    private BacktestReplayResult result(LocalDate date, String symbol, BacktestReplayMode mode,
                                        List<RuleRecordDTO> records) {
        return new BacktestReplayResult(date, symbol, "测试股票", mode,
                records, 1, 0, 1_000, 1_000, 10);
    }

    private RuleRecordDTO rule(int actionType, String symbol, int time, int lastOrderTime) {
        return rule(actionType, symbol, time, lastOrderTime, 1_000);
    }

    private RuleRecordDTO rule(int actionType, String symbol, int time, int lastOrderTime, int price) {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(actionType);
        rule.setRuleCode(actionType == RuleConstant.TRADING_MODE_SELL ? 101 : 14);
        rule.setSymbol(symbol);
        rule.setTime(time);
        rule.setLastOrderTime(lastOrderTime);
        rule.setPrice(price);
        return rule;
    }

    private static final class FakeReplayService extends BackTestTradeService {
        private final Function<ReplayRequest, BacktestReplayResult> provider;
        private final List<ReplayRequest> requests = new ArrayList<>();

        private FakeReplayService(Function<ReplayRequest, BacktestReplayResult> provider) {
            super(null, null, null, null, null, new BacktestOrderExecutionGateway(), 1);
            this.provider = provider;
        }

        @Override
        public synchronized BacktestReplayResult replay(LocalDate tradeDate, String stockCode,
                                                        BacktestReplayMode mode, Integer allowedAfterTime) {
            ReplayRequest request = new ReplayRequest(tradeDate, stockCode, mode, allowedAfterTime);
            requests.add(request);
            return provider.apply(request);
        }
    }

    private static final class FakePersistenceService extends BacktestPersistenceService {
        private final Function<LocalDate, List<StockWatchingTask>> taskProvider;
        private final BacktestRun run;
        private final List<BacktestDayWrite> savedDays = new ArrayList<>();
        private BigDecimal finalAsset;
        private BigDecimal totalReturnRate;

        private FakePersistenceService(Function<LocalDate, List<StockWatchingTask>> taskProvider) {
            super(null, null, null, null, null);
            this.taskProvider = taskProvider;
            this.run = new BacktestRun();
            this.run.setId(7L);
            this.run.setStatus(RUNNING);
        }

        @Override
        public BacktestRun createRun(LocalDate startDate, LocalDate endDate, BigDecimal initialCapital) {
            return run;
        }

        @Override
        public List<StockWatchingTask> findWatchingTasks(LocalDate tradeDate) {
            return taskProvider.apply(tradeDate);
        }

        @Override
        public void saveDay(BacktestDayWrite write) {
            this.savedDays.add(write);
        }

        @Override
        public void completeRun(long runId, BigDecimal finalAsset, BigDecimal totalReturnRate) {
            this.finalAsset = finalAsset;
            this.totalReturnRate = totalReturnRate;
            run.setStatus(COMPLETED);
        }

        @Override
        public BacktestRun findRun(long runId) {
            return run;
        }

        @Override
        public void failRun(long runId, RuntimeException exception) {
            throw new AssertionError("回测不应失败", exception);
        }
    }

    private record ReplayRequest(LocalDate date, String symbol,
                                 BacktestReplayMode mode, Integer allowedAfterTime) {
    }
}
