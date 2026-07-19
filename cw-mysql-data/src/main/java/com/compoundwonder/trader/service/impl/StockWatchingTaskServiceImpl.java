package com.compoundwonder.trader.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.common.strategy.selection.StockSelectionService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 收盘后选股总编排服务。
 *
 * <p>本类只负责调用 common 选股入口、把中立结果转换成 MySQL 实体并替换当天记录，
 * 不承载任何候选过滤、辅助指标、评分或 TopN 业务规则。</p>
 */
@Service
@DS("trade")
public class StockWatchingTaskServiceImpl
        extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask>
        implements StockWatchingTaskService {

    private final StockSelectionService stockSelectionService;

    public StockWatchingTaskServiceImpl(StockSelectionService stockSelectionService) {
        this.stockSelectionService = stockSelectionService;
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
        // 调用三种交易模式统一选股方法。
        List<StockWatchingTask> tasks = stockSelectionService.selectAll(tradeDate).stream()
                .map(this::toEntity)
                .toList();
        // 调用当天三种交易模式旧任务删除方法。
        removeExistingTasks(tradeDate);
        if (!tasks.isEmpty()) {
            saveBatch(tasks);
        }
        return tasks;
    }

    private void removeExistingTasks(LocalDate recommendDate) {
        remove(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getRecommendDate, recommendDate)
                .in(StockWatchingTask::getTradeMode,
                        TradeMode.RELAY_LIMIT_UP.code(),
                        TradeMode.FIRST_BOARD.code(),
                        TradeMode.SMALL_CAP_FIRST_BOARD.code()));
    }

    private StockWatchingTask toEntity(SelectionTaskData data) {
        StockWatchingTask entity = new StockWatchingTask();
        entity.setStockCode(data.getStockCode());
        entity.setStockName(data.getStockName());
        entity.setLimitUpScore(data.getLimitUpScore());
        entity.setConsecutiveLimitUpDays(data.getConsecutiveLimitUpDays());
        entity.setRecommendDate(data.getRecommendDate());
        entity.setTradeDate(data.getTradeDate());
        entity.setTradeMode(data.getTradeMode());
        entity.setCreatedTime(data.getCreatedTime());
        return entity;
    }
}
