package com.compoundwonder.common.orderbook;

/**
 * 构建单日订单簿会话前已经确定的策略事实。
 *
 * <p>这些值来自选股任务、持仓和历史日 K，只允许在会话初始化时计算一次。
 * 行情热路径只读取本对象，不能再查询数据库，也不能把这些字段写回盘口对象。</p>
 */
public record TradeStaticFacts(
        int tradeMode,
        int lbcs,
        long maxVolume,
        double maxHs,
        int initialMarketValue,
        double threeDaysTurnover,
        double twoDaysTurnover,
        double yesterdayTurnover,
        int oneWordLimitUp,
        int averageLimitUpHeight,
        int nextTradingDay,
        int yesterdayKlineState,
        int twoDaysAgoKlineState) {

    public TradeStaticFacts {
        if (tradeMode < 1 || tradeMode > 3) {
            throw new IllegalArgumentException("tradeMode 只能是 1、2、3: " + tradeMode);
        }
        if (lbcs < 0) {
            throw new IllegalArgumentException("lbcs 不能小于 0: " + lbcs);
        }
        if (maxVolume < 0) {
            throw new IllegalArgumentException("maxVolume 不能小于 0: " + maxVolume);
        }
    }
}
