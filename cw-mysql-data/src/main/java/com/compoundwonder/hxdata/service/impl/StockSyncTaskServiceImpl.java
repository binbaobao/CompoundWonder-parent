package com.compoundwonder.hxdata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockPreviousNameHistory;
import com.compoundwonder.hxdata.entity.StockSyncTask;
import com.compoundwonder.hxdata.mapper.StockSyncTaskMapper;
import com.compoundwonder.hxdata.service.StockPreviousNameHistoryService;
import com.compoundwonder.hxdata.service.StockSyncTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 股票历史数据同步任务服务实现。
 * 作用：以曾用名历史表中的股票代码为基准，生成和维护历史数据同步进度。
 */
@Service
@DS("market")
public class StockSyncTaskServiceImpl extends ServiceImpl<StockSyncTaskMapper, StockSyncTask> implements StockSyncTaskService {

    private final StockPreviousNameHistoryService stockPreviousNameHistoryService;

    /**
     * 创建股票历史数据同步任务服务。
     * 作用：注入曾用名历史服务，用它提供的股票代码作为任务来源。
     */
    public StockSyncTaskServiceImpl(StockPreviousNameHistoryService stockPreviousNameHistoryService) {
        this.stockPreviousNameHistoryService = stockPreviousNameHistoryService;
    }

    /**
     * 根据曾用名历史表初始化股票同步任务。
     * 实现逻辑：先查曾用名表去重股票代码，再排除任务表已存在代码，最后批量新增缺失任务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initTasksFromPreviousNameHistory() {
        List<String> stockCodes = stockPreviousNameHistoryService.list(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                        .select(StockPreviousNameHistory::getStockCode)
                        .isNotNull(StockPreviousNameHistory::getStockCode))
                .stream()
                .map(StockPreviousNameHistory::getStockCode)
                .distinct()
                .toList();
        if (stockCodes.isEmpty()) {
            return 0;
        }

        Set<String> existsStockCodes = list(Wrappers.<StockSyncTask>lambdaQuery()
                .select(StockSyncTask::getStockCode)
                .in(StockSyncTask::getStockCode, stockCodes))
                .stream()
                .map(StockSyncTask::getStockCode)
                .collect(Collectors.toSet());

        List<StockSyncTask> newTasks = stockCodes.stream()
                .filter(stockCode -> !existsStockCodes.contains(stockCode))
                .map(this::createInitTask)
                .toList();
        if (newTasks.isEmpty()) {
            return 0;
        }

        saveBatch(newTasks);
        return newTasks.size();
    }

    /**
     * 查询还没有完成自由流通股同步的任务。
     * 实现逻辑：按股票代码升序取指定数量未完成任务，供后续批处理逐只股票消费。
     */
    @Override
    public List<StockSyncTask> listPendingFreeFloatTasks(int limit) {
        return list(Wrappers.<StockSyncTask>lambdaQuery()
                .eq(StockSyncTask::getFreeFloatSynced, false)
                .orderByAsc(StockSyncTask::getStockCode)
                .last("LIMIT " + limit));
    }

    /**
     * 查询还没有完成日 K 同步的任务。
     * 实现逻辑：按股票代码升序取指定数量未完成任务，供后续批处理逐只股票消费。
     */
    @Override
    public List<StockSyncTask> listPendingDailyKlineTasks(int limit) {
        return list(Wrappers.<StockSyncTask>lambdaQuery()
                .eq(StockSyncTask::getDailyKlineSynced, false)
                .orderByAsc(StockSyncTask::getStockCode)
                .last("LIMIT " + limit));
    }

    /**
     * 标记指定股票已完成自由流通股同步。
     * 实现逻辑：按股票代码更新 free_float_synced 为 true。
     */
    @Override
    public boolean markFreeFloatSynced(String stockCode) {
        return update(Wrappers.<StockSyncTask>lambdaUpdate()
                .eq(StockSyncTask::getStockCode, stockCode)
                .set(StockSyncTask::getFreeFloatSynced, true));
    }

    /**
     * 标记指定股票已完成日 K 同步。
     * 实现逻辑：按股票代码更新 daily_kline_synced 为 true。
     */
    @Override
    public boolean markDailyKlineSynced(String stockCode) {
        return update(Wrappers.<StockSyncTask>lambdaUpdate()
                .eq(StockSyncTask::getStockCode, stockCode)
                .set(StockSyncTask::getDailyKlineSynced, true));
    }

    /**
     * 确保指定股票存在同步任务。
     * 实现逻辑：已存在时不处理；不存在时按传入状态新增任务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ensureTask(String stockCode, boolean freeFloatSynced, boolean dailyKlineSynced) {
        long exists = count(Wrappers.<StockSyncTask>lambdaQuery()
                .eq(StockSyncTask::getStockCode, stockCode));
        if (exists > 0) {
            return false;
        }

        StockSyncTask task = new StockSyncTask();
        task.setStockCode(stockCode);
        task.setFreeFloatSynced(freeFloatSynced);
        task.setDailyKlineSynced(dailyKlineSynced);
        return save(task);
    }

    /**
     * 创建初始化任务对象。
     * 默认状态：自由流通股未同步，日 K 未同步。
     */
    private StockSyncTask createInitTask(String stockCode) {
        StockSyncTask task = new StockSyncTask();
        task.setStockCode(stockCode);
        task.setFreeFloatSynced(false);
        task.setDailyKlineSynced(false);
        return task;
    }
}
