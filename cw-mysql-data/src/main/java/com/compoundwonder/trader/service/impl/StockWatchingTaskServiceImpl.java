package com.compoundwonder.trader.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 股票盯盘任务服务实现。
 */
@Service
@DS("trade")
public class StockWatchingTaskServiceImpl extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask> implements StockWatchingTaskService {

    /**
     * 交易模式：连板接力。
     */
    private static final int TRADE_MODE_RELAY_LIMIT_UP = 1;

    /**
     * 交易模式：优质首板。
     */
    private static final int TRADE_MODE_FIRST_LIMIT_UP = 2;

    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;

    /**
     * 创建股票盯盘任务服务。
     * 作用：注入日 K 服务，用于根据日 K 结果生成选股盯盘任务。
     */
    public StockWatchingTaskServiceImpl(StockDailyService stockDailyService, StockTradeCalendarService stockTradeCalendarService) {
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
    }

    /**
     * 创建收盘后选股盯盘任务。
     */
    @Override
    public List<StockWatchingTask> createPostCloseWatchingTasks(LocalDate tradeDate, StockEmotionCycleDaily emotionCycleDaily) {
        List<StockWatchingTask> tasks = new ArrayList<>();
        tasks.addAll(createHighQualityFirstLimitUpTasks(tradeDate));
        tasks.addAll(createRelayLimitUpTasks(tradeDate, emotionCycleDaily));
        return tasks;
    }

    /**
     * 创建优质首板推荐任务。
     * 实现逻辑：查询当天非 ST、涨幅小于 11、K 线为涨停、连续涨停天数为 1 的股票，批量插入任务表。
     */
    @Override
    public List<StockWatchingTask> createHighQualityFirstLimitUpTasks(LocalDate tradeDate) {
        List<StockDailyEntity> stockDailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getChangeRate, 11)
                .between(StockDailyEntity::getKlineState, 1, 5)
                .eq(StockDailyEntity::getConsecutiveLimitUpDays, 1));

        List<StockWatchingTask> tasks = stockDailyList.stream()
                .map(stockDaily -> buildWatchingTask(stockDaily, TRADE_MODE_FIRST_LIMIT_UP))
                .toList();
        replaceTasks(tradeDate, TRADE_MODE_FIRST_LIMIT_UP, tasks);
        return tasks;
    }

    /**
     * 创建连板接力推荐任务。
     * 实现逻辑：查询当天非 ST、涨幅小于 11、K 线为涨停、连续涨停天数为 2/3/4 的股票，批量插入任务表。
     */
    @Override
    public List<StockWatchingTask> createRelayLimitUpTasks(LocalDate tradeDate, StockEmotionCycleDaily emotionCycleDaily) {
        List<StockDailyEntity> stockDailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getChangeRate, 11)
                .between(StockDailyEntity::getKlineState, 1, 5)
                .in(StockDailyEntity::getConsecutiveLimitUpDays, List.of(2, 3, 4)));

        List<StockWatchingTask> tasks = stockDailyList.stream()
                .map(stockDaily -> buildWatchingTask(stockDaily, TRADE_MODE_RELAY_LIMIT_UP, calculateRelayLimitUpScore(stockDaily, emotionCycleDaily)))
                .toList();
        replaceTasks(tradeDate, TRADE_MODE_RELAY_LIMIT_UP, tasks);
        return tasks;
    }

    /**
     * 根据日 K 构建盯盘任务。
     */
    private StockWatchingTask buildWatchingTask(StockDailyEntity stockDaily, int tradeMode) {
        return buildWatchingTask(stockDaily, tradeMode, stockDaily.getConsecutiveLimitUpDays());
    }

    /**
     * 根据日 K 构建盯盘任务。
     */
    private StockWatchingTask buildWatchingTask(StockDailyEntity stockDaily, int tradeMode, Integer limitUpScore) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(stockDaily.getStockCode());
        task.setStockName(stockDaily.getStockName());
        task.setLimitUpScore(limitUpScore);
        task.setConsecutiveLimitUpDays(stockDaily.getConsecutiveLimitUpDays());
        task.setRecommendDate(stockDaily.getTradeDate());
        task.setTradeDate(findNextTradeDate(stockDaily.getTradeDate()));
        task.setTradeMode(tradeMode);
        task.setCreatedTime(LocalDateTime.now());
        return task;
    }

    /**
     * 补跑时先删除同一天同模式旧任务，再写入新任务，避免重复推荐。
     */
    private void replaceTasks(LocalDate recommendDate, int tradeMode, List<StockWatchingTask> tasks) {
        remove(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getRecommendDate, recommendDate)
                .eq(StockWatchingTask::getTradeMode, tradeMode));
        if (!tasks.isEmpty()) {
            saveBatch(tasks);
        }
    }

    /**
     * 接力任务分数优先反映连板高度，空间龙额外加分，便于盯盘列表排序。
     */
    private Integer calculateRelayLimitUpScore(StockDailyEntity stockDaily, StockEmotionCycleDaily emotionCycleDaily) {
        int score = Objects.requireNonNullElse(stockDaily.getConsecutiveLimitUpDays(), 0);
        if (emotionCycleDaily != null && Objects.equals(stockDaily.getStockCode(), emotionCycleDaily.getDominantCycleStockCode())) {
            score += 10;
        }
        return score;
    }

    /**
     * 推荐日收盘后生成的任务，在下一交易日盯盘。
     */
    private LocalDate findNextTradeDate(LocalDate recommendDate) {
        LocalDate tradeDate = recommendDate.plusDays(1);
        for (int i = 0; i < 15; i++) {
            if (stockTradeCalendarService.isTradeDay(tradeDate)) {
                return tradeDate;
            }
            tradeDate = tradeDate.plusDays(1);
        }
        return recommendDate.plusDays(1);
    }
}
