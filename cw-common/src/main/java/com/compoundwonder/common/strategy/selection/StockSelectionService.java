package com.compoundwonder.common.strategy.selection;

import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;

import java.time.LocalDate;
import java.util.List;

/** 三种独立交易模式的收盘后选股入口。 */
public interface StockSelectionService {

    /**
     * 只读执行指定模式的选股，不删除或写入盯盘任务。
     *
     * @param tradeDate 收盘后执行选股的交易日
     * @param tradeMode 本次需要独立回测的交易模式
     * @return 该模式生成的下一交易日候选
     */
    List<SelectionTaskData> select(LocalDate tradeDate, TradeMode tradeMode);

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
