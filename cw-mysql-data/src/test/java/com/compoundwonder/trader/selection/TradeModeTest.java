package com.compoundwonder.trader.selection;

import com.compoundwonder.strategy.TradeMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeModeTest {

    @Test
    void keepsStableDatabaseCodesForThreeIndependentModes() {
        assertEquals(1, TradeMode.RELAY_LIMIT_UP.code());
        assertEquals(2, TradeMode.FIRST_BOARD.code());
        assertEquals(3, TradeMode.SMALL_CAP_FIRST_BOARD.code());
    }
}
