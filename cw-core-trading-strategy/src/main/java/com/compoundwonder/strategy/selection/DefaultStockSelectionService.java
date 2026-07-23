package com.compoundwonder.strategy.selection;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.strategy.selection.StockSelectionService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
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

    /**
     * 使用同一个只读数据端口创建三套彼此独立的选股服务。
     *
     * @param selectionDataService 由应用组装根提供的选股数据端口
     */
    public DefaultStockSelectionService(StockSelectionDataService selectionDataService) {
        this.relaySelectionService = new RelaySelectionService(selectionDataService);
        this.firstBoardSelectionService = new FirstBoardSelectionService(selectionDataService);
        this.smallCapFirstBoardSelectionService =
                new SmallCapFirstBoardSelectionService(selectionDataService);
    }

    /** 按指定交易模式调用其独立选股服务，不在模式之间混合或回退候选。 */
    @Override
    public List<SelectionTaskData> select(LocalDate tradeDate, TradeMode tradeMode) {
        if (tradeDate == null) {
            throw new IllegalArgumentException("选股日期不能为空");
        }
        if (tradeMode == null) {
            throw new IllegalArgumentException("交易模式不能为空");
        }
        return switch (tradeMode) {
            case RELAY_LIMIT_UP -> List.copyOf(relaySelectionService.select(tradeDate));
            case FIRST_BOARD -> List.copyOf(firstBoardSelectionService.select(tradeDate));
            case SMALL_CAP_FIRST_BOARD ->
                    List.copyOf(smallCapFirstBoardSelectionService.select(tradeDate));
        };
    }

    /**
     * 按固定模式顺序执行收盘后选股并聚合任务，不在模式之间回退候选。
     *
     * @param tradeDate 选股所依据的收盘交易日
     * @return 各已启用模式生成的下一交易日盯盘任务只读列表
     */
    @Override
    public List<SelectionTaskData> selectAll(LocalDate tradeDate) {
        List<SelectionTaskData> tasks = new ArrayList<>();
        // 调用连板接力模式选股方法。
        tasks.addAll(select(tradeDate, TradeMode.RELAY_LIMIT_UP));
        // 调用普通首板模式选股方法。
        tasks.addAll(select(tradeDate, TradeMode.FIRST_BOARD));
        // 调用小市值首板模式选股方法。
        tasks.addAll(select(tradeDate, TradeMode.SMALL_CAP_FIRST_BOARD));
        return List.copyOf(tasks);
    }
}
