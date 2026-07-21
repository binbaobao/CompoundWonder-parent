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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** Model 3 全候选、独立持仓的回测执行器。 */
@Slf4j
@Service
public class SingleModeBacktestServiceImpl implements SingleModeBacktestService {
    private static final int SELECTED = 1;
    private static final int NO_BUY = 2;
    private static final int OPEN = 3;
    private static final int CLOSED = 4;
    private static final int DATA_ERROR = 5;
    private static final int OVERNIGHT_BUY_RULE_CODE = 1;

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
        SingleModeBacktestRun run = persistenceService.createRun(startDate, endDate, tradeMode);
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
        SingleModeBacktestRun run = persistenceService.createRun(startDate, endDate, tradeMode);
        executeRun(run);
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
            for (LocalDate recommendDate : recommendDays) {
                List<SelectionTaskData> tasks = selectionService.select(
                        recommendDate, TradeMode.SMALL_CAP_FIRST_BOARD);
                for (SelectionTaskData task : tasks) {
                    // 推荐日之后尚未发生的买入日不计入本轮已完成样本。
                    if (task.getTradeDate() == null || task.getTradeDate().isAfter(run.getEndDate())) {
                        continue;
                    }
                    SingleModeBacktestSample sample = createSample(run, task);
                    persistenceService.insertSample(sample);
                    total++;
                    try {
                        executeSample(sample, run.getEndDate());
                    } catch (RuntimeException exception) {
                        sample.setStatus(DATA_ERROR);
                        sample.setNoBuyReason(abbreviate(exception.getMessage(), 1000));
                        sample.setSampleEndDate(sample.getTradeDate());
                        log.warn("单模式样本回测失败 runId={}, recommendDate={}, symbol={}, reason={}",
                                run.getId(), recommendDate, sample.getSymbol(), exception.getMessage());
                    }
                    persistenceService.updateSample(sample);
                    processed++;
                    if (sample.getBuyDate() != null) bought++;
                    if (Integer.valueOf(CLOSED).equals(sample.getStatus())) closed++;
                }
                persistenceService.updateProgress(run.getId(), recommendDate,
                        total, processed, bought, closed);
                lastCompletedDate = recommendDate;
                log.info("完成 Model 3 单模式选股日 runId={}, recommendDate={}, totalSamples={}, processed={}",
                        run.getId(), recommendDate, total, processed);
            }
            persistenceService.complete(run.getId());
        } catch (RuntimeException exception) {
            persistenceService.updateProgress(run.getId(), lastCompletedDate,
                    total, processed, bought, closed);
            persistenceService.fail(run.getId(), exception);
            log.error("Model 3 单模式全量回测失败 runId={}", run.getId(), exception);
        }
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
        sample.setSelectionBoard(1);
        sample.setStatus(SELECTED);
        sample.setHoldingTradeDays(0);
        sample.setMaxSealedBoards(1);
        sample.setMaxTouchedBoards(1);
        sample.setCreatedTime(LocalDateTime.now());
        return sample;
    }

    private void executeSample(SingleModeBacktestSample sample, LocalDate dataEndDate) {
        StockDailyEntity buyDaily = findDailyOrNull(sample.getSymbol(), sample.getTradeDate());
        if (buyDaily == null) {
            sample.setStatus(NO_BUY);
            sample.setNoBuyReason("买入日无日 K（停牌或数据缺失）");
            sample.setSampleEndDate(sample.getTradeDate());
            return;
        }
        calculateBoardPotential(sample, buyDaily, dataEndDate);
        if (buyDaily.getKlineState() == null || buyDaily.getKlineState() <= 0) {
            sample.setStatus(NO_BUY);
            sample.setNoBuyReason("买入日未触板，klineState=" + buyDaily.getKlineState());
            return;
        }

        BacktestDailyTickBatch buyTicks = replayService.loadDailyTicks(
                sample.getTradeDate(), Set.of(sample.getSymbol()));
        BacktestReplayResult overnight = replayService.replay(
                sample.getTradeDate(), sample.getSymbol(), BacktestReplayMode.OVERNIGHT_BUY,
                null, buyTicks, sample.getTradeMode());
        RuleRecordDTO buyRule;
        RuleRecordDTO cancel = overnight.firstCancelRecord().orElse(null);
        if (cancel != null) {
            buyRule = findIntradayBuy(sample, buyTicks, cancel.getTime());
        } else {
            Double turnover = buyDaily.getTurnover();
            if (!BacktestExecutionPolicy.isOvernightBuyFillable(
                    overnight.lastOrderTime(), turnover)) {
                sample.setStatus(NO_BUY);
                sample.setNoBuyReason("隔夜涨停委托未满足成交条件；队首时间="
                        + overnight.lastOrderTime() + "，成交额=" + turnover + "万元");
                return;
            }
            buyRule = overnightBuyRule(overnight, turnover);
        }
        if (buyRule == null) {
            sample.setStatus(NO_BUY);
            sample.setNoBuyReason("集合竞价撤单后未出现可成交买点");
            return;
        }

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
        calculateActualLifecycle(sample, dataEndDate);
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

    private void calculateActualLifecycle(SingleModeBacktestSample sample,
                                          LocalDate dataEndDate) {
        List<LocalDate> holdingDays = calendarService.findTradeDays(sample.getBuyDate(), dataEndDate);
        BigDecimal highestReturn = BigDecimal.ZERO;
        BigDecimal maximumDrawdown = BigDecimal.ZERO;
        LocalDate sellDate = null;
        for (int index = 0; index < holdingDays.size(); index++) {
            LocalDate date = holdingDays.get(index);
            StockDailyEntity daily = findDaily(sample.getSymbol(), date);
            BigDecimal highReturn = priceReturn(cents(daily.getHighPrice()), sample.getBuyPrice());
            BigDecimal lowReturn = priceReturn(cents(daily.getLowPrice()), sample.getBuyPrice());
            highestReturn = highestReturn.max(highReturn);
            maximumDrawdown = maximumDrawdown.max(highestReturn.subtract(lowReturn));
            sample.setHoldingTradeDays(index + 1);
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

    /** 从首板推荐日开始统计本轮最高封板、最高触板和理论最高收益。 */
    private void calculateBoardPotential(SingleModeBacktestSample sample,
                                         StockDailyEntity buyDaily,
                                         LocalDate dataEndDate) {
        List<StockDailyEntity> rows = stockDailyService.list(
                Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, sample.getSymbol())
                        .between(StockDailyEntity::getTradeDate, sample.getRecommendDate(), dataEndDate)
                        .orderByAsc(StockDailyEntity::getTradeDate));
        int sealed = 1;
        int touched = 1;
        int currentBoard = 1;
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
                .map(this::cents).map(price -> priceReturn(price, sample.getBuyPrice()))
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
    public SingleModeSamplePage findSamples(long runId, int page, int pageSize) {
        findRun(runId);
        return persistenceService.findSamples(runId, Math.max(1, page),
                Math.max(1, Math.min(pageSize, 200)));
    }

    private void validate(LocalDate startDate, LocalDate endDate, int tradeMode) {
        validateTradeMode(tradeMode);
        if (startDate == null || endDate == null) throw new IllegalArgumentException("回测日期不能为空");
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("开始日期不能晚于结束日期");
        if (endDate.isAfter(LocalDate.now())) throw new IllegalArgumentException("结束日期不能晚于今天");
    }

    private void validateTradeMode(int tradeMode) {
        if (tradeMode != TradeMode.SMALL_CAP_FIRST_BOARD.code()) {
            throw new IllegalArgumentException("本版单模式全量回测仅支持 Model 3");
        }
    }

    private boolean isSealedLimitUp(Integer state) { return state != null && state >= 1 && state <= 5; }
    private boolean isBrokenLimitUp(Integer state) { return state != null && state >= 11 && state <= 13; }
    private int cents(Double price) {
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
