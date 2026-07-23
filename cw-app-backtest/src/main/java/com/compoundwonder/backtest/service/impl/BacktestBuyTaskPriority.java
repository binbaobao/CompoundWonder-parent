package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.strategy.relay.selection.RelaySelectionService;
import com.compoundwonder.trader.entity.StockWatchingTask;

import java.util.ArrayList;
import java.util.List;

/** 将普通三模式候选与三板加速缩量备用候选拆成固定先后两组。 */
final class BacktestBuyTaskPriority {

    private BacktestBuyTaskPriority() {
    }

    static TaskGroups group(List<StockWatchingTask> tasks) {
        List<StockWatchingTask> primaryTasks = new ArrayList<>();
        List<StockWatchingTask> backupTasks = new ArrayList<>();
        if (tasks != null) {
            for (StockWatchingTask task : tasks) {
                if (RelaySelectionService.THREE_BOARD_ACCELERATED_BACKUP_VERSION
                        .equals(task.getStrategyVersion())) {
                    backupTasks.add(task);
                } else {
                    primaryTasks.add(task);
                }
            }
        }
        return new TaskGroups(primaryTasks, backupTasks);
    }

    record TaskGroups(List<StockWatchingTask> primaryTasks,
                      List<StockWatchingTask> backupTasks) {
        TaskGroups {
            primaryTasks = List.copyOf(primaryTasks);
            backupTasks = List.copyOf(backupTasks);
        }
    }
}
