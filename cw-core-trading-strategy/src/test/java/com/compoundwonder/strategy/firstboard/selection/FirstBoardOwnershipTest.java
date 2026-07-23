package com.compoundwonder.strategy.firstboard.selection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstBoardOwnershipTest {

    @Test
    void normalFirstBoardModeStartsAtSmallCapBoundary() {
        assertFalse(FirstBoardSelectionService.ownsStartMarketCap(119_998.99D));
        assertTrue(FirstBoardSelectionService.ownsStartMarketCap(119_999D));
    }

    @Test
    void normalFirstBoardModeEndsBeforeTwoPointFourBillion() {
        assertTrue(FirstBoardSelectionService.ownsCurrentFirstBoardMarketCap(239_999.99D));
        assertFalse(FirstBoardSelectionService.ownsCurrentFirstBoardMarketCap(240_000D));
    }
}
