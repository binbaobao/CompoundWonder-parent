package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockDailyUpdateTask;

import java.time.LocalDate;

/**
 * 股票每日更新任务服务。
 * 作用：创建每日任务记录，并在每个异步步骤完成后更新对应标识位。
 */
public interface StockDailyUpdateTaskService extends IService<StockDailyUpdateTask> {

    /**
     * 获取或创建指定日期的任务记录。
     */
    StockDailyUpdateTask ensureTask(LocalDate taskDate, boolean tradeDay);

    /**
     * 标记交易日状态。
     */
    void markTradeDay(LocalDate taskDate, boolean tradeDay);

    /**
     * 标记曾用名同步完成。
     */
    void markPreviousNameSynced(LocalDate taskDate);

    /**
     * 标记新上市股票发现完成。
     */
    void markNewListingSynced(LocalDate taskDate);

    /**
     * 标记自由流通股本同步完成。
     */
    void markFreeFloatSynced(LocalDate taskDate);

    /**
     * 标记可转债当前状态同步完成。
     */
    void markConvertibleBondSynced(LocalDate taskDate);

    /**
     * 标记地域信息同步完成。
     */
    void markRegionSynced(LocalDate taskDate);

    /**
     * 标记融资融券标识维护完成。
     */
    void markMarginTradingSynced(LocalDate taskDate);

    /**
     * 标记日 K 同步完成。
     */
    void markDailyKlineSynced(LocalDate taskDate);

    /**
     * 标记盘前任务完成。
     */
    void markPreOpenFinished(LocalDate taskDate);

    /**
     * 标记盘后任务完成。
     */
    void markPostCloseFinished(LocalDate taskDate);
}
