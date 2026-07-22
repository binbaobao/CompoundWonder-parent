package com.compoundwonder.backtest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.trader.entity.RelaySelectionCandidateRecord;
import com.compoundwonder.trader.entity.RelaySelectionRun;
import com.compoundwonder.trader.entity.RelaySelectionTriggerRecord;
import com.compoundwonder.trader.mapper.RelaySelectionCandidateRecordMapper;
import com.compoundwonder.trader.mapper.RelaySelectionRunMapper;
import com.compoundwonder.trader.mapper.RelaySelectionTriggerRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 连板研究任务的交易库读写边界。 */
@Service
@DS("trade")
public class RelaySelectionResearchPersistenceService {
    static final int RUNNING = 1;
    static final int COMPLETED = 2;
    static final int FAILED = 3;

    private final RelaySelectionRunMapper runMapper;
    private final RelaySelectionTriggerRecordMapper triggerMapper;
    private final RelaySelectionCandidateRecordMapper candidateMapper;

    public RelaySelectionResearchPersistenceService(
            RelaySelectionRunMapper runMapper,
            RelaySelectionTriggerRecordMapper triggerMapper,
            RelaySelectionCandidateRecordMapper candidateMapper) {
        this.runMapper = runMapper;
        this.triggerMapper = triggerMapper;
        this.candidateMapper = candidateMapper;
    }

    public RelaySelectionRun createRun(LocalDate startDate, LocalDate endDate,
                                       String strategyVersion, String parameterSnapshot) {
        RelaySelectionRun run = new RelaySelectionRun();
        run.setRunType(1);
        run.setStrategyVersion(strategyVersion);
        run.setStartDate(startDate);
        run.setEndDate(endDate);
        run.setStatus(RUNNING);
        run.setParameterSnapshot(parameterSnapshot);
        run.setRunRemark("全触发板内原始候选，D+1实际触板最高价成本，观察至首次收盘断板");
        zeroCounts(run);
        run.setStartedTime(LocalDateTime.now());
        run.setCreatedTime(LocalDateTime.now());
        runMapper.insert(run);
        return run;
    }

    public void insertTrigger(RelaySelectionTriggerRecord trigger) {
        triggerMapper.insert(trigger);
    }

    public void insertCandidate(RelaySelectionCandidateRecord candidate) {
        candidateMapper.insert(candidate);
    }

    public void updateCandidate(RelaySelectionCandidateRecord candidate) {
        candidate.setUpdatedTime(LocalDateTime.now());
        candidateMapper.updateById(candidate);
    }

    public void updateTrigger(RelaySelectionTriggerRecord trigger) {
        trigger.setUpdatedTime(LocalDateTime.now());
        triggerMapper.updateById(trigger);
    }

    public void updateProgress(RelaySelectionRun run) {
        run.setUpdatedTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    public void complete(RelaySelectionRun run) {
        run.setStatus(COMPLETED);
        run.setFinishedTime(LocalDateTime.now());
        updateProgress(run);
    }

    public void fail(long runId, RuntimeException exception) {
        RelaySelectionRun update = new RelaySelectionRun();
        update.setId(runId);
        update.setStatus(FAILED);
        String message = exception.getMessage() == null
                ? exception.getClass().getSimpleName() : exception.getMessage();
        update.setErrorMessage(message.length() > 2000 ? message.substring(0, 2000) : message);
        update.setFinishedTime(LocalDateTime.now());
        update.setUpdatedTime(LocalDateTime.now());
        runMapper.updateById(update);
    }

    public RelaySelectionRun findRun(long runId) {
        return runMapper.selectById(runId);
    }

    public List<RelaySelectionRun> findRecentRuns(int limit) {
        return runMapper.selectList(Wrappers.<RelaySelectionRun>lambdaQuery()
                .orderByDesc(RelaySelectionRun::getId)
                .last("LIMIT " + Math.max(1, Math.min(limit, 100))));
    }

    public List<RelaySelectionTriggerRecord> findTriggers(long runId) {
        return triggerMapper.selectList(Wrappers.<RelaySelectionTriggerRecord>lambdaQuery()
                .eq(RelaySelectionTriggerRecord::getRunId, runId)
                .orderByAsc(RelaySelectionTriggerRecord::getRecommendDate));
    }

    public List<RelaySelectionCandidateRecord> findCandidates(long runId, Long triggerRecordId,
                                                               int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(pageSize, 1000));
        long offset = (long) (safePage - 1) * safePageSize;
        var query = Wrappers.<RelaySelectionCandidateRecord>lambdaQuery()
                .eq(RelaySelectionCandidateRecord::getRunId, runId);
        if (triggerRecordId != null) {
            query.eq(RelaySelectionCandidateRecord::getTriggerRecordId, triggerRecordId);
        }
        return candidateMapper.selectList(query
                .orderByAsc(RelaySelectionCandidateRecord::getRecommendDate)
                .orderByAsc(RelaySelectionCandidateRecord::getFinalRank)
                .orderByAsc(RelaySelectionCandidateRecord::getId)
                .last("LIMIT " + safePageSize + " OFFSET " + offset));
    }

    private void zeroCounts(RelaySelectionRun run) {
        run.setTriggerCount(0);
        run.setEmptyPositionCount(0);
        run.setRawCandidateCount(0);
        run.setEligibleCandidateCount(0);
        run.setSelectedCandidateCount(0);
        run.setTouchedLimitUpCount(0);
        run.setSealedLimitUpCount(0);
        run.setBrokenLimitUpCount(0);
        run.setTheoreticalWinCount(0);
        run.setDailyOpportunityCount(0);
        run.setDailyBestCapturedCount(0);
    }
}
