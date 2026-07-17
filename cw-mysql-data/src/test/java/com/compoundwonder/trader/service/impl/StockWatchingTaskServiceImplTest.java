package com.compoundwonder.trader.service.impl;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import com.compoundwonder.trader.entity.StockWatchingTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockWatchingTaskServiceImplTest {

    @Test
    void relayBoardCalculatesFiveDayAmplitudeIncludingCurrentDay() {
        List<StockDailyEntity> dailyList = List.of(
                daily("2026-07-08", 7.0, 10.0),
                daily("2026-07-09", 9.5, 10.1),
                daily("2026-07-10", 9.2, 10.2),
                daily("2026-07-13", 9.0, 10.3),
                daily("2026-07-14", 9.4, 10.4),
                daily("2026-07-15", 8.0, 10.8));

        double amplitude = StockWatchingTaskServiceImpl
                .calculateSelectionAdjustedAmplitude(dailyList, 2);

        assertEquals(35.0, amplitude, 0.000001);
    }

    @Test
    void firstBoardCalculatesThreeDayAmplitudeIncludingCurrentDay() {
        List<StockDailyEntity> dailyList = List.of(
                daily("2026-07-09", 7.0, 10.1),
                daily("2026-07-10", 8.0, 10.2),
                daily("2026-07-13", 9.0, 10.3),
                daily("2026-07-14", 9.4, 10.4),
                daily("2026-07-15", 9.2, 10.8));

        double amplitude = StockWatchingTaskServiceImpl
                .calculateSelectionAdjustedAmplitude(dailyList, 1);

        assertEquals(20.0, amplitude, 0.000001);
    }

    @Test
    void returnsZeroWhenSelectedAmplitudeWindowIsUnavailable() {
        List<StockDailyEntity> dailyList = List.of(
                daily("2026-07-14", 9.4, 10.4),
                daily("2026-07-15", 9.0, 10.8));

        double amplitude = StockWatchingTaskServiceImpl
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

        int abnormalCount = StockWatchingTaskServiceImpl
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

        int abnormalCount = StockWatchingTaskServiceImpl
                .countPriorTwentyDayAbnormalKlineState(descendingDailyList, 3);

        assertEquals(4, abnormalCount);
    }

    @Test
    void onlyAllowsFewerThanFourAbnormalKlinesInPriorTwentyTradingDays() {
        assertTrue(StockWatchingTaskServiceImpl.isRecentAbnormalKlineCountAllowed(3));
        assertFalse(StockWatchingTaskServiceImpl.isRecentAbnormalKlineCountAllowed(4));
    }

    @Test
    void smallMarketCapFirstBoardUsesPreviousCloseMarketCapAndIgnoresTurnover() {
        StockSelectionAssistDTO assist = smallMarketCapFirstBoard(109_998D);
        assist.setCurrentTurnoverRate(80D);
        assist.setMaxTurnoverRate(80D);

        assertTrue(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardCandidate(assist, false));
    }

    @Test
    void smallMarketCapFirstBoardKeepsStrictMarketCapAndConvertibleBondBoundaries() {
        assertFalse(StockWatchingTaskServiceImpl.isSmallMarketCapFirstBoardCandidate(
                smallMarketCapFirstBoard(109_999D), false));
        assertFalse(StockWatchingTaskServiceImpl.isSmallMarketCapFirstBoardCandidate(
                smallMarketCapFirstBoard(100_000D), true));

        StockSelectionAssistDTO relayBoard = smallMarketCapFirstBoard(100_000D);
        relayBoard.setConsecutiveLimitUpDays(2);
        assertFalse(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardCandidate(relayBoard, false));
    }

    @Test
    void smallMarketCapFirstBoardRequiresThreeDayAmplitudeBelowTwentyPercent() {
        assertTrue(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardAmplitudeAllowed(19.999D));
        assertFalse(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardAmplitudeAllowed(20D));
    }

    @Test
    void smallMarketCapFirstBoardRequiresTenDayChangeBetweenMinusTwoAndTwentyFivePercent() {
        assertTrue(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardTenDayChangeAllowed(-1.999D));
        assertTrue(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardTenDayChangeAllowed(24.999D));
        assertFalse(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardTenDayChangeAllowed(-2D));
        assertFalse(StockWatchingTaskServiceImpl
                .isSmallMarketCapFirstBoardTenDayChangeAllowed(25D));
    }

    @Test
    void candidateTasksWithSameScorePreferLowerCurrentPrice() {
        StockWatchingTask expensiveSameScore = watchingTask("600001", 50);
        StockWatchingTask cheapSameScore = watchingTask("600002", 50);
        StockWatchingTask higherScore = watchingTask("600003", 60);
        List<StockWatchingTask> tasks = new ArrayList<>(List.of(
                expensiveSameScore, cheapSameScore, higherScore));

        StockWatchingTaskServiceImpl.sortSelectionTasks(tasks, Map.of(
                "600001", 20D,
                "600002", 10D,
                "600003", 30D));

        assertEquals(List.of("600003", "600002", "600001"),
                tasks.stream().map(StockWatchingTask::getStockCode).toList());
    }

    @Test
    void icePointThreeFourBoardMarketRelaxesAllRelayCandidates() {
        assertTrue(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(3, 2));
        assertTrue(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(3, 3));
        assertTrue(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(4, 2));
        assertTrue(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(4, 3));
        assertFalse(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(2, 2));
        assertFalse(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(5, 3));
        assertFalse(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(3, 1));
        assertFalse(StockWatchingTaskServiceImpl.isIcePointThreeFourBoardCandidate(4, 4));
    }

    @Test
    void normalRelayKeepsFourTasksAndWeakFiveBoardFallbackKeepsThree() {
        assertEquals(4, StockWatchingTaskServiceImpl.NORMAL_RELAY_TASK_LIMIT);
        assertEquals(3, StockWatchingTaskServiceImpl.WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT);
    }

    @Test
    void weakFiveBoardQualityUsesDailyCurrentMetricsAndAssistStartPrice() {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setStockCode("600001");
        daily.setFloatMarketCap(460_000D);
        daily.setTurnoverRate(46D);
        daily.setAmplitude(14D);
        daily.setClosePrice(25D);

        StockSelectionAssistDTO assist = new StockSelectionAssistDTO();
        assist.setStockCode("600001");
        assist.setStartPrice(8D);

        WeakFiveBoardFallbackPolicy.FiveBoardQuality quality =
                StockWatchingTaskServiceImpl.toFiveBoardQuality(daily, assist);

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

        List<StockDailyEntity> candidates = StockWatchingTaskServiceImpl
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

    private StockSelectionAssistDTO smallMarketCapFirstBoard(double startMarketCap) {
        StockSelectionAssistDTO assist = new StockSelectionAssistDTO();
        assist.setConsecutiveLimitUpDays(1);
        assist.setStartMarketCap(startMarketCap);
        return assist;
    }

    private StockWatchingTask watchingTask(String stockCode, int score) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(stockCode);
        task.setLimitUpScore(score);
        return task;
    }
}
