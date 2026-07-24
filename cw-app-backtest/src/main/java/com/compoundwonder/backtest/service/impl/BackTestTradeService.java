package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.orderbook.data.BacktestDailyTickBatch;
import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.DisruptorOrderBookEngine;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.MarketSessionSpec;
import com.compoundwonder.core.engine.TradeExecutionState;
import com.compoundwonder.core.engine.PriceLevel;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplateFactory;
import com.compoundwonder.common.strategy.volume.VolumeStateClassifier;
import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.strategy.DefaultTradeExecutionTemplateFactory;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.compoundwonder.util.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 单股票历史订单簿回测编排服务。
 *
 * <p>负责准备订单簿、驱动 ClickHouse Tick 数据源、等待 Disruptor 消费完成并返回规则记录；
 * 不负责 Level2 的具体查询和字段转换。</p>
 */
@Slf4j
@Service
public class BackTestTradeService {

    private static final int HISTORICAL_VOLUME_LOOKBACK = 200;
    private static final int VOLUME_STATE_UNAVAILABLE = -2;

    private final DisruptorOrderBookEngine orderBookEngine;
    private final BacktestTickDataSource tickDataSource;
    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final BacktestOrderExecutionGateway executionGateway;
    private final TradeExecutionTemplateFactory templateFactory;
    private final Duration replayTimeout;

    @Autowired
    public BackTestTradeService(DisruptorOrderBookEngine orderBookEngine,
                                BacktestTickDataSource tickDataSource,
                                StockDailyService stockDailyService,
                                StockTradeCalendarService stockTradeCalendarService,
                                StockEmotionCycleDailyService stockEmotionCycleDailyService,
                                BacktestOrderExecutionGateway executionGateway,
                                TradeExecutionTemplateFactory templateFactory,
                                @Value("${backtest.replay-timeout-seconds:120}") long replayTimeoutSeconds) {
        this.orderBookEngine = orderBookEngine;
        this.tickDataSource = tickDataSource;
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
        this.executionGateway = executionGateway;
        this.templateFactory = templateFactory;
        this.replayTimeout = Duration.ofSeconds(replayTimeoutSeconds);
    }

    /** 保留控制器和既有单元测试的手工组装入口。 */
    public BackTestTradeService(DisruptorOrderBookEngine orderBookEngine,
                                BacktestTickDataSource tickDataSource,
                                StockDailyService stockDailyService,
                                StockTradeCalendarService stockTradeCalendarService,
                                StockEmotionCycleDailyService stockEmotionCycleDailyService,
                                BacktestOrderExecutionGateway executionGateway,
                                long replayTimeoutSeconds) {
        this(orderBookEngine, tickDataSource, stockDailyService,
                stockTradeCalendarService, stockEmotionCycleDailyService,
                executionGateway, new DefaultTradeExecutionTemplateFactory(),
                replayTimeoutSeconds);
    }

    /**
     * 执行一次单股票、单交易日回测。
     *
     * <p>Disruptor 在应用启动时已经长期运行。该方法只清理并注册本轮订单簿，
     * 不启动或关闭 Disruptor。方法加锁是为了防止多个 HTTP 请求共享同一套 Handler
     * 私有数组时互相覆盖。</p>
     *
     * @param date 回测交易日，格式 yyyy-MM-dd
     * @param stockCode 60 或 00 开头的六位主板代码
     * @param direction 1 表示测试买入规则，2 表示测试卖出规则
     * @return 本轮对应方向触发的规则记录；买入回放同时返回集合竞价撤单记录
     */
    public List<RuleRecordDTO> backTest(String date, String stockCode, int direction) {
        validateDirection(direction);
        validateStockCode(stockCode);
        LocalDate tradeDate = LocalDate.parse(date);
        BacktestReplayMode mode = direction == 1 ? BacktestReplayMode.BUY : BacktestReplayMode.SELL;
        return replay(tradeDate, stockCode, mode, null).records().stream()
                .filter(record -> matchesDirection(record, direction))
                .toList();
    }

    /**
     * 按指定场景执行一次单股票、单交易日订单簿回放。
     *
     * <p>{@link BacktestReplayMode#BUY_AFTER_TIME} 仍会从当天第一条 Tick 开始构建订单簿，
     * 只是到达允许时间后的第一条 Tick 前才打开买入监控。</p>
     */
    public synchronized BacktestReplayResult replay(LocalDate tradeDate, String stockCode,
                                                     BacktestReplayMode mode, Integer allowedAfterTime) {
        return replayInternal(tradeDate, stockCode, mode, allowedAfterTime, null,
                (date, symbol, consumer) -> tickDataSource.replay(date, symbol, consumer));
    }

    /**
     * 使用已经按交易日批量加载的数据执行回放。
     *
     * <p>完整历史回测同一天可能多次重建同一股票的订单簿，此重载只复用原始 Tick，
     * 不复用订单簿或规则记录，交易逻辑与单股票接口保持一致。</p>
     */
    public synchronized BacktestReplayResult replay(
            LocalDate tradeDate, String stockCode, BacktestReplayMode mode,
            Integer allowedAfterTime, BacktestDailyTickBatch dailyTicks) {
        return replay(tradeDate, stockCode, mode, allowedAfterTime, dailyTicks, null);
    }

    /**
     * 使用任务或持仓保存的交易模式执行回放，避免首板和小市值首板共用交易参数。
     */
    public synchronized BacktestReplayResult replay(
            LocalDate tradeDate, String stockCode, BacktestReplayMode mode,
            Integer allowedAfterTime, BacktestDailyTickBatch dailyTicks, Integer tradeMode) {
        if (!tradeDate.equals(dailyTicks.tradeDate())) {
            throw new IllegalArgumentException("回放日期与批量 Tick 日期不一致: replay="
                    + tradeDate + ", batch=" + dailyTicks.tradeDate());
        }
        return replayInternal(tradeDate, stockCode, mode, allowedAfterTime, tradeMode,
                (date, symbol, consumer) -> dailyTicks.replay(symbol, consumer));
    }

    /** 一次加载完整历史回测当天会用到的全部股票 Tick。 */
    public BacktestDailyTickBatch loadDailyTicks(LocalDate tradeDate, Set<String> stockCodes) {
        for (String stockCode : stockCodes) {
            validateStockCode(stockCode);
        }
        return tickDataSource.loadDay(tradeDate, stockCodes);
    }

    private BacktestReplayResult replayInternal(
            LocalDate tradeDate, String stockCode, BacktestReplayMode mode,
            Integer allowedAfterTime, Integer tradeMode, TickReplaySource replaySource) {
        validateStockCode(stockCode);
        validateReplayMode(mode, allowedAfterTime);
        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        OrderBookSession session = buildSession(tradeDate, stockCode, mode, tradeMode);
        OrderBook orderBook = session.orderBook();

        // 每次单票回放独占两个 Handler；先清理上一轮，再注册当前唯一市场会话。
        orderBookEngine.reset();
        executionGateway.clear();
        orderBookEngine.registerSession(symbolId, session);

        boolean replayStarted = false;
        boolean processed = false;
        try {
            replayStarted = true;
            Consumer<TickData> consumer = replayConsumer(symbolId, mode, allowedAfterTime);
            long tickCount = replaySource.replay(tradeDate, stockCode, consumer);
            if (tickCount == 0) {
                throw new IllegalStateException(tradeDate + " 没有找到股票 " + stockCode + " 的 Level2 Tick");
            }

            orderBookEngine.awaitProcessed(replayTimeout);
            processed = true;
            logLimitUpBuyQueue(session);
            List<RuleRecordDTO> records = orderBookEngine.drainRuleRecords();
            fillLastOrderTime(records.stream()
                    .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_BUY)
                            .equals(record.getActionType()))
                    .toList(), orderBook);
            BacktestReplayResult result = new BacktestReplayResult(
                    tradeDate, stockCode, session.getSecurityName(), mode, records,
                    session.executionState().transactionStatus(), orderBook.getLastPriceOrderTime(),
                    session.getLimitUpPrice(), orderBook.getLastPrice(), tickCount,
                    session.template().executionProfile().openingAuctionBuyAllowed(),
                    session.template().executionProfile().earliestContinuousBuyTime(),
                    session.template().executionProfile().openingAuctionBlockReason());
            log.info("回测完成 date={}, stockCode={}, mode={}, tickCount={}, ruleCount={}, orderBook={}",
                    tradeDate, stockCode, mode, tickCount, records.size(), orderBook);
            return result;
        } finally {
            // 数据源异常也要等待已发布事件，禁止在消费者仍运行时清空 Handler 会话数组。
            if (replayStarted && !processed) {
                awaitPartiallyPublishedTicks();
            }
            orderBookEngine.reset();
            executionGateway.clear();
        }
    }

    @FunctionalInterface
    private interface TickReplaySource {
        long replay(LocalDate tradeDate, String stockCode, Consumer<TickData> tickConsumer);
    }

    private Consumer<TickData> replayConsumer(int symbolId, BacktestReplayMode mode,
                                              Integer allowedAfterTime) {
        if (mode != BacktestReplayMode.BUY_AFTER_TIME) {
            return orderBookEngine::publish;
        }
        AtomicBoolean monitoringOpened = new AtomicBoolean(false);
        return tick -> {
            if (!monitoringOpened.get() && tick.time > allowedAfterTime) {
                // 控制事件必须先于同一条 Tick 发布，使允许时点后的第一条行情即可参加判断。
                TickData control = new TickData();
                control.symbolId = symbolId;
                control.time = tick.time;
                control.dataType = 3;
                control.type = 1;
                orderBookEngine.publish(control);
                monitoringOpened.set(true);
            }
            orderBookEngine.publish(tick);
        };
    }

    /**
     * 按回放结束时的最新成交价，从该价位委托队头补充规则记录的最后委托时间。
     */
    static void fillLastOrderTime(List<RuleRecordDTO> records, OrderBook orderBook) {
        int lastOrderTime = orderBook.getLastPriceOrderTime();
        if (lastOrderTime == 0) {
            return;
        }
        for (RuleRecordDTO record : records) {
            record.setLastOrderTime(lastOrderTime);
        }
    }

    /**
     * 回放完成后打印一次涨停价买方委托队列，便于与旧 List 订单簿结果核对。
     */
    private void logLimitUpBuyQueue(OrderBookSession session) {
        OrderBook orderBook = session.orderBook();
        if (orderBook.getStatus() % 2 != 1) {
            return;
        }

        PriceLevel limitUpLevel = orderBook.getPriceLevel(session.getLimitUpPrice());
        if (limitUpLevel == null || limitUpLevel.getBuyHead() == null) {
            log.warn("股票 {} 状态为涨停，但涨停价 {} 没有买方委托队列",
                    session.getSymbol(), session.getLimitUpPrice());
            return;
        }

        TickNode head = limitUpLevel.getBuyHead();
        TickNode tail = limitUpLevel.getBuyTail();
        int printedCount = 0;
        StringBuilder quantities = new StringBuilder();
        for (TickNode node = head; node != null && printedCount < 200; node = node.getNext()) {
            if (printedCount > 0 && printedCount % 10 != 0) {
                quantities.append(',');
            }
            quantities.append(node.getQuantity());
            printedCount++;
            if (printedCount % 10 == 0) {
                quantities.append(System.lineSeparator());
            }
        }

        log.info("股票 {} 涨停封单共计：{} 单，共计：{} 股，"
                        + "队首委托(剩余数量:{},时间:{},订单号:{})，"
                        + "队尾委托(剩余数量:{},时间:{},订单号:{})",
                session.getSymbol(), limitUpLevel.getBuyOrderCount(), limitUpLevel.getBuyQuantity(),
                head.getQuantity(), head.getTime(), head.getOrderId(),
                tail.getQuantity(), tail.getTime(), tail.getOrderId());
        log.info("股票 {} 涨停封单前 {} 单（队首到队尾）：{}{}",
                session.getSymbol(), printedCount, System.lineSeparator(), quantities);
    }

    /**
     * 使用回测当天日 K 的参考昨收价初始化订单簿价格区间。
     *
     * <p>当天日 K 提供 prevClose 和流通股本；名称和所有策略统计仍使用回测日前的数据。</p>
     */
    OrderBook buildOrderBook(LocalDate tradeDate, String stockCode, int direction) {
        validateDirection(direction);
        return buildOrderBook(tradeDate, stockCode,
                direction == 1 ? BacktestReplayMode.BUY : BacktestReplayMode.SELL);
    }

    OrderBook buildOrderBook(LocalDate tradeDate, String stockCode, BacktestReplayMode mode) {
        return buildOrderBook(tradeDate, stockCode, mode, null);
    }

    private OrderBook buildOrderBook(LocalDate tradeDate, String stockCode,
                                     BacktestReplayMode mode, Integer requestedTradeMode) {
        OrderBookSession session = buildSession(tradeDate, stockCode, mode, requestedTradeMode);
        TradeStaticFacts facts = session.facts();
        OrderBook legacy = new OrderBook(stockCode, session.getCirculation(),
                session.getClosePrice() / 100D, facts.maxVolume());
        legacy.setSecurityName(session.getSecurityName());
        legacy.setDate(session.getDate());
        legacy.setTradeMode(facts.tradeMode());
        legacy.setLbcs(facts.lbcs());
        legacy.setMaxHs(facts.maxHs());
        legacy.setInitialMarketValue(facts.initialMarketValue());
        legacy.setThreeDaysTurnover(facts.threeDaysTurnover());
        legacy.setTwoDaysTurnover(facts.twoDaysTurnover());
        legacy.setYesterdayTurnover(facts.yesterdayTurnover());
        legacy.setOneWordLimitUp(facts.oneWordLimitUp());
        legacy.setAverageLimitUpHeight(facts.averageLimitUpHeight());
        legacy.setNextTradingDay(facts.nextTradingDay());
        legacy.setTransactionStatus(session.executionState().transactionStatus());
        return legacy;
    }

    /**
     * 将数据库冷数据一次性编译成“市场参数 + 静态事实 + 模板 + 初始状态”。
     * 逐笔回放开始后 Handler 不再查询日 K、交易日历或市场情绪表。
     */
    private OrderBookSession buildSession(LocalDate tradeDate, String stockCode,
                                          BacktestReplayMode mode, Integer requestedTradeMode) {
        // 调用 stockDailyService.list 查询回测日及量能状态计算所需的前 202 根日K。
        List<StockDailyEntity> dailyRows = stockDailyService.list(
                Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, stockCode)
                        .le(StockDailyEntity::getTradeDate, tradeDate)
                        .orderByDesc(StockDailyEntity::getTradeDate)
                        .last("LIMIT 203"));
        if (dailyRows.isEmpty() || !tradeDate.equals(dailyRows.get(0).getTradeDate())) {
            throw new IllegalArgumentException(tradeDate + " 没有股票 " + stockCode + " 的当日日 K 数据");
        }

        StockDailyEntity currentDaily = dailyRows.get(0);
        List<StockDailyEntity> history = dailyRows.stream()
                .filter(daily -> daily.getTradeDate() != null && daily.getTradeDate().isBefore(tradeDate))
                .limit(200)
                .toList();
        if (history.isEmpty()) {
            throw new IllegalArgumentException(tradeDate + " 之前没有股票 " + stockCode + " 的日 K 数据");
        }
        StockDailyEntity previousDaily = history.get(0);
        if (currentDaily.getPrevClose() == null || currentDaily.getPrevClose() <= 0) {
            throw new IllegalStateException("回测当日日 K 的 prevClose 缺失: " + stockCode + " " + tradeDate);
        }
        if (currentDaily.getFloatShares() == null || currentDaily.getFloatShares() <= 0) {
            throw new IllegalStateException("回测当日日 K 的流通股本缺失: " + stockCode + " " + tradeDate);
        }
        long circulation = currentDaily.getFloatShares();
        long maxVolume = history.stream()
                .map(StockDailyEntity::getVolume)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        int lbcs = Math.max(0, valueOrZero(previousDaily.getConsecutiveLimitUpDays()));
        int initialMarketValue = calculateInitialMarketValue(previousDaily, lbcs);
        int tradeMode = resolveTradeMode(requestedTradeMode, lbcs, initialMarketValue);
        // 调用 calculateVolumeState 计算前一交易日基于其更早 200 根K线的量能状态。
        int yesterdayVolumeState = calculateVolumeState(dailyRows, 1);
        // 调用 calculateVolumeState 计算前两个交易日基于其更早 200 根K线的量能状态。
        int twoDaysAgoVolumeState = calculateVolumeState(dailyRows, 2);
        // 买入回放不会进入卖出状态，只有 SELL 模式才查询三日换手、市场高度和交易日间隔。
        SellHistoryFacts sellHistory = mode == BacktestReplayMode.SELL
                ? loadSellHistory(history, tradeDate, stockCode)
                : SellHistoryFacts.EMPTY;
        TradeStaticFacts facts = new TradeStaticFacts(
                tradeMode, lbcs, maxVolume, maxVolume * 100.0 / circulation,
                initialMarketValue, sellHistory.threeDaysTurnover(),
                sellHistory.twoDaysTurnover(), valueOrZero(previousDaily.getTurnoverRate()),
                sellHistory.oneWordLimitUp(), sellHistory.averageLimitUpHeight(),
                sellHistory.nextTradingDay(),
                valueOrZero(previousDaily.getKlineState()),
                history.size() > 1 ? valueOrZero(history.get(1).getKlineState()) : 0,
                valueOrZero(previousDaily.getAmplitude()),
                history.size() > 1 ? valueOrZero(history.get(1).getTurnoverRate()) : -1D,
                history.size() > 1 ? valueOrZero(history.get(1).getAmplitude()) : -1D,
                yesterdayVolumeState, twoDaysAgoVolumeState);
        MarketSessionSpec spec = MarketSessionSpec.fromPreviousClose(
                stockCode, previousDaily.getStockName(), tradeDate.toString(),
                circulation, currentDaily.getPrevClose());
        OrderBook orderBook = new OrderBook(spec.limitUpPrice(), spec.limitDownPrice());
        TradeExecutionTemplate template = templateFactory.compile(facts);
        return new OrderBookSession(spec, facts, orderBook, template,
                new TradeExecutionState(initialTransactionStatus(mode)));
    }

    /** 按指定交易日之前最多 200 根有效日K的最大换手率计算该交易日量能状态。 */
    static int calculateVolumeState(List<StockDailyEntity> dailyRows, int dayIndex) {
        // 调用 dailyRows.size 校验目标交易日在倒序日K列表中的位置。
        if (dailyRows == null || dayIndex < 0 || dayIndex >= dailyRows.size()) return VOLUME_STATE_UNAVAILABLE;
        // 调用 dailyRows.get 读取待计算量能状态的日K。
        StockDailyEntity daily = dailyRows.get(dayIndex);
        // 调用日K访问器读取分类所需的当日换手率、振幅和K线形态。
        Double turnoverRate = daily.getTurnoverRate();
        Double amplitude = daily.getAmplitude();
        Integer klineState = daily.getKlineState();
        // 调用 Double.isFinite 校验当日换手率和振幅。
        if (turnoverRate == null || amplitude == null || klineState == null || !Double.isFinite(turnoverRate) || !Double.isFinite(amplitude)
                || turnoverRate < 0D || amplitude < 0D) return VOLUME_STATE_UNAVAILABLE;
        // 调用 Math.min 计算该交易日之后最多 200 根更早日K的窗口终点。
        int historyEndExclusive = Math.min(dailyRows.size(), dayIndex + HISTORICAL_VOLUME_LOOKBACK + 1);
        double historicalMaxTurnoverRate = 0D;
        for (int index = dayIndex + 1; index < historyEndExclusive; index++) {
            // 调用 dailyRows.get 和 getTurnoverRate 读取一根更早日K的换手率。
            Double historicalTurnoverRate = dailyRows.get(index).getTurnoverRate();
            // 调用 Double.isFinite 忽略缺失、非有限数和非正数换手率。
            if (historicalTurnoverRate == null || !Double.isFinite(historicalTurnoverRate) || historicalTurnoverRate <= 0D) continue;
            // 调用 Math.max 更新最近 200 根有效日K的最大换手率。
            historicalMaxTurnoverRate = Math.max(historicalMaxTurnoverRate, historicalTurnoverRate);
        }
        if (historicalMaxTurnoverRate <= 0D) return VOLUME_STATE_UNAVAILABLE;
        // 调用 VolumeStateClassifier.classify 按统一公式划分缩量、正常量或放量。
        return VolumeStateClassifier.classify(turnoverRate, amplitude, klineState, historicalMaxTurnoverRate);
    }

    private int resolveTradeMode(Integer requestedTradeMode, int lbcs,
                                 int initialMarketValue) {
        if (requestedTradeMode != null) {
            return TradeMode.fromCode(requestedTradeMode).code();
        }
        if (lbcs >= 2) {
            return TradeMode.RELAY_LIMIT_UP.code();
        }
        return initialMarketValue < 119_999
                ? TradeMode.SMALL_CAP_FIRST_BOARD.code()
                : TradeMode.FIRST_BOARD.code();
    }

    private int initialTransactionStatus(BacktestReplayMode mode) {
        return switch (mode) {
            case BUY -> 1;
            case SELL -> -1;
            case OVERNIGHT_BUY -> 2;
            case BUY_AFTER_TIME -> 0;
        };
    }

    /**
     * 按旧实盘持仓口径补充卖出规则依赖的最近三日日 K 统计。
     * 市场平均高度窗口是交易日前 15 个自然日，不是 15 个交易日。
     */
    private SellHistoryFacts loadSellHistory(List<StockDailyEntity> history,
                                             LocalDate tradeDate, String stockCode) {
        if (history.size() < 3) {
            throw new IllegalArgumentException(tradeDate + " 之前股票 " + stockCode
                    + " 的日 K 不足 3 个交易日，无法初始化卖出订单簿");
        }

        StockDailyEntity yesterday = history.get(0);
        StockDailyEntity twoDaysAgo = history.get(1);
        StockDailyEntity threeDaysAgo = history.get(2);
        double yesterdayTurnover = valueOrZero(yesterday.getTurnoverRate());
        double twoDaysAgoTurnover = valueOrZero(twoDaysAgo.getTurnoverRate());
        double threeDaysAgoTurnover = valueOrZero(threeDaysAgo.getTurnoverRate());

        double twoDaysTurnover = (yesterdayTurnover + twoDaysAgoTurnover) / 2;
        double threeDaysTurnover =
                (yesterdayTurnover + twoDaysAgoTurnover + threeDaysAgoTurnover) / 3;
        int oneWordLimitUp = countConsecutiveOneWordLimitUps(
                yesterday, twoDaysAgo, threeDaysAgo);

        Integer averageLimitUpHeight = stockEmotionCycleDailyService
                .queryRecentAverageLimitUpHeight(tradeDate.minusDays(15), tradeDate);
        if (averageLimitUpHeight == null) {
            throw new IllegalStateException(tradeDate + " 之前 15 日没有市场最高连板数据");
        }

        LocalDate nextTradeDay = stockTradeCalendarService.findNextTradeDay(tradeDate);
        if (nextTradeDay == null) {
            throw new IllegalStateException(tradeDate + " 之后没有交易日历数据");
        }
        int nextTradingDay = (int) ChronoUnit.DAYS.between(tradeDate, nextTradeDay) - 1;
        return new SellHistoryFacts(threeDaysTurnover, twoDaysTurnover,
                oneWordLimitUp, averageLimitUpHeight, nextTradingDay);
    }

    private record SellHistoryFacts(double threeDaysTurnover,
                                    double twoDaysTurnover,
                                    int oneWordLimitUp,
                                    int averageLimitUpHeight,
                                    int nextTradingDay) {
        private static final SellHistoryFacts EMPTY =
                new SellHistoryFacts(0D, 0D, 0, 0, 0);
    }

    private int countConsecutiveOneWordLimitUps(StockDailyEntity... dailyRows) {
        int count = 0;
        for (StockDailyEntity daily : dailyRows) {
            if (valueOrZero(daily.getKlineState()) != 3) {
                break;
            }
            count++;
        }
        return count;
    }

    private int calculateInitialMarketValue(StockDailyEntity previousDaily, int consecutiveLimitUpDays) {
        if (previousDaily.getFloatMarketCap() == null) {
            return 0;
        }
        return (int) Math.round(previousDaily.getFloatMarketCap()
                / Math.pow(1.1, consecutiveLimitUpDays));
    }

    private boolean matchesDirection(RuleRecordDTO record, int direction) {
        Integer actionType = record.getActionType();
        return direction == 1
                ? Integer.valueOf(RuleConstant.TRADING_MODE_BUY).equals(actionType)
                    || Integer.valueOf(RuleConstant.TRADING_MODE_CANCEL).equals(actionType)
                : Integer.valueOf(RuleConstant.TRADING_MODE_SELL).equals(actionType);
    }

    private void validateDirection(int direction) {
        if (direction != 1 && direction != 2) {
            throw new IllegalArgumentException("direction 只能是 1（买入）或 2（卖出）");
        }
    }

    private void validateReplayMode(BacktestReplayMode mode, Integer allowedAfterTime) {
        if (mode == null) {
            throw new IllegalArgumentException("回放模式不能为空");
        }
        if (mode == BacktestReplayMode.BUY_AFTER_TIME && allowedAfterTime == null) {
            throw new IllegalArgumentException("卖出后买入回放必须提供允许买入时间");
        }
    }

    private void validateStockCode(String stockCode) {
        if (stockCode == null || !stockCode.matches("(?:60|00)\\d{4}")) {
            throw new IllegalArgumentException("当前回测只支持 60/00 沪深主板股票: " + stockCode);
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0D : value;
    }

    /**
     * 数据源中途失败时，先等待已经发布的 Tick 消费完成，再清理 Handler 私有数组。
     */
    private void awaitPartiallyPublishedTicks() {
        try {
            orderBookEngine.awaitProcessed(replayTimeout);
        } catch (RuntimeException exception) {
            log.warn("等待部分回测 Tick 消费完成失败，准备清理本轮状态", exception);
        }
    }
}
