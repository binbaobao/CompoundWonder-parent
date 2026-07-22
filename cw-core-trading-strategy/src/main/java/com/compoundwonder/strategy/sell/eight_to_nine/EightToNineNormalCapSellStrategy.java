package com.compoundwonder.strategy.sell.eight_to_nine;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;

/**
 * 昨日 8 板、今日 8 进 9、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 没有本场景样本。后续出现可确认的股票名称、代码、
 * 日期和时间后，再逐条补充。</p>
 */
public final class EightToNineNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }
}
