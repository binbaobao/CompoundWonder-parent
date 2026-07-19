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

    /** @param selectionDataService 普通首板需要的只读选股数据端口 */
    public FirstBoardSelectionService(StockSelectionDataService selectionDataService) {
        this.selectionDataService = selectionDataService;
    }

    /**
     * 判断启动流通市值是否归普通首板模式所有。
     *
     * @param startMarketCap 首板前一交易日收盘流通市值，单位：万元
     */
    public static boolean ownsStartMarketCap(double startMarketCap) {
        return startMarketCap >= MIN_START_MARKET_CAP;
    }

    /**
     * 执行普通首板候选查询、指标构建、过滤、评分和 Top3 截断。
     *
     * @param tradeDate 选股所依据的收盘交易日
     * @return {@code tradeMode=2} 的下一交易日盯盘任务
     */
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

    /** 查询选股日涨幅小于 11%、流通市值小于 30 亿元的非 ST 首板基础池。 */
    private List<StockDailyData> listBaseCandidates(LocalDate tradeDate) {
        return selectionDataService.listDailyByTradeDate(tradeDate).stream()
                .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                .filter(daily -> lessThan(daily.getFloatMarketCap(), 300_000D))
                .filter(daily -> lessThan(daily.getClosePrice(), 40D))
                .filter(daily -> lessThan(daily.getChangeRate(), 11D))
                .filter(daily -> Integer.valueOf(1).equals(daily.getConsecutiveLimitUpDays()))
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

    /** 逐只执行普通首板核心策略，仅把通过候选转换为盯盘任务。 */
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

    /** 将可变辅助对象压缩为核心策略只读候选。 */
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

    /** 为基础日 K 候选逐只补齐普通首板专用辅助指标。 */
    private List<FirstBoardSelectionAssist> buildSelectionAssistList(List<StockDailyData> dailyList) {
        return dailyList.stream().map(this::buildSelectionAssist).toList();
    }

    /** 构建单只首板股票的启动指标、历史筹码、异常次数和近期形态。 */
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

    /** 查询首板前一交易日及以前最近 200 根日 K，用于筹码过滤。 */
    private List<StockDailyData> listChipHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listLatestDaily(stockCode, historyEndDate, 200);
    }

    /** 查询最早 11 根日 K，用第 11 根确定排除新股早期数据后的起算日。 */
    private List<StockDailyData> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) return List.of();
        return selectionDataService.listEarliestDaily(stockCode, historyEndDate, 11);
    }

    /** 查询省份字段，供地域评分使用。 */
    private String findProvince(String stockCode) {
        StockCurrentStatusData status = selectionDataService.findCurrentStatus(stockCode);
        return status == null ? null : status.regionName();
    }

    /** 计算上次摘帽次日或上市日起至选股日的非 ST 自然月数。 */
    private Integer calculateNonStMonthCount(StockDailyData stockDaily) {
        LocalDate lastStDate = selectionDataService.findLatestStDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        LocalDate firstTradeDate = selectionDataService.findFirstTradeDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        LocalDate startDate = lastStDate == null ? firstTradeDate : lastStDate.plusDays(1);
        return startDate == null ? null : Math.toIntExact(ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1), stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    /** 计算上市首个交易日至选股日的自然月数。 */
    private Integer calculateListingMonthCount(StockDailyData stockDaily) {
        LocalDate firstTradeDate = selectionDataService.findFirstTradeDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        if (firstTradeDate == null) return null;
        return Math.toIntExact(ChronoUnit.MONTHS.between(
                firstTradeDate.withDayOfMonth(1),
                stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    /** 统计 18 个月窗口内非零 K 线状态数，并排除本次连板占用的次数。 */
    private int countAbnormalKlineState(List<StockDailyData> dailyList, int currentBoardCount) {
        long count = dailyList.stream().map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count();
        return Math.max(0, Math.toIntExact(count) - currentBoardCount);
    }

    /** 跳过当日首板后，统计之前 20 个交易日的非零 K 线状态数。 */
    static int countPriorTwentyDayAbnormalKlineState(List<StockDailyData> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream().skip(1).limit(20)
                .map(StockDailyData::getKlineState)
                .filter(Objects::nonNull).filter(state -> state != 0).count());
    }

    /** 以最近 3 日最低复权价为基准，计算至选股日复权收盘价的振幅。 */
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

    /** 以窗口起点复权收盘价为基准计算指定交易日数的复权涨跌幅。 */
    private double calculateAdjustedCloseChangeRate(List<StockDailyData> dailyList, int days) {
        if (dailyList.size() <= days) return 0D;
        Double base = dailyList.get(dailyList.size() - 1 - days).getAdjustClosePrice();
        Double current = dailyList.get(dailyList.size() - 1).getAdjustClosePrice();
        if (base == null || current == null || base == 0) return 0D;
        return (current - base) * 100 / base;
    }

    /** 将通过策略的候选转换为下一交易日、模式 2 的盯盘任务。 */
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
    private Map<String, Double> indexCurrentPrices(List<FirstBoardSelectionAssist> assistList) {
        return assistList.stream().filter(assist -> assist.getStockCode() != null)
                .collect(Collectors.toMap(FirstBoardSelectionAssist::getStockCode,
                        FirstBoardSelectionAssist::getCurrentPrice,
                        (left, right) -> left));
    }

    /** 排序后保留前 3 只，并记录因数量上限被淘汰的候选。 */
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

    /** 空值不通过的严格小于比较。 */
    private static boolean lessThan(Double value, double upperBound) {
        return value != null && value < upperBound;
    }

    /** 统一输出普通首板被过滤的股票、层级和指标明细。 */
    private void logFiltered(FirstBoardSelectionAssist assist, String step, String detail) {
        log.info("普通首板选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                assist.getTradeDate(), assist.getStockCode(), assist.getStockName(), step, detail);
    }
}
