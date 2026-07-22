package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeStaticFacts;

/** 在订单簿会话初始化阶段把静态策略事实编译为六阶段执行模板。 */
public interface TradeExecutionTemplateFactory {

    TradeExecutionTemplate compile(TradeStaticFacts facts);
}
