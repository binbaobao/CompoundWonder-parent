package com.compoundwonder.backtest.service;

import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.RuleExecuteRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 连续交易日全仓单票历史回测服务。
 */
public interface HistoricalBacktestTradeService {

    /** 创建任务并在后台开始执行。 */
    BacktestRun startRange(LocalDate startDate, LocalDate endDate);

    /** 同步执行，供测试和内部任务使用。 */
    BacktestRun runRange(LocalDate startDate, LocalDate endDate);

    BacktestRun findRun(long runId);

    /** 查询最近创建的历史回测任务。 */
    List<BacktestRun> findRecentRuns(int limit);

    /** 查询任务的每日账户权益快照。 */
    List<BacktestDailyRecord> findDailyRecords(long runId);

    /** 查询任务的完整持仓生命周期。 */
    List<BacktestPosition> findPositions(long runId);

    /** 查询任务中实际生效的交易规则。 */
    List<RuleExecuteRecord> findRules(long runId);
}
