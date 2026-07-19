package com.compoundwonder.trader.selection.relay;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.entity.StockWatchingTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelaySelectionServiceTest {

    @Test
    void relayBoardCalculatesFiveDayAmplitudeIncludingCurrentDay() {
        List<StockDailyEntity> dailyList = List.of(
                daily("2026-07-08", 7.0, 10.0),
                daily("2026-07-09", 9.5, 10.1),
                daily("2026-07-10", 9.2, 10.2),
                daily("2026-07-13", 9.0, 10.3),
                daily("2026-07-14", 9.4, 10.4),
                daily("2026-07-15", 8.0, 10.8));

        double amplitude = RelaySelectionService
                .calculateSelectionAdjustedAmplitude(dailyList, 2);

        assertEquals(35.0, amplitude, 0.000001);
    }


    @Test
    void returnsZeroWhenSelectedAmplitudeWindowIsUnavailable() {
        List<StockDailyEntity> dailyList = List.of(
                daily("2026-07-14", 9.4, 10.4),
                daily("2026-07-15", 9.0, 10.8));

        double amplitude = RelaySelectionService
                .calculateSelectionAdjustedAmplitude(dailyList, 1);

        assertEquals(0.0, amplitude);
    }

    @Test
    void twoBoardSkipsTwoCurrentLimitUpDaysBeforeCountingPriorTwentyTradingDays() {
        List<StockDailyEntity> descendingDailyList = new ArrayList<>();
        for (int i = 0; i <= 22; i++) {
            int klineState = i <= 1 || (i >= 2 && i <= 4) || i == 22 ? 1 : 0;
            descendingDailyList.add(daily(LocalDate.of(2026, 7, 15).minusDays(i).toString(), klineState));
        }

        int abnormalCount = RelaySelectionService
                .countPriorTwentyDayAbnormalKlineState(descendingDailyList, 2);

        assertEquals(3, abnormalCount);
    }

    @Test
    void threeBoardSkipsThreeCurrentLimitUpDaysBeforeCountingPriorTwentyTradingDays() {
        List<StockDailyEntity> descendingDailyList = new ArrayList<>();
        for (int i = 0; i <= 22; i++) {
            int klineState = i <= 2 || (i >= 3 && i <= 6) ? 1 : 0;
            descendingDailyList.add(daily(LocalDate.of(2026, 7, 15).minusDays(i).toString(), klineState));
        }

        int abnormalCount = RelaySelectionService
                .countPriorTwentyDayAbnormalKlineState(descendingDailyList, 3);

        assertEquals(4, abnormalCount);
    }

    @Test
    void onlyAllowsFewerThanFourAbnormalKlinesInPriorTwentyTradingDays() {
        assertTrue(RelaySelectionService.isRecentAbnormalKlineCountAllowed(3));
        assertFalse(RelaySelectionService.isRecentAbnormalKlineCountAllowed(4));
    }

    @Test
    void twoBoardHasTwoAcceleratedShrinkVolumeBoardsWhenDifferentSingleConditionsMatch() {
        List<StockDailyEntity> descendingDailyList = List.of(
                acceleratedDaily(3, 8D, 30D),
                acceleratedDaily(1, 2.99D, 30D));

        assertTrue(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 2));
    }

    @Test
    void threeBoardHasTwoAcceleratedShrinkVolumeBoardsWhenAnyTwoOfThreeMatch() {
        List<StockDailyEntity> descendingDailyList = List.of(
                acceleratedDaily(1, 8D, 14D),
                acceleratedDaily(1, 8D, 30D),
                acceleratedDaily(1, 1D, 30D));

        assertTrue(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 3));
    }

    @Test
    void firstBoardDoesNotUseLowTurnoverAloneAsAcceleratedShrinkVolumeCondition() {
        List<StockDailyEntity> descendingDailyList = List.of(
                acceleratedDaily(1, 2D, 30D),
                acceleratedDaily(1, 8D, 10D));

        assertFalse(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 2));
    }

    @Test
    void acceleratedShrinkVolumeBoardKeepsAmplitudeAndTurnoverBoundariesStrict() {
        List<StockDailyEntity> descendingDailyList = List.of(
                acceleratedDaily(1, 3D, 30D),
                acceleratedDaily(1, 8D, 15D),
                acceleratedDaily(1, 8D, 30D));

        assertFalse(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 3));
    }









    @Test
    void candidateTasksWithSameScorePreferLowerCurrentPrice() {
        StockWatchingTask expensiveSameScore = watchingTask("600001", 50);
        StockWatchingTask cheapSameScore = watchingTask("600002", 50);
        StockWatchingTask higherScore = watchingTask("600003", 60);
        List<StockWatchingTask> tasks = new ArrayList<>(List.of(
                expensiveSameScore, cheapSameScore, higherScore));

        RelaySelectionService.sortSelectionTasks(tasks, Map.of(
                "600001", 20D,
                "600002", 10D,
                "600003", 30D));

        assertEquals(List.of("600003", "600002", "600001"),
                tasks.stream().map(StockWatchingTask::getStockCode).toList());
    }

    @Test
    void icePointThreeFourBoardMarketRelaxesAllRelayCandidates() {
        assertTrue(RelaySelectionService.isIcePointThreeFourBoardCandidate(3, 2));
        assertTrue(RelaySelectionService.isIcePointThreeFourBoardCandidate(3, 3));
        assertTrue(RelaySelectionService.isIcePointThreeFourBoardCandidate(4, 2));
        assertTrue(RelaySelectionService.isIcePointThreeFourBoardCandidate(4, 3));
        assertFalse(RelaySelectionService.isIcePointThreeFourBoardCandidate(2, 2));
        assertFalse(RelaySelectionService.isIcePointThreeFourBoardCandidate(5, 3));
        assertFalse(RelaySelectionService.isIcePointThreeFourBoardCandidate(3, 1));
        assertFalse(RelaySelectionService.isIcePointThreeFourBoardCandidate(4, 4));
    }

    @Test
    void normalRelayKeepsFourTasksAndWeakFiveBoardFallbackKeepsThree() {
        assertEquals(4, RelaySelectionService.NORMAL_RELAY_TASK_LIMIT);
        assertEquals(3, RelaySelectionService.WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT);
    }


    @Test
    void weakFiveBoardQualityUsesDailyCurrentMetricsAndAssistStartPrice() {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setStockCode("600001");
        daily.setFloatMarketCap(460_000D);
        daily.setTurnoverRate(46D);
        daily.setAmplitude(14D);
        daily.setClosePrice(25D);

        RelaySelectionAssist assist = new RelaySelectionAssist();
        assist.setStockCode("600001");
        assist.setStartPrice(8D);

        WeakFiveBoardFallbackPolicy.FiveBoardQuality quality =
                RelaySelectionService.toFiveBoardQuality(daily, assist);

        assertEquals(460_000D, quality.currentMarketCap());
        assertEquals(46D, quality.currentTurnoverRate());
        assertEquals(14D, quality.currentAmplitude());
        assertEquals(8D, quality.startPrice());
    }

    @Test
    void weakFiveBoardFallbackOnlyKeepsStrictPriceTwoBoardCandidates() {
        StockDailyEntity eligibleTwoBoard = relayDaily("600001", 2, 39.99D);
        StockDailyEntity highPriceTwoBoard = relayDaily("600002", 2, 40D);
        StockDailyEntity threeBoard = relayDaily("600003", 3, 20D);

        List<StockDailyEntity> candidates = RelaySelectionService
                .selectWeakFiveBoardFallbackDailyCandidates(
                        List.of(eligibleTwoBoard, highPriceTwoBoard, threeBoard));

        assertEquals(List.of("600001"),
                candidates.stream().map(StockDailyEntity::getStockCode).toList());
    }

    private StockDailyEntity daily(String tradeDate, double adjustedLow, double adjustedClose) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setAdjustLowPrice(adjustedLow);
        daily.setAdjustClosePrice(adjustedClose);
        return daily;
    }

    private StockDailyEntity daily(String tradeDate, int klineState) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setKlineState(klineState);
        return daily;
    }

    private StockDailyEntity relayDaily(String stockCode,
                                        int consecutiveLimitUpDays,
                                        double closePrice) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setStockCode(stockCode);
        daily.setConsecutiveLimitUpDays(consecutiveLimitUpDays);
        daily.setClosePrice(closePrice);
        return daily;
    }

    private StockDailyEntity acceleratedDaily(int klineState,
                                               double amplitude,
                                               double turnoverRate) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setKlineState(klineState);
        daily.setAmplitude(amplitude);
        daily.setTurnoverRate(turnoverRate);
        return daily;
    }


    private StockWatchingTask watchingTask(String stockCode, int score) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(stockCode);
        task.setLimitUpScore(score);
        return task;
    }
}

