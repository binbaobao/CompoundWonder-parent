package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.StockWatchingTask;

import java.util.Objects;

/**
 * 带交易上下文的回测规则记录。
 *
 * <p>买入候选规则通过推荐任务关联，持仓卖出规则通过持仓关联；两种上下文只会存在一种。</p>
 */
record BacktestRuleAction(StockWatchingTask task,
                          BacktestPosition position,
                          RuleRecordDTO rule) {

    BacktestRuleAction {
        if ((task == null) == (position == null)) {
            throw new IllegalArgumentException("回测规则必须且只能关联推荐任务或持仓");
        }
        Objects.requireNonNull(rule, "回测规则不能为空");
    }

    BacktestRuleAction(StockWatchingTask task, RuleRecordDTO rule) {
        this(task, null, rule);
    }

    BacktestRuleAction(BacktestPosition position, RuleRecordDTO rule) {
        this(null, position, rule);
    }
}
