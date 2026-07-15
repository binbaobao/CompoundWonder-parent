package com.compoundwonder.backtest.service;

import com.compoundwonder.trader.entity.BacktestRun;

import java.time.LocalDate;

/**
 * 连续交易日全仓单票历史回测服务。
 */
public interface HistoricalBacktestTradeService {

    /** 创建任务并在后台开始执行。 */
    BacktestRun startRange(LocalDate startDate, LocalDate endDate);

    /** 同步执行，供测试和内部任务使用。 */
    BacktestRun runRange(LocalDate startDate, LocalDate endDate);

    BacktestRun findRun(long runId);
}
