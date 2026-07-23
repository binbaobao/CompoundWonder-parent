package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.strategy.relay.selection.WeakFiveBoardFallbackPolicy;
import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
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
    void previousTenDayAverageHeightExcludesSelectionDay() {
        List<MarketEmotionData> descendingEmotions = new ArrayList<>();
        descendingEmotions.add(emotion("2026-07-21", 5));
        for (int height = 1; height <= 10; height++) {
            descendingEmotions.add(emotion(
                    LocalDate.of(2026, 7, 21).minusDays(height).toString(), height));
        }

        Double average = RelaySelectionService
                .calculatePreviousTenDayAverageHeight(descendingEmotions);

        assertEquals(5.5D, average, 0.000001D);
    }

    @Test
    void previousTenDayAverageHeightRequiresTenCompletePreviousTradingDays() {
        assertEquals(null, RelaySelectionService.calculatePreviousTenDayAverageHeight(List.of(
                emotion("2026-07-21", 5),
                emotion("2026-07-18", 4))));

        List<MarketEmotionData> incomplete = new ArrayList<>();
        incomplete.add(emotion("2026-07-21", 5));
        for (int index = 1; index <= 10; index++) {
            incomplete.add(emotion(
                    LocalDate.of(2026, 7, 21).minusDays(index).toString(),
                    index == 5 ? null : 4));
        }
        assertEquals(null, RelaySelectionService
                .calculatePreviousTenDayAverageHeight(incomplete));
    }

    @Test
    void relayBoardCalculatesFiveDayAmplitudeIncludingCurrentDay() {
        List<StockDailyData> dailyList = List.of(
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
        List<StockDailyData> dailyList = List.of(
                daily("2026-07-14", 9.4, 10.4),
                daily("2026-07-15", 9.0, 10.8));

        double amplitude = RelaySelectionService
                .calculateSelectionAdjustedAmplitude(dailyList, 1);

        assertEquals(0.0, amplitude);
    }

    @Test
    void twoBoardSkipsTwoCurrentLimitUpDaysBeforeCountingPriorTwentyTradingDays() {
        List<StockDailyData> descendingDailyList = new ArrayList<>();
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
        List<StockDailyData> descendingDailyList = new ArrayList<>();
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
        List<StockDailyData> descendingDailyList = List.of(
                acceleratedDaily(3, 8D, 30D),
                acceleratedDaily(1, 2.99D, 30D));

        assertTrue(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 2));
    }

    @Test
    void threeBoardHasTwoAcceleratedShrinkVolumeBoardsWhenAnyTwoOfThreeMatch() {
        List<StockDailyData> descendingDailyList = List.of(
                acceleratedDaily(1, 8D, 14D),
                acceleratedDaily(1, 8D, 30D),
                acceleratedDaily(1, 1D, 30D));

        assertTrue(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 3));
    }

    @Test
    void firstBoardDoesNotUseLowTurnoverAloneAsAcceleratedShrinkVolumeCondition() {
        List<StockDailyData> descendingDailyList = List.of(
                acceleratedDaily(1, 2D, 30D),
                acceleratedDaily(1, 8D, 10D));

        assertFalse(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 2));
    }

    @Test
    void acceleratedShrinkVolumeBoardKeepsAmplitudeAndTurnoverBoundariesStrict() {
        List<StockDailyData> descendingDailyList = List.of(
                acceleratedDaily(1, 3D, 30D),
                acceleratedDaily(1, 8D, 15D),
                acceleratedDaily(1, 8D, 30D));

        assertFalse(RelaySelectionService
                .hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(descendingDailyList, 3));
    }









    @Test
    void candidateTasksWithSameScorePreferLowerCurrentPrice() {
        SelectionTaskData expensiveSameScore = watchingTask("600001", 50);
        SelectionTaskData cheapSameScore = watchingTask("600002", 50);
        SelectionTaskData higherScore = watchingTask("600003", 60);
        List<SelectionTaskData> tasks = new ArrayList<>(List.of(
                expensiveSameScore, cheapSameScore, higherScore));

        RelaySelectionService.sortSelectionTasks(tasks, Map.of(
                "600001", 20D,
                "600002", 10D,
                "600003", 30D));

        assertEquals(List.of("600003", "600002", "600001"),
                tasks.stream().map(SelectionTaskData::getStockCode).toList());
    }

    @Test
    void everyRelayTriggerKeepsAtMostThreeTasks() {
        assertEquals(3, RelaySelectionService.NORMAL_RELAY_TASK_LIMIT);
        assertEquals(3, RelaySelectionService.WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT);
    }

    @Test
    void backupTasksOnlyFillUnusedPrimaryTopThreeSlots() {
        SelectionTaskData primaryOne = watchingTask("600001", 80);
        SelectionTaskData primaryTwo = watchingTask("600002", 70);
        SelectionTaskData backupOne = watchingTask("600003", 90);
        SelectionTaskData backupTwo = watchingTask("600004", 60);

        List<SelectionTaskData> selectedBackups =
                RelaySelectionService.selectBackupTasks(
                        List.of(primaryOne, primaryTwo),
                        List.of(backupOne, backupTwo),
                        RelaySelectionService.NORMAL_RELAY_TASK_LIMIT);

        assertEquals(List.of("600003"),
                selectedBackups.stream().map(SelectionTaskData::getStockCode).toList());
    }

    @Test
    void higherBoardRanksBeforeScoreInsideMixedBoardPool() {
        SelectionTaskData twoBoard = watchingTask("600001", 99);
        twoBoard.setConsecutiveLimitUpDays(2);
        SelectionTaskData threeBoard = watchingTask("600002", 1);
        threeBoard.setConsecutiveLimitUpDays(3);
        List<SelectionTaskData> tasks = new ArrayList<>(List.of(twoBoard, threeBoard));

        RelaySelectionService.sortSelectionTasks(tasks, Map.of(
                "600001", 5D, "600002", 50D));

        assertEquals(List.of("600002", "600001"),
                tasks.stream().map(SelectionTaskData::getStockCode).toList());
    }


    @Test
    void weakFiveBoardQualityUsesDailyCurrentMetricsAndAssistStartPrice() {
        StockDailyData daily = new StockDailyData();
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

    private StockDailyData daily(String tradeDate, double adjustedLow, double adjustedClose) {
        StockDailyData daily = new StockDailyData();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setAdjustLowPrice(adjustedLow);
        daily.setAdjustClosePrice(adjustedClose);
        return daily;
    }

    private MarketEmotionData emotion(String tradeDate, Integer height) {
        return new MarketEmotionData(LocalDate.parse(tradeDate), height, null);
    }

    private StockDailyData daily(String tradeDate, int klineState) {
        StockDailyData daily = new StockDailyData();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setKlineState(klineState);
        return daily;
    }

    private StockDailyData acceleratedDaily(int klineState,
                                               double amplitude,
                                               double turnoverRate) {
        StockDailyData daily = new StockDailyData();
        daily.setKlineState(klineState);
        daily.setAmplitude(amplitude);
        daily.setTurnoverRate(turnoverRate);
        return daily;
    }


    private SelectionTaskData watchingTask(String stockCode, int score) {
        SelectionTaskData task = new SelectionTaskData();
        task.setStockCode(stockCode);
        task.setLimitUpScore(score);
        return task;
    }
}
