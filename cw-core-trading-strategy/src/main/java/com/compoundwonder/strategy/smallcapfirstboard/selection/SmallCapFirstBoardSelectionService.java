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

    /** @param selectionDataService 小市值首板需要的只读选股数据端口 */
    public SmallCapFirstBoardSelectionService(StockSelectionDataService selectionDataService) {
        this.selectionDataService = selectionDataService;
    }

    /**
     * 判断启动流通市值是否归小市值首板模式所有。
     *
     * @param startMarketCap 首板前一交易日收盘流通市值，单位：万元
     */
    public static boolean ownsStartMarketCap(double startMarketCap) {
        return startMarketCap < MAX_START_MARKET_CAP_EXCLUSIVE;
    }

    /**
     * 执行小市值首板候选查询、指标构建、过滤、评分和 Top2 截断。
     *
     * @param tradeDate 选股所依据的收盘交易日
     * @return {@code tradeMode=3} 的下一交易日盯盘任务
     */
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

    /** 查询选股日涨幅小于 11%、流通市值小于 MAX_START_MARKET_CAP_EXCLUSIVE * 1.1 亿元的非 ST 首板基础池。 */
    private List<StockDailyData> listBaseCandidates(LocalDate tradeDate) {
        return selectionDataService.listDailyByTradeDate(tradeDate).stream()
                .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                .filter(daily -> lessThan(daily.getFloatMarketCap(), MAX_START_MARKET_CAP_EXCLUSIVE * 1.11))
                .filter(daily -> lessThan(daily.getClosePrice(), 40D))
                .filter(daily -> lessThan(daily.getChangeRate(), 11D))
                .filter(daily -> Integer.valueOf(1).equals(daily.getConsecutiveLimitUpDays()))
                .filter(daily -> Integer.valueOf(1).equals(daily.getKlineState()))// 首板必须是实体板
                .filter(daily -> daily.getAmplitude() > 7)// 首板必须是实体板
                .toList();
    }

    /** 查询选股日仍有有效可转债的正股代码。 */
    private Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) {
        return selectionDataService.listConvertibleBondStockCodes(tradeDate);
    }

    /** 从基础池排除有效可转债正股，并逐只记录过滤原因。 */
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

    /** 逐只执行小市值首板核心策略，仅把通过候选转换为盯盘任务。 */
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

    /** 将可变辅助对象压缩为核心策略只读候选。 */
    private SmallCapFirstBoardSelectionCandidate toSelectionCandidate(
            SmallCapFirstBoardSelectionAssist assist) {
        return new SmallCapFirstBoardSelectionCandidate(
                assist.getStartMarketCap(), assist.getCurrentPrice(), assist.getMaxTurnoverRate(),
                assist.getHighestConsecutiveLimitUpDays(), assist.getAbnormalKlineStateCount(),
                assist.getPriorTwentyDayAbnormalKlineStateCount(), assist.getThreeDayAmplitude(),
                assist.getTenDayChangeRate());
    }

    /** 为基础日 K 候选逐只补齐小市值首板专用辅助指标。 */
    private List<SmallCapFirstBoardSelectionAssist> buildSelectionAssistList(
            List<StockDailyData> dailyList) {
        return dailyList.stream().map(this::buildSelectionAssist).toList();
    }

    /** 构建单只首板股票的启动市值、历史硬指标、异常次数和近期形态。 */
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

    /** 查询截至选股日最近 23 根日 K，覆盖 10 日形态及首板前 20 日异常窗口。 */
    private List<StockDailyData> listRecentDaily(StockDailyData stockDaily) {
        return selectionDataService.listLatestDaily(
                stockDaily.getStockCode(), stockDaily.getTradeDate(), 23);
    }

    /** 查询截至选股日的 18 个月日 K，仅用于异常状态统计。 */
    private List<StockDailyData> listSelectionWindowDaily(StockDailyData stockDaily) {
        return selectionDataService.listDailyBetween(stockDaily.getStockCode(),
                stockDaily.getTradeDate().minusMonths(18), stockDaily.getTradeDate());
    }

    /** 查询首板前一交易日及以前最近 200 根日 K，用于模式独立硬过滤。 */
    private List<StockDailyData> listHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listLatestDaily(stockCode, historyEndDate, 200);
    }

    /** 查询最早 11 根日 K，用第 11 根确定排除新股早期数据后的起算日。 */
    private List<StockDailyData> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listEarliestDaily(stockCode, historyEndDate, 11);
    }

    /** 统计 18 个月窗口内非零 K 线状态数，并排除本次首板。 */
    private int countAbnormalKlineState(List<StockDailyData> dailyList) {
        long count = dailyList.stream().map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count();
        return Math.max(0, Math.toIntExact(count) - 1);
    }

    /** 跳过当日首板后，统计之前 20 个交易日的非零 K 线状态数。 */
    static int countPriorTwentyDayAbnormalKlineState(List<StockDailyData> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream().skip(1).limit(20)
                .map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count());
    }

    /** 以最近 3 日最低复权价为基准，计算至选股日复权收盘价的振幅。 */
    static double calculateThreeDayAdjustedAmplitude(List<StockDailyData> dailyList) {
        if (dailyList.size() < 3) return 0D;
        List<StockDailyData> window = dailyList.subList(dailyList.size() - 3, dailyList.size());
        Double currentClose = window.get(2).getAdjustClosePrice();
        Double lowest = window.stream().map(StockDailyData::getAdjustLowPrice)
                .filter(Objects::nonNull).min(Double::compareTo).orElse(null);
        if (currentClose == null || lowest == null || lowest <= 0) return 0D;
        return (currentClose - lowest) * 100 / lowest;
    }

    /** 以窗口起点复权收盘价为基准计算指定交易日数的复权涨跌幅。 */
    private double calculateAdjustedCloseChangeRate(List<StockDailyData> dailyList, int days) {
        if (dailyList.size() <= days) return 0D;
        Double base = dailyList.get(dailyList.size() - 1 - days).getAdjustClosePrice();
        Double current = dailyList.get(dailyList.size() - 1).getAdjustClosePrice();
        if (base == null || current == null || base == 0) return 0D;
        return (current - base) * 100 / base;
    }

    /** 将通过策略的候选转换为下一交易日、模式 3 的盯盘任务。 */
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

    /** 查询推荐日之后的下一个交易日。 */
    private LocalDate findNextTradeDate(LocalDate recommendDate) {
        return selectionDataService.findNextTradeDate(recommendDate);
    }

    /** 按分数降序、同分价格升序、再按代码升序稳定排序。 */
    static void sortSelectionTasks(List<SelectionTaskData> tasks, Map<String, Double> priceMap) {
        tasks.sort(Comparator
                .comparing(SelectionTaskData::getLimitUpScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(task -> priceMap.get(task.getStockCode()),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SelectionTaskData::getStockCode,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /** 建立股票代码到选股日收盘价的索引，供同分候选排序。 */
    /**
     * 小市值首板排序使用 先 200日最高板 正序。了，连板越少越好，最大换手越小越好
     * @param assistList
     * @return
     */
    private Map<String, Double> indexCurrentPrices(
            List<SmallCapFirstBoardSelectionAssist> assistList) {
        return assistList.stream().filter(assist -> assist.getStockCode() != null)
                .collect(Collectors.toMap(SmallCapFirstBoardSelectionAssist::getStockCode,
                        SmallCapFirstBoardSelectionAssist::getCurrentPrice,
                        (left, right) -> left));
    }

    /** 排序后保留前 2 只，并记录因数量上限被淘汰的候选。 */
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

    /** 空值不通过的严格小于比较。 */
    private static boolean lessThan(Double value, double upperBound) {
        return value != null && value < upperBound;
    }

    /** 统一输出小市值首板被过滤的股票、层级和指标明细。 */
    private void logFiltered(SmallCapFirstBoardSelectionAssist assist, String step, String detail) {
        log.info("小市值首板选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                assist.getTradeDate(), assist.getStockCode(), assist.getStockName(), step, detail);
    }
}
