package com.compoundwonder.strategy.sell.four_to_five;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.BoardSellStrategy;

/**
 * 昨日 4 板、今日 4 进 5、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 的 177 笔真实/虚拟卖出中，没有昨日 4 板进入本场景后
 * 由板位策略触发的样本。后续回测出现可确认的股票名称、代码、日期和时间后，再逐条
 * 补充并建立对应测试。</p>
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
