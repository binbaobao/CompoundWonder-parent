package com.compoundwonder.trader.service.impl;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockChipFilterTest {

    @Test
    void rejectsAnyStockWhenHistoricalMaxTurnoverExceedsFiftyPercent() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setMaxTurnoverRate(50.01D);

        StockChipFilter.Decision decision = StockChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("历史最大换手", decision.layer());
    }

    @Test
    void rejectsMoreThanFiveBoardsInPriorEighteenMonths() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setHighestConsecutiveLimitUpDays(6);

        StockChipFilter.Decision decision = StockChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("18个月历史最高板", decision.layer());
    }

    @Test
    void allowsExactlyFiveBoardsOutsidePriorNinetyDays() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setHighestConsecutiveLimitUpDays(5);
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(2);

        assertTrue(StockChipFilter.evaluate(assist).passed());
    }

    @Test
    void rejectsExactlyThreeBoardsInPriorNinetyNaturalDays() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(3);

        StockChipFilter.Decision decision = StockChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("90日历史最高板", decision.layer());
    }

    @Test
    void appliesMutuallyExclusiveMarketCapTurnoverAndPriceBands() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setStartMarketCap(168_000D);
        assist.setMaxTurnoverRate(38.99D);
        assist.setCurrentPrice(21.99D);

        assertTrue(StockChipFilter.evaluate(assist).passed());

        assist.setCurrentPrice(22D);
        StockChipFilter.Decision decision = StockChipFilter.evaluate(assist);
        assertFalse(decision.passed());
        assertEquals("市值换手价格阶梯及特殊通道", decision.layer());
    }

    @Test
    void globalFiftyPercentBoundaryStillAllowsSmallestMarketCapBand() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setStartMarketCap(93_000D);
        assist.setMaxTurnoverRate(50D);

        assertTrue(StockChipFilter.evaluate(assist).passed());
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
                new BandCase(208_000D, 27D, 17.5D),
                new BandCase(220_000D, 25D, 15D),
                new BandCase(250_000D, 20D, 15D));

        for (BandCase bandCase : bandCases) {
            StockSelectionAssistDTO passing = eligibleAssist();
            passing.setStartMarketCap(bandCase.marketCap());
            passing.setMaxTurnoverRate(bandCase.turnoverLimit() - 0.01D);
            if (bandCase.priceLimit() != null) {
                passing.setCurrentPrice(bandCase.priceLimit() - 0.01D);
            }
            assertTrue(StockChipFilter.evaluate(passing).passed(), bandCase.toString());

            StockSelectionAssistDTO turnoverBoundary = eligibleAssist();
            turnoverBoundary.setStartMarketCap(bandCase.marketCap());
            turnoverBoundary.setMaxTurnoverRate(bandCase.turnoverLimit());
            if (bandCase.priceLimit() != null) {
                turnoverBoundary.setCurrentPrice(bandCase.priceLimit() - 0.01D);
            }
            assertFalse(StockChipFilter.evaluate(turnoverBoundary).passed(), bandCase.toString());
        }
    }

    @Test
    void preservesLowTurnoverLowChipAmountSpecialChannel() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setStartMarketCap(280_000D);
        assist.setMaxTurnoverRate(17.99D);
        assist.setCurrentPrice(17.49D);
        assist.setHistoricalMaxVolume(22_750_000L);

        StockChipFilter.Decision decision = StockChipFilter.evaluate(assist);

        assertTrue(decision.passed());
        assertEquals("低换手低筹码金额特殊通道", decision.layer());
    }

    @Test
    void rejectsWhenRequiredHistoricalChipDataIsMissing() {
        StockSelectionAssistDTO assist = eligibleAssist();
        assist.setMaxTurnoverRate(null);

        StockChipFilter.Decision decision = StockChipFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("筹码数据完整性", decision.layer());
    }

    @Test
    void excludesOnlyTheFirstTenStoredKlinesForNewStocks() {
        List<StockDailyEntity> earliestStored = new ArrayList<>();
        List<StockDailyEntity> rawWindow = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            StockDailyEntity daily = daily(
                    LocalDate.of(2026, 1, 2).plusDays(i),
                    i < 10 ? 80D : 20D,
                    i < 10 ? 10_000_000L : 1_000_000L,
                    i < 10 ? 6 : 2);
            earliestStored.add(daily);
            rawWindow.add(daily);
        }

        StockChipFilter.HistoricalMetrics metrics = StockChipFilter.calculateHistoricalMetrics(
                rawWindow, earliestStored, LocalDate.of(2026, 7, 1));

        assertEquals(20D, metrics.maxTurnoverRate());
        assertEquals(1_000_000L, metrics.maxVolume());
        assertEquals(2, metrics.eighteenMonthHighestBoard());
    }

    @Test
    void doesNotRemoveOldStockRowsWhenStoredFirstTenAreOutsideWindow() {
        List<StockDailyEntity> earliestStored = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            earliestStored.add(daily(
                    LocalDate.of(2022, 1, 4).plusDays(i), 10D, 100_000L, 1));
        }
        List<StockDailyEntity> rawWindow = List.of(
                daily(LocalDate.of(2025, 12, 1), 42D, 8_000_000L, 5),
                daily(LocalDate.of(2026, 6, 15), 30D, 5_000_000L, 3));

        StockChipFilter.HistoricalMetrics metrics = StockChipFilter.calculateHistoricalMetrics(
                rawWindow, earliestStored, LocalDate.of(2026, 7, 1));

        assertEquals(42D, metrics.maxTurnoverRate());
        assertEquals(8_000_000L, metrics.maxVolume());
        assertEquals(5, metrics.eighteenMonthHighestBoard());
        assertEquals(3, metrics.ninetyDayHighestBoard());
    }

    private StockSelectionAssistDTO eligibleAssist() {
        StockSelectionAssistDTO assist = new StockSelectionAssistDTO();
        assist.setStockCode("600001");
        assist.setStartMarketCap(90_000D);
        assist.setCurrentPrice(20D);
        assist.setMaxTurnoverRate(30D);
        assist.setHighestConsecutiveLimitUpDays(2);
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(2);
        assist.setHistoricalMaxVolume(1_000_000L);
        return assist;
    }

    private StockDailyEntity daily(LocalDate tradeDate, double turnoverRate,
                                   long volume, int consecutiveLimitUpDays) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setTradeDate(tradeDate);
        daily.setTurnoverRate(turnoverRate);
        daily.setVolume(volume);
        daily.setConsecutiveLimitUpDays(consecutiveLimitUpDays);
        return daily;
    }

    private record BandCase(double marketCap, double turnoverLimit, Double priceLimit) {
    }
}
