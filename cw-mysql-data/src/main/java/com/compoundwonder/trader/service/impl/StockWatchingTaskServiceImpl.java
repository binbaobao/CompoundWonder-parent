package com.compoundwonder.trader.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.selection.firstboard.FirstBoardSelectionService;
import com.compoundwonder.trader.selection.relay.RelaySelectionService;
import com.compoundwonder.trader.selection.smallcapfirstboard.SmallCapFirstBoardSelectionService;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 收盘后选股总编排服务。
 *
 * <p>本类只负责依次调用三个交易模式，不承载任何候选过滤、辅助指标、评分或 TopN
 * 业务规则。三个模式在各自包内独立查询、计算并替换自己的数据库记录。</p>
 */
@Service
@DS("trade")
public class StockWatchingTaskServiceImpl
        extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask>
        implements StockWatchingTaskService {

    private final RelaySelectionService relaySelectionService;
    private final FirstBoardSelectionService firstBoardSelectionService;
    private final SmallCapFirstBoardSelectionService smallCapFirstBoardSelectionService;

    public StockWatchingTaskServiceImpl(
            RelaySelectionService relaySelectionService,
            FirstBoardSelectionService firstBoardSelectionService,
            SmallCapFirstBoardSelectionService smallCapFirstBoardSelectionService) {
        this.relaySelectionService = relaySelectionService;
        this.firstBoardSelectionService = firstBoardSelectionService;
        this.smallCapFirstBoardSelectionService = smallCapFirstBoardSelectionService;
    }

    /**
     * 依次生成连板、普通首板和小市值首板任务。
     *
     * <p>这里不能增加 Spring 本地 {@code @Transactional}：三个选股模式会通过
     * {@code @DS("market")} 服务读取行情，再向 {@code trade} 数据源写任务。
     * 顶层事务会提前绑定单一数据库连接，使后续 market 查询错误落到 cw_backtest。</p>
     */
    @Override
    public List<StockWatchingTask> createPostCloseWatchingTasks(LocalDate tradeDate) {
        List<StockWatchingTask> tasks = new ArrayList<>();
        // 调用连板接力选股方法。
        tasks.addAll(relaySelectionService.select(tradeDate));
        // 调用普通首板选股方法。
        tasks.addAll(firstBoardSelectionService.select(tradeDate));
        // 调用小市值首板选股方法。
        tasks.addAll(smallCapFirstBoardSelectionService.select(tradeDate));
        return tasks;
    }
}
