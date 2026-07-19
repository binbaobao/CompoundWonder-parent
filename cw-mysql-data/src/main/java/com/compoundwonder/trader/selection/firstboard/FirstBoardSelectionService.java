package com.compoundwonder.trader.selection.firstboard;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockCurrentStatus;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockConvertibleBondHistoryService;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.strategy.TradeMode;
import com.compoundwonder.strategy.firstboard.selection.FirstBoardSelectionCandidate;
import com.compoundwonder.strategy.firstboard.selection.FirstBoardSelectionPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 普通首板模式独立选股服务。
 *
 * <p>普通首板只拥有启动流通市值大于等于 119999 万元的股票。低于该边界的股票
 * 只允许由小市值首板模式判断，即使未通过小市值规则，也不能回退到普通首板。</p>
 */
@Slf4j
@Service
@DS("trade")
public class FirstBoardSelectionService extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask> {

    /** 普通首板与小市值首板的强制分界，单位：万元。 */
    public static final double MIN_START_MARKET_CAP = FirstBoardSelectionPolicy.MIN_START_MARKET_CAP;

    /** 普通首板最终最多保留 3 只。 */
    static final int TASK_LIMIT = 3;

    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockCurrentStatusService stockCurrentStatusService;
    private final StockConvertibleBondHistoryService stockConvertibleBondHistoryService;

    public FirstBoardSelectionService(StockDailyService stockDailyService,
                                      StockTradeCalendarService stockTradeCalendarService,
                                      StockCurrentStatusService stockCurrentStatusService,
                                      StockConvertibleBondHistoryService stockConvertibleBondHistoryService) {
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockCurrentStatusService = stockCurrentStatusService;
        this.stockConvertibleBondHistoryService = stockConvertibleBondHistoryService;
    }

    /** 判断该启动流通市值是否归普通首板模式所有。 */
    public static boolean ownsStartMarketCap(double startMarketCap) {
        return startMarketCap >= MIN_START_MARKET_CAP;
    }

    /** 执行普通首板选股并替换当天 mode=2 的任务。 */
    public List<StockWatchingTask> select(LocalDate tradeDate) {
        // 调用普通首板基础候选查询方法。
        List<StockDailyEntity> dailyList = listBaseCandidates(tradeDate);
        // 调用可转债正股查询方法。
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);
        // 调用普通首板可转债过滤方法。
        dailyList = filterConvertibleBondStocks(dailyList, convertibleBondStockCodes);
        // 调用普通首板辅助对象构建方法。
        List<FirstBoardSelectionAssist> assistList = buildSelectionAssistList(dailyList);
        // 调用普通首板候选过滤与评分方法。
        List<StockWatchingTask> eligibleTasks = selectEligibleTasks(assistList);
        // 调用普通首板价格索引构建方法。
        Map<String, Double> currentPriceByStockCode = indexCurrentPrices(assistList);
        // 调用普通首板排序方法。
        sortSelectionTasks(eligibleTasks, currentPriceByStockCode);
        // 调用普通首板 Top3 截取方法。
        List<StockWatchingTask> tasks = takeTopTasks(eligibleTasks);
        // 调用普通首板任务替换方法。
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
        return dailyList.stream()
                .filter(daily -> {
                    boolean excluded = convertibleBondStockCodes.contains(daily.getStockCode());
                    if (excluded) {
                        log.info("普通首板选股过滤 tradeDate={} stockCode={} stockName={} step=可转债正股",
                                daily.getTradeDate(), daily.getStockCode(), daily.getStockName());
                    }
                    return !excluded;
                })
                .toList();
    }

    private List<StockWatchingTask> selectEligibleTasks(List<FirstBoardSelectionAssist> assistList) {
        List<StockWatchingTask> result = new ArrayList<>();
        for (FirstBoardSelectionAssist assist : assistList) {
            // 调用普通首板核心选股方法。
            FirstBoardSelectionPolicy.Decision decision =
                    FirstBoardSelectionPolicy.evaluate(toSelectionCandidate(assist));
            if (!decision.passed()) {
                logFiltered(assist, decision.layer(), decision.detail());
                continue;
            }
            // 调用普通首板任务构建方法。
            result.add(buildWatchingTask(assist, decision.score()));
        }
        return result;
    }

    private FirstBoardSelectionCandidate toSelectionCandidate(FirstBoardSelectionAssist assist) {
        return new FirstBoardSelectionCandidate(
                assist.getStartMarketCap(), assist.getCurrentPrice(), assist.getStartPrice(),
                assist.getCurrentTurnoverRate(), assist.getProvince(), assist.getNonStMonthCount(),
                assist.getListingMonthCount(), assist.getMaxTurnoverRate(),
                assist.getHighestConsecutiveLimitUpDays(),
                assist.getPriorNinetyDayHighestConsecutiveLimitUpDays(),
                assist.getHistoricalMaxVolume(), assist.getAbnormalKlineStateCount(),
                assist.getPriorTwentyDayAbnormalKlineStateCount(), assist.getThreeDayAmplitude(),
                assist.getTenDayChangeRate());
    }

    private List<FirstBoardSelectionAssist> buildSelectionAssistList(List<StockDailyEntity> dailyList) {
        return dailyList.stream().map(this::buildSelectionAssist).toList();
    }

    private FirstBoardSelectionAssist buildSelectionAssist(StockDailyEntity stockDaily) {
        List<StockDailyEntity> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyEntity> ascRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate)).toList();
        StockDailyEntity startDaily = recentDailyList.size() <= 1 ? null : recentDailyList.get(1);
        LocalDate historyEndDate = startDaily == null ? null : startDaily.getTradeDate();
        // 调用普通首板筹码历史查询方法。
        List<StockDailyEntity> chipHistoryDailyList =
                listChipHistoryDaily(stockDaily.getStockCode(), historyEndDate);
        // 调用普通首板最早日 K 查询方法。
        List<StockDailyEntity> earliestStoredDailyList =
                listEarliestStoredDaily(stockDaily.getStockCode(), historyEndDate);
        // 调用普通首板历史筹码指标计算方法。
        FirstBoardHistoricalMetricsCalculator.HistoricalMetrics metrics =
                FirstBoardHistoricalMetricsCalculator.calculateHistoricalMetrics(
                chipHistoryDailyList, earliestStoredDailyList, historyEndDate);

        FirstBoardSelectionAssist assist = new FirstBoardSelectionAssist();
        assist.setStockCode(stockDaily.getStockCode());
        assist.setStockName(stockDaily.getStockName());
        assist.setTradeDate(stockDaily.getTradeDate());
        assist.setConsecutiveLimitUpDays(stockDaily.getConsecutiveLimitUpDays());
        assist.setProvince(findProvince(stockDaily.getStockCode()));
        assist.setCurrentPrice(stockDaily.getClosePrice());
        assist.setStartMarketCap(startDaily == null ? null : startDaily.getFloatMarketCap());
        assist.setStartPrice(startDaily == null ? null : startDaily.getClosePrice());
        assist.setCurrentTurnoverRate(stockDaily.getTurnoverRate());
        assist.setNonStMonthCount(calculateNonStMonthCount(stockDaily));
        assist.setListingMonthCount(calculateListingMonthCount(stockDaily));
        assist.setMaxTurnoverRate(metrics.maxTurnoverRate());
        assist.setHighestConsecutiveLimitUpDays(metrics.twoHundredKlineHighestBoard());
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(metrics.ninetyDayHighestBoard());
        assist.setHistoricalMaxVolume(metrics.maxVolume());
        assist.setAbnormalKlineStateCount(countAbnormalKlineState(
                listSelectionWindowDaily(stockDaily), 1));
        assist.setPriorTwentyDayAbnormalKlineStateCount(
                countPriorTwentyDayAbnormalKlineState(recentDailyList));
        assist.setThreeDayAmplitude(calculateThreeDayAdjustedAmplitude(ascRecentDailyList));
        assist.setTenDayChangeRate(calculateAdjustedCloseChangeRate(ascRecentDailyList, 10));
        return assist;
    }

    private List<StockDailyEntity> listRecentDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 23"));
    }

    private List<StockDailyEntity> listSelectionWindowDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .ge(StockDailyEntity::getTradeDate, stockDaily.getTradeDate().minusMonths(18))
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate));
    }

    private List<StockDailyEntity> listChipHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, historyEndDate)
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 200"));
    }

    private List<StockDailyEntity> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, historyEndDate)
                .orderByAsc(StockDailyEntity::getTradeDate)
                .last("LIMIT 11"));
    }

    private String findProvince(String stockCode) {
        StockCurrentStatus status = stockCurrentStatusService.getOne(
                Wrappers.<StockCurrentStatus>lambdaQuery()
                        .eq(StockCurrentStatus::getStockCode, stockCode)
                        .last("LIMIT 1"));
        return status == null ? null : status.getRegionName();
    }

    private Integer calculateNonStMonthCount(StockDailyEntity stockDaily) {
        StockDailyEntity lastStDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .lt(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .eq(StockDailyEntity::getIsSt, true)
                .orderByDesc(StockDailyEntity::getTradeDate).last("LIMIT 1"));
        StockDailyEntity firstDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByAsc(StockDailyEntity::getTradeDate).last("LIMIT 1"));
        LocalDate startDate = lastStDaily == null
                ? firstDaily == null ? null : firstDaily.getTradeDate()
                : lastStDaily.getTradeDate().plusDays(1);
        return startDate == null ? null : Math.toIntExact(ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1), stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    private Integer calculateListingMonthCount(StockDailyEntity stockDaily) {
        StockDailyEntity firstDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByAsc(StockDailyEntity::getTradeDate).last("LIMIT 1"));
        if (firstDaily == null || firstDaily.getTradeDate() == null) return null;
        return Math.toIntExact(ChronoUnit.MONTHS.between(
                firstDaily.getTradeDate().withDayOfMonth(1),
                stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    private int countAbnormalKlineState(List<StockDailyEntity> dailyList, int currentBoardCount) {
        long count = dailyList.stream().map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count();
        return Math.max(0, Math.toIntExact(count) - currentBoardCount);
    }

    static int countPriorTwentyDayAbnormalKlineState(List<StockDailyEntity> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream().skip(1).limit(20)
                .map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count());
    }

    static double calculateThreeDayAdjustedAmplitude(List<StockDailyEntity> ascendingDailyList) {
        if (ascendingDailyList.size() < 3) return 0D;
        List<StockDailyEntity> window = ascendingDailyList.subList(
                ascendingDailyList.size() - 3, ascendingDailyList.size());
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

    private StockWatchingTask buildWatchingTask(FirstBoardSelectionAssist assist, int score) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(assist.getStockCode());
        task.setStockName(assist.getStockName());
        task.setLimitUpScore(score);
        task.setConsecutiveLimitUpDays(assist.getConsecutiveLimitUpDays());
        task.setRecommendDate(assist.getTradeDate());
        task.setTradeDate(findNextTradeDate(assist.getTradeDate()));
        task.setTradeMode(TradeMode.FIRST_BOARD.code());
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

    private Map<String, Double> indexCurrentPrices(List<FirstBoardSelectionAssist> assistList) {
        return assistList.stream().filter(assist -> assist.getStockCode() != null)
                .collect(Collectors.toMap(FirstBoardSelectionAssist::getStockCode,
                        FirstBoardSelectionAssist::getCurrentPrice,
                        (left, right) -> left));
    }

    private List<StockWatchingTask> takeTopTasks(List<StockWatchingTask> tasks) {
        if (tasks.size() <= TASK_LIMIT) return tasks;
        List<StockWatchingTask> selected = new ArrayList<>(tasks.subList(0, TASK_LIMIT));
        for (int i = TASK_LIMIT; i < tasks.size(); i++) {
            StockWatchingTask task = tasks.get(i);
            log.info("普通首板选股过滤 tradeDate={} stockCode={} stockName={} step=Top3截断 score={}",
                    task.getRecommendDate(), task.getStockCode(), task.getStockName(), task.getLimitUpScore());
        }
        return selected;
    }

    private void replaceTasks(LocalDate recommendDate, List<StockWatchingTask> tasks) {
        remove(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getRecommendDate, recommendDate)
                .eq(StockWatchingTask::getTradeMode, TradeMode.FIRST_BOARD.code()));
        if (!tasks.isEmpty()) saveBatch(tasks);
    }

    private void logFiltered(FirstBoardSelectionAssist assist, String step, String detail) {
        log.info("普通首板选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                assist.getTradeDate(), assist.getStockCode(), assist.getStockName(), step, detail);
    }
}
