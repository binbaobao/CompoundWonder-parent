package com.compoundwonder.strategy.selection;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.strategy.selection.StockSelectionService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.strategy.firstboard.selection.FirstBoardSelectionService;
import com.compoundwonder.strategy.relay.selection.RelaySelectionService;
import com.compoundwonder.strategy.smallcapfirstboard.selection.SmallCapFirstBoardSelectionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 三种交易模式的收盘后选股入口。
 *
 * <p>每种模式仍在自己的包内完成辅助指标、过滤、评分和 TopN；本类只按固定顺序
 * 聚合结果，不查询数据库、不落库，也不添加跨模式兜底。</p>
 */
public class DefaultStockSelectionService implements StockSelectionService {

    private final RelaySelectionService relaySelectionService;
    private final FirstBoardSelectionService firstBoardSelectionService;
    private final SmallCapFirstBoardSelectionService smallCapFirstBoardSelectionService;

    public DefaultStockSelectionService(StockSelectionDataService selectionDataService) {
        this.relaySelectionService = new RelaySelectionService(selectionDataService);
        this.firstBoardSelectionService = new FirstBoardSelectionService(selectionDataService);
        this.smallCapFirstBoardSelectionService =
                new SmallCapFirstBoardSelectionService(selectionDataService);
    }

    @Override
    public List<SelectionTaskData> selectAll(LocalDate tradeDate) {
        List<SelectionTaskData> tasks = new ArrayList<>();
        // 调用连板接力模式选股方法。
        tasks.addAll(relaySelectionService.select(tradeDate));
        // 调用普通首板模式选股方法。
        tasks.addAll(firstBoardSelectionService.select(tradeDate));
        // 调用小市值首板模式选股方法。
        tasks.addAll(smallCapFirstBoardSelectionService.select(tradeDate));
        return List.copyOf(tasks);
    }
}
