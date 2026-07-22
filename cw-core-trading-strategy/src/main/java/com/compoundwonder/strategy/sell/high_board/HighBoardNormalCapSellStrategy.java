package com.compoundwonder.strategy.sell.high_board;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import com.compoundwonder.strategy.sell.NormalCapCrossBoardFallbackSellRules;

/**
 * 昨日 9 板及以上、今日继续晋级、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 没有本场景直接样本，因此只调用已经由其他普通市值
 * 板位真实样本确认的跨板位安全兜底。</p>
 */
public final class HighBoardNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return NormalCapCrossBoardFallbackSellRules.evaluateHighBoard(market, record);
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }
}
