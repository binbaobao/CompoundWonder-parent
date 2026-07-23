package com.compoundwonder.core.engine;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;

/**
 * 单个策略在一只股票、一个交易日内的独立执行会话。
 *
 * <p>盘口热数据来自共享的 {@link OrderBookSession}，静态事实、模板和交易状态只属于
 * 当前策略，因而同一股票上的多个策略不会互相覆盖买卖或持仓状态。本对象也是多策略
 * 执行时唯一允许传给规则的 {@link TradeMarketState}，以免规则误读主策略的静态事实。</p>
 */
public final class StrategyExecutionSession implements TradeMarketState {
    static final String UNSPECIFIED_TRADE_DATE = "UNSPECIFIED";
    private final StrategySessionKey key;
    private final OrderBookSession marketSession;
    private final TradeStaticFacts facts;
    private final TradeExecutionTemplate template;
    private final TradeExecutionState executionState;

    public StrategyExecutionSession(StrategySessionKey key,
                                    OrderBookSession marketSession,
                                    TradeStaticFacts facts,
                                    TradeExecutionTemplate template,
                                    TradeExecutionState executionState) {
        if (key == null || marketSession == null || facts == null
                || template == null || executionState == null) {
            throw new IllegalArgumentException("策略执行会话参数不能为 null");
        }
        // 允许模板保存同值副本，但拒绝事实内容不一致，防止初始化后规则看到另一模式参数。
        if (template.facts() != facts && !template.facts().equals(facts)) {
            throw new IllegalArgumentException("交易模板与策略静态事实不一致");
        }
        String marketTradeDate = marketSession.getDate() == null
                || marketSession.getDate().isBlank()
                ? UNSPECIFIED_TRADE_DATE : marketSession.getDate();
        if (!key.symbol().equals(marketSession.getSymbol())
                || !key.tradeDate().equals(marketTradeDate)) {
            throw new IllegalArgumentException("策略会话与共享订单簿的股票或交易日不一致");
        }
        this.key = key;
        this.marketSession = marketSession;
        this.facts = facts;
        this.template = template;
        this.executionState = executionState;
    }

    public StrategySessionKey key() { return key; }
    public OrderBookSession marketSession() { return marketSession; }
    public MarketSessionSpec spec() { return marketSession.spec(); }
    public OrderBook orderBook() { return marketSession.orderBook(); }
    public TradeStaticFacts facts() { return facts; }
    public TradeExecutionTemplate template() { return template; }
    public TradeExecutionState executionState() { return executionState; }

    @Override public int getTradeMode() { return facts.tradeMode(); }
    @Override public String getSymbol() { return marketSession.getSymbol(); }
    @Override public int getStatus() { return marketSession.getStatus(); }
    @Override public int getLbcs() { return facts.lbcs(); }
    @Override public int getTime() { return marketSession.getTime(); }
    @Override public int getClosePrice() { return marketSession.getClosePrice(); }
    @Override public int getLastPrice() { return marketSession.getLastPrice(); }
    @Override public int getLowPrice() { return marketSession.getLowPrice(); }
    @Override public double getLowPriceIncrease() { return marketSession.getLowPriceIncrease(); }
    @Override public int getHighestPrice() { return marketSession.getHighestPrice(); }
    @Override public int getLimitUpPrice() { return marketSession.getLimitUpPrice(); }
    @Override public int getLimitDownPrice() { return marketSession.getLimitDownPrice(); }
    @Override public int getOpenPrice() { return marketSession.getOpenPrice(); }
    @Override public double getOpenIncrease() { return marketSession.getOpenIncrease(); }
    @Override public double getIncrease() { return marketSession.getIncrease(); }
    @Override public double getAmplitude() { return marketSession.getAmplitude(); }
    @Override public double getLimitUpBreakDepth() { return marketSession.getLimitUpBreakDepth(); }
    @Override public double getTurnoverRate() { return marketSession.getTurnoverRate(); }
    @Override public long getTurnover() { return marketSession.getTurnover(); }
    @Override public long getVolume() { return marketSession.getVolume(); }
    @Override public long getMaxVolume() { return facts.maxVolume(); }
    @Override public double getMaxHs() { return facts.maxHs(); }
    @Override public long getCirculation() { return marketSession.getCirculation(); }
    @Override public int getInitialMarketValue() { return facts.initialMarketValue(); }
    @Override public double getThreeDaysTurnover() { return facts.threeDaysTurnover(); }
    @Override public double getTwoDaysTurnover() { return facts.twoDaysTurnover(); }
    @Override public double getYesterdayTurnover() { return facts.yesterdayTurnover(); }
    @Override public int getOneWordLimitUp() { return facts.oneWordLimitUp(); }
    @Override public int getAverageLimitUpHeight() { return facts.averageLimitUpHeight(); }
    @Override public int getNextTradingDay() { return facts.nextTradingDay(); }
    @Override public long getLimitUpBuyAmount() { return marketSession.getLimitUpBuyAmount(); }
    @Override public int getLastLimitUptime() { return marketSession.getLastLimitUptime(); }
    @Override public int getLastLimitUpBreakTime() { return marketSession.getLastLimitUpBreakTime(); }
    @Override public double getLastSealedAmplitude() { return marketSession.getLastSealedAmplitude(); }
    @Override public double getLastSealedChangePercent() { return marketSession.getLastSealedChangePercent(); }
    @Override public long getLastSealedAmount() { return marketSession.getLastSealedAmount(); }
    @Override public double getLastEmaVolume() { return marketSession.getLastEmaVolume(); }
    @Override public double getChangePercent() { return marketSession.getChangePercent(); }
    @Override public int getEmaSealTrend() { return marketSession.getEmaSealTrend(); }
    @Override public long getLastSealAmount() { return marketSession.getLastSealAmount(); }
    @Override public int getMinAveragePrice() { return marketSession.getMinAveragePrice(); }
    @Override public double getMinAveragePriceIncrease() { return marketSession.getMinAveragePriceIncrease(); }
    @Override public int getMinutePriceAt(int index) { return marketSession.getMinutePriceAt(index); }
    @Override public int getAveragePriceAt(int index) { return marketSession.getAveragePriceAt(index); }
    @Override public int getLargestBuyOrderPrice() { return marketSession.getLargestBuyOrderPrice(); }
    @Override public int getLargestBuyOrderQuantity() { return marketSession.getLargestBuyOrderQuantity(); }

    public String getSecurityName() { return marketSession.getSecurityName(); }
    public String getDate() { return marketSession.getDate(); }
}
