package com.compoundwonder.strategy.sell.four_to_five;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import com.compoundwonder.strategy.sell.NormalCapCrossBoardFallbackSellRules;

/**
 * 昨日 4 板、今日 4 进 5、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 没有本场景直接样本，因此只调用已经由其他普通市值
 * 板位真实样本确认的跨板位安全兜底。</p>
 */
public final class FourToFiveNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return NormalCapCrossBoardFallbackSellRules.evaluateOrderBook(market, record);
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }
}
