package com.compoundwonder.backtest.service.impl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelaySelectionResearchMetricTest {

    @Test
    void noCompletedDailyOpportunityDoesNotReportFullCapture() {
        assertNull(RelaySelectionResearchServiceImpl.aggregateCaptureRate(
                0, 0, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void anySelectedTieAtMaximumCountsAsCaptured() {
        assertTrue(RelaySelectionResearchServiceImpl.isDailyBestCaptured(
                new BigDecimal("0.10000000"), new BigDecimal("0.100")));
        assertFalse(RelaySelectionResearchServiceImpl.isDailyBestCaptured(
                new BigDecimal("0.10000000"), new BigDecimal("0.09000000")));
    }

    @Test
    void zeroReturnOpportunitiesUseCapturedDayRatio() {
        assertEquals("0.50000000",
                RelaySelectionResearchServiceImpl.aggregateCaptureRate(
                        2, 1, BigDecimal.ZERO, BigDecimal.ZERO).toPlainString());
    }
}
