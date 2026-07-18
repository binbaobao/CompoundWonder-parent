package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.orderbook.data.BacktestDailyTickBatch;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.entity.StockTradeCalendar;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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
                stockDailyService(daily("600001", tradeDate, 10D)),
                noOpSelectionService(), Runnable::run);

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
        assertEquals(0, result.getLimitUpBreakCount());
        assertEquals(BacktestPersistenceService.COMPLETED, result.getStatus());
    }

    @Test
    void keepsOvernightCancelRecordAndSelectedBuy() {
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
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 93_200_000, 93_200_101)));
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of());
        });
        StockDailyEntity overnightDaily = daily("600001", tradeDate, 10D);
        StockDailyEntity selectedDaily = daily("000001", tradeDate, 10D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(overnightDaily, selectedDaily), selectedDaily),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertEquals("000001", write.buyRule().getSymbol());
        assertEquals(RuleConstant.TRADING_MODE_BUY, write.buyRule().getActionType());
        assertEquals(2L, write.buyTask().getId());
        assertEquals(1, write.actionRules().size());
        assertEquals(RuleConstant.TRADING_MODE_CANCEL,
                write.actionRules().get(0).rule().getActionType());
    }

    @Test
    void recordsIntradayBuyThatMissesFillBecauseOrderWasSubmittedTooLate() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask task = watchingTask(1L, "600001", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(date -> List.of(task));
        FakeReplayService replay = new FakeReplayService(request -> result(
                tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(),
                                100_000_000, 100_000_400))));
        StockDailyEntity currentDaily = daily("600001", tradeDate, 10D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(currentDaily), currentDaily),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNull(write.newPosition());
        assertNull(write.buyRule());
        assertEquals(0, write.dailyRecord().getAccountStatus());
        assertEquals(0, write.actionRules().size());
        assertEquals(1, write.triggeredRules().size());
        RuleRecordDTO unfilledRule = write.triggeredRules().get(0).rule();
        assertEquals(RuleConstant.TRADING_MODE_BUY, unfilledRule.getActionType());
        assertEquals("600001", unfilledRule.getSymbol());
        assertEquals(100_000_000, unfilledRule.getTime());
        assertEquals(100_000_400, unfilledRule.getLastOrderTime());
    }

    @Test
    void reusesSameStockBuyTriggeredAfterOvernightCancel() {
        LocalDate tradeDate = LocalDate.of(2026, 6, 18);
        StockWatchingTask overnightTask = watchingTask(1L, "600876", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(
                date -> List.of(overnightTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.mode() == BacktestReplayMode.OVERNIGHT_BUY) {
                RuleRecordDTO cancel = rule(
                        RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 91_827_000, 0);
                cancel.setRuleCode(2);
                RuleRecordDTO intradayBuy = rule(
                        RuleConstant.TRADING_MODE_BUY, request.symbol(), 93_457_270, 93_457_771);
                intradayBuy.setRuleCode(14);
                return result(tradeDate, request.symbol(), request.mode(),
                        List.of(cancel, intradayBuy));
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of());
        });
        StockDailyEntity currentDaily = daily("600876", tradeDate, 10.36D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(currentDaily), currentDaily),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNotNull(write.newPosition());
        assertEquals("600876", write.newPosition().getSymbol());
        assertEquals(14, write.buyRule().getRuleCode());
        assertEquals(93_457_270, write.buyRule().getTime());
        assertEquals(List.of(RuleConstant.TRADING_MODE_CANCEL),
                write.actionRules().stream()
                        .map(action -> action.rule().getActionType())
                        .toList());
        assertEquals(1, replay.requests.size());
    }

    @Test
    void fillsIntradayLimitUpBuyAfterEarlierAuctionCancelWhenBoardLaterBreaks() {
        LocalDate tradeDate = LocalDate.of(2026, 6, 18);
        StockWatchingTask skippedTopTask = watchingTask(1L, "600615", tradeDate);
        StockWatchingTask targetTask = watchingTask(2L, "600876", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(
                date -> List.of(skippedTopTask, targetTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (!"600876".equals(request.symbol())) {
                throw new AssertionError("kline_state <= 0 的股票不应执行回放");
            }
            RuleRecordDTO intradayBuy = rule(
                    RuleConstant.TRADING_MODE_BUY, request.symbol(), 93_457_270, 0);
            intradayBuy.setRuleCode(14);
            if (request.mode() == BacktestReplayMode.BUY) {
                RuleRecordDTO auctionBuy = rule(
                        RuleConstant.TRADING_MODE_BUY, request.symbol(), 91_500_000, 0);
                auctionBuy.setRuleCode(2);
                RuleRecordDTO auctionCancel = rule(
                        RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 91_827_000, 0);
                auctionCancel.setRuleCode(2);
                return new BacktestReplayResult(
                        tradeDate, request.symbol(), "凯盛新能", request.mode(),
                        List.of(auctionBuy, auctionCancel, intradayBuy),
                        1, 0, 1_036, 993, 10);
            }
            if (request.mode() == BacktestReplayMode.BUY_AFTER_TIME) {
                return new BacktestReplayResult(
                        tradeDate, request.symbol(), "凯盛新能", request.mode(),
                        List.of(intradayBuy), 1, 0, 1_036, 993, 10);
            }
            throw new AssertionError("未预期的回放模式: " + request.mode());
        });
        StockDailyEntity skippedTopDaily = daily("600615", tradeDate, 10D);
        skippedTopDaily.setKlineState(0);
        StockDailyEntity targetDaily = daily("600876", tradeDate, 9.93D);
        targetDaily.setKlineState(11);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(skippedTopDaily, targetDaily), targetDaily),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNotNull(write.newPosition());
        assertEquals("600876", write.newPosition().getSymbol());
        assertEquals(14, write.buyRule().getRuleCode());
        assertEquals(93_457_270, write.buyRule().getTime());
        assertEquals(1_036, write.buyRule().getPrice());
        assertEquals(List.of(
                        RuleConstant.TRADING_MODE_BUY,
                        RuleConstant.TRADING_MODE_CANCEL),
                write.actionRules().stream()
                        .map(action -> action.rule().getActionType())
                        .toList());
        assertEquals(List.of(BacktestReplayMode.BUY, BacktestReplayMode.BUY_AFTER_TIME),
                replay.requests.stream().map(ReplayRequest::mode).toList());
    }

    @Test
    void cancelledCandidateDoesNotOpenPositionAndAllowsNextCandidate() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 9);
        StockWatchingTask overnightTask = watchingTask(1L, "600001", tradeDate);
        StockWatchingTask cancelledTask = watchingTask(2L, "000524", tradeDate);
        StockWatchingTask selectedTask = watchingTask(3L, "600002", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(
                date -> List.of(overnightTask, cancelledTask, selectedTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.mode() == BacktestReplayMode.OVERNIGHT_BUY) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 91_952_000, 0)));
            }
            if ("000524".equals(request.symbol())) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 93_100_000, 93_100_101),
                        rule(RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 93_100_500, 0)));
            }
            if ("600002".equals(request.symbol())) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 93_101_000, 93_101_501)));
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of());
        });
        StockDailyEntity overnightDaily = daily("600001", tradeDate, 10D);
        StockDailyEntity cancelledDaily = daily("000524", tradeDate, 10D);
        StockDailyEntity selectedDaily = daily("600002", tradeDate, 10D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(
                        List.of(overnightDaily, cancelledDaily, selectedDaily), selectedDaily),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNotNull(write.newPosition());
        assertEquals("600002", write.newPosition().getSymbol());
        assertEquals("600002", write.buyRule().getSymbol());
        assertEquals(List.of(
                        RuleConstant.TRADING_MODE_CANCEL,
                        RuleConstant.TRADING_MODE_BUY,
                        RuleConstant.TRADING_MODE_CANCEL),
                write.actionRules().stream()
                        .map(action -> action.rule().getActionType())
                        .toList());
        assertEquals(List.of("600001", "000524", "000524"),
                write.actionRules().stream()
                        .map(action -> action.rule().getSymbol())
                        .toList());
        assertEquals(1, replay.dailyBatchRequests.size());
        assertEquals(Set.of("600001", "000524", "600002"),
                replay.dailyBatchRequests.get(0).symbols());
    }

    @Test
    void cancelledCandidateLeavesAccountEmptyWhenNoOtherBuyExists() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 9);
        StockWatchingTask overnightTask = watchingTask(1L, "600001", tradeDate);
        StockWatchingTask cancelledTask = watchingTask(2L, "000524", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(
                date -> List.of(overnightTask, cancelledTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if (request.mode() == BacktestReplayMode.OVERNIGHT_BUY) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 91_952_000, 0)));
            }
            if ("000524".equals(request.symbol())) {
                return result(tradeDate, request.symbol(), request.mode(), List.of(
                        rule(RuleConstant.TRADING_MODE_BUY, request.symbol(), 93_100_000, 93_100_101),
                        rule(RuleConstant.TRADING_MODE_CANCEL, request.symbol(), 93_100_500, 0)));
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of());
        });
        StockDailyEntity overnightDaily = daily("600001", tradeDate, 10D);
        StockDailyEntity cancelledDaily = daily("000524", tradeDate, 10D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(overnightDaily, cancelledDaily)),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNull(write.newPosition());
        assertNull(write.buyRule());
        assertEquals(0, write.dailyRecord().getAccountStatus());
        assertEquals(List.of(
                        RuleConstant.TRADING_MODE_CANCEL,
                        RuleConstant.TRADING_MODE_BUY,
                        RuleConstant.TRADING_MODE_CANCEL),
                write.actionRules().stream()
                        .map(action -> action.rule().getActionType())
                        .toList());
    }

    @Test
    void skipsNonPositiveTopTaskAndReplaysOnlyPositiveCandidatesWithoutOvernightOrder() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask overnightTask = watchingTask(1L, "600001", tradeDate);
        StockWatchingTask zeroStateTask = watchingTask(2L, "000001", tradeDate);
        StockWatchingTask negativeStateTask = watchingTask(3L, "600002", tradeDate);
        StockWatchingTask positiveStateTask = watchingTask(4L, "000002", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(date ->
                List.of(overnightTask, zeroStateTask, negativeStateTask, positiveStateTask));
        FakeReplayService replay = new FakeReplayService(request ->
                result(tradeDate, request.symbol(), request.mode(), List.of(
                    rule(RuleConstant.TRADING_MODE_BUY, request.symbol(),
                            100_000_000, 100_000_101))));
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
                        List.of(overnightDaily, zeroStateDaily, negativeStateDaily, positiveStateDaily),
                        positiveStateDaily), noOpSelectionService(), Runnable::run);

        BacktestRun result = service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertEquals("000002", write.buyRule().getSymbol());
        assertEquals(1, replay.requests.size());
        assertEquals("000002", replay.requests.get(0).symbol());
        assertEquals(BacktestReplayMode.BUY, replay.requests.get(0).mode());
        assertEquals(1, result.getLimitUpBreakCount());
    }

    @Test
    void skipsCandidateWithoutCurrentDailyAndContinuesWithOtherStocks() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask missingDailyTask = watchingTask(1L, "600001", tradeDate);
        StockWatchingTask availableTask = watchingTask(2L, "000001", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(
                date -> List.of(missingDailyTask, availableTask));
        FakeReplayService replay = new FakeReplayService(request -> {
            if ("600001".equals(request.symbol())) {
                throw new IllegalArgumentException("缺少当日日K");
            }
            return result(tradeDate, request.symbol(), request.mode(), List.of(
                    rule(RuleConstant.TRADING_MODE_BUY, request.symbol(),
                            100_000_000, 100_000_101)));
        });
        StockDailyEntity availableDaily = daily("000001", tradeDate, 10D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(availableDaily), availableDaily),
                noOpSelectionService(), Runnable::run);

        BacktestRun result = service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertEquals(BacktestPersistenceService.COMPLETED, result.getStatus());
        assertEquals("000001", write.buyRule().getSymbol());
        assertEquals(1, replay.requests.size());
        assertEquals("000001", replay.requests.get(0).symbol());
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
        StockDailyEntity sealedBuyDay = daily("600001", firstDate, 10D);
        StockDailyEntity brokenBuyDay = daily("000001", secondDate, 10D);
        brokenBuyDay.setKlineState(11);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replay, persistence, calendarService(List.of(firstDate, secondDate)),
                stockDailyService(sealedBuyDay, brokenBuyDay),
                noOpSelectionService(), Runnable::run);

        BacktestRun result = service.runRange(firstDate, secondDate);

        BacktestDayWrite secondDay = persistence.savedDays.get(1);
        assertEquals("600001", secondDay.sellRule().getSymbol());
        assertEquals("000001", secondDay.buyRule().getSymbol());
        assertEquals(2, secondDay.previousPosition().getStatus());
        assertEquals(100_000_000, replay.requests.get(2).allowedAfterTime());
        assertEquals(1, result.getLimitUpBreakCount());
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
                        daily("600001", secondDate, 10.5D)), noOpSelectionService(), Runnable::run);

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
                stockDailyService(List.of(buyDayForValuation), buyDayForValuation, sellDay),
                noOpSelectionService(), Runnable::run);

        service.runRange(buyDate, sellDate);

        BacktestDayWrite secondDay = persistence.savedDays.get(1);
        assertEquals(1, replay.requests.size());
        assertEquals(RuleConstant.SELL_BACKTEST_LIMIT_UP_BREAK_NEXT_OPEN,
                secondDay.sellRule().getRuleCode());
        assertEquals(92_500_000, secondDay.sellRule().getTime());
        assertEquals(2_169, secondDay.sellRule().getPrice());
        assertEquals(new BigDecimal("80253.00"), secondDay.previousPosition().getSellAmount());
        assertEquals(2, secondDay.previousPosition().getStatus());
    }

    @Test
    void regeneratesSelectionForEveryReplayDayBeforeReadingTasks() {
        LocalDate firstTradeDate = LocalDate.of(2026, 7, 14);
        LocalDate secondTradeDate = LocalDate.of(2026, 7, 15);
        LocalDate firstRecommendDate = LocalDate.of(2026, 7, 13);
        List<LocalDate> selectionDates = new ArrayList<>();
        StockWatchingTaskService selectionService = selectionService(selectionDates);
        FakePersistenceService persistence = new FakePersistenceService(date -> {
            int expectedSelections = date.equals(firstTradeDate) ? 1 : 2;
            assertEquals(expectedSelections, selectionDates.size());
            return List.of();
        });
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                new FakeReplayService(request -> result(
                        request.date(), request.symbol(), request.mode(), List.of())),
                persistence,
                calendarService(List.of(firstTradeDate, secondTradeDate), firstRecommendDate),
                stockDailyService(), selectionService, Runnable::run);

        service.runRange(firstTradeDate, secondTradeDate);

        assertEquals(List.of(firstRecommendDate, firstTradeDate), selectionDates);
    }

    @Test
    void fillsOvernightBuyWhenDailyTurnoverExceedsFortyMillionYuan() {
        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask task = watchingTask(9L, "600001", tradeDate);
        FakePersistenceService persistence = new FakePersistenceService(date -> List.of(task));
        BackTestTradeService replayService = new FakeReplayService(request -> new BacktestReplayResult(
                tradeDate, "600001", "测试股票", request.mode(),
                List.of(), 2, 91_501_000, 1_000, 1_000, 10));
        StockDailyEntity currentDaily = daily("600001", tradeDate, 10D);
        currentDaily.setTurnover(4_000.01D);
        HistoricalBacktestTradeServiceImpl service = new HistoricalBacktestTradeServiceImpl(
                replayService, persistence, calendarService(List.of(tradeDate)),
                stockDailyService(List.of(currentDaily), currentDaily),
                noOpSelectionService(), Runnable::run);

        service.runRange(tradeDate, tradeDate);

        BacktestDayWrite write = persistence.savedDays.get(0);
        assertNotNull(write.newPosition());
        assertEquals("600001", write.buyRule().getSymbol());
        assertEquals(1, write.buyRule().getRuleCode());
    }

    private StockTradeCalendarService calendarService(List<LocalDate> tradeDates) {
        return calendarService(tradeDates, tradeDates.get(0).minusDays(1));
    }

    private StockTradeCalendarService calendarService(List<LocalDate> tradeDates,
                                                       LocalDate previousTradeDate) {
        return (StockTradeCalendarService) Proxy.newProxyInstance(
                StockTradeCalendarService.class.getClassLoader(),
                new Class<?>[]{StockTradeCalendarService.class},
                (proxy, method, args) -> {
                    if ("findTradeDays".equals(method.getName())) {
                        return tradeDates;
                    }
                    if ("getOne".equals(method.getName())) {
                        StockTradeCalendar calendar = new StockTradeCalendar();
                        calendar.setTradeDate(previousTradeDate);
                        return calendar;
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockWatchingTaskService noOpSelectionService() {
        return selectionService(new ArrayList<>());
    }

    private StockWatchingTaskService selectionService(List<LocalDate> selectionDates) {
        return (StockWatchingTaskService) Proxy.newProxyInstance(
                StockWatchingTaskService.class.getClassLoader(),
                new Class<?>[]{StockWatchingTaskService.class},
                (proxy, method, args) -> {
                    if ("createPostCloseWatchingTasks".equals(method.getName())) {
                        selectionDates.add((LocalDate) args[0]);
                        return List.of();
                    }
                    throw new UnsupportedOperationException("Unexpected method: " + method.getName());
                });
    }

    private StockDailyService stockDailyService(StockDailyEntity... dailyRows) {
        return stockDailyService(List.of(dailyRows), dailyRows);
    }

    private StockDailyService stockDailyService(List<StockDailyEntity> candidateDailyRows,
                                                StockDailyEntity... dailyRows) {
        Deque<StockDailyEntity> rows = new ArrayDeque<>(List.of(dailyRows));
        return (StockDailyService) Proxy.newProxyInstance(
                StockDailyService.class.getClassLoader(),
                new Class<?>[]{StockDailyService.class},
                (proxy, method, args) -> {
                    if ("list".equals(method.getName())) {
                        return candidateDailyRows;
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
        private final List<DailyBatchRequest> dailyBatchRequests = new ArrayList<>();

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

        @Override
        public BacktestDailyTickBatch loadDailyTicks(LocalDate tradeDate, Set<String> stockCodes) {
            Set<String> requested = Set.copyOf(new LinkedHashSet<>(stockCodes));
            dailyBatchRequests.add(new DailyBatchRequest(tradeDate, requested));
            return new BacktestDailyTickBatch() {
                @Override
                public LocalDate tradeDate() {
                    return tradeDate;
                }

                @Override
                public Set<String> stockCodes() {
                    return requested;
                }

                @Override
                public long replay(String stockCode, Consumer<TickData> tickConsumer) {
                    return 1;
                }
            };
        }

        @Override
        public synchronized BacktestReplayResult replay(
                LocalDate tradeDate, String stockCode, BacktestReplayMode mode,
                Integer allowedAfterTime, BacktestDailyTickBatch dailyTicks) {
            return replay(tradeDate, stockCode, mode, allowedAfterTime);
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
        public void completeRun(long runId, BigDecimal finalAsset, BigDecimal totalReturnRate,
                                int limitUpBreakCount) {
            this.finalAsset = finalAsset;
            this.totalReturnRate = totalReturnRate;
            run.setLimitUpBreakCount(limitUpBreakCount);
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

    private record DailyBatchRequest(LocalDate date, Set<String> symbols) {
    }
}
