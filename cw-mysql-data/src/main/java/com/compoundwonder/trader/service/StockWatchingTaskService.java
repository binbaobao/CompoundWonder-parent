package com.compoundwonder.trader.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
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
     * 作用：按交易日期生成连板、普通首板和小市值首板三类次交易日盯盘任务。
     */
    List<StockWatchingTask> createPostCloseWatchingTasks(LocalDate tradeDate);

    /**
     * 使用一轮连板研究在指定推荐日产生的最终任务替换原有 mode 1 任务。
     * 空列表也会执行删除，确保重跑后不会保留旧规则选出的股票。
     */
    List<StockWatchingTask> replaceRelaySelectionTasks(
            LocalDate recommendDate, List<SelectionTaskData> selectedTasks);

}
