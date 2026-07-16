package com.compoundwonder.trader.service.impl;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
}
