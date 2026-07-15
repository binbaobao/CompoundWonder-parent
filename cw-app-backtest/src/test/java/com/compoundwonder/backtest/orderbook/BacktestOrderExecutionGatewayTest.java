package com.compoundwonder.backtest.orderbook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BacktestOrderExecutionGatewayTest {

    @Test
    void recordsCoreTradingActionsInEmissionOrder() {
        BacktestOrderExecutionGateway gateway = new BacktestOrderExecutionGateway();

        gateway.buy("2026-07-15", 1600000, 1100, 93000000);
        gateway.cancel("600000");

        assertEquals(2, gateway.actions().size());
        assertEquals(BacktestOrderExecutionGateway.ActionType.BUY, gateway.actions().get(0).type());
        assertEquals("600000", gateway.actions().get(0).symbol());
        assertEquals("600000", gateway.actions().get(1).symbol());
    }
}
