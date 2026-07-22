package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.orderbook.data.BacktestDailyTickBatch;
import com.compoundwonder.backtest.service.SingleModeBacktestService;
import com.compoundwonder.backtest.service.model.SingleModeBacktestSummary;
import com.compoundwonder.backtest.service.model.SingleModeBoardStat;
import com.compoundwonder.backtest.service.model.SingleModeSamplePage;
import com.compoundwonder.common.strategy.selection.StockSelectionService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.common.strategy.trade.TradeExecutionProfile;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.entity.StockTradeCalendar;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.strategy.sell.BreakBoardNextOpenSellPolicy;
import com.compoundwonder.trader.entity.SingleModeBacktestRun;
import com.compoundwonder.trader.entity.SingleModeBacktestSample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** Model 1/2/3 全候选、独立持仓的回测执行器。 */
@Slf4j
@Service
public class SingleModeBacktestServiceImpl implements SingleModeBacktestService {
    static final String STRATEGY_VERSION = "multi-model-009";
    static final String RELAY_STRATEGY_VERSION = "relay-model-006";
    static final String DEFERRED_RELAY_TRIGGER = "DEFERRED_3_TO_4";
    private static final double RELAY_HIGH_OPEN_FAST_MIN_EXCLUSIVE = 4.5D;
    private static final int SELECTED = 1;
    private static final int NO_BUY = 2;
    private static final int OPEN = 3;
    private static final int CLOSED = 4;
    private static final int DATA_ERROR = 5;
    private static final int OVERNIGHT_BUY_RULE_CODE = 1;
    private static final int VIRTUAL_BUY_RULE_CODE = 0;

    private final StockSelectionService selectionService;
    private final StockTradeCalendarService calendarService;
    private final StockDailyService stockDailyService;
    private final BackTestTradeService replayService;
    private final SingleModeBacktestPersistenceService persistenceService;
    private final Executor executor;

    public SingleModeBacktestServiceImpl(StockSelectionService selectionService,
                                         StockTradeCalendarService calendarService,
                                         StockDailyService stockDailyService,
                                         BackTestTradeService replayService,
                                         SingleModeBacktestPersistenceService persistenceService,
                                         @Qualifier("historicalBacktestExecutor") Executor executor) {
        this.selectionService = selectionService;
        this.calendarService = calendarService;
        this.stockDailyService = stockDailyService;
        this.replayService = replayService;
        this.persistenceService = persistenceService;
        this.executor = executor;
    }

    @Override
    public SingleModeBacktestRun startRange(LocalDate startDate, LocalDate endDate, int tradeMode) {
        validate(startDate, endDate, tradeMode);
        SingleModeBacktestRun run = persistenceService.createRun(
                startDate, endDate, tradeMode, null, strategyVersion(tradeMode));
        try {
            executor.execute(() -> executeRun(run));
            return run;
        } catch (RejectedExecutionException exception) {
            persistenceService.fail(run.getId(), exception);
            throw new IllegalStateException("单模式回测任务队列已满，请稍后重试", exception);
        }
    }

    @Override
    public SingleModeBacktestRun runRange(LocalDate startDate, LocalDate endDate, int tradeMode) {
        validate(startDate, endDate, tradeMode);
        SingleModeBacktestRun run = persistenceService.createRun(
                startDate, endDate, tradeMode, null, strategyVersion(tradeMode));
        executeRun(run);
        return findRun(run.getId());
    }

    @Override
    public SingleModeBacktestRun startReplay(long sourceRunId) {
        SingleModeBacktestRun source = requireReplaySource(sourceRunId);
        SingleModeBacktestRun run = persistenceService.createRun(
                source.getStartDate(), source.getEndDate(), source.getTradeMode(),
                source.getId(), strategyVersion(source.getTradeMode()));
        try {
            executor.execute(() -> executeReplayRun(run));
            return run;
        } catch (RejectedExecutionException exception) {
            persistenceService.fail(run.getId(), exception);
            throw new IllegalStateException("单模式回测任务队列已满，请稍后重试", exception);
        }
    }

    @Override
    public SingleModeBacktestRun runReplay(long sourceRunId) {
        SingleModeBacktestRun source = requireReplaySource(sourceRunId);
        SingleModeBacktestRun run = persistenceService.createRun(
                source.getStartDate(), source.getEndDate(), source.getTradeMode(),
                source.getId(), strategyVersion(source.getTradeMode()));
        executeReplayRun(run);
        return findRun(run.getId());
    }

    @Override
    public SingleModeBacktestRun startCandidateReplay(long sourceRunId) {
        SingleModeBacktestRun source = requireReplaySource(sourceRunId);
        SingleModeBacktestRun run = persistenceService.createRun(
                source.getStartDate(), source.getEndDate(), source.getTradeMode(),
                source.getId(), strategyVersion(source.getTradeMode()));
        try {
            executor.execute(() -> executeCandidateReplayRun(run));
            return run;
        } catch (RejectedExecutionException exception) {
            persistenceService.fail(run.getId(), exception);
            throw new IllegalStateException("单模式回测任务队列已满，请稍后重试", exception);
        }
    }

    @Override
    public SingleModeBacktestRun runCandidateReplay(long sourceRunId) {
        SingleModeBacktestRun source = requireReplaySource(sourceRunId);
        SingleModeBacktestRun run = persistenceService.createRun(
                source.getStartDate(), source.getEndDate(), source.getTradeMode(),
                source.getId(), strategyVersion(source.getTradeMode()));
        executeCandidateReplayRun(run);
        return findRun(run.getId());
    }

    private synchronized void executeRun(SingleModeBacktestRun run) {
        int total = 0;
        int processed = 0;
        int bought = 0;
        int closed = 0;
        LocalDate lastCompletedDate = null;
        try {
            List<LocalDate> executionDays = calendarService.findTradeDays(
                    run.getStartDate(), run.getEndDate());
            if (executionDays.isEmpty()) {
                throw new IllegalArgumentException("回测区间没有交易日: "
                        + run.getStartDate() + " 至 " + run.getEndDate());
            }
            List<LocalDate> recommendDays = recommendationDays(
                    executionDays, findPreviousTradeDate(executionDays.get(0)));
            ConsecutiveRelaySelectionDeduplicator relayDeduplicator =
                    Integer.valueOf(TradeMode.RELAY_LIMIT_UP.code()).equals(run.getTradeMode())
                            ? new ConsecutiveRelaySelectionDeduplicator() : null;
            Set<String> scheduledSampleKeys = new HashSet<>();
            for (LocalDate recommendDate : recommendDays) {
                TradeMode tradeMode = TradeMode.fromCode(run.getTradeMode());
                List<SelectionTaskData> selectedTasks = selectionService.select(recommendDate, tradeMode);
                List<SelectionTaskData> eligibleTasks = selectedTasks.stream()
                        .filter(task -> task.getTradeDate() != null
                                && !task.getTradeDate().isAfter(run.getEndDate()))
                        .toList();
                List<SelectionTaskData> tasks = relayDeduplicator == null
                        ? eligibleTasks : relayDeduplicator.keepFirst(eligibleTasks);
                if (tasks.size() < eligibleTasks.size()) {
                    log.info("Model 1 连续交易日重复候选只保留首笔 runId={}, recommendDate={}, selected={}, kept={}",
                            run.getId(), recommendDate, eligibleTasks.size(), tasks.size());
                }
                for (SelectionTaskData task : tasks) {
                    SingleModeBacktestSample sample = createSample(run, task);
                    if (!scheduledSampleKeys.add(sampleKey(sample))) {
                        continue;
                    }
                    List<SingleModeBacktestSample> completedSamples =
                            executeCandidateWithDeferredOpportunity(
                                    run, sample, run.getEndDate(), scheduledSampleKeys);
                    total += completedSamples.size();
                    processed += completedSamples.size();
                    for (SingleModeBacktestSample completedSample : completedSamples) {
                        if (Integer.valueOf(SingleModeBacktestSample.POSITION_ACTUAL)
                                .equals(completedSample.getPositionType())) {
                            bought++;
                            if (Integer.valueOf(CLOSED).equals(completedSample.getStatus())) closed++;
                        }
                    }
                }
                persistenceService.updateProgress(run.getId(), recommendDate,
                        total, processed, bought, closed);
                lastCompletedDate = recommendDate;
                log.info("完成单模式选股日 runId={}, tradeMode={}, recommendDate={}, totalSamples={}, processed={}",
                        run.getId(), run.getTradeMode(), recommendDate, total, processed);
            }
            persistenceService.complete(run.getId());
        } catch (RuntimeException exception) {
            persistenceService.updateProgress(run.getId(), lastCompletedDate,
                    total, processed, bought, closed);
            persistenceService.fail(run.getId(), exception);
            log.error("单模式全量回测失败 runId={}, tradeMode={}",
                    run.getId(), run.getTradeMode(), exception);
        }
    }

    /** 不再重新选股，固定源任务的真实/虚拟买入事实并重新执行卖出。 */
    private synchronized void executeReplayRun(SingleModeBacktestRun run) {
        executeFixedCandidateRun(run, false);
    }

    /** 不再重新选股，固定复用源任务的全部候选并按当前规则重新执行买入和卖出。 */
    private synchronized void executeCandidateReplayRun(SingleModeBacktestRun run) {
        executeFixedCandidateRun(run, true);
    }

    private void executeFixedCandidateRun(SingleModeBacktestRun run, boolean replayBuys) {
        int total = 0;
        int processed = 0;
        int bought = 0;
        int closed = 0;
        LocalDate lastCompletedDate = null;
        try {
            List<SingleModeBacktestSample> sourceSamples =
                    persistenceService.findAllSamples(run.getSourceRunId());
            List<SingleModeBacktestSample> candidates = replayBuys
                    ? sourceSamples : replayCandidates(sourceSamples);
            Set<String> scheduledSampleKeys = new HashSet<>();
            if (replayBuys) {
                candidates.stream().map(source -> createReplaySample(run, source))
                        .map(SingleModeBacktestServiceImpl::sampleKey)
                        .forEach(scheduledSampleKeys::add);
            }
            for (SingleModeBacktestSample source : candidates) {
                SingleModeBacktestSample sample = createReplaySample(run, source);
                if (replayBuys) {
                    List<SingleModeBacktestSample> completedSamples =
                            executeCandidateWithDeferredOpportunity(
                                    run, sample, run.getEndDate(), scheduledSampleKeys);
                    total += completedSamples.size();
                    processed += completedSamples.size();
                    for (SingleModeBacktestSample completedSample : completedSamples) {
                        if (Integer.valueOf(SingleModeBacktestSample.POSITION_ACTUAL)
                                .equals(completedSample.getPositionType())) {
                            bought++;
                            if (Integer.valueOf(CLOSED).equals(completedSample.getStatus())) closed++;
                        }
                        if (lastCompletedDate == null
                                || completedSample.getRecommendDate().isAfter(lastCompletedDate)) {
                            lastCompletedDate = completedSample.getRecommendDate();
                        }
                    }
                    persistenceService.updateProgress(run.getId(), lastCompletedDate,
                            total, processed, bought, closed);
                    continue;
                }
                persistenceService.insertSample(sample);
                total++;
                try {
                    executeReplaySample(sample, source, run.getEndDate());
                } catch (RuntimeException exception) {
                    sample.setStatus(DATA_ERROR);
                    sample.setNoBuyReason(abbreviate(exception.getMessage(), 1000));
                    sample.setSampleEndDate(sample.getTradeDate());
                    log.warn("单模式固定候选回放失败 runId={}, replayBuys={}, sourceSampleId={}, symbol={}, reason={}",
                            run.getId(), replayBuys, source.getId(), sample.getSymbol(), exception.getMessage());
                }
                persistenceService.updateSample(sample);
                processed++;
                if (Integer.valueOf(SingleModeBacktestSample.POSITION_ACTUAL)
                        .equals(sample.getPositionType())) {
                    bought++;
                    if (Integer.valueOf(CLOSED).equals(sample.getStatus())) closed++;
                }
                lastCompletedDate = sample.getRecommendDate();
                persistenceService.updateProgress(run.getId(), lastCompletedDate,
                        total, processed, bought, closed);
            }
            persistenceService.complete(run.getId());
        } catch (RuntimeException exception) {
            persistenceService.updateProgress(run.getId(), lastCompletedDate,
                    total, processed, bought, closed);
            persistenceService.fail(run.getId(), exception);
            log.error("固定候选回放失败 runId={}, replayBuys={}, tradeMode={}, sourceRunId={}",
                    run.getId(), replayBuys, run.getTradeMode(), run.getSourceRunId(), exception);
        }
    }

    /** 执行一个固定候选；若命中递延规则，则在同一任务中追加并执行下一交易日 3 进 4。 */
    private List<SingleModeBacktestSample> executeCandidateWithDeferredOpportunity(
            SingleModeBacktestRun run, SingleModeBacktestSample initial,
            LocalDate dataEndDate, Set<String> scheduledSampleKeys) {
        List<SingleModeBacktestSample> pending = new ArrayList<>();
        List<SingleModeBacktestSample> completed = new ArrayList<>();
        pending.add(initial);
        for (int index = 0; index < pending.size(); index++) {
            SingleModeBacktestSample sample = pending.get(index);
            persistenceService.insertSample(sample);
            boolean createDeferred = false;
            try {
                createDeferred = executeSample(sample, dataEndDate);
            } catch (RuntimeException exception) {
                sample.setStatus(DATA_ERROR);
                sample.setNoBuyReason(abbreviate(exception.getMessage(), 1000));
                sample.setSampleEndDate(sample.getTradeDate());
                log.warn("单模式候选回放失败 runId={}, sourceSampleId={}, symbol={}, tradeDate={}, reason={}",
                        run.getId(), sample.getSourceSampleId(), sample.getSymbol(),
                        sample.getTradeDate(), exception.getMessage());
            }
            persistenceService.updateSample(sample);
            completed.add(sample);

            if (!createDeferred) continue;
            LocalDate nextTradeDate = calendarService.findNextTradeDay(sample.getTradeDate());
            if (nextTradeDate == null || nextTradeDate.isAfter(dataEndDate)) continue;
            SingleModeBacktestSample deferred = createDeferredRelaySample(
                    run, sample, nextTradeDate);
            if (scheduledSampleKeys.add(sampleKey(deferred))) {
                pending.add(deferred);
                log.info("Model 1 二板加速样本递延到 3 进 4 runId={}, sourceSampleId={}, symbol={}, thirdBoardDate={}, fourthBoardDate={}",
                        run.getId(), deferred.getSourceSampleId(), deferred.getSymbol(),
                        deferred.getRecommendDate(), deferred.getTradeDate());
            }
        }
        return List.copyOf(completed);
    }

    private SingleModeBacktestSample createSample(SingleModeBacktestRun run,
                                                   SelectionTaskData task) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setRunId(run.getId());
        sample.setSymbol(task.getStockCode());
        sample.setSymbolName(task.getStockName());
        sample.setTradeMode(run.getTradeMode());
        sample.setLimitUpScore(task.getLimitUpScore());
        sample.setRecommendDate(task.getRecommendDate());
        sample.setTradeDate(task.getTradeDate());
        int selectionBoard = selectionBoard(task);
        sample.setSelectionBoard(selectionBoard);
        sample.setSelectionTrigger(task.getSelectionTrigger());
        sample.setSelectionStrength(task.getSelectionStrength());
        sample.setStrategyVersion(task.getStrategyVersion());
        sample.setSelectionRunId(task.getSelectionRunId());
        sample.setRelayCandidateRecordId(task.getRelayCandidateRecordId());
        sample.setStatus(SELECTED);
        sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
        sample.setHoldingTradeDays(0);
        sample.setMaxSealedBoards(selectionBoard);
        sample.setMaxTouchedBoards(selectionBoard);
        sample.setCreatedTime(LocalDateTime.now());
        return sample;
    }

    /**
     * 固定源任务的买入事实，只重放卖出生命周期。
     * 真实成交复用源任务快照；触板未成交样本按当日涨停价建立虚拟持仓。
     */
    private void executeReplaySample(SingleModeBacktestSample sample,
                                     SingleModeBacktestSample source,
                                     LocalDate dataEndDate) {
        StockDailyEntity buyDaily = findDailyOrNull(sample.getSymbol(), sample.getTradeDate());
        if (buyDaily == null) {
            sample.setStatus(DATA_ERROR);
            sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
            sample.setNoBuyReason("固定样本买入日无日 K（停牌或数据缺失）");
            sample.setSampleEndDate(sample.getTradeDate());
            return;
        }
        if (buyDaily.getKlineState() == null || buyDaily.getKlineState() <= 0) {
            sample.setStatus(DATA_ERROR);
            sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
            sample.setNoBuyReason("固定触板样本的买入日日 K 状态不一致，klineState=" + buyDaily.getKlineState());
            sample.setSampleEndDate(sample.getTradeDate());
            return;
        }
        prepareReplayPosition(sample, source, buyDaily);
        calculateBoardPotential(sample, buyDaily, dataEndDate);
        calculateSellLifecycle(sample, dataEndDate);
    }

    static void prepareReplayPosition(SingleModeBacktestSample sample,
                                      SingleModeBacktestSample source,
                                      StockDailyEntity buyDaily) {
        if (isActualSourcePosition(source)) {
            if (source.getBuyPrice() == null || source.getBuyPrice() <= 0) {
                throw new IllegalStateException("源任务真实成交样本缺少有效买入价");
            }
            sample.setPositionType(SingleModeBacktestSample.POSITION_ACTUAL);
            sample.setBuyDate(source.getBuyDate());
            sample.setBuyTime(source.getBuyTime());
            sample.setBuyPrice(source.getBuyPrice());
            sample.setBuyRuleCode(source.getBuyRuleCode());
            sample.setBuyRemark(source.getBuyRemark());
            sample.setBuyDayKlineState(source.getBuyDayKlineState() == null
                    ? buyDaily.getKlineState() : source.getBuyDayKlineState());
            sample.setStatus(OPEN);
            sample.setHoldingTradeDays(1);
            return;
        }

        sample.setPositionType(SingleModeBacktestSample.POSITION_VIRTUAL);
        sample.setNoBuyReason(source.getNoBuyReason());
        sample.setBuyDate(sample.getTradeDate());
        sample.setBuyTime(BacktestExecutionPolicy.OVERNIGHT_FILL_TIME);
        sample.setBuyPrice(cents(buyDaily.getHighPrice()));
        sample.setBuyRuleCode(VIRTUAL_BUY_RULE_CODE);
        sample.setBuyRemark("卖出场景虚拟持仓；复用固定选股结果；实际未成交原因="
                + source.getNoBuyReason());
        sample.setBuyDayKlineState(buyDaily.getKlineState());
        sample.setStatus(OPEN);
        sample.setHoldingTradeDays(1);
    }

    private static boolean isActualSourcePosition(SingleModeBacktestSample source) {
        if (source.getPositionType() != null) {
            return source.getPositionType() == SingleModeBacktestSample.POSITION_ACTUAL;
        }
        return source.getBuyDate() != null;
    }

    private SingleModeBacktestSample createReplaySample(SingleModeBacktestRun run,
                                                         SingleModeBacktestSample source) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setRunId(run.getId());
        sample.setSourceSampleId(source.getId());
        sample.setSymbol(source.getSymbol());
        sample.setSymbolName(source.getSymbolName());
        sample.setTradeMode(run.getTradeMode());
        sample.setLimitUpScore(source.getLimitUpScore());
        sample.setRecommendDate(source.getRecommendDate());
        sample.setTradeDate(source.getTradeDate());
        sample.setSelectionBoard(source.getSelectionBoard() == null ? 1 : source.getSelectionBoard());
        sample.setSelectionTrigger(source.getSelectionTrigger());
        sample.setSelectionStrength(source.getSelectionStrength());
        sample.setStrategyVersion(source.getStrategyVersion());
        sample.setSelectionRunId(source.getSelectionRunId());
        sample.setRelayCandidateRecordId(source.getRelayCandidateRecordId());
        sample.setStatus(SELECTED);
        sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
        sample.setHoldingTradeDays(0);
        int selectionBoard = sample.getSelectionBoard();
        sample.setMaxSealedBoards(selectionBoard);
        sample.setMaxTouchedBoards(selectionBoard);
        sample.setCreatedTime(LocalDateTime.now());
        return sample;
    }

    private boolean executeSample(SingleModeBacktestSample sample, LocalDate dataEndDate) {
        StockDailyEntity buyDaily = findDailyOrNull(sample.getSymbol(), sample.getTradeDate());
        if (buyDaily == null) {
            sample.setStatus(NO_BUY);
            sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
            sample.setNoBuyReason("买入日无日 K（停牌或数据缺失）");
            sample.setSampleEndDate(sample.getTradeDate());
            return false;
        }
        calculateBoardPotential(sample, buyDaily, dataEndDate);
        if (buyDaily.getKlineState() == null || buyDaily.getKlineState() <= 0) {
            sample.setStatus(NO_BUY);
            sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
            sample.setNoBuyReason("买入日未触板，klineState=" + buyDaily.getKlineState());
            return false;
        }

        StockDailyEntity secondBoard = Integer.valueOf(TradeMode.RELAY_LIMIT_UP.code())
                .equals(sample.getTradeMode()) && Integer.valueOf(2).equals(sample.getSelectionBoard())
                ? findDailyOrNull(sample.getSymbol(), sample.getRecommendDate()) : null;

        BacktestDailyTickBatch buyTicks = replayService.loadDailyTicks(
                sample.getTradeDate(), Set.of(sample.getSymbol()));
        BacktestReplayResult overnight = replayService.replay(
                sample.getTradeDate(), sample.getSymbol(), BacktestReplayMode.OVERNIGHT_BUY,
                null, buyTicks, sample.getTradeMode());
        int firstLimitUpTime = secondBoard == null ? 0 : firstLimitUpTradeTime(
                buyTicks, sample.getSymbol(), overnight.limitUpPrice());
        RuleRecordDTO buyRule = null;
        RuleRecordDTO cancel = overnight.firstCancelRecord().orElse(null);
        if (!overnight.openingAuctionBuyAllowed()) {
            int allowedAfterTime = Math.max(ConstantUtil.TIME_930,
                    overnight.earliestContinuousBuyTime() - 1);
            buyRule = findIntradayBuy(sample, buyTicks, allowedAfterTime);
            if (buyRule == null) {
                sample.setNoBuyReason(overnight.openingAuctionBlockReason()
                        + "；盘中允许时段未出现可成交买点");
            }
        } else if (cancel != null) {
            buyRule = findIntradayBuy(sample, buyTicks, cancel.getTime());
            if (buyRule == null) {
                sample.setNoBuyReason("集合竞价撤单后未出现可成交买点");
            }
        } else {
            Double turnover = buyDaily.getTurnover();
            boolean overnightFillable;
            if (Integer.valueOf(TradeMode.FIRST_BOARD.code()).equals(sample.getTradeMode())) {
                overnightFillable = BacktestExecutionPolicy.isModelTwoOvernightBuyFillable(
                        overnight.lastOrderTime());
            } else if (Integer.valueOf(TradeMode.RELAY_LIMIT_UP.code()).equals(sample.getTradeMode())) {
                overnightFillable = BacktestExecutionPolicy.isRelayOvernightBuyFillable(
                        overnight.lastOrderTime(), turnover);
            } else {
                overnightFillable = BacktestExecutionPolicy.isOvernightBuyFillable(
                        overnight.lastOrderTime(), turnover);
            }
            if (!overnightFillable) {
                sample.setNoBuyReason("隔夜涨停委托未满足成交条件；队首时间="
                        + overnight.lastOrderTime() + "，成交额=" + turnover + "万元");
            } else {
                buyRule = overnightBuyRule(overnight, turnover);
            }
        }
        if (shouldDeferRelayThirdBoardBuy(
                sample, secondBoard, buyDaily, firstLimitUpTime)) {
            String reason = Integer.valueOf(3).equals(buyDaily.getKlineState())
                    ? "二板加速后不接三板一字板，递延观察 3 进 4"
                    : "二板加速后不接三板高开秒板，递延观察 3 进 4";
            sample.setNoBuyReason(reason);
            buyRule = null;
        }
        if (buyRule == null) {
            prepareVirtualPosition(sample, buyDaily, overnight);
            calculateBoardPotential(sample, buyDaily, dataEndDate);
            calculateSellLifecycle(sample, dataEndDate);
            return shouldCreateDeferredRelayOpportunity(sample, secondBoard, buyDaily);
        }

        sample.setPositionType(SingleModeBacktestSample.POSITION_ACTUAL);
        sample.setBuyDate(sample.getTradeDate());
        sample.setBuyTime(buyRule.getTime());
        sample.setBuyPrice(buyRule.getPrice());
        sample.setBuyRuleCode(buyRule.getRuleCode());
        sample.setBuyRemark(buyRule.getRemark());
        sample.setBuyDayKlineState(buyDaily.getKlineState());
        sample.setStatus(OPEN);
        sample.setHoldingTradeDays(1);
        // 买入价确定后重新按真实成交价计算本轮理论最高收益。
        calculateBoardPotential(sample, buyDaily, dataEndDate);
        calculateSellLifecycle(sample, dataEndDate);
        return false;
    }

    /** 为触板但未实际成交的样本建立只用于卖出场景统计的虚拟持仓。 */
    static void prepareVirtualPosition(SingleModeBacktestSample sample,
                                       StockDailyEntity buyDaily,
                                       BacktestReplayResult overnight) {
        if (overnight.limitUpPrice() <= 0) {
            throw new IllegalStateException("虚拟持仓缺少有效涨停价格");
        }
        String missReason = sample.getNoBuyReason();
        sample.setPositionType(SingleModeBacktestSample.POSITION_VIRTUAL);
        sample.setBuyDate(sample.getTradeDate());
        sample.setBuyTime(BacktestExecutionPolicy.OVERNIGHT_FILL_TIME);
        sample.setBuyPrice(overnight.limitUpPrice());
        sample.setBuyRuleCode(VIRTUAL_BUY_RULE_CODE);
        sample.setBuyRemark("卖出场景虚拟持仓；实际未成交原因=" + missReason);
        sample.setBuyDayKlineState(buyDaily.getKlineState());
        sample.setStatus(OPEN);
        sample.setHoldingTradeDays(1);
    }

    private RuleRecordDTO findIntradayBuy(SingleModeBacktestSample sample,
                                          BacktestDailyTickBatch dailyTicks,
                                          int allowedAfterTime) {
        int currentAllowedTime = allowedAfterTime;
        for (int attempt = 0; attempt < 20; attempt++) {
            int replayAfterTime = currentAllowedTime;
            BacktestReplayResult result = replayService.replay(
                    sample.getTradeDate(), sample.getSymbol(), BacktestReplayMode.BUY_AFTER_TIME,
                    replayAfterTime, dailyTicks, sample.getTradeMode());
            List<RuleRecordDTO> buys = result.records().stream()
                    .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_BUY)
                            .equals(record.getActionType()))
                    .filter(record -> record.getTime() != null && record.getTime() > replayAfterTime)
                    .sorted(Comparator.comparingInt(RuleRecordDTO::getTime)).toList();
            if (buys.isEmpty()) return null;
            RuleRecordDTO buy = buys.get(0);
            RuleRecordDTO cancel = result.records().stream()
                    .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_CANCEL)
                            .equals(record.getActionType()))
                    .filter(record -> record.getTime() != null && record.getTime() > buy.getTime())
                    .min(Comparator.comparingInt(RuleRecordDTO::getTime)).orElse(null);
            if (cancel != null) {
                currentAllowedTime = cancel.getTime();
                continue;
            }
            if (BacktestExecutionPolicy.isIntradayBuyFillable(
                    buy, result.lastPrice(), result.limitUpPrice())) {
                buy.setPrice(result.limitUpPrice());
                return buy;
            }
            return null;
        }
        return null;
    }

    private void calculateSellLifecycle(SingleModeBacktestSample sample,
                                        LocalDate dataEndDate) {
        List<LocalDate> holdingDays = calendarService.findTradeDays(sample.getBuyDate(), dataEndDate);
        BigDecimal highestReturn = BigDecimal.ZERO;
        BigDecimal maximumDrawdown = BigDecimal.ZERO;
        LocalDate sellDate = null;
        for (int index = 0; index < holdingDays.size(); index++) {
            LocalDate date = holdingDays.get(index);
            sample.setHoldingTradeDays(index + 1);
            StockDailyEntity daily = findDailyOrNull(sample.getSymbol(), date);
            if (shouldSkipHoldingDay(daily)) {
                log.warn("单模式卖出生命周期跳过缺失日K runId={}, symbol={}, date={}",
                        sample.getRunId(), sample.getSymbol(), date);
                continue;
            }
            BigDecimal highReturn = priceReturn(cents(daily.getHighPrice()), sample.getBuyPrice());
            BigDecimal lowReturn = priceReturn(cents(daily.getLowPrice()), sample.getBuyPrice());
            highestReturn = highestReturn.max(highReturn);
            maximumDrawdown = maximumDrawdown.max(highestReturn.subtract(lowReturn));
            if (index == 0) continue;

            RuleRecordDTO sellRule;
            if (index == 1 && BreakBoardNextOpenSellPolicy.shouldSellAtNextOpen(
                    sample.getBuyDayKlineState())) {
                sellRule = breakBoardNextOpenSellRule(sample, daily);
            } else {
                BacktestDailyTickBatch sellTicks = replayService.loadDailyTicks(
                        date, Set.of(sample.getSymbol()));
                sellRule = replayService.replay(date, sample.getSymbol(), BacktestReplayMode.SELL,
                                null, sellTicks, sample.getTradeMode())
                        .firstSellRecord().orElse(null);
            }
            if (sellRule != null) {
                sample.setSellDate(date);
                sample.setSellTime(sellRule.getTime());
                sample.setSellPrice(sellRule.getPrice());
                sample.setSellRuleCode(sellRule.getRuleCode());
                sample.setSellRemark(sellRule.getRemark());
                sample.setSellBoard(resolveSellBoard(daily, sample));
                sample.setReturnRate(priceReturn(sellRule.getPrice(), sample.getBuyPrice()));
                sample.setStatus(CLOSED);
                sellDate = date;
                break;
            }
        }
        sample.setMaxFloatingReturnRate(highestReturn);
        sample.setMaxDrawdownRate(maximumDrawdown.max(BigDecimal.ZERO));
        if (sellDate != null) {
            sample.setPostSellMaxReturnRate(calculatePostSellMaximum(
                    sample, sellDate, dataEndDate));
        }
    }

    static boolean shouldSkipHoldingDay(StockDailyEntity daily) {
        return daily == null;
    }

    /** 从推荐板位开始统计本轮最高封板、最高触板和理论最高收益。 */
    private void calculateBoardPotential(SingleModeBacktestSample sample,
                                         StockDailyEntity buyDaily,
                                         LocalDate dataEndDate) {
        List<StockDailyEntity> rows = stockDailyService.list(
                Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, sample.getSymbol())
                        .between(StockDailyEntity::getTradeDate, sample.getRecommendDate(), dataEndDate)
                        .orderByAsc(StockDailyEntity::getTradeDate));
        int selectedBoard = sample.getSelectionBoard() == null
                ? 1 : Math.max(1, sample.getSelectionBoard());
        int sealed = selectedBoard;
        int touched = selectedBoard;
        int currentBoard = selectedBoard;
        int referencePrice = sample.getBuyPrice() == null
                ? cents(buyDaily.getPrevClose() == null
                    ? buyDaily.getClosePrice() : buyDaily.getPrevClose() * 1.1D)
                : sample.getBuyPrice();
        BigDecimal potential = BigDecimal.ZERO;
        boolean afterRecommend = false;
        for (StockDailyEntity daily : rows) {
            if (daily.getTradeDate().equals(sample.getRecommendDate())) {
                afterRecommend = true;
                continue;
            }
            if (!afterRecommend) continue;
            Integer state = daily.getKlineState();
            if (isSealedLimitUp(state)) {
                currentBoard++;
                sealed = currentBoard;
                touched = currentBoard;
                potential = potential.max(priceReturn(cents(daily.getHighPrice()), referencePrice));
                sample.setSampleEndDate(daily.getTradeDate());
                continue;
            }
            if (isBrokenLimitUp(state)) {
                currentBoard++;
                touched = currentBoard;
                potential = potential.max(priceReturn(cents(daily.getHighPrice()), referencePrice));
                sample.setSampleEndDate(daily.getTradeDate());
            } else {
                sample.setSampleEndDate(daily.getTradeDate());
            }
            break;
        }
        sample.setMaxSealedBoards(sealed);
        sample.setMaxTouchedBoards(touched);
        sample.setPotentialMaxReturnRate(potential);
        if (sample.getSampleEndDate() == null) sample.setSampleEndDate(sample.getTradeDate());
    }

    private BigDecimal calculatePostSellMaximum(SingleModeBacktestSample sample,
                                                LocalDate sellDate, LocalDate dataEndDate) {
        LocalDate end = sample.getSampleEndDate() == null || sample.getSampleEndDate().isAfter(dataEndDate)
                ? dataEndDate : sample.getSampleEndDate();
        if (sellDate.isAfter(end)) return BigDecimal.ZERO;
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, sample.getSymbol())
                        .between(StockDailyEntity::getTradeDate, sellDate, end))
                .stream().map(StockDailyEntity::getHighPrice).filter(java.util.Objects::nonNull)
                .map(SingleModeBacktestServiceImpl::cents)
                .map(price -> priceReturn(price, sample.getBuyPrice()))
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private RuleRecordDTO overnightBuyRule(BacktestReplayResult result, Double turnover) {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(RuleConstant.TRADING_MODE_BUY);
        rule.setRuleCode(OVERNIGHT_BUY_RULE_CODE);
        rule.setSymbol(result.symbol());
        rule.setTime(BacktestExecutionPolicy.OVERNIGHT_FILL_TIME);
        rule.setLastOrderTime(result.lastOrderTime());
        rule.setPrice(result.limitUpPrice());
        rule.setRemark("隔夜涨停委托成交；回放队首时间=" + result.lastOrderTime()
                + "，当日成交额=" + turnover + "万元");
        return rule;
    }

    private RuleRecordDTO breakBoardNextOpenSellRule(SingleModeBacktestSample sample,
                                                     StockDailyEntity daily) {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(RuleConstant.TRADING_MODE_SELL);
        rule.setRuleCode(RuleConstant.SELL_BACKTEST_LIMIT_UP_BREAK_NEXT_OPEN);
        rule.setSymbol(sample.getSymbol());
        rule.setTime(ConstantUtil.TIME_925);
        rule.setPrice(cents(daily.getOpenPrice()));
        rule.setRemark("回测卖出 - 买入日炸板，下一交易日按开盘价成交；买入日K线状态="
                + sample.getBuyDayKlineState());
        return rule;
    }

    private int resolveSellBoard(StockDailyEntity daily, SingleModeBacktestSample sample) {
        Integer boards = daily.getConsecutiveLimitUpDays();
        if (boards != null && boards > 0) return boards;
        return sample.getMaxSealedBoards() == null ? 1 : sample.getMaxSealedBoards();
    }

    private StockDailyEntity findDaily(String symbol, LocalDate date) {
        StockDailyEntity daily = findDailyOrNull(symbol, date);
        if (daily == null) {
            throw new IllegalArgumentException(date + " 没有股票 " + symbol + " 的日 K 数据");
        }
        return daily;
    }

    private StockDailyEntity findDailyOrNull(String symbol, LocalDate date) {
        return stockDailyService.getOne(
                Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, symbol)
                        .eq(StockDailyEntity::getTradeDate, date)
                        .last("LIMIT 1"));
    }

    /** 用区间首个买入交易日的前一交易日生成候选，覆盖跨年推荐。 */
    private LocalDate findPreviousTradeDate(LocalDate tradeDate) {
        StockTradeCalendar previous = calendarService.getOne(
                Wrappers.<StockTradeCalendar>lambdaQuery()
                        .lt(StockTradeCalendar::getTradeDate, tradeDate)
                        .orderByDesc(StockTradeCalendar::getTradeDate)
                        .last("LIMIT 1"));
        if (previous == null || previous.getTradeDate() == null) {
            throw new IllegalStateException(tradeDate + " 之前没有交易日历数据");
        }
        return previous.getTradeDate();
    }

    static List<LocalDate> recommendationDays(List<LocalDate> executionDays,
                                              LocalDate previousTradeDate) {
        if (executionDays == null || executionDays.isEmpty()) return List.of();
        java.util.ArrayList<LocalDate> dates = new java.util.ArrayList<>(executionDays.size() + 1);
        dates.add(previousTradeDate);
        dates.addAll(executionDays);
        return List.copyOf(dates);
    }

    static String strategyVersion(int tradeMode) {
        return tradeMode == TradeMode.RELAY_LIMIT_UP.code()
                ? RELAY_STRATEGY_VERSION : STRATEGY_VERSION;
    }

    /** Model 1 同一股票连续多个推荐日重复出现时，整条连续链只保留第一笔。 */
    static final class ConsecutiveRelaySelectionDeduplicator {
        private Set<String> previousDaySymbols = Set.of();

        List<SelectionTaskData> keepFirst(List<SelectionTaskData> tasks) {
            if (tasks == null || tasks.isEmpty()) {
                previousDaySymbols = Set.of();
                return List.of();
            }
            Set<String> currentDaySymbols = new LinkedHashSet<>();
            List<SelectionTaskData> kept = new ArrayList<>();
            for (SelectionTaskData task : tasks) {
                String symbol = task == null ? null : task.getStockCode();
                if (symbol == null || !currentDaySymbols.add(symbol)) {
                    continue;
                }
                if (!previousDaySymbols.contains(symbol)) {
                    kept.add(task);
                }
            }
            previousDaySymbols = Set.copyOf(currentDaySymbols);
            return List.copyOf(kept);
        }
    }

    static int selectionBoard(SelectionTaskData task) {
        if (task == null || task.getConsecutiveLimitUpDays() == null) return 1;
        return Math.max(1, task.getConsecutiveLimitUpDays());
    }

    /** 二板当天出现一字、低振幅、低换手 T 字或整体低换手，视为二板加速。 */
    static boolean isAcceleratedSecondBoard(StockDailyEntity secondBoard) {
        if (secondBoard == null) return false;
        return TradeExecutionProfile.isAcceleratedBoard(
                secondBoard.getKlineState() == null ? 0 : secondBoard.getKlineState(),
                secondBoard.getAmplitude() == null ? -1D : secondBoard.getAmplitude(),
                secondBoard.getTurnoverRate() == null ? -1D : secondBoard.getTurnoverRate());
    }

    /** 二板加速后，三板一字或高开并在 09:35 前触发买点时递延到四板观察。 */
    static boolean shouldDeferRelayThirdBoardBuy(SingleModeBacktestSample sample,
                                                  StockDailyEntity secondBoard,
                                                  StockDailyEntity thirdBoard,
                                                  int firstLimitUpTime) {
        if (sample == null || thirdBoard == null
                || !Integer.valueOf(TradeMode.RELAY_LIMIT_UP.code()).equals(sample.getTradeMode())
                || !Integer.valueOf(2).equals(sample.getSelectionBoard())
                || !isAcceleratedSecondBoard(secondBoard)) {
            return false;
        }
        if (Integer.valueOf(3).equals(thirdBoard.getKlineState())) {
            return true;
        }
        if (firstLimitUpTime <= 0 || firstLimitUpTime >= ConstantUtil.TIME_935
                || thirdBoard.getOpenPrice() == null || thirdBoard.getPrevClose() == null
                || thirdBoard.getPrevClose() <= 0) {
            return false;
        }
        double openIncrease = (thirdBoard.getOpenPrice() - thirdBoard.getPrevClose())
                * 100D / thirdBoard.getPrevClose();
        return openIncrease > RELAY_HIGH_OPEN_FAST_MIN_EXCLUSIVE;
    }

    /** 从当日 Level2 成交中读取第一次触及涨停价的真实时间，隔夜下单时间不参与判断。 */
    static int firstLimitUpTradeTime(BacktestDailyTickBatch dailyTicks,
                                     String symbol, int limitUpPrice) {
        if (dailyTicks == null || symbol == null || limitUpPrice <= 0) return 0;
        int[] firstTime = {0};
        dailyTicks.replay(symbol, tick -> {
            if (firstTime[0] == 0 && tick.dataType == 2
                    && tick.time >= ConstantUtil.TIME_930
                    && tick.price == limitUpPrice) {
                firstTime[0] = tick.time;
            }
        });
        return firstTime[0];
    }

    /** 二板加速样本三板未买到但收盘封住时，允许下一交易日重新尝试 3 进 4。 */
    static boolean shouldCreateDeferredRelayOpportunity(
            SingleModeBacktestSample sample, StockDailyEntity secondBoard,
            StockDailyEntity thirdBoard) {
        return sample != null && thirdBoard != null
                && Integer.valueOf(TradeMode.RELAY_LIMIT_UP.code()).equals(sample.getTradeMode())
                && Integer.valueOf(2).equals(sample.getSelectionBoard())
                && !Integer.valueOf(SingleModeBacktestSample.POSITION_ACTUAL)
                        .equals(sample.getPositionType())
                && isAcceleratedSecondBoard(secondBoard)
                && thirdBoard.getKlineState() != null
                && thirdBoard.getKlineState() >= 1
                && thirdBoard.getKlineState() <= 5;
    }

    /** 使用已确认封住的三板日作为新推荐日，生成下一交易日独立执行的 3 进 4 样本。 */
    static SingleModeBacktestSample createDeferredRelaySample(
            SingleModeBacktestRun run, SingleModeBacktestSample source, LocalDate nextTradeDate) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setRunId(run.getId());
        sample.setSourceSampleId(source.getSourceSampleId() == null
                ? source.getId() : source.getSourceSampleId());
        sample.setSymbol(source.getSymbol());
        sample.setSymbolName(source.getSymbolName());
        sample.setTradeMode(run.getTradeMode());
        sample.setLimitUpScore(source.getLimitUpScore());
        sample.setRecommendDate(source.getTradeDate());
        sample.setTradeDate(nextTradeDate);
        sample.setSelectionBoard(3);
        sample.setSelectionTrigger(DEFERRED_RELAY_TRIGGER);
        sample.setSelectionStrength(source.getSelectionStrength());
        sample.setStrategyVersion(run.getStrategyVersion());
        sample.setSelectionRunId(source.getSelectionRunId());
        sample.setRelayCandidateRecordId(source.getRelayCandidateRecordId());
        sample.setStatus(SELECTED);
        sample.setPositionType(SingleModeBacktestSample.POSITION_NONE);
        sample.setHoldingTradeDays(0);
        sample.setMaxSealedBoards(3);
        sample.setMaxTouchedBoards(3);
        sample.setCreatedTime(LocalDateTime.now());
        return sample;
    }

    private static String sampleKey(SingleModeBacktestSample sample) {
        return sample.getSymbol() + "|" + sample.getTradeDate();
    }

    /** 固定复用源任务选股结果，仅保留触板或已经真实成交的样本。 */
    static List<SingleModeBacktestSample> replayCandidates(List<SingleModeBacktestSample> samples) {
        if (samples == null || samples.isEmpty()) return List.of();
        return samples.stream()
                .filter(sample -> sample.getBuyDate() != null
                        || sample.getMaxTouchedBoards() != null && sample.getMaxTouchedBoards() >= 2)
                .toList();
    }

    private SingleModeBacktestRun requireReplaySource(long sourceRunId) {
        SingleModeBacktestRun source = findRun(sourceRunId);
        validateTradeMode(source.getTradeMode());
        if (!Integer.valueOf(SingleModeBacktestPersistenceService.COMPLETED)
                .equals(source.getStatus())) {
            throw new IllegalArgumentException("只能复用已完成的单模式任务: " + sourceRunId);
        }
        return source;
    }

    @Override
    public SingleModeBacktestRun findRun(long runId) {
        SingleModeBacktestRun run = persistenceService.findRun(runId);
        if (run == null) throw new IllegalArgumentException("单模式回测任务不存在: " + runId);
        return run;
    }

    @Override
    public List<SingleModeBacktestRun> findRecentRuns(int tradeMode, int limit) {
        validateTradeMode(tradeMode);
        return persistenceService.findRecentRuns(tradeMode, Math.max(1, Math.min(limit, 100)));
    }

    @Override
    public SingleModeBacktestSummary summarize(long runId) {
        findRun(runId);
        return SingleModeBacktestMetrics.summarize(persistenceService.findAllSamples(runId));
    }

    @Override
    public List<SingleModeBoardStat> boardStats(long runId) {
        findRun(runId);
        return SingleModeBacktestMetrics.boardStats(persistenceService.findAllSamples(runId));
    }

    @Override
    public SingleModeSamplePage findSamples(long runId, int page, int pageSize,
                                            Integer positionType) {
        findRun(runId);
        return persistenceService.findSamples(runId, Math.max(1, page),
                Math.max(1, Math.min(pageSize, 200)), positionType);
    }

    private void validate(LocalDate startDate, LocalDate endDate, int tradeMode) {
        validateTradeMode(tradeMode);
        if (startDate == null || endDate == null) throw new IllegalArgumentException("回测日期不能为空");
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("开始日期不能晚于结束日期");
        if (endDate.isAfter(LocalDate.now())) throw new IllegalArgumentException("结束日期不能晚于今天");
    }

    private void validateTradeMode(int tradeMode) {
        TradeMode.fromCode(tradeMode);
    }

    private boolean isSealedLimitUp(Integer state) { return state != null && state >= 1 && state <= 5; }
    private boolean isBrokenLimitUp(Integer state) { return state != null && state >= 11 && state <= 13; }
    private static int cents(Double price) {
        if (price == null || price <= 0) throw new IllegalStateException("日 K 缺少有效价格");
        return BigDecimal.valueOf(price).movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
    }
    private BigDecimal priceReturn(Integer price, Integer basePrice) {
        if (price == null || basePrice == null || basePrice <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(price - basePrice).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(basePrice), 6, RoundingMode.HALF_UP);
    }
    private String abbreviate(String value, int length) {
        if (value == null) return "未知数据异常";
        return value.length() <= length ? value : value.substring(0, length);
    }
}
