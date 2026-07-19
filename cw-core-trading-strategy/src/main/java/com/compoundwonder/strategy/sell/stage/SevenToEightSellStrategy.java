package com.compoundwonder.strategy.sell.stage;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import com.compoundwonder.strategy.sell.LegacySellRules;
import com.compoundwonder.strategy.sell.SellMarketCapBand;

/** 昨日 7 板、今日 7 进 8 的持仓卖出场景；盘口、均价和两档市值均可独立调整。 */
public final class SevenToEightSellStrategy implements BoardSellStrategy {
    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return SellMarketCapBand.from(market.getInitialMarketValue()) == SellMarketCapBand.SMALL_CAP
                ? evaluateSmallCapOrderBook(market, record) : evaluateNormalCapOrderBook(market, record);
    }
    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return SellMarketCapBand.from(market.getInitialMarketValue()) == SellMarketCapBand.SMALL_CAP
                ? evaluateSmallCapAveragePrice(index, market, record) : evaluateNormalCapAveragePrice(index, market, record);
    }
    private boolean evaluateSmallCapOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return LegacySellRules.evaluateOrderBook(market, record);
    }
    private boolean evaluateNormalCapOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return LegacySellRules.evaluateOrderBook(market, record);
    }
    private boolean evaluateSmallCapAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return LegacySellRules.evaluateAveragePrice(index, market, record);
    }
    private boolean evaluateNormalCapAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return LegacySellRules.evaluateAveragePrice(index, market, record);
    }
}
