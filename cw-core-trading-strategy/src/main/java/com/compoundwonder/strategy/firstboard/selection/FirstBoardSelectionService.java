package com.compoundwonder.strategy.firstboard.selection;

import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.StockCurrentStatusData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import lombok.extern.slf4j.Slf4j;

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
public class FirstBoardSelectionService {

    /** 普通首板与小市值首板的强制分界，单位：万元。 */
    public static final double MIN_START_MARKET_CAP = FirstBoardSelectionPolicy.MIN_START_MARKET_CAP;

    /** 普通首板最终最多保留 3 只。 */
    static final int TASK_LIMIT = 3;

    private final StockSelectionDataService selectionDataService;

    public FirstBoardSelectionService(StockSelectionDataService selectionDataService) {
        this.selectionDataService = selectionDataService;
    }

    /** 判断该启动流通市值是否归普通首板模式所有。 */
    public static boolean ownsStartMarketCap(double startMarketCap) {
        return startMarketCap >= MIN_START_MARKET_CAP;
    }

    /** 执行普通首板选股并返回 mode=2 的中立任务结果。 */
    public List<SelectionTaskData> select(LocalDate tradeDate) {
        // 调用普通首板基础候选查询方法。
        List<StockDailyData> dailyList = listBaseCandidates(tradeDate);
        // 调用可转债正股查询方法。
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);
        // 调用普通首板可转债过滤方法。
        dailyList = filterConvertibleBondStocks(dailyList, convertibleBondStockCodes);
        // 调用普通首板辅助对象构建方法。
        List<FirstBoardSelectionAssist> assistList = buildSelectionAssistList(dailyList);
        // 调用普通首板候选过滤与评分方法。
        List<SelectionTaskData> eligibleTasks = selectEligibleTasks(assistList);
        // 调用普通首板价格索引构建方法。
        Map<String, Double> currentPriceByStockCode = indexCurrentPrices(assistList);
        // 调用普通首板排序方法。
        sortSelectionTasks(eligibleTasks, currentPriceByStockCode);
        // 调用普通首板 Top3 截取方法。
        List<SelectionTaskData> tasks = takeTopTasks(eligibleTasks);
        return tasks;
    }

    private List<StockDailyData> listBaseCandidates(LocalDate tradeDate) {
        return selectionDataService.listDailyByTradeDate(tradeDate).stream()
                .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                .filter(daily -> lessThan(daily.getFloatMarketCap(), 300_000D))
                .filter(daily -> lessThan(daily.getClosePrice(), 40D))
                .filter(daily -> lessThan(daily.getChangeRate(), 11D))
                .filter(daily -> Integer.valueOf(1).equals(daily.getConsecutiveLimitUpDays()))
                .toList();
    }

    private Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) {
        return selectionDataService.listConvertibleBondStockCodes(tradeDate);
    }

    private List<StockDailyData> filterConvertibleBondStocks(
            List<StockDailyData> dailyList,
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

    private List<SelectionTaskData> selectEligibleTasks(List<FirstBoardSelectionAssist> assistList) {
        List<SelectionTaskData> result = new ArrayList<>();
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

    private List<FirstBoardSelectionAssist> buildSelectionAssistList(List<StockDailyData> dailyList) {
        return dailyList.stream().map(this::buildSelectionAssist).toList();
    }

    private FirstBoardSelectionAssist buildSelectionAssist(StockDailyData stockDaily) {
        List<StockDailyData> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyData> ascRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyData::getTradeDate)).toList();
        StockDailyData startDaily = recentDailyList.size() <= 1 ? null : recentDailyList.get(1);
        LocalDate historyEndDate = startDaily == null ? null : startDaily.getTradeDate();
        // 调用普通首板筹码历史查询方法。
        List<StockDailyData> chipHistoryDailyList =
                listChipHistoryDaily(stockDaily.getStockCode(), historyEndDate);
        // 调用普通首板最早日 K 查询方法。
        List<StockDailyData> earliestStoredDailyList =
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

    private List<StockDailyData> listRecentDaily(StockDailyData stockDaily) {
        return selectionDataService.listLatestDaily(
                stockDaily.getStockCode(), stockDaily.getTradeDate(), 23);
    }

    private List<StockDailyData> listSelectionWindowDaily(StockDailyData stockDaily) {
        return selectionDataService.listDailyBetween(stockDaily.getStockCode(),
                stockDaily.getTradeDate().minusMonths(18), stockDaily.getTradeDate());
    }

    private List<StockDailyData> listChipHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listLatestDaily(stockCode, historyEndDate, 200);
    }

    private List<StockDailyData> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listEarliestDaily(stockCode, historyEndDate, 11);
    }

    private String findProvince(String stockCode) {
        StockCurrentStatusData status = selectionDataService.findCurrentStatus(stockCode);
        return status == null ? null : status.regionName();
    }

    private Integer calculateNonStMonthCount(StockDailyData stockDaily) {
        LocalDate lastStDate = selectionDataService.findLatestStDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        LocalDate firstTradeDate = selectionDataService.findFirstTradeDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        LocalDate startDate = lastStDate == null ? firstTradeDate : lastStDate.plusDays(1);
        return startDate == null ? null : Math.toIntExact(ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1), stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    private Integer calculateListingMonthCount(StockDailyData stockDaily) {
        LocalDate firstTradeDate = selectionDataService.findFirstTradeDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        if (firstTradeDate == null) return null;
        return Math.toIntExact(ChronoUnit.MONTHS.between(
                firstTradeDate.withDayOfMonth(1),
                stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    private int countAbnormalKlineState(List<StockDailyData> dailyList, int currentBoardCount) {
        long count = dailyList.stream().map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count();
        return Math.max(0, Math.toIntExact(count) - currentBoardCount);
    }

    static int countPriorTwentyDayAbnormalKlineState(List<StockDailyData> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream().skip(1).limit(20)
                .map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count());
    }

    static double calculateThreeDayAdjustedAmplitude(List<StockDailyData> ascendingDailyList) {
        if (ascendingDailyList.size() < 3) return 0D;
        List<StockDailyData> window = ascendingDailyList.subList(
                ascendingDailyList.size() - 3, ascendingDailyList.size());
        Double currentClose = window.get(2).getAdjustClosePrice();
        Double lowest = window.stream().map(StockDailyData::getAdjustLowPrice)
                .filter(Objects::nonNull).min(Double::compareTo).orElse(null);
        if (currentClose == null || lowest == null || lowest <= 0) return 0D;
        return (currentClose - lowest) * 100 / lowest;
    }

    private double calculateAdjustedCloseChangeRate(List<StockDailyData> dailyList, int days) {
        if (dailyList.size() <= days) return 0D;
        Double base = dailyList.get(dailyList.size() - 1 - days).getAdjustClosePrice();
        Double current = dailyList.get(dailyList.size() - 1).getAdjustClosePrice();
        if (base == null || current == null || base == 0) return 0D;
        return (current - base) * 100 / base;
    }

    private SelectionTaskData buildWatchingTask(FirstBoardSelectionAssist assist, int score) {
        SelectionTaskData task = new SelectionTaskData();
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
        return selectionDataService.findNextTradeDate(recommendDate);
    }

    static void sortSelectionTasks(List<SelectionTaskData> tasks, Map<String, Double> priceMap) {
        tasks.sort(Comparator
                .comparing(SelectionTaskData::getLimitUpScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(task -> priceMap.get(task.getStockCode()),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SelectionTaskData::getStockCode,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private Map<String, Double> indexCurrentPrices(List<FirstBoardSelectionAssist> assistList) {
        return assistList.stream().filter(assist -> assist.getStockCode() != null)
                .collect(Collectors.toMap(FirstBoardSelectionAssist::getStockCode,
                        FirstBoardSelectionAssist::getCurrentPrice,
                        (left, right) -> left));
    }

    private List<SelectionTaskData> takeTopTasks(List<SelectionTaskData> tasks) {
        if (tasks.size() <= TASK_LIMIT) return tasks;
        List<SelectionTaskData> selected = new ArrayList<>(tasks.subList(0, TASK_LIMIT));
        for (int i = TASK_LIMIT; i < tasks.size(); i++) {
            SelectionTaskData task = tasks.get(i);
            log.info("普通首板选股过滤 tradeDate={} stockCode={} stockName={} step=Top3截断 score={}",
                    task.getRecommendDate(), task.getStockCode(), task.getStockName(), task.getLimitUpScore());
        }
        return selected;
    }

    private static boolean lessThan(Double value, double upperBound) {
        return value != null && value < upperBound;
    }

    private void logFiltered(FirstBoardSelectionAssist assist, String step, String detail) {
        log.info("普通首板选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                assist.getTradeDate(), assist.getStockCode(), assist.getStockName(), step, detail);
    }
}
