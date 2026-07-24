package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.service.RelaySelectionResearchService;
import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.strategy.relay.selection.RelayCandidateEvaluation;
import com.compoundwonder.strategy.relay.selection.RelaySelectionAssist;
import com.compoundwonder.strategy.relay.selection.RelaySelectionPlan;
import com.compoundwonder.strategy.relay.selection.RelaySelectionResult;
import com.compoundwonder.strategy.relay.selection.RelaySelectionService;
import com.compoundwonder.strategy.relay.selection.RelaySelectionTrigger;
import com.compoundwonder.trader.entity.RelaySelectionCandidateRecord;
import com.compoundwonder.trader.entity.RelaySelectionRun;
import com.compoundwonder.trader.entity.RelaySelectionTriggerRecord;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** 执行连板全触发、全板内原始候选的日 K 理论结果研究。 */
@Slf4j
@Service
public class RelaySelectionResearchServiceImpl implements RelaySelectionResearchService {
    private final RelaySelectionService relaySelectionService;
    private final StockSelectionDataService dataService;
    private final RelaySelectionResearchPersistenceService persistenceService;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    private final StockWatchingTaskService stockWatchingTaskService;

    public RelaySelectionResearchServiceImpl(
            RelaySelectionService relaySelectionService,
            StockSelectionDataService dataService,
            RelaySelectionResearchPersistenceService persistenceService,
            ObjectMapper objectMapper,
            @Qualifier("historicalBacktestExecutor") Executor executor,
            StockWatchingTaskService stockWatchingTaskService) {
        this.relaySelectionService = relaySelectionService;
        this.dataService = dataService;
        this.persistenceService = persistenceService;
        this.objectMapper = objectMapper;
        this.executor = executor;
        this.stockWatchingTaskService = stockWatchingTaskService;
    }

    @Override
    public RelaySelectionRun startRange(LocalDate startDate, LocalDate endDate) {
        validate(startDate, endDate);
        RelaySelectionRun run = createRun(startDate, endDate);
        try {
            executor.execute(() -> executeRun(run));
            return run;
        } catch (RejectedExecutionException exception) {
            persistenceService.fail(run.getId(), exception);
            throw new IllegalStateException("连板研究任务队列已满，请稍后重试", exception);
        }
    }

    @Override
    public RelaySelectionRun runRange(LocalDate startDate, LocalDate endDate) {
        validate(startDate, endDate);
        RelaySelectionRun run = createRun(startDate, endDate);
        executeRun(run);
        return findRun(run.getId());
    }

    @Override
    public RelaySelectionRun findRun(long runId) {
        RelaySelectionRun run = persistenceService.findRun(runId);
        if (run == null) throw new IllegalArgumentException("连板研究任务不存在: " + runId);
        return run;
    }

    @Override
    public List<RelaySelectionRun> findRecentRuns(int limit) {
        return persistenceService.findRecentRuns(limit);
    }

    @Override
    public List<RelaySelectionTriggerRecord> findTriggers(long runId) {
        findRun(runId);
        return persistenceService.findTriggers(runId);
    }

    @Override
    public List<RelaySelectionCandidateRecord> findCandidates(long runId, Long triggerRecordId,
                                                              int page, int pageSize) {
        findRun(runId);
        return persistenceService.findCandidates(runId, triggerRecordId, page, pageSize);
    }

    private RelaySelectionRun createRun(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("strategyVersion", RelaySelectionService.STRATEGY_VERSION);
        parameters.put("selectionRange", List.of(startDate, endDate));
        parameters.put("buyCost", "D+1实际触板最高价");
        parameters.put("exit", "首次收盘不涨停日最高价");
        parameters.put("taskLimit", 3);
        parameters.put("watchingTaskCopy", true);
        return persistenceService.createRun(startDate, endDate,
                RelaySelectionService.STRATEGY_VERSION, json(parameters));
    }

    private synchronized void executeRun(RelaySelectionRun run) {
        RunAccumulator accumulator = new RunAccumulator();
        try {
            List<LocalDate> tradeDates = dataService.listTradeDates(
                    run.getStartDate(), run.getEndDate());
            if (tradeDates.isEmpty()) {
                throw new IllegalArgumentException("研究区间没有交易日: "
                        + run.getStartDate() + " 至 " + run.getEndDate());
            }
            for (LocalDate recommendDate : tradeDates) {
                RelaySelectionResult result = relaySelectionService.selectDetailed(recommendDate);
                if (result.executedPlan().trigger() != RelaySelectionTrigger.NONE) {
                    processTrigger(run, result, accumulator);
                } else {
                    stockWatchingTaskService.replaceRelaySelectionTasks(
                            recommendDate, List.of());
                }
                run.setLastCompletedDate(recommendDate);
                applyProgress(run, accumulator);
                persistenceService.updateProgress(run);
            }
            applyFinalMetrics(run, accumulator);
            persistenceService.complete(run);
        } catch (RuntimeException exception) {
            persistenceService.fail(run.getId(), exception);
            log.error("连板研究任务失败 runId={}, range={}-{}",
                    run.getId(), run.getStartDate(), run.getEndDate(), exception);
        }
    }

    private void processTrigger(RelaySelectionRun run,
                                RelaySelectionResult result,
                                RunAccumulator accumulator) {
        List<MarketEmotionData> emotions = dataService.listLatestMarketEmotion(
                result.selectionDate(), 3);
        RelaySelectionTriggerRecord trigger = buildTriggerRecord(run, result, emotions);
        persistenceService.insertTrigger(trigger);

        RankIndex ranks = buildRanks(result.candidateEvaluations());
        List<RelaySelectionCandidateRecord> records = new ArrayList<>();
        for (RelayCandidateEvaluation evaluation : result.candidateEvaluations()) {
            RelaySelectionCandidateRecord record = toCandidateRecord(
                    run, trigger, evaluation, ranks, result.selectionDate());
            persistenceService.insertCandidate(record);
            records.add(record);
        }
        completeTriggerMetrics(trigger, records, accumulator);
        persistenceService.updateTrigger(trigger);
        stockWatchingTaskService.replaceRelaySelectionTasks(
                result.selectionDate(), attachResearchReferences(
                        run.getId(), result.tasks(), records));
        accumulator.triggerCount++;
        if (Boolean.TRUE.equals(trigger.getIsEmptyPosition())) accumulator.emptyPositionCount++;
        log.info("完成连板触发研究 runId={}, date={}, trigger={}, raw={}, eligible={}, selected={}",
                run.getId(), result.selectionDate(), trigger.getEffectiveTriggerType(),
                trigger.getRawCandidateCount(), trigger.getEligibleCandidateCount(),
                trigger.getSelectedCandidateCount());
    }

    /**
     * 将研究运行和已落库候选记录主键回填到最终 Top3 任务，供 task 表追溯来源。
     */
    static List<SelectionTaskData> attachResearchReferences(
            Long runId,
            List<SelectionTaskData> selectedTasks,
            List<RelaySelectionCandidateRecord> candidateRecords) {
        List<SelectionTaskData> safeTasks = selectedTasks == null
                ? List.of() : selectedTasks;
        Map<String, Long> selectedCandidateIds = new HashMap<>();
        if (candidateRecords != null) {
            for (RelaySelectionCandidateRecord record : candidateRecords) {
                if (record != null && Boolean.TRUE.equals(record.getIsSelected())) {
                    selectedCandidateIds.put(record.getStockCode(), record.getId());
                }
            }
        }
        for (SelectionTaskData task : safeTasks) {
            Long candidateRecordId = selectedCandidateIds.get(task.getStockCode());
            if (candidateRecordId == null) {
                throw new IllegalStateException(
                        "最终连板任务缺少候选审计记录: " + task.getStockCode());
            }
            task.setSelectionRunId(runId);
            task.setRelayCandidateRecordId(candidateRecordId);
        }
        return List.copyOf(safeTasks);
    }

    private RelaySelectionTriggerRecord buildTriggerRecord(
            RelaySelectionRun run,
            RelaySelectionResult result,
            List<MarketEmotionData> emotions) {
        RelaySelectionTriggerRecord trigger = new RelaySelectionTriggerRecord();
        trigger.setRunId(run.getId());
        trigger.setRecommendDate(result.selectionDate());
        trigger.setTradeDate(dataService.findNextTradeDate(result.selectionDate()));
        trigger.setMainTriggerType(triggerName(result.primaryPlan()));
        trigger.setEffectiveTriggerType(triggerName(result.executedPlan()));
        if (!emotions.isEmpty()) {
            trigger.setTodayHighestBoard(emotions.get(0).highestConsecutiveLimitUpDays());
            trigger.setCurrentDominantStockCode(emotions.get(0).dominantCycleStockCode());
        }
        if (emotions.size() > 1) trigger.setYesterdayHighestBoard(
                emotions.get(1).highestConsecutiveLimitUpDays());
        if (emotions.size() > 2) trigger.setDayBeforeHighestBoard(
                emotions.get(2).highestConsecutiveLimitUpDays());
        populateReference(trigger, result, emotions);
        trigger.setSelectionPlan(json(result.executedPlan()));
        trigger.setWeakFiveReason(result.fallbackDetail() == null
                ? null : json(Map.of("detail", result.fallbackDetail())));
        trigger.setRawCandidateCount(result.candidateEvaluations().size());
        trigger.setEligibleCandidateCount((int) result.candidateEvaluations().stream()
                .filter(RelayCandidateEvaluation::eligible).count());
        trigger.setSelectedCandidateCount(result.tasks().size());
        trigger.setIsEmptyPosition(result.tasks().isEmpty());
        trigger.setTouchedLimitUpCount(0);
        trigger.setSealedLimitUpCount(0);
        trigger.setBrokenLimitUpCount(0);
        trigger.setTheoreticalWinCount(0);
        trigger.setCreatedTime(LocalDateTime.now());
        return trigger;
    }

    private void populateReference(RelaySelectionTriggerRecord trigger,
                                   RelaySelectionResult result,
                                   List<MarketEmotionData> emotions) {
        RelaySelectionTrigger main = result.primaryPlan().trigger();
        int referenceIndex = main == RelaySelectionTrigger.HIGH_TO_LOW_BREAK ? 1
                : main == RelaySelectionTrigger.HIGH_TO_LOW_SECOND ? 2 : -1;
        if (referenceIndex >= 0 && emotions.size() > referenceIndex) {
            MarketEmotionData referenceEmotion = emotions.get(referenceIndex);
            StockDailyData referenceDaily = findHighestDaily(referenceEmotion);
            trigger.setReferenceDate(referenceEmotion.tradeDate());
            trigger.setReferenceBoard(referenceEmotion.highestConsecutiveLimitUpDays());
            trigger.setReferenceDominantStockCode(referenceEmotion.dominantCycleStockCode());
            if (referenceDaily != null) {
                trigger.setReferenceStockCode(referenceDaily.getStockCode());
                trigger.setReferenceStockName(referenceDaily.getStockName());
            }
        }
        if (result.executedPlan().trigger() == RelaySelectionTrigger.WEAK_FIVE_CARD) {
            StockDailyData weakFive = dataService.listDailyByTradeDate(result.selectionDate()).stream()
                    .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                    .filter(daily -> Integer.valueOf(5).equals(daily.getConsecutiveLimitUpDays()))
                    .findFirst().orElse(null);
            if (weakFive != null) {
                trigger.setWeakFiveStockCode(weakFive.getStockCode());
                trigger.setWeakFiveStockName(weakFive.getStockName());
            }
        }
    }

    private RelaySelectionCandidateRecord toCandidateRecord(
            RelaySelectionRun run,
            RelaySelectionTriggerRecord trigger,
            RelayCandidateEvaluation evaluation,
            RankIndex ranks,
            LocalDate recommendDate) {
        StockDailyData daily = evaluation.daily();
        RelaySelectionAssist assist = evaluation.assist();
        RelaySelectionCandidateRecord record = new RelaySelectionCandidateRecord();
        record.setRunId(run.getId());
        record.setTriggerRecordId(trigger.getId());
        record.setRecommendDate(recommendDate);
        record.setTradeDate(trigger.getTradeDate());
        record.setStockCode(daily.getStockCode());
        record.setStockName(daily.getStockName());
        record.setCandidateBoard(daily.getConsecutiveLimitUpDays());
        record.setSelectionStrength(evaluation.strength().name());
        record.setIsSt(Boolean.TRUE.equals(daily.getIsSt()));
        record.setHasConvertibleBond(evaluation.hasConvertibleBond());
        record.setProvince(assist == null ? null : assist.getProvince());
        record.setChangeRate(decimal(daily.getChangeRate()));
        record.setFloatMarketCap(decimal(daily.getFloatMarketCap()));
        record.setCurrentPrice(decimal(daily.getClosePrice()));
        copyAssist(record, assist);
        record.setDecisionStatus(evaluation.assist() == null ? 0
                : evaluation.eligible() ? (evaluation.selected() ? 3 : 2) : 1);
        record.setFilterPassed(evaluation.eligible());
        record.setFirstRejectStage(evaluation.eligible() ? null : evaluation.decisionLayer());
        record.setFirstRejectReason(evaluation.eligible() ? null
                : abbreviate(evaluation.decisionDetail(), 1000));
        record.setDecisionTrace(json(Map.of(
                "trigger", evaluation.trigger().name(),
                "layer", Objects.toString(evaluation.decisionLayer(), ""),
                "detail", Objects.toString(evaluation.decisionDetail(), ""))));
        record.setScoreDetail(evaluation.score() == null ? null
                : json(Map.of("selectionScore", evaluation.score())));
        record.setSelectionScore(evaluation.score());
        record.setBoardRank(ranks.boardRankByCode().get(daily.getStockCode()));
        record.setFinalRank(ranks.finalRankByCode().get(daily.getStockCode()));
        record.setIsSelected(evaluation.selected());
        RelayTheoreticalOutcome outcome = calculateOutcome(run, evaluation, trigger.getTradeDate());
        copyOutcome(record, outcome);
        record.setIsDailyTheoreticalBest(false);
        record.setCreatedTime(LocalDateTime.now());
        return record;
    }

    private RelayTheoreticalOutcome calculateOutcome(RelaySelectionRun run,
                                                     RelayCandidateEvaluation evaluation,
                                                     LocalDate tradeDate) {
        if (tradeDate == null || tradeDate.isAfter(run.getEndDate())) {
            return RelayTheoreticalOutcomeCalculator.evaluate(
                    evaluation.daily().getStockCode(),
                    Boolean.TRUE.equals(evaluation.daily().getIsSt()), tradeDate, List.of());
        }
        List<StockDailyData> afterSelection = dataService.listDailyBetween(
                evaluation.daily().getStockCode(), tradeDate, run.getEndDate());
        return RelayTheoreticalOutcomeCalculator.evaluate(
                evaluation.daily().getStockCode(),
                Boolean.TRUE.equals(evaluation.daily().getIsSt()), tradeDate, afterSelection);
    }

    private void copyAssist(RelaySelectionCandidateRecord record, RelaySelectionAssist assist) {
        if (assist == null) return;
        record.setStartMarketCap(decimal(assist.getStartMarketCap()));
        record.setStartPrice(decimal(assist.getStartPrice()));
        record.setCurrentTurnoverRate(decimal(assist.getCurrentTurnoverRate()));
        record.setCurrentTurnover(decimal(assist.getCurrentTurnover()));
        record.setCurrentAmplitude(decimal(assist.getCurrentAmplitude()));
        record.setNonStMonthCount(assist.getNonStMonthCount());
        record.setListingMonthCount(assist.getListingMonthCount());
        record.setMaxTurnoverRate(decimal(assist.getMaxTurnoverRate()));
        record.setHistoricalHighestBoard(assist.getHighestConsecutiveLimitUpDays());
        record.setPriorNinetyDayHighestBoard(
                assist.getPriorNinetyDayHighestConsecutiveLimitUpDays());
        record.setPriorNinetyDayMaxTurnoverRate(
                decimal(assist.getPriorNinetyDayMaxTurnoverRate()));
        record.setHistoricalMaxVolume(assist.getHistoricalMaxVolume());
        record.setMaxVolumeDayTurnoverRate(decimal(assist.getMaxVolumeDayTurnoverRate()));
        record.setMaxVolumeDayTurnover(decimal(assist.getMaxVolumeDayTurnover()));
        record.setTwoAcceleratedShrinkVolumeLimitUps(
                assist.isTwoAcceleratedShrinkVolumeLimitUps());
        record.setAbnormalKlineStateCount(assist.getAbnormalKlineStateCount());
        record.setPriorTwentyDayAbnormalKlineCount(
                assist.getPriorTwentyDayAbnormalKlineStateCount());
        record.setFiveDayAmplitude(decimal(assist.getFiveDayAmplitude()));
        record.setTenDayChangeRate(decimal(assist.getTenDayChangeRate()));
    }

    private void copyOutcome(RelaySelectionCandidateRecord record,
                             RelayTheoreticalOutcome outcome) {
        record.setOutcomeStatus(outcome.status());
        record.setBuyLimitPrice(outcome.buyLimitPrice());
        StockDailyData buy = outcome.buyDaily();
        if (buy != null) {
            record.setBuyDayHighPrice(decimal(buy.getHighPrice()));
            record.setBuyDayClosePrice(decimal(buy.getClosePrice()));
            record.setBuyDayKlineState(buy.getKlineState());
        }
        record.setIsTouchedLimitUp(outcome.touchedLimitUp());
        record.setIsSealedLimitUp(outcome.sealedLimitUp());
        record.setPostSelectionSealedDays(outcome.postSelectionSealedDays());
        record.setBreakDate(outcome.breakDate());
        StockDailyData broken = outcome.breakDaily();
        if (broken != null) {
            record.setBreakDayOpenPrice(decimal(broken.getOpenPrice()));
            record.setBreakDayHighPrice(decimal(broken.getHighPrice()));
            record.setBreakDayLowPrice(decimal(broken.getLowPrice()));
            record.setBreakDayClosePrice(decimal(broken.getClosePrice()));
            record.setBreakDayKlineState(broken.getKlineState());
        }
        record.setBreakDayLimitPrice(outcome.breakDayLimitPrice());
        record.setIsBreakDayTouchedLimitUp(outcome.breakDayTouchedLimitUp());
        record.setTheoreticalMaxSellPrice(outcome.theoreticalMaxSellPrice());
        record.setTheoreticalMaxReturnRate(outcome.theoreticalMaxReturnRate());
        record.setIsTheoreticalWin(outcome.theoreticalWin());
        record.setOutcomeUpdatedTime(LocalDateTime.now());
    }

    private void completeTriggerMetrics(RelaySelectionTriggerRecord trigger,
                                        List<RelaySelectionCandidateRecord> records,
                                        RunAccumulator accumulator) {
        List<RelaySelectionCandidateRecord> completedTouched = records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getIsTouchedLimitUp()))
                .filter(record -> record.getTheoreticalMaxReturnRate() != null)
                .toList();
        RelaySelectionCandidateRecord dailyBest = completedTouched.stream()
                .max(candidateReturnComparator()).orElse(null);
        RelaySelectionCandidateRecord selectedBest = completedTouched.stream()
                .filter(record -> Boolean.TRUE.equals(record.getIsSelected()))
                .max(candidateReturnComparator()).orElse(null);
        if (dailyBest != null) {
            dailyBest.setIsDailyTheoreticalBest(true);
            persistenceService.updateCandidate(dailyBest);
            trigger.setDailyBestCandidateRecordId(dailyBest.getId());
            trigger.setDailyBestStockCode(dailyBest.getStockCode());
            trigger.setDailyBestStockName(dailyBest.getStockName());
            trigger.setDailyBestReturnRate(dailyBest.getTheoreticalMaxReturnRate());
            accumulator.dailyOpportunityCount++;
            accumulator.dailyBestReturns.add(dailyBest.getTheoreticalMaxReturnRate());
        }
        if (selectedBest != null) {
            trigger.setSelectedBestCandidateRecordId(selectedBest.getId());
            trigger.setSelectedBestStockCode(selectedBest.getStockCode());
            trigger.setSelectedBestReturnRate(selectedBest.getTheoreticalMaxReturnRate());
        }
        if (dailyBest != null) {
            boolean captured = isDailyBestCaptured(
                    dailyBest.getTheoreticalMaxReturnRate(),
                    selectedBest == null ? null : selectedBest.getTheoreticalMaxReturnRate());
            trigger.setIsDailyBestCaptured(captured);
            if (captured) accumulator.dailyBestCapturedCount++;
            trigger.setTheoreticalReturnCaptureRate(captureRate(
                    dailyBest.getTheoreticalMaxReturnRate(),
                    selectedBest == null ? null : selectedBest.getTheoreticalMaxReturnRate()));
        }

        int touched = (int) records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getIsTouchedLimitUp())).count();
        int sealed = (int) records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getIsSealedLimitUp())).count();
        int broken = (int) records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getIsTouchedLimitUp()))
                .filter(record -> !Boolean.TRUE.equals(record.getIsSealedLimitUp())).count();
        int wins = (int) records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getIsTheoreticalWin())).count();
        trigger.setTouchedLimitUpCount(touched);
        trigger.setSealedLimitUpCount(sealed);
        trigger.setBrokenLimitUpCount(broken);
        trigger.setTheoreticalWinCount(wins);

        accumulator.rawCandidateCount += records.size();
        accumulator.eligibleCandidateCount += trigger.getEligibleCandidateCount();
        accumulator.selectedCandidateCount += trigger.getSelectedCandidateCount();
        accumulator.touchedCount += touched;
        accumulator.sealedCount += sealed;
        accumulator.brokenCount += broken;
        accumulator.winCount += wins;
        for (RelaySelectionCandidateRecord record : completedTouched) {
            accumulator.completedReturns.add(record.getTheoreticalMaxReturnRate());
        }
        if (dailyBest != null) {
            accumulator.dailyBestReturnSum = accumulator.dailyBestReturnSum
                    .add(dailyBest.getTheoreticalMaxReturnRate());
            if (selectedBest != null) {
                accumulator.selectedBestReturnSum = accumulator.selectedBestReturnSum
                        .add(selectedBest.getTheoreticalMaxReturnRate());
            }
        }
    }

    private void applyProgress(RelaySelectionRun run, RunAccumulator accumulator) {
        run.setTriggerCount(accumulator.triggerCount);
        run.setEmptyPositionCount(accumulator.emptyPositionCount);
        run.setRawCandidateCount(accumulator.rawCandidateCount);
        run.setEligibleCandidateCount(accumulator.eligibleCandidateCount);
        run.setSelectedCandidateCount(accumulator.selectedCandidateCount);
        run.setTouchedLimitUpCount(accumulator.touchedCount);
        run.setSealedLimitUpCount(accumulator.sealedCount);
        run.setBrokenLimitUpCount(accumulator.brokenCount);
        run.setTheoreticalWinCount(accumulator.winCount);
        run.setDailyOpportunityCount(accumulator.dailyOpportunityCount);
        run.setDailyBestCapturedCount(accumulator.dailyBestCapturedCount);
    }

    private void applyFinalMetrics(RelaySelectionRun run, RunAccumulator accumulator) {
        applyProgress(run, accumulator);
        run.setTouchRate(rate(accumulator.touchedCount, accumulator.rawCandidateCount));
        run.setSealRate(rate(accumulator.sealedCount, accumulator.touchedCount));
        run.setBreakRate(rate(accumulator.brokenCount, accumulator.touchedCount));
        run.setTheoreticalWinRate(rate(accumulator.winCount,
                accumulator.completedReturns.size()));
        run.setDailyBestCaptureRate(rate(accumulator.dailyBestCapturedCount,
                accumulator.dailyOpportunityCount));
        run.setTheoreticalReturnCaptureRate(aggregateCaptureRate(
                accumulator.dailyOpportunityCount, accumulator.dailyBestCapturedCount,
                accumulator.dailyBestReturnSum, accumulator.selectedBestReturnSum));
        run.setAverageTheoreticalMaxReturnRate(average(accumulator.completedReturns));
        run.setMedianTheoreticalMaxReturnRate(median(accumulator.completedReturns));
        run.setAverageDailyBestReturnRate(average(accumulator.dailyBestReturns));
        run.setMetricDetail(json(Map.of(
                "sealRateDenominator", "D+1触板候选",
                "winRateDenominator", "D+1触板且已出现首次收盘断板的候选",
                "pendingOutcomeExcluded", true,
                "forcedMinimumSelection", false)));
    }

    private RankIndex buildRanks(List<RelayCandidateEvaluation> evaluations) {
        Comparator<RelayCandidateEvaluation> comparator = Comparator
                .comparing((RelayCandidateEvaluation e) -> e.daily().getConsecutiveLimitUpDays(),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RelayCandidateEvaluation::score,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(e -> e.assist() == null ? null : e.assist().getCurrentPrice(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.daily().getStockCode(),
                        Comparator.nullsLast(Comparator.naturalOrder()));
        List<RelayCandidateEvaluation> eligible = evaluations.stream()
                .filter(RelayCandidateEvaluation::eligible).sorted(comparator).toList();
        Map<String, Integer> finalRanks = new HashMap<>();
        Map<String, Integer> boardRanks = new HashMap<>();
        Map<Integer, Integer> countsByBoard = new HashMap<>();
        for (int i = 0; i < eligible.size(); i++) {
            RelayCandidateEvaluation evaluation = eligible.get(i);
            String code = evaluation.daily().getStockCode();
            int board = Objects.requireNonNullElse(
                    evaluation.daily().getConsecutiveLimitUpDays(), 0);
            finalRanks.put(code, i + 1);
            boardRanks.put(code, countsByBoard.merge(board, 1, Integer::sum));
        }
        return new RankIndex(boardRanks, finalRanks);
    }

    private StockDailyData findHighestDaily(MarketEmotionData emotion) {
        return dataService.listDailyByTradeDate(emotion.tradeDate()).stream()
                .filter(daily -> !Boolean.TRUE.equals(daily.getIsSt()))
                .filter(daily -> Objects.equals(daily.getConsecutiveLimitUpDays(),
                        emotion.highestConsecutiveLimitUpDays()))
                .max(Comparator.comparing(StockDailyData::getChangeRate,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    private Comparator<RelaySelectionCandidateRecord> candidateReturnComparator() {
        return Comparator.comparing(RelaySelectionCandidateRecord::getTheoreticalMaxReturnRate)
                .thenComparing(RelaySelectionCandidateRecord::getStockCode,
                        Comparator.reverseOrder());
    }

    static boolean isDailyBestCaptured(BigDecimal dailyBest, BigDecimal selectedBest) {
        return dailyBest != null && selectedBest != null
                && selectedBest.compareTo(dailyBest) == 0;
    }

    static BigDecimal aggregateCaptureRate(int opportunityCount,
                                           int capturedCount,
                                           BigDecimal dailyBestReturnSum,
                                           BigDecimal selectedBestReturnSum) {
        if (opportunityCount == 0) return null;
        if (dailyBestReturnSum == null || dailyBestReturnSum.signum() == 0) {
            return rate(capturedCount, opportunityCount);
        }
        return captureRate(dailyBestReturnSum, selectedBestReturnSum);
    }

    private static BigDecimal rate(int numerator, int denominator) {
        if (denominator == 0) return null;
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 8, RoundingMode.HALF_UP);
    }

    private static BigDecimal captureRate(BigDecimal dailyBest, BigDecimal selectedBest) {
        if (dailyBest == null) return null;
        if (dailyBest.signum() == 0) {
            return selectedBest == null ? BigDecimal.ZERO : BigDecimal.ONE;
        }
        if (selectedBest == null) return BigDecimal.ZERO;
        return selectedBest.divide(dailyBest, 8, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal median(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        List<BigDecimal> sorted = values.stream().sorted().toList();
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) return sorted.get(middle).setScale(8, RoundingMode.HALF_UP);
        return sorted.get(middle - 1).add(sorted.get(middle))
                .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
    }

    private String triggerName(RelaySelectionPlan plan) {
        return plan == null || plan.trigger() == RelaySelectionTrigger.NONE
                ? null : plan.trigger().name();
    }

    private BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException("连板研究JSON序列化失败", exception);
        }
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }

    private void validate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    private record RankIndex(Map<String, Integer> boardRankByCode,
                             Map<String, Integer> finalRankByCode) {
    }

    private static final class RunAccumulator {
        private int triggerCount;
        private int emptyPositionCount;
        private int rawCandidateCount;
        private int eligibleCandidateCount;
        private int selectedCandidateCount;
        private int touchedCount;
        private int sealedCount;
        private int brokenCount;
        private int winCount;
        private int dailyOpportunityCount;
        private int dailyBestCapturedCount;
        private BigDecimal dailyBestReturnSum = BigDecimal.ZERO;
        private BigDecimal selectedBestReturnSum = BigDecimal.ZERO;
        private final List<BigDecimal> completedReturns = new ArrayList<>();
        private final List<BigDecimal> dailyBestReturns = new ArrayList<>();
    }
}
