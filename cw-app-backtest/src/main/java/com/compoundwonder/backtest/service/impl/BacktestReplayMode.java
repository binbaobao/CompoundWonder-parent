package com.compoundwonder.backtest.service.impl;

/**
 * 单股票订单簿回放场景。
 */
public enum BacktestReplayMode {
    /** 普通买入监控，从开盘起允许买入。 */
    BUY,
    /** 持仓卖出监控。 */
    SELL,
    /** 已经存在隔夜买入委托，集合竞价期间允许策略撤单。 */
    OVERNIGHT_BUY,
    /** 完整构建订单簿，但只在指定时间之后打开买入监控。 */
    BUY_AFTER_TIME
}
