package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.strategy.relay.selection.RelaySelectionService;
import com.compoundwonder.trader.entity.StockWatchingTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BacktestBuyTaskPriorityTest {

    @Test
    void separatesBackupRelayTasksWithoutChangingPrimaryOrder() {
        StockWatchingTask primaryRelay = task("600001", "relay-v1");
        StockWatchingTask firstBoard = task("600002", null);
        StockWatchingTask backupRelay = task(
                "600003",
                RelaySelectionService.THREE_BOARD_ACCELERATED_BACKUP_VERSION);

        BacktestBuyTaskPriority.TaskGroups groups =
                BacktestBuyTaskPriority.group(List.of(
                        primaryRelay, firstBoard, backupRelay));

        assertEquals(List.of("600001", "600002"),
                groups.primaryTasks().stream()
                        .map(StockWatchingTask::getStockCode)
                        .toList());
        assertEquals(List.of("600003"),
                groups.backupTasks().stream()
                        .map(StockWatchingTask::getStockCode)
                        .toList());
    }

    private StockWatchingTask task(String stockCode, String strategyVersion) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(stockCode);
        task.setStrategyVersion(strategyVersion);
        return task;
    }
}
