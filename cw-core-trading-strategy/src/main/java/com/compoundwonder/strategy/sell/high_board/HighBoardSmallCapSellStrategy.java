package com.compoundwonder.strategy.sell.high_board;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;

/**
 * 昨日 9 板及以上、今日继续晋级、启动流通市值严格小于 119999 万元的卖出策略。
 *
 * <p>回测 145、147、146 的 52 笔卖出中没有本场景样本，等待后续回测补充。</p>
 */
public final class HighBoardSmallCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }
}
