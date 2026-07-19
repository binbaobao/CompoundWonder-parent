package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelayChipFilterTest {

    @Test
    void rejectsAnyStockWhenHistoricalMaxTurnoverExceedsFiftyFivePercent() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setMaxTurnoverRate(55.01D);

        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("历史最大换手", decision.layer());
    }

    @Test
    void rejectsMoreThanFiveBoardsInPriorTwoHundredKlines() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setHighestConsecutiveLimitUpDays(6);

        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("200根K线历史最高板", decision.layer());
    }

    @Test
    void allowsExactlyFiveBoardsOutsidePriorNinetyDays() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setHighestConsecutiveLimitUpDays(5);
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(2);

        assertTrue(RelayChipFilter.evaluate(assist).passed());
    }

    @Test
    void rejectsExactlyThreeBoardsInPriorNinetyNaturalDays() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(3);

        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("90日历史最高板", decision.layer());
    }

    @Test
    void allowsExactlyThirtyFivePercentTurnoverInPriorNinetyNaturalDaysForRelaySelection() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setPriorNinetyDayMaxTurnoverRate(35D);

        assertTrue(RelayChipFilter.evaluateRelayNinetyDayTurnoverLimit(assist).passed());
    }

    @Test
    void rejectsTurnoverAboveThirtyFivePercentInPriorNinetyNaturalDaysForRelaySelection() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setPriorNinetyDayMaxTurnoverRate(35.01D);

        RelayChipFilter.Decision decision =
                RelayChipFilter.evaluateRelayNinetyDayTurnoverLimit(assist);

        assertFalse(decision.passed());
        assertEquals("90日历史最大换手", decision.layer());
    }

    @Test
    void doesNotApplyRelayNinetyDayTurnoverLimitToFirstBoardChipEvaluation() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setPriorNinetyDayMaxTurnoverRate(40D);

        assertTrue(RelayChipFilter.evaluate(assist).passed());
    }

    @Test
    void appliesMutuallyExclusiveMarketCapTurnoverAndPriceBands() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setStartMarketCap(168_000D);
        assist.setMaxTurnoverRate(38.99D);
        assist.setCurrentPrice(21.99D);

        assertTrue(RelayChipFilter.evaluate(assist).passed());

        assist.setCurrentPrice(22D);
        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);
        assertFalse(decision.passed());
        assertEquals("市值换手价格阶梯及特殊通道", decision.layer());
    }

    @Test
    void smallestMarketCapBandAllowsTurnoverJustBelowFiftyFivePercent() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setStartMarketCap(93_000D);
        assist.setMaxTurnoverRate(54.99D);

        assertTrue(RelayChipFilter.evaluate(assist).passed());
    }

    @Test
    void smallestMarketCapBandStillRejectsExactlyFiftyFivePercent() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setStartMarketCap(93_000D);
        assist.setMaxTurnoverRate(55D);

        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("市值换手价格阶梯及特殊通道", decision.layer());
    }

    @Test
    void preservesEveryHistoricalMarketCapTurnoverAndPriceBoundary() {
        List<BandCase> bandCases = List.of(
                new BandCase(106_000D, 50D, null),
                new BandCase(120_000D, 46D, null),
                new BandCase(138_800D, 44D, null),
                new BandCase(151_000D, 43D, 25D),
                new BandCase(168_000D, 39D, 22D),
                new BandCase(187_000D, 35D, 20D),
                new BandCase(200_000D, 30D, 20D),
                new BandCase(208_000D, 27D, 18.5D),
                new BandCase(220_000D, 25D, 17D),
                new BandCase(250_000D, 25D, 16D));

        for (BandCase bandCase : bandCases) {
            RelaySelectionAssist passing = eligibleAssist();
            passing.setStartMarketCap(bandCase.marketCap());
            passing.setMaxTurnoverRate(bandCase.turnoverLimit() - 0.01D);
            if (bandCase.priceLimit() != null) {
                passing.setCurrentPrice(bandCase.priceLimit() - 0.01D);
            }
            assertTrue(RelayChipFilter.evaluate(passing).passed(), bandCase.toString());

            RelaySelectionAssist turnoverBoundary = eligibleAssist();
            turnoverBoundary.setStartMarketCap(bandCase.marketCap());
            turnoverBoundary.setMaxTurnoverRate(bandCase.turnoverLimit());
            if (bandCase.priceLimit() != null) {
                turnoverBoundary.setCurrentPrice(bandCase.priceLimit() - 0.01D);
            }
            assertFalse(RelayChipFilter.evaluate(turnoverBoundary).passed(), bandCase.toString());
        }
    }

    @Test
    void preservesLowTurnoverLowChipAmountSpecialChannel() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setStartMarketCap(280_000D);
        assist.setMaxTurnoverRate(17.99D);
        assist.setCurrentPrice(17.49D);
        assist.setHistoricalMaxVolume(22_750_000L);

        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);

        assertTrue(decision.passed());
        assertEquals("低换手低筹码金额特殊通道", decision.layer());
    }

    @Test
    void rejectsWhenRequiredHistoricalChipDataIsMissing() {
        RelaySelectionAssist assist = eligibleAssist();
        assist.setMaxTurnoverRate(null);

        RelayChipFilter.Decision decision = RelayChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("筹码数据完整性", decision.layer());
    }

    @Test
    void excludesOnlyTheFirstTenStoredKlinesForNewStocks() {
        List<StockDailyData> earliestStored = new ArrayList<>();
        List<StockDailyData> rawWindow = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            StockDailyData daily = daily(
                    LocalDate.of(2026, 1, 2).plusDays(i),
                    i < 10 ? 80D : 20D,
                    i < 10 ? 10_000_000L : 1_000_000L,
                    i < 10 ? 6 : 2);
            earliestStored.add(daily);
            rawWindow.add(daily);
        }

        RelayChipFilter.HistoricalMetrics metrics = RelayChipFilter.calculateHistoricalMetrics(
                rawWindow, earliestStored, LocalDate.of(2026, 7, 1));

        assertEquals(20D, metrics.maxTurnoverRate());
        assertEquals(1_000_000L, metrics.maxVolume());
        assertEquals(2, metrics.twoHundredKlineHighestBoard());
    }

    @Test
    void doesNotRemoveOldStockRowsWhenStoredFirstTenAreOutsideWindow() {
        List<StockDailyData> earliestStored = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            earliestStored.add(daily(
                    LocalDate.of(2022, 1, 4).plusDays(i), 10D, 100_000L, 1));
        }
        List<StockDailyData> rawWindow = List.of(
                daily(LocalDate.of(2025, 12, 1), 42D, 8_000_000L, 5),
                daily(LocalDate.of(2026, 6, 15), 30D, 5_000_000L, 3));

        RelayChipFilter.HistoricalMetrics metrics = RelayChipFilter.calculateHistoricalMetrics(
                rawWindow, earliestStored, LocalDate.of(2026, 7, 1));

        assertEquals(42D, metrics.maxTurnoverRate());
        assertEquals(8_000_000L, metrics.maxVolume());
        assertEquals(5, metrics.twoHundredKlineHighestBoard());
        assertEquals(3, metrics.ninetyDayHighestBoard());
        assertEquals(30D, metrics.ninetyDayMaxTurnoverRate());
    }

    @Test
    void usesOnlyLatestTwoHundredKlinesBeforeCurrentLimitUpRun() {
        List<StockDailyData> earliestStored = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            earliestStored.add(daily(
                    LocalDate.of(2022, 1, 4).plusDays(i), 10D, 100_000L, 1));
        }
        LocalDate historyEndDate = LocalDate.of(2026, 7, 1);
        List<StockDailyData> rawHistory = new ArrayList<>();
        for (int i = 200; i >= 0; i--) {
            boolean outsideLatestTwoHundred = i == 200;
            rawHistory.add(daily(
                    historyEndDate.minusDays(i),
                    outsideLatestTwoHundred ? 80D : 20D,
                    outsideLatestTwoHundred ? 99_000_000L : 1_000_000L,
                    outsideLatestTwoHundred ? 6 : 2));
        }

        RelayChipFilter.HistoricalMetrics metrics = RelayChipFilter.calculateHistoricalMetrics(
                rawHistory, earliestStored, historyEndDate);

        assertEquals(20D, metrics.maxTurnoverRate());
        assertEquals(1_000_000L, metrics.maxVolume());
        assertEquals(2, metrics.twoHundredKlineHighestBoard());
    }

    @Test
    void retainsTurnoverMetricsFromTheMaximumVolumeDay() {
        List<StockDailyData> earliestStored = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            earliestStored.add(daily(
                    LocalDate.of(2022, 1, 4).plusDays(i), 10D, 100_000L, 1));
        }
        StockDailyData lowerVolumeDay = daily(
                LocalDate.of(2026, 6, 1), 20D, 1_000_000L, 2);
        lowerVolumeDay.setTurnover(50_000D);
        StockDailyData maximumVolumeDay = daily(
                LocalDate.of(2026, 6, 2), 49D, 2_000_000L, 2);
        maximumVolumeDay.setTurnover(280_000D);

        RelayChipFilter.HistoricalMetrics metrics = RelayChipFilter.calculateHistoricalMetrics(
                List.of(lowerVolumeDay, maximumVolumeDay), earliestStored, LocalDate.of(2026, 7, 1));

        assertEquals(2_000_000L, metrics.maxVolume());
        assertEquals(49D, metrics.maxVolumeDayTurnoverRate());
        assertEquals(280_000D, metrics.maxVolumeDayTurnover());
    }

    private RelaySelectionAssist eligibleAssist() {
        RelaySelectionAssist assist = new RelaySelectionAssist();
        assist.setStockCode("600001");
        assist.setStartMarketCap(90_000D);
        assist.setCurrentPrice(20D);
        assist.setMaxTurnoverRate(30D);
        assist.setHighestConsecutiveLimitUpDays(2);
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(2);
        assist.setPriorNinetyDayMaxTurnoverRate(30D);
        assist.setHistoricalMaxVolume(1_000_000L);
        return assist;
    }

    private StockDailyData daily(LocalDate tradeDate, double turnoverRate,
                                   long volume, int consecutiveLimitUpDays) {
        StockDailyData daily = new StockDailyData();
        daily.setTradeDate(tradeDate);
        daily.setTurnoverRate(turnoverRate);
        daily.setVolume(volume);
        daily.setConsecutiveLimitUpDays(consecutiveLimitUpDays);
        return daily;
    }

    private record BandCase(double marketCap, double turnoverLimit, Double priceLimit) {
    }
}
