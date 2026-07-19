package com.compoundwonder.common.orderbook;

/** 交易策略写入规则记录所需的最小接口。 */
public interface TradeRuleRecord {

    /**
     * 覆盖当前预分配记录对象的全部字段，不创建新的规则记录实例。
     *
     * @param actionType 动作类型，沿用规则记录表的买入、卖出或撤单编号
     * @param ruleCode 当前交易模式内稳定的规则编号
     * @param symbol 六位股票代码
     * @param time 规则触发时间，格式为 {@code HHmmssSSS}
     * @param price 触发价格，整数价格口径为元乘以 100
     * @param increase 触发时相对昨收的涨跌幅，单位为百分比
     * @param remark 规则命中条件及指标说明
     */
    void fill(int actionType, int ruleCode, String symbol, int time,
              int price, double increase, String remark);
}
