package com.compoundwonder.backtest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.service.model.SingleModeSamplePage;
import com.compoundwonder.trader.entity.SingleModeBacktestRun;
import com.compoundwonder.trader.entity.SingleModeBacktestSample;
import com.compoundwonder.trader.mapper.SingleModeBacktestRunMapper;
import com.compoundwonder.trader.mapper.SingleModeBacktestSampleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 单模式回测交易库持久化边界。 */
@Service
@DS("trade")
public class SingleModeBacktestPersistenceService {
    static final int RUNNING = 1;
    static final int COMPLETED = 2;
    static final int FAILED = 3;

    private final SingleModeBacktestSchemaService schemaService;
    private final SingleModeBacktestRunMapper runMapper;
    private final SingleModeBacktestSampleMapper sampleMapper;

    public SingleModeBacktestPersistenceService(SingleModeBacktestSchemaService schemaService,
                                                SingleModeBacktestRunMapper runMapper,
                                                SingleModeBacktestSampleMapper sampleMapper) {
        this.schemaService = schemaService;
        this.runMapper = runMapper;
        this.sampleMapper = sampleMapper;
    }

    public SingleModeBacktestRun createRun(LocalDate startDate, LocalDate endDate, int tradeMode) {
        schemaService.ensureSchema();
        SingleModeBacktestRun run = new SingleModeBacktestRun();
        run.setStartDate(startDate);
        run.setEndDate(endDate);
        run.setTradeMode(tradeMode);
        run.setStatus(RUNNING);
        run.setTotalSamples(0);
        run.setProcessedSamples(0);
        run.setBoughtSamples(0);
        run.setClosedSamples(0);
        run.setStartedTime(LocalDateTime.now());
        run.setCreatedTime(LocalDateTime.now());
        runMapper.insert(run);
        return run;
    }

    public void insertSample(SingleModeBacktestSample sample) { sampleMapper.insert(sample); }
    public void updateSample(SingleModeBacktestSample sample) {
        sample.setUpdatedTime(LocalDateTime.now());
        sampleMapper.updateById(sample);
    }

    public void updateProgress(long runId, LocalDate completedDate, int total, int processed,
                               int bought, int closed) {
        SingleModeBacktestRun update = new SingleModeBacktestRun();
        update.setId(runId);
        update.setLastCompletedDate(completedDate);
        update.setTotalSamples(total);
        update.setProcessedSamples(processed);
        update.setBoughtSamples(bought);
        update.setClosedSamples(closed);
        update.setUpdatedTime(LocalDateTime.now());
        runMapper.updateById(update);
    }

    public void complete(long runId) {
        SingleModeBacktestRun update = new SingleModeBacktestRun();
        update.setId(runId);
        update.setStatus(COMPLETED);
        update.setFinishedTime(LocalDateTime.now());
        update.setUpdatedTime(LocalDateTime.now());
        runMapper.updateById(update);
    }

    public void fail(long runId, RuntimeException exception) {
        SingleModeBacktestRun update = new SingleModeBacktestRun();
        update.setId(runId);
        update.setStatus(FAILED);
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        update.setErrorMessage(message.length() > 1000 ? message.substring(0, 1000) : message);
        update.setFinishedTime(LocalDateTime.now());
        update.setUpdatedTime(LocalDateTime.now());
        runMapper.updateById(update);
    }

    public SingleModeBacktestRun findRun(long runId) {
        schemaService.ensureSchema();
        return runMapper.selectById(runId);
    }

    public List<SingleModeBacktestRun> findRecentRuns(int tradeMode, int limit) {
        schemaService.ensureSchema();
        return runMapper.selectList(Wrappers.<SingleModeBacktestRun>lambdaQuery()
                .eq(SingleModeBacktestRun::getTradeMode, tradeMode)
                .orderByDesc(SingleModeBacktestRun::getId)
                .last("LIMIT " + limit));
    }

    public List<SingleModeBacktestSample> findAllSamples(long runId) {
        schemaService.ensureSchema();
        return sampleMapper.selectList(Wrappers.<SingleModeBacktestSample>lambdaQuery()
                .eq(SingleModeBacktestSample::getRunId, runId)
                .orderByAsc(SingleModeBacktestSample::getRecommendDate)
                .orderByAsc(SingleModeBacktestSample::getId));
    }

    public SingleModeSamplePage findSamples(long runId, int page, int pageSize) {
        schemaService.ensureSchema();
        long total = sampleMapper.selectCount(Wrappers.<SingleModeBacktestSample>lambdaQuery()
                .eq(SingleModeBacktestSample::getRunId, runId));
        int offset = (page - 1) * pageSize;
        List<SingleModeBacktestSample> rows = sampleMapper.selectList(
                Wrappers.<SingleModeBacktestSample>lambdaQuery()
                        .eq(SingleModeBacktestSample::getRunId, runId)
                        .orderByAsc(SingleModeBacktestSample::getRecommendDate)
                        .orderByAsc(SingleModeBacktestSample::getId)
                        .last("LIMIT " + pageSize + " OFFSET " + offset));
        return new SingleModeSamplePage(total, page, pageSize, rows);
    }
}
