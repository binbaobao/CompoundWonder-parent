package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.trader.entity.StockWatchingTask;

/** 没有形成持仓、但在回测交易链路中实际执行过的委托或撤单动作。 */
record BacktestRuleAction(StockWatchingTask task, RuleRecordDTO rule) {
}
