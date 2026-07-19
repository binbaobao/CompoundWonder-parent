package com.compoundwonder.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradeModeTest {

    @Test
    void keepsStableDatabaseCodes() {
        assertEquals(1, TradeMode.RELAY_LIMIT_UP.code());
        assertEquals(2, TradeMode.FIRST_BOARD.code());
        assertEquals(3, TradeMode.SMALL_CAP_FIRST_BOARD.code());
    }

    @Test
    void resolvesModeWithoutMapLookup() {
        assertEquals(TradeMode.RELAY_LIMIT_UP, TradeMode.fromCode(1));
        assertEquals(TradeMode.FIRST_BOARD, TradeMode.fromCode(2));
        assertEquals(TradeMode.SMALL_CAP_FIRST_BOARD, TradeMode.fromCode(3));
        assertThrows(IllegalArgumentException.class, () -> TradeMode.fromCode(0));
    }
}
