package com.compoundwonder.core.processor;

import com.compoundwonder.constant.ConstantUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedTradeExecutorTest {

    @Test
    void disablesOnlyFirstBoardThatHasNeverTouchedLimitUpByTenOClock() {
        assertFalse(UnifiedTradeExecutor.shouldDisableWeakFirstBoard(
                ConstantUtil.TIME_1000 - 1, 0));
        assertTrue(UnifiedTradeExecutor.shouldDisableWeakFirstBoard(
                ConstantUtil.TIME_1000, 0));
        assertFalse(UnifiedTradeExecutor.shouldDisableWeakFirstBoard(
                ConstantUtil.TIME_1000, 1));
        assertFalse(UnifiedTradeExecutor.shouldDisableWeakFirstBoard(
                ConstantUtil.TIME_1000, 2));
        assertFalse(UnifiedTradeExecutor.shouldDisableWeakFirstBoard(
                ConstantUtil.TIME_1000, 4));
    }
}
