package com.compoundwonder.common.strategy.selection;

import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;

import java.time.LocalDate;
import java.util.List;

/** 三种独立交易模式的收盘后选股入口。 */
public interface StockSelectionService {

    /**
     * 按固定模式顺序执行连板、普通首板和小市值首板选股。
     *
     * <p>该方法只返回中立任务结果，不负责删除或写入数据库记录。</p>
     *
     * @param tradeDate 收盘后执行选股的交易日
     * @return 三种模式合并后的选股任务；没有候选时返回空列表
     */
    List<SelectionTaskData> selectAll(LocalDate tradeDate);
}
