package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockCurrentStatusData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 连板接力模式独立选股服务。
 *
 * <p>负责触发解析、2/3 板原始池、三级强度过滤、弱唯一 5 板卡位和 Top3；
 * 不与两种首板模式共享候选或回退结果。</p>
 */
@Slf4j
public class RelaySelectionService {

    public static final String STRATEGY_VERSION = "relay-v1";

    /** 所有连板触发点最终最多保留 3 只。 */
    static final int NORMAL_RELAY_TASK_LIMIT = 3;

    /** 当日加上之前 10 个交易日，供弱 5 板计算前 10 日平均高度。 */
    static final int WEAK_FIVE_BOARD_EMOTION_LOOKBACK = 11;

    /**
     * 唯一弱 5 板属于主观卡位预判，只允许严格过滤后的 2 板候选保留前 3 只。
     */
    static final int WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT = 3;

    private final StockSelectionDataService selectionDataService;

    /** @param selectionDataService 连板接力需要的只读选股数据端口 */
    public RelaySelectionService(StockSelectionDataService selectionDataService) {
        this.selectionDataService = selectionDataService;
    }

    /**
     * 执行连板接力候选查询、指标构建、分通道过滤和数量截断。
     *
     * @param tradeDate 选股所依据的收盘交易日
     * @return {@code tradeMode=1} 的下一交易日盯盘任务
     */
    public List<SelectionTaskData> select(LocalDate tradeDate) {
        return selectDetailed(tradeDate).tasks();
    }

    /**
     * 执行触发解析并保留触发板数内全部原始候选的过滤轨迹。
     * 原始池在 ST、可转债、市值、价格、筹码等过滤之前建立。
     */
    public RelaySelectionResult selectDetailed(LocalDate tradeDate) {
        if (tradeDate == null) {
            throw new IllegalArgumentException("选股日期不能为空");
        }
        List<StockDailyData> allDaily = selectionDataService.listDailyByTradeDate(tradeDate);
        List<MarketEmotionData> emotions = selectionDataService.listLatestMarketEmotion(
                tradeDate, WEAK_FIVE_BOARD_EMOTION_LOOKBACK);
        if (emotions.size() < 3) {
            RelaySelectionPlan none = RelaySelectionPlan.none("缺少连续三个交易日的市场情绪数据");
            return new RelaySelectionResult(tradeDate, none, none, List.of(), List.of(), null);
        }

        MarketEmotionData today = emotions.get(0);
        MarketEmotionData yesterday = emotions.get(1);
        MarketEmotionData dayBefore = emotions.get(2);
        RelayTriggerContext context = new RelayTriggerContext(today, yesterday, dayBefore,
                findHighestLimitUp(allDailyFor(yesterday.tradeDate()),
                        yesterday.highestConsecutiveLimitUpDays()),
                findHighestLimitUp(allDailyFor(dayBefore.tradeDate()),
                        dayBefore.highestConsecutiveLimitUpDays()));
        RelaySelectionPlan primaryPlan = RelayTriggerResolver.resolve(context);
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);

        PlanEvaluation primary = evaluatePlan(tradeDate, allDaily,
                convertibleBondStockCodes, primaryPlan);
        if (!primary.tasks().isEmpty()) {
            return new RelaySelectionResult(tradeDate, primaryPlan, primaryPlan,
                    primary.evaluations(), primary.tasks(), null);
        }

        int todayHeight = Objects.requireNonNullElse(today.highestConsecutiveLimitUpDays(), 0);
        String fallbackDetail = null;
        if (todayHeight == 5) {
            Double previousTenDayAverageHeight =
                    calculatePreviousTenDayAverageHeight(emotions);
            List<WeakFiveBoardFallbackPolicy.FiveBoardQuality> fiveBoardQualities = List.of();
            if (WeakFiveBoardFallbackPolicy.isAverageHeightAllowed(
                    previousTenDayAverageHeight)) {
                List<StockDailyData> fiveBoardDailyList = allDaily.stream()
                        .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                        .filter(daily -> Integer.valueOf(5)
                                .equals(daily.getConsecutiveLimitUpDays()))
                        .toList();
                fiveBoardQualities = buildFiveBoardQualities(fiveBoardDailyList);
            }
            WeakFiveBoardFallbackPolicy.Decision fallbackDecision =
                    WeakFiveBoardFallbackPolicy.evaluate(
                            todayHeight, false, previousTenDayAverageHeight, fiveBoardQualities);
            fallbackDetail = fallbackDecision.layer() + ": " + fallbackDecision.detail();
            if (fallbackDecision.triggered()) {
                RelaySelectionPlan fallbackPlan = new RelaySelectionPlan(
                        RelaySelectionTrigger.WEAK_FIVE_CARD,
                        List.of(new RelayBoardPlan(2, RelaySelectionStrength.STRICT)),
                        WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT,
                        fallbackDetail);
                PlanEvaluation fallback = evaluatePlan(tradeDate, allDaily,
                        convertibleBondStockCodes, fallbackPlan);
                return new RelaySelectionResult(tradeDate, primaryPlan, fallbackPlan,
                        fallback.evaluations(), fallback.tasks(), fallbackDetail);
            }
        }

        return new RelaySelectionResult(tradeDate, primaryPlan, primaryPlan,
                primary.evaluations(), List.of(), fallbackDetail);
    }

    /**
     * 计算触发日前 10 个交易日的市场最高板算术平均值，不包含触发日。
     * 情绪数据按交易日倒序排列；不足 10 日或任一高度缺失时返回 {@code null}，
     * 由弱 5 板策略按不执行处理。
     */
    static Double calculatePreviousTenDayAverageHeight(
            List<MarketEmotionData> descendingEmotions) {
        if (descendingEmotions == null
                || descendingEmotions.size() < WEAK_FIVE_BOARD_EMOTION_LOOKBACK) {
            return null;
        }
        int totalHeight = 0;
        for (int index = 1; index < WEAK_FIVE_BOARD_EMOTION_LOOKBACK; index++) {
            MarketEmotionData emotion = descendingEmotions.get(index);
            if (emotion == null || emotion.highestConsecutiveLimitUpDays() == null) {
                return null;
            }
            totalHeight += emotion.highestConsecutiveLimitUpDays();
        }
        return totalHeight / 10D;
    }

    private PlanEvaluation evaluatePlan(LocalDate tradeDate,
                                        List<StockDailyData> allDaily,
                                        Set<String> convertibleBondStockCodes,
                                        RelaySelectionPlan plan) {
        if (plan == null || plan.trigger() == RelaySelectionTrigger.NONE) {
            return new PlanEvaluation(List.of(), List.of());
        }
        Map<Integer, RelaySelectionStrength> strengthByBoard = new HashMap<>();
        for (RelayBoardPlan boardPlan : plan.boardPlans()) {
            strengthByBoard.put(boardPlan.board(), boardPlan.strength());
        }

        List<RelayCandidateEvaluation> evaluations = new ArrayList<>();
        List<SelectionTaskData> eligibleTasks = new ArrayList<>();
        Map<String, Double> currentPriceByStockCode = new HashMap<>();
        for (StockDailyData daily : allDaily) {
            RelaySelectionStrength strength = strengthByBoard.get(daily.getConsecutiveLimitUpDays());
            if (strength == null) {
                continue;
            }
            boolean hasConvertibleBond = convertibleBondStockCodes.contains(daily.getStockCode());
            RelaySelectionAssist assist;
            try {
                assist = buildSelectionAssist(daily);
            } catch (RuntimeException exception) {
                String detail = exception.getMessage() == null
                        ? exception.getClass().getSimpleName() : exception.getMessage();
                evaluations.add(new RelayCandidateEvaluation(plan.trigger(), strength,
                        daily, null, hasConvertibleBond, false, null, false, null,
                        "指标准备异常", detail));
                continue;
            }
            int selectionScore = RelaySelectionPolicy.calculateSelectionScore(
                    toSelectionCandidate(assist));
            BaseFilterDecision baseDecision = evaluateBaseFilters(
                    daily, hasConvertibleBond);
            if (!baseDecision.passed()) {
                evaluations.add(new RelayCandidateEvaluation(plan.trigger(), strength,
                        daily, assist, hasConvertibleBond, false, selectionScore, false, null,
                        baseDecision.layer(), baseDecision.detail()));
                continue;
            }
            RelaySelectionPolicy.Decision decision = RelaySelectionPolicy.evaluate(
                    toSelectionCandidate(assist), strength);
            if (!decision.passed()) {
                logSelectionFiltered(plan.trigger().name(), assist,
                        decision.layer(), decision.detail());
                evaluations.add(new RelayCandidateEvaluation(plan.trigger(), strength,
                        daily, assist, hasConvertibleBond, false, selectionScore, false, null,
                        decision.layer(), decision.detail()));
                continue;
            }
            SelectionTaskData task = buildWatchingTask(assist, selectionScore,
                    plan.trigger(), strength);
            eligibleTasks.add(task);
            currentPriceByStockCode.put(task.getStockCode(), assist.getCurrentPrice());
            evaluations.add(new RelayCandidateEvaluation(plan.trigger(), strength,
                    daily, assist, hasConvertibleBond, true, selectionScore, false, null,
                    decision.layer(), decision.detail()));
        }

        sortSelectionTasks(eligibleTasks, currentPriceByStockCode);
        List<SelectionTaskData> selectedTasks = takeTopTasks(
                plan.trigger().name(), eligibleTasks, plan.taskLimit());
        Map<String, Integer> selectedRankByCode = new HashMap<>();
        for (int i = 0; i < selectedTasks.size(); i++) {
            selectedRankByCode.put(selectedTasks.get(i).getStockCode(), i + 1);
        }
        List<RelayCandidateEvaluation> ranked = evaluations.stream()
                .map(evaluation -> {
                    Integer rank = selectedRankByCode.get(evaluation.daily().getStockCode());
                    return rank == null ? evaluation : evaluation.selectedAt(rank);
                })
                .toList();
        return new PlanEvaluation(ranked, selectedTasks);
    }

    private BaseFilterDecision evaluateBaseFilters(StockDailyData daily,
                                                   boolean hasConvertibleBond) {
        if (Boolean.TRUE.equals(daily.getIsSt())) {
            return BaseFilterDecision.rejected("ST过滤", "选股日为ST");
        }
        if (!lessThan(daily.getChangeRate(), 11D)) {
            return BaseFilterDecision.rejected("涨幅范围",
                    "actual=" + daily.getChangeRate() + ", required<11%");
        }
        if (!lessThan(daily.getClosePrice(), 40D)) {
            return BaseFilterDecision.rejected("当日价格",
                    "actual=" + daily.getClosePrice() + ", required<40元");
        }
        if (hasConvertibleBond) {
            return BaseFilterDecision.rejected("可转债", "当天存在有效可转债");
        }
        return BaseFilterDecision.allowed();
    }

    private record BaseFilterDecision(boolean passed, String layer, String detail) {
        private static BaseFilterDecision allowed() {
            return new BaseFilterDecision(true, null, null);
        }

        private static BaseFilterDecision rejected(String layer, String detail) {
            return new BaseFilterDecision(false, layer, detail);
        }
    }

    private List<StockDailyData> allDailyFor(LocalDate tradeDate) {
        return tradeDate == null ? List.of() : selectionDataService.listDailyByTradeDate(tradeDate);
    }

    private String findHighestLimitUp(List<StockDailyData> dailyList, Integer highestLimitUp) {
        return dailyList.stream()
                .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                .filter(daily -> Objects.equals(daily.getConsecutiveLimitUpDays(), highestLimitUp))
                .max(Comparator.comparing(StockDailyData::getChangeRate,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(StockDailyData::getStockCode)
                .orElse(null);
    }

    private record PlanEvaluation(List<RelayCandidateEvaluation> evaluations,
                                  List<SelectionTaskData> tasks) {
    }

    /** 将可变辅助对象压缩为核心策略只读候选。 */
    private RelaySelectionCandidate toSelectionCandidate(RelaySelectionAssist dto) {
        return new RelaySelectionCandidate(
                dto.getConsecutiveLimitUpDays(), dto.isTwoAcceleratedShrinkVolumeLimitUps(),
                dto.getProvince(), dto.getCurrentPrice(), dto.getStartMarketCap(),
                dto.getStartPrice(), dto.getCurrentTurnoverRate(), dto.getCurrentTurnover(),
                dto.getCurrentAmplitude(), dto.getNonStMonthCount(), dto.getListingMonthCount(),
                dto.getMaxTurnoverRate(), dto.getHighestConsecutiveLimitUpDays(),
                dto.getPriorNinetyDayHighestConsecutiveLimitUpDays(),
                dto.getPriorNinetyDayMaxTurnoverRate(), dto.getHistoricalMaxVolume(),
                dto.getMaxVolumeDayTurnoverRate(), dto.getMaxVolumeDayTurnover(),
                dto.getAbnormalKlineStateCount(), dto.getPriorTwentyDayAbnormalKlineStateCount(),
                dto.getFiveDayAmplitude(), dto.getTenDayChangeRate());
    }

    /**
     * 构建 5 板质量快照。只有恰好一只 5 板时才计算完整辅助对象，
     * 避免在数量条件已经失败时执行多余的历史日 K 查询。
     */
    private List<WeakFiveBoardFallbackPolicy.FiveBoardQuality> buildFiveBoardQualities(
            List<StockDailyData> fiveBoardDailyList) {
        if (fiveBoardDailyList == null || fiveBoardDailyList.isEmpty()) {
            return List.of();
        }
        if (fiveBoardDailyList.size() != 1) {
            return fiveBoardDailyList.stream()
                    .map(daily -> toFiveBoardQuality(daily, null))
                    .toList();
        }

        StockDailyData fiveBoardDaily = fiveBoardDailyList.get(0);
        RelaySelectionAssist fiveBoardAssist = buildSelectionAssist(fiveBoardDaily);
        return List.of(toFiveBoardQuality(fiveBoardDaily, fiveBoardAssist));
    }

    /**
     * 组合 5 板质量数据：当日市值、换手和振幅取当天日 K；
     * 启动价格仅作研究观察，仍取选股辅助对象中的本轮首板前一交易日收盘价。
     */
    static WeakFiveBoardFallbackPolicy.FiveBoardQuality toFiveBoardQuality(
            StockDailyData fiveBoardDaily,
            RelaySelectionAssist fiveBoardAssist) {
        return new WeakFiveBoardFallbackPolicy.FiveBoardQuality(
                fiveBoardDaily == null ? null : fiveBoardDaily.getStockCode(),
                fiveBoardDaily == null ? null : fiveBoardDaily.getFloatMarketCap(),
                fiveBoardDaily == null ? null : fiveBoardDaily.getTurnoverRate(),
                fiveBoardDaily == null ? null : fiveBoardDaily.getAmplitude(),
                fiveBoardAssist == null ? null : fiveBoardAssist.getStartPrice());
    }

    /** 按板数降序、同板分数降序、价格升序、代码升序稳定排序。 */
    static void sortSelectionTasks(List<SelectionTaskData> tasks,
                                   Map<String, Double> currentPriceByStockCode) {
        tasks.sort(Comparator
                .comparing(SelectionTaskData::getConsecutiveLimitUpDays,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(SelectionTaskData::getLimitUpScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(task -> currentPriceByStockCode.get(task.getStockCode()),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SelectionTaskData::getStockCode,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * 按分数顺序保留指定数量任务，并记录因数量上限被过滤的股票。
     */
    private List<SelectionTaskData> takeTopTasks(String selectionMode,
                                                 List<SelectionTaskData> sortedTasks,
                                                 int limit) {
        List<SelectionTaskData> selectedTasks = new ArrayList<>();
        for (int i = 0; i < sortedTasks.size(); i++) {
            SelectionTaskData task = sortedTasks.get(i);
            if (i >= limit) {
                log.info("{}选股过滤 tradeDate={} stockCode={} stockName={} step=数量上限 detail=rank={}, limit={}, score={}",
                        selectionMode, task.getRecommendDate(), task.getStockCode(), task.getStockName(),
                        i + 1, limit, task.getLimitUpScore());
                continue;
            }
            selectedTasks.add(task);
        }
        return selectedTasks;
    }

    /**
     * 记录辅助对象在选股流程中被过滤的具体步骤和指标值。
     */
    private void logSelectionFiltered(String selectionMode,
                                      RelaySelectionAssist dto,
                                      String step,
                                      String detail) {
        log.info("{}选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                selectionMode, dto.getTradeDate(), dto.getStockCode(), dto.getStockName(), step, detail);
    }

    /**
     * 查询指定日期当天有有效可转债的正股代码，供所有选股候选池排除。
     */
    private Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) {
        return selectionDataService.listConvertibleBondStockCodes(tradeDate);
    }

    /**
     * 根据选股辅助对象构建盯盘任务。
     */
    private SelectionTaskData buildWatchingTask(RelaySelectionAssist assist,
                                                Integer limitUpScore,
                                                RelaySelectionTrigger trigger,
                                                RelaySelectionStrength strength) {
        SelectionTaskData task = new SelectionTaskData();
        task.setStockCode(assist.getStockCode());
        task.setStockName(assist.getStockName());
        task.setLimitUpScore(limitUpScore);
        task.setConsecutiveLimitUpDays(assist.getConsecutiveLimitUpDays());
        task.setRecommendDate(assist.getTradeDate());
        task.setTradeDate(findNextTradeDate(assist.getTradeDate()));
        task.setTradeMode(TradeMode.RELAY_LIMIT_UP.code());
        task.setSelectionTrigger(trigger.name());
        task.setSelectionStrength(strength.name());
        task.setStrategyVersion(STRATEGY_VERSION);
        task.setCreatedTime(LocalDateTime.now());
        return task;
    }

    /**
     * 推荐日收盘后生成的任务，在下一交易日盯盘。
     */
    private LocalDate findNextTradeDate(LocalDate recommendDate) {
        return selectionDataService.findNextTradeDate(recommendDate);
    }

    /**
     * 填充单只股票的选股辅助字段。
     */
    private RelaySelectionAssist buildSelectionAssist(StockDailyData stockDaily) {
        List<StockDailyData> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyData> selectionWindowDailyList = listSelectionWindowDaily(stockDaily);
        List<StockDailyData> ascRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyData::getTradeDate))
                .toList();
        StockDailyData startDaily = findStartDaily(
                recentDailyList, stockDaily.getConsecutiveLimitUpDays());
        LocalDate chipHistoryEndDate = startDaily == null ? null : startDaily.getTradeDate();
        // 调用连板筹码历史查询方法。
        List<StockDailyData> chipHistoryDailyList =
                listChipHistoryDaily(stockDaily.getStockCode(), chipHistoryEndDate);
        // 调用连板最早日 K 查询方法。
        List<StockDailyData> earliestStoredDailyList =
                listEarliestStoredDaily(stockDaily.getStockCode(), chipHistoryEndDate);
        // 调用连板历史筹码指标计算方法。
        RelayHistoricalMetricsCalculator.HistoricalMetrics chipMetrics =
                RelayHistoricalMetricsCalculator.calculateHistoricalMetrics(
                        chipHistoryDailyList, earliestStoredDailyList, chipHistoryEndDate);

        RelaySelectionAssist assist = new RelaySelectionAssist();
        assist.setStockCode(stockDaily.getStockCode());
        assist.setStockName(stockDaily.getStockName());
        assist.setTradeDate(stockDaily.getTradeDate());
        assist.setConsecutiveLimitUpDays(stockDaily.getConsecutiveLimitUpDays());
        assist.setTwoAcceleratedShrinkVolumeLimitUps(hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(
                recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setProvince(findProvince(stockDaily.getStockCode()));
        assist.setCurrentPrice(stockDaily.getClosePrice());
        assist.setStartMarketCap(startDaily == null ? null : startDaily.getFloatMarketCap());
        assist.setStartPrice(startDaily == null ? null : startDaily.getClosePrice());
        assist.setCurrentTurnoverRate(stockDaily.getTurnoverRate());
        assist.setCurrentTurnover(stockDaily.getTurnover());
        assist.setCurrentAmplitude(stockDaily.getAmplitude());
        assist.setNonStMonthCount(calculateNonStMonthCount(stockDaily));
        assist.setListingMonthCount(calculateListingMonthCount(stockDaily));
        assist.setMaxTurnoverRate(chipMetrics.maxTurnoverRate());
        assist.setHistoricalMaxVolume(chipMetrics.maxVolume());
        assist.setMaxVolumeDayTurnoverRate(chipMetrics.maxVolumeDayTurnoverRate());
        assist.setMaxVolumeDayTurnover(chipMetrics.maxVolumeDayTurnover());
        assist.setHighestConsecutiveLimitUpDays(chipMetrics.twoHundredKlineHighestBoard());
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(chipMetrics.ninetyDayHighestBoard());
        assist.setPriorNinetyDayMaxTurnoverRate(chipMetrics.ninetyDayMaxTurnoverRate());
        assist.setAbnormalKlineStateCount(countAbnormalKlineState(selectionWindowDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setPriorTwentyDayAbnormalKlineStateCount(
                countPriorTwentyDayAbnormalKlineState(
                        recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setFiveDayAmplitude(calculateSelectionAdjustedAmplitude(
                ascRecentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setTenDayChangeRate(calculateAdjustedCloseChangeRate(ascRecentDailyList, 10));
        return assist;
    }

    /**
     * 查询当前交易日及之前最多 22 个交易日：最多跳过本轮 3 个连板日后，再统计前 20 个交易日。
     */
    private List<StockDailyData> listRecentDaily(StockDailyData stockDaily) {
        return selectionDataService.listLatestDaily(
                stockDaily.getStockCode(), stockDaily.getTradeDate(), 23);
    }

    /**
     * 查询选股日期往前 18 个自然月内的日 K，用于计算过滤和打分辅助指标。
     */
    private List<StockDailyData> listSelectionWindowDaily(StockDailyData stockDaily) {
        return selectionDataService.listDailyBetween(stockDaily.getStockCode(),
                stockDaily.getTradeDate().minusMonths(18), stockDaily.getTradeDate());
    }

    /**
     * 查询本轮首板前最近 200 根日 K，供独立筹码过滤器计算历史指标。
     * 其他异常状态、评分辅助指标仍使用原来的 18 个自然月窗口。
     */
    private List<StockDailyData> listChipHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) {
            return List.of();
        }
        return selectionDataService.listLatestDaily(stockCode, historyEndDate, 200);
    }

    /**
     * 查询数据库中该股票最早的 11 根日 K，用第 11 根确定历史筹码统计的首个有效交易日。
     * 这样只排除新股上市最早 10 根日 K，不会误删老股票最近 200 根筹码窗口的数据。
     */
    private List<StockDailyData> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) {
            return List.of();
        }
        return selectionDataService.listEarliestDaily(stockCode, historyEndDate, 11);
    }

    /**
     * 查询省份属性。
     */
    private String findProvince(String stockCode) {
        StockCurrentStatusData status = selectionDataService.findCurrentStatus(stockCode);
        return status == null ? null : status.regionName();
    }

    /**
     * 定位本轮首板前一交易日，用于读取启动市值和启动价格。
     * 日 K 按交易日倒序排列，下标等于当前连板数：1 板取下标 1，2 板取下标 2，以此类推。
     */
    private StockDailyData findStartDaily(List<StockDailyData> recentDailyList, Integer consecutiveLimitUpDays) {
        int startIndex = Math.max(1, Objects.requireNonNullElse(consecutiveLimitUpDays, 1));
        return recentDailyList.size() <= startIndex ? null : recentDailyList.get(startIndex);
    }

    /**
     * 按当前连板数回看本轮涨停日，判断是否至少有两根加速缩量板。
     *
     * <p>本轮首板只判断 {@code klineState == 3} 的一字板或振幅严格小于 3%，
     * 不使用换手率条件；第 2/3 板满足一字板、振幅严格小于 3%、换手率严格
     * 小于 15% 中的任意一项即命中。因此 2 板要求两根都命中，3 板要求三根中
     * 至少两根命中。</p>
     */
    static boolean hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(
            List<StockDailyData> recentDailyList,
            Integer consecutiveLimitUpDays) {
        if (recentDailyList == null) {
            return false;
        }
        int consecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        int count = 0;
        for (int i = 0; i < consecutiveDays && i < recentDailyList.size(); i++) {
            StockDailyData stockDailyEntity = recentDailyList.get(i);
            Double amplitude = stockDailyEntity.getAmplitude();
            Double turnoverRate = stockDailyEntity.getTurnoverRate();
            boolean firstBoardOfCurrentRun = i == consecutiveDays - 1;
            if (Objects.equals(stockDailyEntity.getKlineState(), 3)
                    || (amplitude != null && amplitude < 3D)
                    || (stockDailyEntity.getKlineState()== 2 && turnoverRate < 18D)//冀凯股份 2025-01-17
                    || (!firstBoardOfCurrentRun && turnoverRate != null && turnoverRate < 15D)) {
                if (++count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 计算上次 ST 后或新上市以来的自然月数。
     */
    private Integer calculateNonStMonthCount(StockDailyData stockDaily) {
        LocalDate lastStDate = selectionDataService.findLatestStDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        LocalDate firstTradeDate = selectionDataService.findFirstTradeDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        LocalDate startDate = lastStDate == null ? firstTradeDate : lastStDate.plusDays(1);
        if (startDate == null) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    /**
     * 计算股票从最早日 K 交易日到选股日期的自然月数。
     */
    private Integer calculateListingMonthCount(StockDailyData stockDaily) {
        LocalDate firstTradeDate = selectionDataService.findFirstTradeDate(
                stockDaily.getStockCode(), stockDaily.getTradeDate());
        if (firstTradeDate == null) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.MONTHS.between(
                firstTradeDate.withDayOfMonth(1),
                stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    /**
     * 统计选股窗口内非正常 K 线状态次数。
     */
    /**
     * 统计 18 个月窗口内的非正常状态次数，并减去本次连板数。
     */
    private Integer countAbnormalKlineState(List<StockDailyData> selectionWindowDailyList, Integer consecutiveLimitUpDays) {
        int currentConsecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        long abnormalCount = selectionWindowDailyList.stream()
                .map(StockDailyData::getKlineState)
                .filter(Objects::nonNull)
                .filter(klineState -> klineState != 0)
                .count();
        return Math.max(0, Math.toIntExact(abnormalCount) - currentConsecutiveDays);
    }

    /**
     * 统计本轮连续涨停开始前 20 个交易日中 klineState != 0 的日 K 数量。
     * recentDailyList 按交易日倒序排列，先按当前连板数跳过本轮涨停日，再读取之前 20 根日 K。
     */
    static int countPriorTwentyDayAbnormalKlineState(
            List<StockDailyData> recentDailyList, Integer consecutiveLimitUpDays) {
        int currentConsecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        return Math.toIntExact(recentDailyList.stream()
                .skip(currentConsecutiveDays)
                .limit(20)
                .map(StockDailyData::getKlineState)
                .filter(Objects::nonNull)
                .filter(klineState -> klineState != 0)
                .count());
    }

    /**
     * 前 20 个交易日非正常 K 线少于 4 次才允许进入后续筛选。
     */
    static boolean isRecentAbnormalKlineCountAllowed(Integer abnormalCount) {
        return Objects.requireNonNullElse(abnormalCount, 0) < 4;
    }

    /**
     * 使用复权收盘价计算 N 日涨跌幅。
     */
    private Double calculateAdjustedCloseChangeRate(List<StockDailyData> ascRecentDailyList, int days) {
        if (ascRecentDailyList.size() <= days) {
            return 0.0;
        }
        Double basePrice = ascRecentDailyList.get(ascRecentDailyList.size() - 1 - days).getAdjustClosePrice();
        Double currentPrice = ascRecentDailyList.get(ascRecentDailyList.size() - 1).getAdjustClosePrice();
        if (basePrice == null || currentPrice == null || basePrice == 0) {
            return 0.0;
        }
        return (currentPrice - basePrice) * 100 / basePrice;
    }

    /**
     * 计算包含当日在内的选股振幅：首板使用 3 个交易日，连板使用 5 个交易日。
     * 公式为（当日复权收盘价 - 窗口最低复权价）/ 窗口最低复权价。
     */
    static Double calculateSelectionAdjustedAmplitude(
            List<StockDailyData> ascRecentDailyList, Integer consecutiveLimitUpDays) {
        int windowDays = Objects.equals(consecutiveLimitUpDays, 1) ? 3 : 5;
        if (ascRecentDailyList.size() < windowDays) {
            return 0.0;
        }
        int currentIndex = ascRecentDailyList.size() - 1;
        Double currentClosePrice = ascRecentDailyList.get(currentIndex).getAdjustClosePrice();
        Double lowestPrice = ascRecentDailyList.subList(
                        ascRecentDailyList.size() - windowDays, ascRecentDailyList.size())
                .stream()
                .map(StockDailyData::getAdjustLowPrice)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);
        if (currentClosePrice == null || lowestPrice == null || lowestPrice <= 0) {
            return 0.0;
        }
        return (currentClosePrice - lowestPrice) * 100 / lowestPrice;
    }

    /** 空值不通过的严格小于比较。 */
    private static boolean lessThan(Double value, double upperBound) {
        return value != null && value < upperBound;
    }

}
