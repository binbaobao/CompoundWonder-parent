package com.compoundwonder.strategy.sell.eight_to_nine;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import com.compoundwonder.strategy.sell.NormalCapCrossBoardFallbackSellRules;

/**
 * 昨日 8 板、今日 8 进 9、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 没有本场景直接样本，因此只调用已经由其他普通市值
 * 板位真实样本确认的跨板位安全兜底。</p>
 */
public final class EightToNineNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return NormalCapCrossBoardFallbackSellRules.evaluateEightToNine(market, record);
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }
}
