package com.compoundwonder.core.engine;

/**
 * 策略执行会话的稳定标识。同一股票可同时注册多个互不共享交易状态的策略会话。
 */
public record StrategySessionKey(
        String sessionId,
        String strategyId,
        String symbol,
        String tradeDate) {

    public StrategySessionKey {
        requireText(sessionId, "sessionId");
        requireText(strategyId, "strategyId");
        requireText(symbol, "symbol");
        requireText(tradeDate, "tradeDate");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
    }
}
