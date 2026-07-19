package com.compoundwonder.common.orderbook;

/** 交易策略写入规则记录所需的最小接口。 */
public interface TradeRuleRecord {

    void fill(int actionType, int ruleCode, String symbol, int time,
              int price, double increase, String remark);
}
