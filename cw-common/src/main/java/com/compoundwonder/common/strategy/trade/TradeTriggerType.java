package com.compoundwonder.common.strategy.trade;

/** 策略会话声明需要接收的触发类型；低频策略无需订阅逐笔事件。 */
public enum TradeTriggerType {
    OPENING_AUCTION,
    CONTINUOUS_TICK,
    MINUTE_CLOSE,
    CLOSING_AUCTION,
    DAILY_CLOSE,
    SCHEDULED
}
