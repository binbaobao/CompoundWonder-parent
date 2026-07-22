package com.compoundwonder.backtest.service;

import com.compoundwonder.trader.entity.RelaySelectionCandidateRecord;
import com.compoundwonder.trader.entity.RelaySelectionRun;
import com.compoundwonder.trader.entity.RelaySelectionTriggerRecord;

import java.time.LocalDate;
import java.util.List;

/** 连板触发全候选日 K 理论结果研究入口。 */
public interface RelaySelectionResearchService {
    RelaySelectionRun startRange(LocalDate startDate, LocalDate endDate);
    RelaySelectionRun runRange(LocalDate startDate, LocalDate endDate);
    RelaySelectionRun findRun(long runId);
    List<RelaySelectionRun> findRecentRuns(int limit);
    List<RelaySelectionTriggerRecord> findTriggers(long runId);
    List<RelaySelectionCandidateRecord> findCandidates(long runId, Long triggerRecordId,
                                                       int page, int pageSize);
}
