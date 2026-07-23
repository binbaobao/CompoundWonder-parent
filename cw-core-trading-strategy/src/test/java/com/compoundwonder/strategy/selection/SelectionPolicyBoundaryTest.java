package com.compoundwonder.strategy.selection;

import com.compoundwonder.strategy.firstboard.selection.FirstBoardSelectionCandidate;
import com.compoundwonder.strategy.firstboard.selection.FirstBoardSelectionPolicy;
import com.compoundwonder.strategy.relay.selection.RelaySelectionCandidate;
import com.compoundwonder.strategy.relay.selection.RelaySelectionPolicy;
import com.compoundwonder.strategy.relay.selection.RelaySelectionStrength;
import com.compoundwonder.strategy.smallcapfirstboard.selection.SmallCapFirstBoardSelectionCandidate;
import com.compoundwonder.strategy.smallcapfirstboard.selection.SmallCapFirstBoardSelectionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionPolicyBoundaryTest {

    @Test
    void firstBoardAndSmallCapUseStrictMarketCapOwnership() {
        FirstBoardSelectionCandidate firstBoard = firstBoardCandidate(119_999D);
        SmallCapFirstBoardSelectionCandidate smallCap = smallCapCandidate(119_999D, 4.5D);

        assertTrue(FirstBoardSelectionPolicy.evaluate(firstBoard).passed());
        assertFalse(SmallCapFirstBoardSelectionPolicy.evaluate(smallCap).passed());
    }

    @Test
    void smallCapKeepsSixtyPercentHistoricalTurnoverBoundary() {
        assertTrue(SmallCapFirstBoardSelectionPolicy.evaluate(
                smallCapCandidate(100_000D, 4.5D)).passed());
        SmallCapFirstBoardSelectionCandidate overLimit =
                new SmallCapFirstBoardSelectionCandidate(
                        100_000D, 4.5D, 60.01D, 2, 0, 0, 10D, 10D);
        assertFalse(SmallCapFirstBoardSelectionPolicy.evaluate(overLimit).passed());
    }

    @Test
    void smallCapRejectsFirstBoardLimitPriceBelowFourPointFiveAndKeepsBoundary() {
        assertFalse(SmallCapFirstBoardSelectionPolicy.evaluate(
                smallCapCandidate(100_000D, 4.49D)).passed());
        assertTrue(SmallCapFirstBoardSelectionPolicy.evaluate(
                smallCapCandidate(100_000D, 4.5D)).passed());
    }

    @Test
    void relaxedRelayStillCannotBypassFiftyPercentStrengthLimit() {
        RelaySelectionCandidate candidate = relayCandidate(50.01D);
        assertFalse(RelaySelectionPolicy.evaluate(
                candidate, RelaySelectionStrength.RELAXED).passed());
    }

    private FirstBoardSelectionCandidate firstBoardCandidate(double cap) {
        return new FirstBoardSelectionCandidate(
                cap, 10D, 8D, 20D, "江苏", 24, 24,
                20D, 2, 2, 10_000_000L, 0, 0, 10D, 10D);
    }

    private SmallCapFirstBoardSelectionCandidate smallCapCandidate(double cap,
                                                                    double firstBoardLimitPrice) {
        return new SmallCapFirstBoardSelectionCandidate(
                cap, firstBoardLimitPrice, 60D, 2, 0, 0, 10D, 10D);
    }

    private RelaySelectionCandidate relayCandidate(double maxTurnover) {
        return new RelaySelectionCandidate(
                2, false, "江苏", 10D, 100_000D, 8D,
                20D, 100_000D, 10D, 24, 24, maxTurnover,
                2, 2, 30D, 10_000_000L, 20D, 100_000D,
                0, 0, 20D, 20D);
    }
}
