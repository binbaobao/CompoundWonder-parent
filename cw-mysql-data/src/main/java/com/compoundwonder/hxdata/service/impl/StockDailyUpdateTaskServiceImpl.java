package com.compoundwonder.hxdata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockDailyUpdateTask;
import com.compoundwonder.hxdata.mapper.StockDailyUpdateTaskMapper;
import com.compoundwonder.hxdata.service.StockDailyUpdateTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 股票每日更新任务服务实现。
 * 作用：按 task_date 唯一维护每日任务执行状态。
 */
@Service
@DS("market")
public class StockDailyUpdateTaskServiceImpl extends ServiceImpl<StockDailyUpdateTaskMapper, StockDailyUpdateTask> implements StockDailyUpdateTaskService {

    /**
     * 获取或创建指定日期的任务记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockDailyUpdateTask ensureTask(LocalDate taskDate, boolean tradeDay) {
        StockDailyUpdateTask task = getByTaskDate(taskDate);
        if (task != null) {
            task.setTradeDay(tradeDay);
            task.setUpdatedTime(LocalDateTime.now());
            updateById(task);
            return task;
        }

        LocalDateTime now = LocalDateTime.now();
        task = new StockDailyUpdateTask();
        task.setTaskDate(taskDate);
        task.setTradeDay(tradeDay);
        task.setPreviousNameSynced(false);
        task.setNewListingSynced(false);
        task.setFreeFloatSynced(false);
        task.setConvertibleBondSynced(false);
        task.setRegionSynced(false);
        task.setMarginTradingSynced(false);
        task.setDailyKlineSynced(false);
        task.setPreOpenFinished(false);
        task.setPostCloseFinished(false);
        task.setCreatedTime(now);
        task.setUpdatedTime(now);
        save(task);
        return task;
    }

    /**
     * 标记交易日状态。
     */
    @Override
    public void markTradeDay(LocalDate taskDate, boolean tradeDay) {
        updateFlag(taskDate, task -> task.setTradeDay(tradeDay));
    }

    /**
     * 标记曾用名同步完成。
     */
    @Override
    public void markPreviousNameSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setPreviousNameSynced(true));
    }

    /**
     * 标记新上市股票发现完成。
     */
    @Override
    public void markNewListingSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setNewListingSynced(true));
    }

    /**
     * 标记自由流通股本同步完成。
     */
    @Override
    public void markFreeFloatSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setFreeFloatSynced(true));
    }

    /**
     * 标记可转债当前状态同步完成。
     */
    @Override
    public void markConvertibleBondSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setConvertibleBondSynced(true));
    }

    /**
     * 标记地域信息同步完成。
     */
    @Override
    public void markRegionSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setRegionSynced(true));
    }

    /**
     * 标记融资融券标识维护完成。
     */
    @Override
    public void markMarginTradingSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setMarginTradingSynced(true));
    }

    /**
     * 标记日 K 同步完成。
     */
    @Override
    public void markDailyKlineSynced(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setDailyKlineSynced(true));
    }

    /**
     * 标记盘前任务完成。
     */
    @Override
    public void markPreOpenFinished(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setPreOpenFinished(true));
    }

    /**
     * 标记盘后任务完成。
     */
    @Override
    public void markPostCloseFinished(LocalDate taskDate) {
        updateFlag(taskDate, task -> task.setPostCloseFinished(true));
    }

    /**
     * 按日期更新任务字段。
     */
    private void updateFlag(LocalDate taskDate, Consumer<StockDailyUpdateTask> updater) {
        StockDailyUpdateTask task = getByTaskDate(taskDate);
        if (task == null) {
            task = ensureTask(taskDate, false);
        }
        updater.accept(task);
        task.setUpdatedTime(LocalDateTime.now());
        updateById(task);
    }

    /**
     * 按任务日期查询记录。
     */
    private StockDailyUpdateTask getByTaskDate(LocalDate taskDate) {
        return getOne(Wrappers.<StockDailyUpdateTask>lambdaQuery()
                .eq(StockDailyUpdateTask::getTaskDate, taskDate)
                .last("LIMIT 1"));
    }
}
