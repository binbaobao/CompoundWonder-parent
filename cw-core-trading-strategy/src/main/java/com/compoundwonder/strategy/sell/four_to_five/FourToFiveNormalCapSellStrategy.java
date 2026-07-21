package com.compoundwonder.strategy.sell.four_to_five;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;

/**
 * 昨日 4 板、今日 4 进 5、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>基准任务 145、147、146 中没有该市值档的真实卖出样本，因此先清空从旧卖出逻辑
 * 复制来的规则。后续回测出现可确认的股票名和日期后，再逐条补充并建立对应测试。</p>
 */
public final class FourToFiveNormalCapSellStrategy implements BoardSellStrategy {

    /** 当前没有经过基准回测确认的盘口卖出规则。 */
    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return false;
    }

    /** 当前没有经过基准回测确认的分钟均价卖出规则。 */
    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }
}
