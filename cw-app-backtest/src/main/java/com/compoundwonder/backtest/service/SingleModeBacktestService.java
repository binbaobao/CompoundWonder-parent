package com.compoundwonder.backtest.service;

import com.compoundwonder.backtest.service.model.SingleModeBacktestSummary;
import com.compoundwonder.backtest.service.model.SingleModeBoardStat;
import com.compoundwonder.backtest.service.model.SingleModeSamplePage;
import com.compoundwonder.trader.entity.SingleModeBacktestRun;

import java.time.LocalDate;
import java.util.List;

/** Model 3 单模式全样本回测应用服务。 */
public interface SingleModeBacktestService {
    SingleModeBacktestRun startRange(LocalDate startDate, LocalDate endDate, int tradeMode);
    SingleModeBacktestRun runRange(LocalDate startDate, LocalDate endDate, int tradeMode);
    SingleModeBacktestRun findRun(long runId);
    List<SingleModeBacktestRun> findRecentRuns(int tradeMode, int limit);
    SingleModeBacktestSummary summarize(long runId);
    List<SingleModeBoardStat> boardStats(long runId);
    SingleModeSamplePage findSamples(long runId, int page, int pageSize);
}
