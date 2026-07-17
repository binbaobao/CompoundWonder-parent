package com.compoundwonder.trader.service.impl;

import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelayRecentPatternFilterTest {

    @Test
    void threeBoardKeepsCurrentFiveDayAmplitudeAndTenDayChangeBoundaries() {
        StockSelectionAssistDTO passing = assist(3, 48D, 49.99D);
        assertTrue(RelayRecentPatternFilter.evaluate(passing).passed());

        StockSelectionAssistDTO amplitudeAboveBoundary = assist(3, 48.01D, 40D);
        RelayRecentPatternFilter.Decision amplitudeDecision =
                RelayRecentPatternFilter.evaluate(amplitudeAboveBoundary);
        assertFalse(amplitudeDecision.passed());
        assertEquals("5日振幅", amplitudeDecision.layer());

        StockSelectionAssistDTO tenDayBoundary = assist(3, 40D, 50D);
        RelayRecentPatternFilter.Decision tenDayDecision =
                RelayRecentPatternFilter.evaluate(tenDayBoundary);
        assertFalse(tenDayDecision.passed());
        assertEquals("10日涨跌幅", tenDayDecision.layer());
    }

    @Test
    void twoBoardKeepsCurrentFiveDayAmplitudeAndTenDayChangeRange() {
        StockSelectionAssistDTO passing = assist(2, 33.99D, 12.51D);
        assertTrue(RelayRecentPatternFilter.evaluate(passing).passed());

        assertFalse(RelayRecentPatternFilter.evaluate(assist(2, 34D, 20D)).passed());
        assertFalse(RelayRecentPatternFilter.evaluate(assist(2, 30D, 12.5D)).passed());
        assertFalse(RelayRecentPatternFilter.evaluate(assist(2, 30D, 35D)).passed());
    }

    @Test
    void rejectsCandidatesOutsideCurrentRelayBoardRange() {
        RelayRecentPatternFilter.Decision decision =
                RelayRecentPatternFilter.evaluate(assist(4, 30D, 30D));

        assertFalse(decision.passed());
        assertEquals("候选连板数", decision.layer());
    }

    private StockSelectionAssistDTO assist(int consecutiveLimitUpDays,
                                           double fiveDayAmplitude,
                                           double tenDayChangeRate) {
        StockSelectionAssistDTO assist = new StockSelectionAssistDTO();
        assist.setConsecutiveLimitUpDays(consecutiveLimitUpDays);
        assist.setSelectionAmplitude(fiveDayAmplitude);
        assist.setTenDayChangeRate(tenDayChangeRate);
        return assist;
    }
}
