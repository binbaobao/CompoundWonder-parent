package com.compoundwonder.trader.selection.smallcapfirstboard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmallCapFirstBoardOwnershipTest {

    @Test
    void smallCapModeOwnsMarketCapStrictlyBelowBoundary() {
        assertTrue(SmallCapFirstBoardSelectionService.ownsStartMarketCap(119_998.99D));
        assertFalse(SmallCapFirstBoardSelectionService.ownsStartMarketCap(119_999D));
    }
}
