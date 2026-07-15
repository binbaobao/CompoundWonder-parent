package com.compoundwonder.backtest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.RuleExecuteRecord;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.BacktestDailyRecordMapper;
import com.compoundwonder.trader.mapper.BacktestPositionMapper;
import com.compoundwonder.trader.mapper.BacktestRunMapper;
import com.compoundwonder.trader.mapper.RuleExecuteRecordMapper;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 回测交易库读写服务，确保单个交易日的结果原子落库。
 */
@Service
@DS("trade")
public class BacktestPersistenceService {

    static final int RUNNING = 1;
    static final int COMPLETED = 2;
    static final int FAILED = 3;
    static final int EXECUTION_SOURCE_BACKTEST = 2;

    private final BacktestRunMapper runMapper;
    private final BacktestPositionMapper positionMapper;
    private final BacktestDailyRecordMapper dailyRecordMapper;
    private final RuleExecuteRecordMapper ruleRecordMapper;
    private final StockWatchingTaskMapper watchingTaskMapper;

    public BacktestPersistenceService(BacktestRunMapper runMapper,
                                      BacktestPositionMapper positionMapper,
                                      BacktestDailyRecordMapper dailyRecordMapper,
                                      RuleExecuteRecordMapper ruleRecordMapper,
                                      StockWatchingTaskMapper watchingTaskMapper) {
        this.runMapper = runMapper;
        this.positionMapper = positionMapper;
        this.dailyRecordMapper = dailyRecordMapper;
        this.ruleRecordMapper = ruleRecordMapper;
        this.watchingTaskMapper = watchingTaskMapper;
    }

    public BacktestRun createRun(LocalDate startDate, LocalDate endDate, BigDecimal initialCapital) {
        BacktestRun run = new BacktestRun();
        run.setStartDate(startDate);
        run.setEndDate(endDate);
        run.setInitialCapital(initialCapital);
        run.setShanghaiDelayMs(500);
        run.setShenzhenDelayMs(100);
        run.setOvernightFillTime(BacktestExecutionPolicy.OVERNIGHT_FILL_TIME);
        run.setStatus(RUNNING);
        run.setStartedTime(LocalDateTime.now());
        run.setCreatedTime(LocalDateTime.now());
        runMapper.insert(run);
        return run;
    }

    public BacktestRun findRun(long runId) {
        return runMapper.selectById(runId);
    }

    public List<StockWatchingTask> findWatchingTasks(LocalDate tradeDate) {
        return watchingTaskMapper.selectList(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getTradeDate, tradeDate)
                .orderByAsc(StockWatchingTask::getTradeMode)
                .orderByDesc(StockWatchingTask::getLimitUpScore)
                .orderByAsc(StockWatchingTask::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDay(BacktestDayWrite write) {
        if (write.previousPosition() != null) {
            positionMapper.updateById(write.previousPosition());
        }

        BacktestPosition closingPosition = write.previousPosition();
        if (write.sellRule() != null && closingPosition != null) {
            ruleRecordMapper.insert(toRuleEntity(
                    write.runId(), closingPosition, write.sellRule(), closingPosition.getWatchingTaskId()));
        }

        BacktestPosition currentPosition = closingPosition != null
                && Integer.valueOf(1).equals(closingPosition.getStatus()) ? closingPosition : null;
        if (write.newPosition() != null) {
            positionMapper.insert(write.newPosition());
            currentPosition = write.newPosition();
            ruleRecordMapper.insert(toRuleEntity(
                    write.runId(), currentPosition, write.buyRule(), write.buyTask().getId()));
        }

        BacktestDailyRecord dailyRecord = write.dailyRecord();
        dailyRecord.setBacktestRunId(write.runId());
        dailyRecord.setTradeDate(write.tradeDate());
        dailyRecord.setPositionId(currentPosition == null ? null : currentPosition.getId());
        dailyRecordMapper.insert(dailyRecord);

        BacktestRun progress = new BacktestRun();
        progress.setId(write.runId());
        progress.setLastCompletedDate(write.tradeDate());
        runMapper.updateById(progress);
    }

    public void completeRun(long runId, BigDecimal finalAsset, BigDecimal totalReturnRate) {
        BacktestRun run = new BacktestRun();
        run.setId(runId);
        run.setStatus(COMPLETED);
        run.setFinalAsset(finalAsset);
        run.setTotalReturnRate(totalReturnRate);
        run.setFinishedTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    public void failRun(long runId, RuntimeException exception) {
        BacktestRun run = new BacktestRun();
        run.setId(runId);
        run.setStatus(FAILED);
        run.setErrorMessage(abbreviate(exception.getMessage(), 1000));
        run.setFinishedTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    private RuleExecuteRecord toRuleEntity(long runId, BacktestPosition position,
                                           RuleRecordDTO dto, Long watchingTaskId) {
        RuleExecuteRecord entity = new RuleExecuteRecord();
        entity.setExecutionSource(EXECUTION_SOURCE_BACKTEST);
        entity.setBacktestRunId(runId);
        entity.setPositionId(position.getId());
        entity.setWatchingTaskId(watchingTaskId);
        entity.setActionType(dto.getActionType());
        entity.setRuleCode(dto.getRuleCode());
        entity.setSymbol(dto.getSymbol());
        entity.setSymbolName(position.getSymbolName());
        boolean buy = Integer.valueOf(RuleConstant.TRADING_MODE_BUY).equals(dto.getActionType());
        entity.setTradeDate(buy
                ? position.getBuyDate() : position.getSellDate());
        entity.setTime(dto.getTime());
        entity.setLastOrderTime(dto.getLastOrderTime());
        entity.setQuantity(position.getQuantity());
        entity.setTradeAmount(buy ? position.getBuyAmount() : position.getSellAmount());
        entity.setFeeAmount(buy ? position.getBuyFee() : position.getSellFee());
        entity.setTradeMode(position.getTradeMode());
        entity.setLimitUpScore(position.getLimitUpScore());
        entity.setPrice(dto.getPrice());
        entity.setIncrease(dto.getIncrease());
        entity.setRemark(dto.getRemark());
        entity.setCreatedTime(LocalDateTime.now());
        return entity;
    }

    private String abbreviate(String message, int maxLength) {
        if (message == null || message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength);
    }
}
