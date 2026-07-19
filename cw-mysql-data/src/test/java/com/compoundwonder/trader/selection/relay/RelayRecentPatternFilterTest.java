package com.compoundwonder.trader.selection.relay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelayRecentPatternFilterTest {

    @Test
    void threeBoardKeepsCurrentFiveDayAmplitudeAndTenDayChangeBoundaries() {
        RelaySelectionAssist passing = assist(3, 48D, 49.99D);
        assertTrue(RelayRecentPatternFilter.evaluate(passing).passed());

        RelaySelectionAssist amplitudeAboveBoundary = assist(3, 48.01D, 40D);
        RelayRecentPatternFilter.Decision amplitudeDecision =
                RelayRecentPatternFilter.evaluate(amplitudeAboveBoundary);
        assertFalse(amplitudeDecision.passed());
        assertEquals("5日振幅", amplitudeDecision.layer());

        RelaySelectionAssist tenDayBoundary = assist(3, 40D, 50D);
        RelayRecentPatternFilter.Decision tenDayDecision =
                RelayRecentPatternFilter.evaluate(tenDayBoundary);
        assertFalse(tenDayDecision.passed());
        assertEquals("10日涨跌幅", tenDayDecision.layer());
    }

    @Test
    void twoBoardKeepsCurrentFiveDayAmplitudeAndTenDayChangeRange() {
        RelaySelectionAssist passing = assist(2, 33.99D, 11.51D);
        assertTrue(RelayRecentPatternFilter.evaluate(passing).passed());

        assertFalse(RelayRecentPatternFilter.evaluate(assist(2, 34D, 20D)).passed());
        assertFalse(RelayRecentPatternFilter.evaluate(assist(2, 30D, 11.5D)).passed());
        assertFalse(RelayRecentPatternFilter.evaluate(assist(2, 30D, 35D)).passed());
    }

    @Test
    void rejectsCandidatesOutsideCurrentRelayBoardRange() {
        RelayRecentPatternFilter.Decision decision =
                RelayRecentPatternFilter.evaluate(assist(4, 30D, 30D));

        assertFalse(decision.passed());
        assertEquals("候选连板数", decision.layer());
    }

    private RelaySelectionAssist assist(int consecutiveLimitUpDays,
                                           double fiveDayAmplitude,
                                           double tenDayChangeRate) {
        RelaySelectionAssist assist = new RelaySelectionAssist();
        assist.setConsecutiveLimitUpDays(consecutiveLimitUpDays);
        assist.setFiveDayAmplitude(fiveDayAmplitude);
        assist.setTenDayChangeRate(tenDayChangeRate);
        return assist;
    }
}

