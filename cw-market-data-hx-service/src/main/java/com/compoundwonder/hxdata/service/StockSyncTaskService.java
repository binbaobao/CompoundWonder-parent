package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockSyncTask;

import java.util.List;

/**
 * 股票历史数据同步任务服务。
 * 作用：根据曾用名历史表初始化任务，并维护流通股、日 K 的同步进度。
 */
public interface StockSyncTaskService extends IService<StockSyncTask> {

    /**
     * 根据曾用名历史表初始化股票同步任务。
     * 处理逻辑：取 stock_previous_name_history 中所有去重股票代码，缺失的任务才新增。
     */
    int initTasksFromPreviousNameHistory();

    /**
     * 查询还没有完成自由流通股同步的任务。
     */
    List<StockSyncTask> listPendingFreeFloatTasks(int limit);

    /**
     * 查询还没有完成日 K 同步的任务。
     */
    List<StockSyncTask> listPendingDailyKlineTasks(int limit);

    /**
     * 标记指定股票已完成自由流通股同步。
     */
    boolean markFreeFloatSynced(String stockCode);

    /**
     * 标记指定股票已完成日 K 同步。
     */
    boolean markDailyKlineSynced(String stockCode);
}
