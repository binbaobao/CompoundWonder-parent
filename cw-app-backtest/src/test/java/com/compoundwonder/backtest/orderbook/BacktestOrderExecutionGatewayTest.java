package com.compoundwonder.backtest.orderbook;

import com.compoundwonder.common.orderbook.TradeOrderIntent;
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

    @Test
    void preservesStrategyIdentityFromOrderIntent() {
        BacktestOrderExecutionGateway gateway = new BacktestOrderExecutionGateway();

        gateway.execute(TradeOrderIntent.buy(
                "run-45:model-1:600000:2025-01-02", "MODEL_1",
                "2025-01-02", 1_600_000, "600000", 1_100, 93_100_000));

        BacktestOrderExecutionGateway.Action action = gateway.actions().get(0);
        assertEquals("run-45:model-1:600000:2025-01-02", action.strategySessionId());
        assertEquals("MODEL_1", action.strategyId());
        assertEquals(BacktestOrderExecutionGateway.ActionType.BUY, action.type());
    }
}
