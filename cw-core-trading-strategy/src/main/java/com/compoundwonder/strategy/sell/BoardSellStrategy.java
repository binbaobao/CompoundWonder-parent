package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/**
 * 单一昨日板高场景的卖出策略。
 *
 * <p>实现类必须同时提供盘口卖出和分钟均价卖出，并在类内按启动流通市值
 * {@code 119999} 万元分成小市值、普通市值两个明确入口。策略只读取订单簿并
 * 填充调用方预分配的记录，不查询数据库、不修改订单簿、不发送券商委托。</p>
 */
public interface BoardSellStrategy {

    /**
     * 使用逐笔与盘口状态评估卖出规则。
     *
     * @param market Handler 私有订单簿的只读视图
     * @param record 调用方预分配的规则记录
     * @return 命中卖出规则时返回 {@code true}
     */
    boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record);

    /**
     * 使用分钟最新价和当日成交均价评估卖出规则。
     *
     * @param calculateIndex 当前分钟采样下标
     * @param market Handler 私有订单簿的只读视图
     * @param record 调用方预分配的规则记录
     * @return 命中卖出规则时返回 {@code true}
     */
    boolean evaluateAveragePrice(int calculateIndex, TradeMarketState market,
                                 TradeRuleRecord record);
}
