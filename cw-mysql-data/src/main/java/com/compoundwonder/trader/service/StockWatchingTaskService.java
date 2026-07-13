package com.compoundwonder.trader.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.entity.StockWatchingTask;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票盯盘任务服务。
 * 作用：提供 stock_watching_task 表的基础读写能力，后续承接选股任务生成和维护逻辑。
 */
public interface StockWatchingTaskService extends IService<StockWatchingTask> {

    /**
     * 创建收盘后选股盯盘任务。
     * 作用：按交易日期生成首板和连板接力两类次交易日盯盘任务。
     */
    List<StockWatchingTask> createPostCloseWatchingTasks(LocalDate tradeDate);

}
