package com.compoundwonder.strategy.smallcapfirstboard.selection;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import lombok.extern.slf4j.Slf4j;

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
public class SmallCapFirstBoardSelectionService {

    /** 小市值首板启动流通市值严格上限，单位：万元。 */
    public static final double MAX_START_MARKET_CAP_EXCLUSIVE =
            SmallCapFirstBoardSelectionPolicy.MAX_START_MARKET_CAP_EXCLUSIVE;
    /** 小市值首板最终最多保留 2 只。 */
    static final int TASK_LIMIT = 2;

    private final StockSelectionDataService selectionDataService;

    public SmallCapFirstBoardSelectionService(StockSelectionDataService selectionDataService) {
        this.selectionDataService = selectionDataService;
    }

    /** 判断该启动流通市值是否归小市值首板模式所有。 */
    public static boolean ownsStartMarketCap(double startMarketCap) {
        return startMarketCap < MAX_START_MARKET_CAP_EXCLUSIVE;
    }

    /** 执行小市值首板选股并返回 mode=3 的中立任务结果。 */
    public List<SelectionTaskData> select(LocalDate tradeDate) {
        // 调用小市值首板基础候选查询方法。
        List<StockDailyData> dailyList = listBaseCandidates(tradeDate);
        // 调用可转债正股查询方法。
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);
        // 调用小市值首板可转债过滤方法。
        dailyList = filterConvertibleBondStocks(dailyList, convertibleBondStockCodes);
        // 调用小市值首板辅助对象构建方法。
        List<SmallCapFirstBoardSelectionAssist> assistList = buildSelectionAssistList(dailyList);
        // 调用小市值首板候选过滤与评分方法。
        List<SelectionTaskData> eligibleTasks = selectEligibleTasks(assistList);
        // 调用小市值首板价格索引构建方法。
        Map<String, Double> currentPriceByStockCode = indexCurrentPrices(assistList);
        // 调用小市值首板排序方法。
        sortSelectionTasks(eligibleTasks, currentPriceByStockCode);
        // 调用小市值首板 Top2 截取方法。
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
        return dailyList.stream().filter(daily -> {
            boolean excluded = convertibleBondStockCodes.contains(daily.getStockCode());
            if (excluded) {
                log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step=可转债正股",
                        daily.getTradeDate(), daily.getStockCode(), daily.getStockName());
            }
            return !excluded;
        }).toList();
    }

    private List<SelectionTaskData> selectEligibleTasks(
            List<SmallCapFirstBoardSelectionAssist> assistList) {
        List<SelectionTaskData> result = new ArrayList<>();
        for (SmallCapFirstBoardSelectionAssist assist : assistList) {
            // 调用小市值首板核心选股方法。
            SmallCapFirstBoardSelectionPolicy.Decision decision =
                    SmallCapFirstBoardSelectionPolicy.evaluate(toSelectionCandidate(assist));
            if (!decision.passed()) {
                logFiltered(assist, decision.layer(), decision.detail());
                continue;
            }
            // 调用小市值首板任务构建方法。
            result.add(buildWatchingTask(assist, decision.score()));
        }
        return result;
    }

    private SmallCapFirstBoardSelectionCandidate toSelectionCandidate(
            SmallCapFirstBoardSelectionAssist assist) {
        return new SmallCapFirstBoardSelectionCandidate(
                assist.getStartMarketCap(), assist.getMaxTurnoverRate(),
                assist.getHighestConsecutiveLimitUpDays(), assist.getAbnormalKlineStateCount(),
                assist.getPriorTwentyDayAbnormalKlineStateCount(), assist.getThreeDayAmplitude(),
                assist.getTenDayChangeRate());
    }

    private List<SmallCapFirstBoardSelectionAssist> buildSelectionAssistList(
            List<StockDailyData> dailyList) {
        return dailyList.stream().map(this::buildSelectionAssist).toList();
    }

    private SmallCapFirstBoardSelectionAssist buildSelectionAssist(StockDailyData stockDaily) {
        List<StockDailyData> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyData> ascendingRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyData::getTradeDate)).toList();
        StockDailyData startDaily = recentDailyList.size() <= 1 ? null : recentDailyList.get(1);
        LocalDate historyEndDate = startDaily == null ? null : startDaily.getTradeDate();
        // 调用小市值首板历史日 K 查询方法。
        List<StockDailyData> historyDailyList =
                listHistoryDaily(stockDaily.getStockCode(), historyEndDate);
        // 调用小市值首板最早日 K 查询方法。
        List<StockDailyData> earliestStoredDailyList =
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

    private List<StockDailyData> listRecentDaily(StockDailyData stockDaily) {
        return selectionDataService.listLatestDaily(
                stockDaily.getStockCode(), stockDaily.getTradeDate(), 23);
    }

    private List<StockDailyData> listSelectionWindowDaily(StockDailyData stockDaily) {
        return selectionDataService.listDailyBetween(stockDaily.getStockCode(),
                stockDaily.getTradeDate().minusMonths(18), stockDaily.getTradeDate());
    }

    private List<StockDailyData> listHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listLatestDaily(stockCode, historyEndDate, 200);
    }

    private List<StockDailyData> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listEarliestDaily(stockCode, historyEndDate, 11);
    }

    private int countAbnormalKlineState(List<StockDailyData> dailyList) {
        long count = dailyList.stream().map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count();
        return Math.max(0, Math.toIntExact(count) - 1);
    }

    static int countPriorTwentyDayAbnormalKlineState(List<StockDailyData> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream().skip(1).limit(20)
                .map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count());
    }

    static double calculateThreeDayAdjustedAmplitude(List<StockDailyData> dailyList) {
        if (dailyList.size() < 3) return 0D;
        List<StockDailyData> window = dailyList.subList(dailyList.size() - 3, dailyList.size());
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

    private SelectionTaskData buildWatchingTask(SmallCapFirstBoardSelectionAssist assist, int score) {
        SelectionTaskData task = new SelectionTaskData();
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

    private Map<String, Double> indexCurrentPrices(
            List<SmallCapFirstBoardSelectionAssist> assistList) {
        return assistList.stream().filter(assist -> assist.getStockCode() != null)
                .collect(Collectors.toMap(SmallCapFirstBoardSelectionAssist::getStockCode,
                        SmallCapFirstBoardSelectionAssist::getCurrentPrice,
                        (left, right) -> left));
    }

    private List<SelectionTaskData> takeTopTasks(List<SelectionTaskData> tasks) {
        if (tasks.size() <= TASK_LIMIT) return tasks;
        List<SelectionTaskData> selected = new ArrayList<>(tasks.subList(0, TASK_LIMIT));
        for (int i = TASK_LIMIT; i < tasks.size(); i++) {
            SelectionTaskData task = tasks.get(i);
            log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step=Top2截断 score={}",
                    task.getRecommendDate(), task.getStockCode(), task.getStockName(), task.getLimitUpScore());
        }
        return selected;
    }

    private static boolean lessThan(Double value, double upperBound) {
        return value != null && value < upperBound;
    }

    private void logFiltered(SmallCapFirstBoardSelectionAssist assist, String step, String detail) {
        log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                assist.getTradeDate(), assist.getStockCode(), assist.getStockName(), step, detail);
    }
}
