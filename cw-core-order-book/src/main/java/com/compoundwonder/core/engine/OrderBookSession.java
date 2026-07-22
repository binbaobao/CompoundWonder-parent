package com.compoundwonder.core.engine;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;

/** 静态参数、预编译规则、热盘口和执行状态组成的单日订单簿会话。 */
public final class OrderBookSession implements TradeMarketState {
    private final MarketSessionSpec spec;
    private final TradeStaticFacts facts;
    private final OrderBook orderBook;
    private final TradeExecutionTemplate template;
    private final TradeExecutionState executionState;

    public OrderBookSession(MarketSessionSpec spec, TradeStaticFacts facts,
                            OrderBook orderBook, TradeExecutionTemplate template,
                            TradeExecutionState executionState) {
        if (spec == null || facts == null || orderBook == null
                || template == null || executionState == null) {
            throw new IllegalArgumentException("订单簿会话参数不能为 null");
        }
        if (template.facts() != facts && !template.facts().equals(facts)) {
            throw new IllegalArgumentException("交易模板与会话静态事实不一致");
        }
        this.spec = spec;
        this.facts = facts;
        this.orderBook = orderBook;
        this.template = template;
        this.executionState = executionState;
    }

    public MarketSessionSpec spec() { return spec; }
    public TradeStaticFacts facts() { return facts; }
    public OrderBook orderBook() { return orderBook; }
    public TradeExecutionTemplate template() { return template; }
    public TradeExecutionState executionState() { return executionState; }

    @Override public int getTradeMode() { return facts.tradeMode(); }
    @Override public String getSymbol() { return spec.symbol(); }
    @Override public int getStatus() { return orderBook.getStatus(); }
    @Override public int getLbcs() { return facts.lbcs(); }
    @Override public int getTime() { return orderBook.getTime(); }
    @Override public int getClosePrice() { return spec.closePrice(); }
    @Override public int getLastPrice() { return orderBook.getLastPrice(); }
    @Override public int getLowPrice() { return orderBook.getLowPrice(); }
    @Override public double getLowPriceIncrease() { return orderBook.getLowPriceIncrease(); }
    @Override public int getHighestPrice() { return orderBook.getHighestPrice(); }
    @Override public int getLimitUpPrice() { return spec.limitUpPrice(); }
    @Override public int getLimitDownPrice() { return spec.limitDownPrice(); }
    @Override public int getOpenPrice() { return orderBook.getOpenPrice(); }
    @Override public double getOpenIncrease() { return orderBook.getOpenIncrease(); }
    @Override public double getIncrease() { return orderBook.getIncrease(); }
    @Override public double getAmplitude() { return orderBook.getAmplitude(); }
    @Override public double getLimitUpBreakDepth() { return orderBook.getLimitUpBreakDepth(); }
    @Override public double getTurnoverRate() { return orderBook.getTurnoverRate(); }
    @Override public long getTurnover() { return orderBook.getTurnover(); }
    @Override public long getVolume() { return orderBook.getVolume(); }
    @Override public long getMaxVolume() { return facts.maxVolume(); }
    @Override public double getMaxHs() { return facts.maxHs(); }
    @Override public long getCirculation() { return spec.circulation(); }
    @Override public int getInitialMarketValue() { return facts.initialMarketValue(); }
    @Override public double getThreeDaysTurnover() { return facts.threeDaysTurnover(); }
    @Override public double getTwoDaysTurnover() { return facts.twoDaysTurnover(); }
    @Override public double getYesterdayTurnover() { return facts.yesterdayTurnover(); }
    @Override public int getOneWordLimitUp() { return facts.oneWordLimitUp(); }
    @Override public int getAverageLimitUpHeight() { return facts.averageLimitUpHeight(); }
    @Override public int getNextTradingDay() { return facts.nextTradingDay(); }
    @Override public long getLimitUpBuyAmount() { return orderBook.getLimitUpBuyAmount(); }
    @Override public int getLastLimitUptime() { return orderBook.getLastLimitUptime(); }
    @Override public int getLastLimitUpBreakTime() { return orderBook.getLastLimitUpBreakTime(); }
    @Override public double getLastSealedAmplitude() { return orderBook.getLastSealedAmplitude(); }
    @Override public double getLastSealedChangePercent() { return orderBook.getLastSealedChangePercent(); }
    @Override public long getLastSealedAmount() { return orderBook.getLastSealedAmount(); }
    @Override public double getLastEmaVolume() { return orderBook.getLastEmaVolume(); }
    @Override public double getChangePercent() { return orderBook.getChangePercent(); }
    @Override public int getEmaSealTrend() { return orderBook.getEmaSealTrend(); }
    @Override public long getLastSealAmount() { return orderBook.getLastSealAmount(); }
    @Override public int getMinAveragePrice() { return orderBook.getMinAveragePrice(); }
    @Override public double getMinAveragePriceIncrease() { return orderBook.getMinAveragePriceIncrease(); }
    @Override public int getMinutePriceAt(int index) { return orderBook.getMinutePriceAt(index); }
    @Override public int getAveragePriceAt(int index) { return orderBook.getAveragePriceAt(index); }
    @Override public int getLargestBuyOrderPrice() { return orderBook.getLargestBuyOrderPrice(); }
    @Override public int getLargestBuyOrderQuantity() { return orderBook.getLargestBuyOrderQuantity(); }

    public String getSecurityName() { return spec.securityName(); }
    public String getDate() { return spec.date(); }
}
