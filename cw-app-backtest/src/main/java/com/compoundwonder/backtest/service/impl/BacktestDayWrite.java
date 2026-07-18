package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.StockWatchingTask;

import java.time.LocalDate;
import java.util.List;

/**
 * 一个交易日需要原子落库的全部结果。
 */
record BacktestDayWrite(long runId,
                        LocalDate tradeDate,
                        BacktestPosition previousPosition,
                        BacktestPosition newPosition,
                        RuleRecordDTO sellRule,
                        RuleRecordDTO buyRule,
                        StockWatchingTask buyTask,
                        List<BacktestRuleAction> actionRules,
                        List<BacktestRuleAction> triggeredRules,
                        BacktestDailyRecord dailyRecord) {

    BacktestDayWrite {
        actionRules = List.copyOf(actionRules);
        triggeredRules = List.copyOf(triggeredRules);
    }
}
