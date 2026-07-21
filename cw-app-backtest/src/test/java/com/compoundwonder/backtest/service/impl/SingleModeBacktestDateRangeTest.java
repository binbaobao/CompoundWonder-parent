package com.compoundwonder.backtest.service.impl;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleModeBacktestDateRangeTest {

    @Test
    void prependsPreviousTradeDateSoFirstExecutionDayKeepsCrossYearRecommendation() {
        List<LocalDate> executionDays = new ArrayList<>(List.of(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 6)));

        List<LocalDate> recommendationDays = SingleModeBacktestServiceImpl.recommendationDays(
                executionDays, LocalDate.of(2025, 12, 31));

        assertEquals(List.of(
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 6)), recommendationDays);
        assertEquals(2, executionDays.size());
    }
}
