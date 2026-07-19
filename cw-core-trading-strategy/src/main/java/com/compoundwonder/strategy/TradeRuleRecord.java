package com.compoundwonder.strategy;

/**
 * 策略模块写入规则记录所需的最小接口。
 *
 * <p>接口避免策略模块依赖订单簿内部的复用缓冲对象。</p>
 */
public interface TradeRuleRecord {

    void fill(int actionType, int ruleCode, String symbol, int time,
              int price, double increase, String remark);
}
