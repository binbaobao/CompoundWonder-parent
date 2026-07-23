package com.compoundwonder.common.strategy.trade;

/**
 * 策略会话声明需要接收的触发类型；低频策略无需订阅逐笔事件。
 * 当前订单簿 Handler 只产生前四类，日线收盘与定时触发留给后续低频执行器。
 */
public enum TradeTriggerType {
    OPENING_AUCTION,
    CONTINUOUS_TICK,
    MINUTE_CLOSE,
    CLOSING_AUCTION,
    DAILY_CLOSE,
    SCHEDULED
}
