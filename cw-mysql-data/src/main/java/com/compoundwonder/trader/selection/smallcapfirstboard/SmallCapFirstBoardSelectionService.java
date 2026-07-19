package com.compoundwonder.trader.selection.smallcapfirstboard;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockConvertibleBondHistoryService;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.selection.TradeMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 小市值首板模式独立选股服务。
 *
 * <p>小市值首板独占启动流通市值严格小于 119999 万元的股票。该模式以后拥有独立
 * 的选股与交易参数，失败候选不能交给普通首板兜底。</p>
 */
@Slf4j
@Service
@DS("trade")
public class SmallCapFirstBoardSelectionService
        extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask> {

    /** 小市值首板启动流通市值严格上限，单位：万元。 */
    public static final double MAX_START_MARKET_CAP_EXCLUSIVE = 119_999D;
    /** 首板前 200 根有效 K 线最大换手率允许等于 30%。 */
    private static final double MAX_HISTORICAL_TURNOVER_RATE = 30D;
    /** 小市值首板最终最多保留 2 只。 */
    static final int TASK_LIMIT = 2;

    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockConvertibleBondHistoryService stockConvertibleBondHistoryService;

    public SmallCapFirstBoardSelectionService(
            StockDailyService stockDailyService,
            StockTradeCalendarService stockTradeCalendarService,
            StockConvertibleBondHistoryService stockConvertibleBondHistoryService) {
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockConvertibleBondHistoryService = stockConvertibleBondHistoryService;
    }

    /** 判断该启动流通市值是否归小市值首板模式所有。 */
    public static boolean ownsStartMarketCap(double startMarketCap) {
        return startMarketCap < MAX_START_MARKET_CAP_EXCLUSIVE;
    }

    /** 执行小市值首板选股并替换当天 mode=3 的任务。 */
    public List<StockWatchingTask> select(LocalDate tradeDate) {
        // 调用小市值首板基础候选查询方法。
        List<StockDailyEntity> dailyList = listBaseCandidates(tradeDate);
        // 调用可转债正股查询方法。
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);
        // 调用小市值首板可转债过滤方法。
        dailyList = filterConvertibleBondStocks(dailyList, convertibleBondStockCodes);
        // 调用小市值首板辅助对象构建方法。
        List<SmallCapFirstBoardSelectionAssist> assistList = buildSelectionAssistList(dailyList);
        // 调用小市值首板候选过滤与评分方法。
        List<StockWatchingTask> eligibleTasks = selectEligibleTasks(assistList);
        // 调用小市值首板价格索引构建方法。
        Map<String, Double> currentPriceByStockCode = indexCurrentPrices(assistList);
        // 调用小市值首板排序方法。
        sortSelectionTasks(eligibleTasks, currentPriceByStockCode);
        // 调用小市值首板 Top2 截取方法。
        List<StockWatchingTask> tasks = takeTopTasks(eligibleTasks);
        // 调用小市值首板任务替换方法。
        replaceTasks(tradeDate, tasks);
        return tasks;
    }

    private List<StockDailyEntity> listBaseCandidates(LocalDate tradeDate) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt)
                        .or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getFloatMarketCap, 300_000)
                .lt(StockDailyEntity::getClosePrice, 40)
                .lt(StockDailyEntity::getChangeRate, 11)
                .eq(StockDailyEntity::getConsecutiveLimitUpDays, 1));
    }

    private Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) {
        return stockConvertibleBondHistoryService.listTradableStockCodes(tradeDate);
    }

    private List<StockDailyEntity> filterConvertibleBondStocks(
            List<StockDailyEntity> dailyList,
            Set<String> convertibleBondStockCodes) {
        return dailyList.stream().filter(daily -> {
            boolean excluded = convertibleBondStockCodes.contains(daily.getStockCode());
            if (excluded) {
                log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step=可转债正股",
                        daily.getTradeDate(), daily.getStockCode(), daily.getStockName());
            }
            return !excluded;
        }).toList();
    }

    private List<StockWatchingTask> selectEligibleTasks(
            List<SmallCapFirstBoardSelectionAssist> assistList) {
        List<StockWatchingTask> result = new ArrayList<>();
        for (SmallCapFirstBoardSelectionAssist assist : assistList) {
            Double startMarketCap = assist.getStartMarketCap();
            if (startMarketCap == null || !ownsStartMarketCap(startMarketCap)) {
                logFiltered(assist, "模式市值归属", "actual=" + startMarketCap
                        + "万元, required<119999万元; 普通首板与小市值首板互不回退");
                continue;
            }
            double maxTurnoverRate = Objects.requireNonNullElse(assist.getMaxTurnoverRate(), 0D);
            if (maxTurnoverRate > MAX_HISTORICAL_TURNOVER_RATE) {
                logFiltered(assist, "200根K线历史最大换手率",
                        "actual=" + maxTurnoverRate + "%, required<=30%");
                continue;
            }
            int highestBoard = Objects.requireNonNullElse(
                    assist.getHighestConsecutiveLimitUpDays(), 0);
            if (highestBoard >= 3) {
                logFiltered(assist, "200根K线历史最高板",
                        "actual=" + highestBoard + ", required<3");
                continue;
            }
            int priorTwentyAbnormal = Objects.requireNonNullElse(
                    assist.getPriorTwentyDayAbnormalKlineStateCount(), 0);
            if (priorTwentyAbnormal >= 4) {
                logFiltered(assist, "前20日非正常K线次数",
                        "actual=" + priorTwentyAbnormal + ", required<4");
                continue;
            }
            int abnormalCount = Objects.requireNonNullElse(assist.getAbnormalKlineStateCount(), 0);
            if (abnormalCount > 25) {
                logFiltered(assist, "18个月非正常状态次数",
                        "actual=" + abnormalCount + ", required<=25");
                continue;
            }
            double amplitude = Objects.requireNonNullElse(assist.getThreeDayAmplitude(), 0D);
            if (amplitude >= 20) {
                logFiltered(assist, "3日振幅", "actual=" + amplitude + ", required<20");
                continue;
            }
            double tenDayChangeRate = Objects.requireNonNullElse(assist.getTenDayChangeRate(), 0D);
            if (tenDayChangeRate <= -2 || tenDayChangeRate >= 25) {
                logFiltered(assist, "10日涨跌幅", "actual=" + tenDayChangeRate
                        + ", required=(-2,25)");
                continue;
            }
            // 调用小市值首板市值评分方法。
            int score = scoreStartMarketCap(startMarketCap);
            // 调用小市值首板任务构建方法。
            result.add(buildWatchingTask(assist, score));
        }
        return result;
    }

    private List<SmallCapFirstBoardSelectionAssist> buildSelectionAssistList(
            List<StockDailyEntity> dailyList) {
        return dailyList.stream().map(this::buildSelectionAssist).toList();
    }

    private SmallCapFirstBoardSelectionAssist buildSelectionAssist(StockDailyEntity stockDaily) {
        List<StockDailyEntity> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyEntity> ascendingRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate)).toList();
        StockDailyEntity startDaily = recentDailyList.size() <= 1 ? null : recentDailyList.get(1);
        LocalDate historyEndDate = startDaily == null ? null : startDaily.getTradeDate();
        // 调用小市值首板历史日 K 查询方法。
        List<StockDailyEntity> historyDailyList =
                listHistoryDaily(stockDaily.getStockCode(), historyEndDate);
        // 调用小市值首板最早日 K 查询方法。
        List<StockDailyEntity> earliestStoredDailyList =
                listEarliestStoredDaily(stockDaily.getStockCode(), historyEndDate);
        // 调用小市值首板历史指标计算方法。
        SmallCapFirstBoardHistoryCalculator.HistoricalMetrics metrics =
                SmallCapFirstBoardHistoryCalculator.calculate(
                        historyDailyList, earliestStoredDailyList, historyEndDate);

        SmallCapFirstBoardSelectionAssist assist = new SmallCapFirstBoardSelectionAssist();
        assist.setStockCode(stockDaily.getStockCode());
        assist.setStockName(stockDaily.getStockName());
        assist.setTradeDate(stockDaily.getTradeDate());
        assist.setConsecutiveLimitUpDays(stockDaily.getConsecutiveLimitUpDays());
        assist.setCurrentPrice(stockDaily.getClosePrice());
        assist.setStartMarketCap(startDaily == null ? null : startDaily.getFloatMarketCap());
        assist.setMaxTurnoverRate(metrics.maxTurnoverRate());
        assist.setHighestConsecutiveLimitUpDays(metrics.highestBoard());
        assist.setAbnormalKlineStateCount(countAbnormalKlineState(
                listSelectionWindowDaily(stockDaily)));
        assist.setPriorTwentyDayAbnormalKlineStateCount(
                countPriorTwentyDayAbnormalKlineState(recentDailyList));
        assist.setThreeDayAmplitude(calculateThreeDayAdjustedAmplitude(ascendingRecentDailyList));
        assist.setTenDayChangeRate(calculateAdjustedCloseChangeRate(ascendingRecentDailyList, 10));
        return assist;
    }

    private List<StockDailyEntity> listRecentDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate).last("LIMIT 23"));
    }

    private List<StockDailyEntity> listSelectionWindowDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .ge(StockDailyEntity::getTradeDate, stockDaily.getTradeDate().minusMonths(18))
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate));
    }

    private List<StockDailyEntity> listHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, historyEndDate)
                .orderByDesc(StockDailyEntity::getTradeDate).last("LIMIT 200"));
    }

    private List<StockDailyEntity> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, historyEndDate)
                .orderByAsc(StockDailyEntity::getTradeDate).last("LIMIT 11"));
    }

    private int countAbnormalKlineState(List<StockDailyEntity> dailyList) {
        long count = dailyList.stream().map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count();
        return Math.max(0, Math.toIntExact(count) - 1);
    }

    static int countPriorTwentyDayAbnormalKlineState(List<StockDailyEntity> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream().skip(1).limit(20)
                .map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count());
    }

    static double calculateThreeDayAdjustedAmplitude(List<StockDailyEntity> dailyList) {
        if (dailyList.size() < 3) return 0D;
        List<StockDailyEntity> window = dailyList.subList(dailyList.size() - 3, dailyList.size());
        Double currentClose = window.get(2).getAdjustClosePrice();
        Double lowest = window.stream().map(StockDailyEntity::getAdjustLowPrice)
                .filter(Objects::nonNull).min(Double::compareTo).orElse(null);
        if (currentClose == null || lowest == null || lowest <= 0) return 0D;
        return (currentClose - lowest) * 100 / lowest;
    }

    private double calculateAdjustedCloseChangeRate(List<StockDailyEntity> dailyList, int days) {
        if (dailyList.size() <= days) return 0D;
        Double base = dailyList.get(dailyList.size() - 1 - days).getAdjustClosePrice();
        Double current = dailyList.get(dailyList.size() - 1).getAdjustClosePrice();
        if (base == null || current == null || base == 0) return 0D;
        return (current - base) * 100 / base;
    }

    private int scoreStartMarketCap(double value) {
        if (value > 200_000) return 0;
        if (value <= 81_000) return 30;
        if (value <= 95_000) return interpolate(value, 81_000, 95_000, 30, 20);
        if (value <= 150_000) return interpolate(value, 95_000, 150_000, 20, 10);
        return interpolate(value, 150_000, 200_000, 10, 0);
    }

    private int interpolate(double value, double min, double max, int minScore, int maxScore) {
        return (int) Math.round(minScore + (value - min) * (maxScore - minScore) / (max - min));
    }

    private StockWatchingTask buildWatchingTask(SmallCapFirstBoardSelectionAssist assist, int score) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(assist.getStockCode());
        task.setStockName(assist.getStockName());
        task.setLimitUpScore(score);
        task.setConsecutiveLimitUpDays(assist.getConsecutiveLimitUpDays());
        task.setRecommendDate(assist.getTradeDate());
        task.setTradeDate(findNextTradeDate(assist.getTradeDate()));
        task.setTradeMode(TradeMode.SMALL_CAP_FIRST_BOARD.code());
        task.setCreatedTime(LocalDateTime.now());
        return task;
    }

    private LocalDate findNextTradeDate(LocalDate recommendDate) {
        LocalDate date = recommendDate.plusDays(1);
        for (int i = 0; i < 15; i++) {
            if (stockTradeCalendarService.isTradeDay(date)) return date;
            date = date.plusDays(1);
        }
        return recommendDate.plusDays(1);
    }

    static void sortSelectionTasks(List<StockWatchingTask> tasks, Map<String, Double> priceMap) {
        tasks.sort(Comparator
                .comparing(StockWatchingTask::getLimitUpScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(task -> priceMap.get(task.getStockCode()),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(StockWatchingTask::getStockCode,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private Map<String, Double> indexCurrentPrices(
            List<SmallCapFirstBoardSelectionAssist> assistList) {
        return assistList.stream().filter(assist -> assist.getStockCode() != null)
                .collect(Collectors.toMap(SmallCapFirstBoardSelectionAssist::getStockCode,
                        SmallCapFirstBoardSelectionAssist::getCurrentPrice,
                        (left, right) -> left));
    }

    private List<StockWatchingTask> takeTopTasks(List<StockWatchingTask> tasks) {
        if (tasks.size() <= TASK_LIMIT) return tasks;
        List<StockWatchingTask> selected = new ArrayList<>(tasks.subList(0, TASK_LIMIT));
        for (int i = TASK_LIMIT; i < tasks.size(); i++) {
            StockWatchingTask task = tasks.get(i);
            log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step=Top2截断 score={}",
                    task.getRecommendDate(), task.getStockCode(), task.getStockName(), task.getLimitUpScore());
        }
        return selected;
    }

    private void replaceTasks(LocalDate recommendDate, List<StockWatchingTask> tasks) {
        remove(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getRecommendDate, recommendDate)
                .eq(StockWatchingTask::getTradeMode, TradeMode.SMALL_CAP_FIRST_BOARD.code()));
        if (!tasks.isEmpty()) saveBatch(tasks);
    }

    private void logFiltered(SmallCapFirstBoardSelectionAssist assist, String step, String detail) {
        log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                assist.getTradeDate(), assist.getStockCode(), assist.getStockName(), step, detail);
    }
}
