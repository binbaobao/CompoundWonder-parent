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
        int twoDaysAgoKlineState,
        double yesterdayAmplitude,
        double twoDaysAgoTurnover,
        double twoDaysAgoAmplitude) {

    /** 旧调用方兼容构造器；负数表示尚未提供对应的历史日 K 指标。 */
    public TradeStaticFacts(int tradeMode, int lbcs, long maxVolume, double maxHs,
                            int initialMarketValue, double threeDaysTurnover,
                            double twoDaysTurnover, double yesterdayTurnover,
                            int oneWordLimitUp, int averageLimitUpHeight,
                            int nextTradingDay, int yesterdayKlineState,
                            int twoDaysAgoKlineState) {
        this(tradeMode, lbcs, maxVolume, maxHs, initialMarketValue,
                threeDaysTurnover, twoDaysTurnover, yesterdayTurnover,
                oneWordLimitUp, averageLimitUpHeight, nextTradingDay,
                yesterdayKlineState, twoDaysAgoKlineState, -1D, -1D, -1D);
    }

    public TradeStaticFacts {
        if (tradeMode < 1) {
            throw new IllegalArgumentException("tradeMode 必须大于 0: " + tradeMode);
        }
        if (lbcs < 0) {
            throw new IllegalArgumentException("lbcs 不能小于 0: " + lbcs);
        }
        if (maxVolume < 0) {
            throw new IllegalArgumentException("maxVolume 不能小于 0: " + maxVolume);
        }
    }
}
