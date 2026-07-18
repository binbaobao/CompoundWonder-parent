package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.service.HistoricalBacktestTradeService;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.entity.StockTradeCalendar;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.RuleExecuteRecord;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import com.compoundwonder.util.SymbolUtil;
import com.compoundwonder.util.TradeCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * 按交易日串行执行推荐股票回测，并维护唯一持仓和每日权益。
 */
@Slf4j
@Service
public class HistoricalBacktestTradeServiceImpl implements HistoricalBacktestTradeService {

    private static final BigDecimal INITIAL_CAPITAL = new BigDecimal("100000.00");
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");
    private static final int POSITION_OPEN = 1;
    private static final int POSITION_CLOSED = 2;
    private static final int ACCOUNT_EMPTY = 0;
    private static final int ACCOUNT_HOLDING = 1;
    private static final int OVERNIGHT_BUY_RULE_CODE = 1;

    private final BackTestTradeService replayService;
    private final BacktestPersistenceService persistenceService;
    private final StockTradeCalendarService calendarService;
    private final StockDailyService stockDailyService;
    private final StockWatchingTaskService stockWatchingTaskService;
    private final Executor backtestExecutor;

    public HistoricalBacktestTradeServiceImpl(BackTestTradeService replayService,
                                              BacktestPersistenceService persistenceService,
                                              StockTradeCalendarService calendarService,
                                              StockDailyService stockDailyService,
                                              StockWatchingTaskService stockWatchingTaskService,
                                              @Qualifier("historicalBacktestExecutor") Executor backtestExecutor) {
        this.replayService = replayService;
        this.persistenceService = persistenceService;
        this.calendarService = calendarService;
        this.stockDailyService = stockDailyService;
        this.stockWatchingTaskService = stockWatchingTaskService;
        this.backtestExecutor = backtestExecutor;
    }

    @Override
    public BacktestRun startRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        BacktestRun run = persistenceService.createRun(startDate, endDate, INITIAL_CAPITAL);
        try {
            backtestExecutor.execute(() -> executeRun(run));
            return run;
        } catch (RejectedExecutionException exception) {
            persistenceService.failRun(run.getId(), exception);
            throw new IllegalStateException("历史回测任务队列已满，请稍后重试", exception);
        }
    }

    @Override
    public BacktestRun runRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        BacktestRun run = persistenceService.createRun(startDate, endDate, INITIAL_CAPITAL);
        executeRun(run);
        return persistenceService.findRun(run.getId());
    }

    private synchronized void executeRun(BacktestRun run) {
        AccountState account = new AccountState(INITIAL_CAPITAL, INITIAL_CAPITAL);
        try {
            List<LocalDate> tradeDays = calendarService.findTradeDays(run.getStartDate(), run.getEndDate());
            if (tradeDays.isEmpty()) {
                throw new IllegalArgumentException("回测区间没有交易日: "
                        + run.getStartDate() + " 至 " + run.getEndDate());
            }
            LocalDate recommendDate = findPreviousTradeDate(tradeDays.get(0));
            for (LocalDate tradeDate : tradeDays) {
                int taskCount = stockWatchingTaskService
                        .createPostCloseWatchingTasks(recommendDate).size();
                log.info("回测前重新选股 recommendDate={}, tradeDate={}, taskCount={}",
                        recommendDate, tradeDate, taskCount);
                processDay(run.getId(), tradeDate, account);
                recommendDate = tradeDate;
            }
            BigDecimal totalReturnRate = rate(account.previousTotalAsset.subtract(INITIAL_CAPITAL), INITIAL_CAPITAL);
            persistenceService.completeRun(
                    run.getId(), account.previousTotalAsset, totalReturnRate,
                    account.limitUpBreakCount);
        } catch (RuntimeException exception) {
            persistenceService.failRun(run.getId(), exception);
            log.error("历史回测失败 runId={}, startDate={}, endDate={}",
                    run.getId(), run.getStartDate(), run.getEndDate(), exception);
        }
    }

    @Override
    public BacktestRun findRun(long runId) {
        BacktestRun run = persistenceService.findRun(runId);
        if (run == null) {
            throw new IllegalArgumentException("回测任务不存在: " + runId);
        }
        return run;
    }

    /** 查询最近创建的历史回测任务。 */
    @Override
    public List<BacktestRun> findRecentRuns(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return persistenceService.findRecentRuns(safeLimit);
    }

    /** 查询任务的每日账户权益快照。 */
    @Override
    public List<BacktestDailyRecord> findDailyRecords(long runId) {
        findRun(runId);
        return persistenceService.findDailyRecords(runId);
    }

    /** 查询任务的完整持仓生命周期。 */
    @Override
    public List<BacktestPosition> findPositions(long runId) {
        findRun(runId);
        return persistenceService.findPositions(runId);
    }

    /** 查询任务中实际生效的交易规则。 */
    @Override
    public List<RuleExecuteRecord> findRules(long runId) {
        findRun(runId);
        return persistenceService.findRules(runId);
    }

    private void processDay(long runId, LocalDate tradeDate, AccountState account) {
        List<StockWatchingTask> tasks = persistenceService.findWatchingTasks(tradeDate);
        BacktestPosition previousPosition = account.position;
        RuleRecordDTO sellRule = null;
        RuleRecordDTO buyRule = null;
        StockWatchingTask buyTask = null;
        BacktestPosition newPosition = null;
        List<BacktestRuleAction> actionRules = new ArrayList<>();
        TriggeredRuleCollector triggeredRules = new TriggeredRuleCollector();

        if (previousPosition != null) {
            if (tradeDate.isAfter(previousPosition.getBuyDate())) {
                previousPosition.setHoldingTradeDays(valueOrZero(previousPosition.getHoldingTradeDays()) + 1);
            }
            sellRule = createBreakBoardNextOpenSellRule(
                    previousPosition, tradeDate, account.positionBuyKlineState);
            if (sellRule == null) {
                BacktestReplayResult sellResult = replayService.replay(
                        tradeDate, previousPosition.getSymbol(), BacktestReplayMode.SELL, null);
                triggeredRules.addAll(previousPosition, sellResult.records());
                sellRule = sellResult.firstSellRecord().orElse(null);
            }
            if (sellRule != null) {
                closePosition(previousPosition, sellRule, tradeDate, account);
                String soldSymbol = previousPosition.getSymbol();
                account.position = null;
                account.positionBuyKlineState = null;
                BuyCandidate candidate = findEarliestBuy(
                        tradeDate, tasks, sellRule.getTime(), Set.of(soldSymbol),
                        actionRules, triggeredRules);
                if (candidate != null) {
                    buyTask = candidate.task();
                    buyRule = candidate.rule();
                }
            }
        } else if (!tasks.isEmpty()) {
            StockWatchingTask overnightTask = tasks.get(0);
            Set<String> nonBuyableSymbols = findNonBuyableSymbols(tradeDate, tasks);
            if (nonBuyableSymbols.contains(overnightTask.getStockCode())) {
                BuyCandidate candidate = findEarliestBuy(
                        tradeDate, tasks, BacktestReplayMode.BUY, null,
                        Set.of(), nonBuyableSymbols, actionRules, triggeredRules);
                if (candidate != null) {
                    buyTask = candidate.task();
                    buyRule = candidate.rule();
                }
            } else {
                BacktestReplayResult overnightResult = replayService.replay(
                        tradeDate, overnightTask.getStockCode(), BacktestReplayMode.OVERNIGHT_BUY, null);
                triggeredRules.addAll(overnightTask, overnightResult.records());
                RuleRecordDTO cancelRule = overnightResult.firstCancelRecord().orElse(null);
                if (cancelRule != null) {
                    actionRules.add(new BacktestRuleAction(overnightTask, cancelRule));
                    BuyCandidate candidate = findEarliestBuy(
                            tradeDate, tasks, BacktestReplayMode.BUY_AFTER_TIME,
                            cancelRule.getTime(), Set.of(), nonBuyableSymbols, actionRules,
                            triggeredRules,
                            new ReusableReplayResult(overnightTask, overnightResult));
                    if (candidate != null) {
                        buyTask = candidate.task();
                        buyRule = candidate.rule();
                    }
                } else {
                    Double dailyTurnover = findDailyTurnover(
                            tradeDate, overnightTask.getStockCode());
                    if (BacktestExecutionPolicy.isOvernightBuyFillable(
                            overnightResult.lastOrderTime(), dailyTurnover)) {
                        buyTask = overnightTask;
                        buyRule = overnightBuyRule(overnightResult, dailyTurnover);
                    }
                }
            }
        }

        if (account.position == null && buyRule != null && buyTask != null) {
            newPosition = openPosition(runId, tradeDate, buyTask, buyRule, account);
            account.position = newPosition;
            if (newPosition == null) {
                buyRule = null;
                buyTask = null;
            }
        }

        BacktestDailyRecord dailyRecord = buildDailyRecord(tradeDate, account);
        if (newPosition != null) {
            account.positionBuyKlineState = dailyRecord.getKlineState();
            if (isLimitUpBreakState(dailyRecord.getKlineState())) {
                account.limitUpBreakCount++;
            }
        }
        persistenceService.saveDay(new BacktestDayWrite(
                runId, tradeDate, previousPosition, newPosition,
                sellRule, buyRule, buyTask, actionRules,
                triggeredRules.actions(), dailyRecord));
        account.previousTotalAsset = dailyRecord.getTotalAsset();
        log.info("完成单日回测 runId={}, date={}, tasks={}, triggeredRules={}, sell={}, buy={}, holding={}, totalAsset={}",
                runId, tradeDate, tasks.size(), triggeredRules.actions().size(),
                sellRule == null ? null : sellRule.getSymbol(),
                buyRule == null ? null : buyRule.getSymbol(),
                account.position == null ? null : account.position.getSymbol(), dailyRecord.getTotalAsset());
    }

    private BuyCandidate findEarliestBuy(LocalDate tradeDate, List<StockWatchingTask> tasks,
                                         int allowedAfterTime, Set<String> excludedSymbols,
                                         List<BacktestRuleAction> actionRules,
                                         TriggeredRuleCollector triggeredRules) {
        return findEarliestBuy(tradeDate, tasks, BacktestReplayMode.BUY_AFTER_TIME,
                allowedAfterTime, excludedSymbols, findNonBuyableSymbols(tradeDate, tasks),
                actionRules, triggeredRules);
    }

    private BuyCandidate findEarliestBuy(LocalDate tradeDate, List<StockWatchingTask> tasks,
                                         BacktestReplayMode replayMode, Integer allowedAfterTime,
                                         Set<String> excludedSymbols,
                                         Set<String> nonBuyableSymbols,
                                         List<BacktestRuleAction> actionRules,
                                         TriggeredRuleCollector triggeredRules) {
        return findEarliestBuy(tradeDate, tasks, replayMode, allowedAfterTime,
                excludedSymbols, nonBuyableSymbols, actionRules, triggeredRules, null);
    }

    private BuyCandidate findEarliestBuy(LocalDate tradeDate, List<StockWatchingTask> tasks,
                                         BacktestReplayMode replayMode, Integer allowedAfterTime,
                                         Set<String> excludedSymbols,
                                         Set<String> nonBuyableSymbols,
                                         List<BacktestRuleAction> actionRules,
                                         TriggeredRuleCollector triggeredRules,
                                         ReusableReplayResult reusableResult) {
        BacktestReplayMode currentMode = replayMode;
        Integer currentAllowedAfterTime = allowedAfterTime;
        while (true) {
            List<ReplayBuyCandidate> candidates = findReplayBuyCandidates(
                    tradeDate, tasks, currentMode, currentAllowedAfterTime,
                    excludedSymbols, nonBuyableSymbols, triggeredRules, reusableResult);
            ReplayBuyCandidate earliest = candidates.stream()
                    .min(java.util.Comparator.comparingInt(candidate -> candidate.buyRule().getTime()))
                    .orElse(null);
            if (earliest == null) {
                return null;
            }
            if (earliest.cancelRule() == null) {
                return new BuyCandidate(earliest.task(), earliest.buyRule());
            }
            actionRules.add(new BacktestRuleAction(earliest.task(), earliest.buyRule()));
            actionRules.add(new BacktestRuleAction(earliest.task(), earliest.cancelRule()));
            currentAllowedAfterTime = earliest.cancelRule().getTime();
            currentMode = BacktestReplayMode.BUY_AFTER_TIME;
        }
    }

    private List<ReplayBuyCandidate> findReplayBuyCandidates(
            LocalDate tradeDate, List<StockWatchingTask> tasks,
            BacktestReplayMode replayMode, Integer allowedAfterTime,
            Set<String> excludedSymbols, Set<String> nonBuyableSymbols,
            TriggeredRuleCollector triggeredRules,
            ReusableReplayResult reusableResult) {
        List<ReplayBuyCandidate> candidates = new ArrayList<>();
        Set<String> replayedSymbols = new HashSet<>();
        if (reusableResult != null) {
            StockWatchingTask task = reusableResult.task();
            String symbol = task.getStockCode();
            if (symbol != null && !excludedSymbols.contains(symbol)
                    && !nonBuyableSymbols.contains(symbol) && replayedSymbols.add(symbol)) {
                triggeredRules.addAll(task, reusableResult.result().records());
                ReplayBuyCandidate candidate = firstReplayBuyCandidate(
                        task, reusableResult.result(), allowedAfterTime);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }
        for (StockWatchingTask task : tasks) {
            String symbol = task.getStockCode();
            if (symbol == null || excludedSymbols.contains(symbol) || !replayedSymbols.add(symbol)) {
                continue;
            }
            if (nonBuyableSymbols.contains(symbol)) {
                log.debug("跳过当日不可买入或缺少日K的候选股票 date={}, symbol={}", tradeDate, symbol);
                continue;
            }
            try {
                BacktestReplayResult result = replayService.replay(
                        tradeDate, symbol, replayMode, allowedAfterTime);
                triggeredRules.addAll(task, result.records());
                ReplayBuyCandidate candidate = firstReplayBuyCandidate(
                        task, result, allowedAfterTime);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            } catch (IllegalArgumentException exception) {
                log.warn("跳过缺少回测数据的候选股票 date={}, symbol={}, reason={}",
                        tradeDate, symbol, exception.getMessage());
            }
        }
        return candidates;
    }

    private ReplayBuyCandidate firstReplayBuyCandidate(
            StockWatchingTask task, BacktestReplayResult result, Integer allowedAfterTime) {
        List<RuleRecordDTO> records = result.records();
        for (RuleRecordDTO buyRule : records.stream()
                .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_BUY)
                        .equals(record.getActionType()))
                .filter(record -> record.getTime() != null
                        && (allowedAfterTime == null || record.getTime() > allowedAfterTime))
                .sorted(java.util.Comparator.comparingInt(RuleRecordDTO::getTime))
                .toList()) {
            RuleRecordDTO cancelRule = records.stream()
                    .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_CANCEL)
                            .equals(record.getActionType()))
                    .filter(record -> record.getTime() != null && record.getTime() > buyRule.getTime())
                    .min(java.util.Comparator.comparingInt(RuleRecordDTO::getTime))
                    .orElse(null);
            if (cancelRule != null || BacktestExecutionPolicy.isIntradayBuyFillable(
                    buyRule, result.lastPrice(), result.limitUpPrice())) {
                buyRule.setPrice(result.limitUpPrice());
                return new ReplayBuyCandidate(task, buyRule, cancelRule);
            }
        }
        return null;
    }

    /**
     * 一次性查询当天 {@code kline_state <= 0} 的候选股票，避免逐只查询日 K，
     * 也避免为全天没有触及涨停的股票读取并回放 Level2 数据。
     *
     * <p>批量查询结果中缺少当日日 K 的股票按停牌处理，只跳过该股票，
     * 不影响同一天其他候选股票继续回放。</p>
     */
    private Set<String> findNonBuyableSymbols(LocalDate tradeDate, List<StockWatchingTask> tasks) {
        List<String> symbols = tasks.stream()
                .map(StockWatchingTask::getStockCode)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (symbols.isEmpty()) {
            return Set.of();
        }

        List<StockDailyEntity> dailyRows = stockDailyService.list(
                Wrappers.<StockDailyEntity>query()
                        .select("stock_code", "kline_state")
                        .eq("trade_date", tradeDate)
                        .in("stock_code", symbols));
        Set<String> existingDailySymbols = new HashSet<>(dailyRows.size());
        Set<String> nonBuyableSymbols = new HashSet<>(symbols.size());
        for (StockDailyEntity daily : dailyRows) {
            if (daily.getStockCode() == null) {
                continue;
            }
            existingDailySymbols.add(daily.getStockCode());
            if (daily.getKlineState() != null
                    && daily.getKlineState() <= 0) {
                nonBuyableSymbols.add(daily.getStockCode());
            }
        }
        for (String symbol : symbols) {
            if (!existingDailySymbols.contains(symbol)) {
                nonBuyableSymbols.add(symbol);
                log.warn("跳过缺少当日日K的候选股票 date={}, symbol={}", tradeDate, symbol);
            }
        }
        return nonBuyableSymbols;
    }

    /** 查询当日日 K 成交额，单位万元，供隔夜委托成交判定使用。 */
    private Double findDailyTurnover(LocalDate tradeDate, String symbol) {
        List<StockDailyEntity> rows = stockDailyService.list(
                Wrappers.<StockDailyEntity>query()
                        .select("turnover")
                        .eq("trade_date", tradeDate)
                        .eq("stock_code", symbol)
                        .last("LIMIT 1"));
        return rows.isEmpty() ? null : rows.get(0).getTurnover();
    }

    /**
     * 买入当天已经炸板的持仓，下一交易日直接按日 K 开盘价卖出。
     *
     * <p>该规则不依赖下一交易日 Level2，可覆盖历史只备份 {@code kline_state > 0}
     * 股票 Tick 的数据范围。炸板状态口径为 11、12、13；成交价取日 K 开盘价，
     * 成交时间记为开盘集合竞价结束的 09:25。</p>
     */
    private RuleRecordDTO createBreakBoardNextOpenSellRule(BacktestPosition position,
                                                            LocalDate tradeDate,
                                                            Integer buyKlineState) {
        if (!tradeDate.isAfter(position.getBuyDate()) || !isLimitUpBreakState(buyKlineState)) {
            return null;
        }

        StockDailyEntity sellDaily = findStockDaily(position.getSymbol(), tradeDate);
        int openPrice = cents(sellDaily.getOpenPrice());
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(RuleConstant.TRADING_MODE_SELL);
        rule.setRuleCode(RuleConstant.SELL_BACKTEST_LIMIT_UP_BREAK_NEXT_OPEN);
        rule.setSymbol(position.getSymbol());
        rule.setTime(ConstantUtil.TIME_925);
        rule.setPrice(openPrice);
        if (sellDaily.getPrevClose() != null && sellDaily.getPrevClose() > 0) {
            rule.setIncrease((sellDaily.getOpenPrice() - sellDaily.getPrevClose())
                    * 100D / sellDaily.getPrevClose());
        }
        rule.setRemark("回测卖出 - 买入日炸板，下一交易日按开盘价成交；买入日K线状态="
                + buyKlineState);
        return rule;
    }

    private boolean isLimitUpBreakState(Integer klineState) {
        return klineState != null && klineState >= 11 && klineState <= 13;
    }

    private RuleRecordDTO overnightBuyRule(BacktestReplayResult result, Double dailyTurnover) {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(RuleConstant.TRADING_MODE_BUY);
        rule.setRuleCode(OVERNIGHT_BUY_RULE_CODE);
        rule.setSymbol(result.symbol());
        rule.setTime(BacktestExecutionPolicy.OVERNIGHT_FILL_TIME);
        rule.setLastOrderTime(result.lastOrderTime());
        rule.setPrice(result.limitUpPrice());
        if (BacktestExecutionPolicy.isOvernightBuyFillable(result.lastOrderTime())) {
            rule.setRemark("隔夜涨停买单未撤单，回放结束队首时间满足成交条件");
        } else {
            rule.setRemark("隔夜涨停买单未撤单，当日成交额超过4000万元，默认成交；成交额="
                    + dailyTurnover + "万元");
        }
        return rule;
    }

    /** 查询回测首日的前一交易日，用该日收盘数据重新生成首日推荐任务。 */
    private LocalDate findPreviousTradeDate(LocalDate tradeDate) {
        StockTradeCalendar previous = calendarService.getOne(
                Wrappers.<StockTradeCalendar>query()
                        .select("trade_date")
                        .lt("trade_date", tradeDate)
                        .orderByDesc("trade_date")
                        .last("LIMIT 1"));
        if (previous == null || previous.getTradeDate() == null) {
            throw new IllegalStateException(tradeDate + " 之前没有交易日历数据，无法执行回测前选股");
        }
        return previous.getTradeDate();
    }

    private BacktestPosition openPosition(long runId, LocalDate tradeDate, StockWatchingTask task,
                                          RuleRecordDTO buyRule, AccountState account) {
        int symbolId = SymbolUtil.fastSymbolToInt(task.getStockCode());
        TradeCalculator.OrderResultContainer orders = TradeCalculator.calculateBuyOrders(
                symbolId, account.cash.doubleValue(), buyRule.getPrice());
        int quantity = 0;
        for (int i = 0; i < orders.getSize(); i++) {
            quantity += orders.getOrder(i).quantity;
        }
        if (quantity == 0) {
            return null;
        }

        BigDecimal buyAmount = tradeAmount(quantity, buyRule.getPrice());
        BigDecimal buyFee = BigDecimal.valueOf(
                        TradeCalculator.calculateBuyFee(quantity, buyRule.getPrice()))
                .setScale(2, RoundingMode.HALF_UP);
        account.cash = account.cash.subtract(buyAmount).subtract(buyFee);
        BacktestPosition position = new BacktestPosition();
        position.setBacktestRunId(runId);
        position.setWatchingTaskId(task.getId());
        position.setSymbol(task.getStockCode());
        position.setSymbolName(task.getStockName());
        position.setTradeMode(task.getTradeMode());
        position.setLimitUpScore(task.getLimitUpScore());
        position.setBuyDate(tradeDate);
        position.setBuyTime(buyRule.getTime());
        position.setBuyPrice(buyRule.getPrice());
        position.setQuantity(quantity);
        position.setBuyAmount(buyAmount);
        position.setBuyFee(buyFee);
        position.setSellFee(ZERO_MONEY);
        position.setStatus(POSITION_OPEN);
        position.setHoldingTradeDays(1);
        position.setMaxFloatingReturnRate(BigDecimal.ZERO);
        position.setMaxDrawdownRate(BigDecimal.ZERO);
        position.setLimitUpBreakDays(0);
        return position;
    }

    private void closePosition(BacktestPosition position, RuleRecordDTO sellRule,
                               LocalDate tradeDate, AccountState account) {
        if (sellRule.getPrice() == null || sellRule.getPrice() <= 0) {
            throw new IllegalStateException("卖出规则缺少有效价格: " + position.getSymbol() + " " + tradeDate);
        }
        BigDecimal sellAmount = tradeAmount(position.getQuantity(), sellRule.getPrice());
        account.cash = account.cash.add(sellAmount);
        position.setSellDate(tradeDate);
        position.setSellTime(sellRule.getTime());
        position.setSellPrice(sellRule.getPrice());
        position.setSellAmount(sellAmount);
        position.setStatus(POSITION_CLOSED);
        BigDecimal realizedProfit = sellAmount.subtract(position.getBuyAmount())
                .subtract(valueOrZero(position.getBuyFee()))
                .subtract(valueOrZero(position.getSellFee()));
        position.setRealizedProfit(realizedProfit);
        position.setReturnRate(rate(realizedProfit,
                position.getBuyAmount().add(valueOrZero(position.getBuyFee()))));
    }

    private BacktestDailyRecord buildDailyRecord(LocalDate tradeDate, AccountState account) {
        BacktestDailyRecord daily = new BacktestDailyRecord();
        daily.setAvailableCash(account.cash);
        daily.setQuantity(0);
        daily.setPositionMarketValue(ZERO_MONEY);
        daily.setAccountStatus(ACCOUNT_EMPTY);

        BigDecimal totalAsset = account.cash;
        if (account.position != null) {
            BacktestPosition position = account.position;
            StockDailyEntity stockDaily = findStockDaily(position.getSymbol(), tradeDate);
            int closePrice = cents(stockDaily.getClosePrice());
            BigDecimal marketValue = tradeAmount(position.getQuantity(), closePrice);
            BigDecimal positionProfit = marketValue.subtract(position.getBuyAmount())
                    .subtract(valueOrZero(position.getBuyFee()));
            BigDecimal positionReturn = rate(positionProfit,
                    position.getBuyAmount().add(valueOrZero(position.getBuyFee())));
            updatePositionMetrics(position, stockDaily, positionReturn);

            totalAsset = account.cash.add(marketValue);
            daily.setPositionId(position.getId());
            daily.setAccountStatus(ACCOUNT_HOLDING);
            daily.setSymbol(position.getSymbol());
            daily.setSymbolName(position.getSymbolName());
            daily.setQuantity(position.getQuantity());
            daily.setClosePrice(closePrice);
            daily.setPositionMarketValue(marketValue);
            daily.setPositionReturnRate(positionReturn);
            daily.setKlineState(stockDaily.getKlineState());
            if (stockDaily.getAdjustFactor() != null) {
                daily.setAdjustFactor(BigDecimal.valueOf(stockDaily.getAdjustFactor()));
            }
        } else {
            daily.setPositionReturnRate(BigDecimal.ZERO);
        }

        daily.setTotalAsset(totalAsset);
        daily.setDailyReturnRate(rate(totalAsset.subtract(account.previousTotalAsset), account.previousTotalAsset));
        daily.setCumulativeReturnRate(rate(totalAsset.subtract(INITIAL_CAPITAL), INITIAL_CAPITAL));
        return daily;
    }

    private void updatePositionMetrics(BacktestPosition position, StockDailyEntity daily,
                                       BigDecimal currentReturn) {
        BigDecimal maxReturn = valueOrZero(position.getMaxFloatingReturnRate()).max(currentReturn);
        position.setMaxFloatingReturnRate(maxReturn);
        BigDecimal drawdown = maxReturn.subtract(currentReturn).max(BigDecimal.ZERO);
        position.setMaxDrawdownRate(valueOrZero(position.getMaxDrawdownRate()).max(drawdown));
        Integer klineState = daily.getKlineState();
        if (klineState != null && klineState >= 11 && klineState <= 13) {
            position.setLimitUpBreakDays(valueOrZero(position.getLimitUpBreakDays()) + 1);
        }
    }

    private StockDailyEntity findStockDaily(String symbol, LocalDate tradeDate) {
        StockDailyEntity daily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, symbol)
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .last("LIMIT 1"));
        if (daily == null || daily.getClosePrice() == null) {
            throw new IllegalStateException(tradeDate + " 缺少持仓股票 " + symbol + " 的日K数据");
        }
        return daily;
    }

    private BigDecimal tradeAmount(int quantity, int price) {
        return BigDecimal.valueOf(quantity)
                .multiply(BigDecimal.valueOf(price))
                .movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
        if (numerator.signum() == 0 || denominator.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 8, RoundingMode.HALF_UP);
    }

    private int cents(Double price) {
        if (price == null || price <= 0) {
            throw new IllegalStateException("日K价格无效: " + price);
        }
        return (int) Math.round(price * 100);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("回测开始和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("回测开始日期不能晚于结束日期");
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record BuyCandidate(StockWatchingTask task, RuleRecordDTO rule) {
    }

    private record ReplayBuyCandidate(StockWatchingTask task,
                                      RuleRecordDTO buyRule,
                                      RuleRecordDTO cancelRule) {
    }

    /** 已经完成的回放结果，可在后续时间窗口筛选中复用，避免同一股票重复回放丢失规则。 */
    private record ReusableReplayResult(StockWatchingTask task, BacktestReplayResult result) {
    }

    /**
     * 收集当日所有回放产生的原始规则，并按数据库唯一键口径去重。
     *
     * <p>这里不判断规则是否最终成交。买入规则原样保留 {@code time} 和
     * {@code lastOrderTime}，由展示层按沪市 500ms、深市 100ms 的延迟口径判断。</p>
     */
    private static final class TriggeredRuleCollector {
        private final List<BacktestRuleAction> actions = new ArrayList<>();
        private final Set<RuleEventKey> keys = new HashSet<>();

        private void addAll(StockWatchingTask task, List<RuleRecordDTO> records) {
            for (RuleRecordDTO rule : records) {
                add(new BacktestRuleAction(task, rule));
            }
        }

        private void addAll(BacktestPosition position, List<RuleRecordDTO> records) {
            for (RuleRecordDTO rule : records) {
                add(new BacktestRuleAction(position, rule));
            }
        }

        private void add(BacktestRuleAction action) {
            RuleRecordDTO rule = action.rule();
            RuleEventKey key = new RuleEventKey(
                    rule.getSymbol(), rule.getActionType(), rule.getRuleCode(), rule.getTime());
            if (keys.add(key)) {
                actions.add(action);
            }
        }

        private List<BacktestRuleAction> actions() {
            return List.copyOf(actions);
        }
    }

    /** 与 rule_execute_record 的单日事件唯一键保持一致。 */
    private record RuleEventKey(String symbol, Integer actionType, Integer ruleCode, Integer time) {
    }

    private static final class AccountState {
        private BigDecimal cash;
        private BigDecimal previousTotalAsset;
        private BacktestPosition position;
        private Integer positionBuyKlineState;
        private int limitUpBreakCount;

        private AccountState(BigDecimal cash, BigDecimal previousTotalAsset) {
            this.cash = cash;
            this.previousTotalAsset = previousTotalAsset;
        }
    }
}
