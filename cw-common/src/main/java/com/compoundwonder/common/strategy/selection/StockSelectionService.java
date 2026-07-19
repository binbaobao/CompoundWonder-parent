package com.compoundwonder.common.strategy.selection;

import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;

import java.time.LocalDate;
import java.util.List;

/** 三种独立交易模式的收盘后选股入口。 */
public interface StockSelectionService {

    List<SelectionTaskData> selectAll(LocalDate tradeDate);
}
