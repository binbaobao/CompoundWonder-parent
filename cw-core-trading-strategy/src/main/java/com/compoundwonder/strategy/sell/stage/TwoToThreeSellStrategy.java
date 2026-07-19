package com.compoundwonder.strategy.sell.stage;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import com.compoundwonder.strategy.sell.LegacySellRules;
import com.compoundwonder.strategy.sell.SellMarketCapBand;

/**
 * 昨日 2 板、今日 2 进 3 的持仓卖出场景。
 *
 * <p>盘口与分钟均价分别评估；每种数据源再按启动流通市值 119999 万元分档。
 * 当前四个入口先复用原规则，后续可根据回测结果独立替换任一方法。</p>
 */
public final class TwoToThreeSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return SellMarketCapBand.from(market.getInitialMarketValue()) == SellMarketCapBand.SMALL_CAP
                ? evaluateSmallCapOrderBook(market, record)
                : evaluateNormalCapOrderBook(market, record);
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return SellMarketCapBand.from(market.getInitialMarketValue()) == SellMarketCapBand.SMALL_CAP
                ? evaluateSmallCapAveragePrice(index, market, record)
                : evaluateNormalCapAveragePrice(index, market, record);
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
