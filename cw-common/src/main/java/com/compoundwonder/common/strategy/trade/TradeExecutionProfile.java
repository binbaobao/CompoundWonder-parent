package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeStaticFacts;

/**
 * 根据盘前静态事实编译出的跨交易所执行约束。板位和市值只作为事实，不再决定 Handler 类型。
 *
 * <p>本层按历史涨停 K 线状态统一编译隔夜与开盘竞价资格。
 * 上海、深圳各自的启动市值竞价上限仍归具体竞价规则所有，禁止在这里再增加统一市值门槛。</p>
 */
public record TradeExecutionProfile(
        int previousBoardHeight,
        int targetBoardHeight,
        MarketCapTier marketCapTier,
        boolean previousBoardAccelerated,
        boolean openingAuctionBuyAllowed,
        int earliestContinuousBuyTime,
        String openingAuctionBlockReason) {

    public static final int AFTER_ACCELERATION_BUY_TIME = 93_500_000;
    private static final int SMALL_CAP_UPPER_EXCLUSIVE_WAN = 119_999;

    public static TradeExecutionProfile from(TradeStaticFacts facts) {
        if (facts == null) throw new IllegalArgumentException("静态交易事实不能为空");
        // 调用 isPreviousBoardAccelerated 判断前一涨停板是否属于加速板。
        boolean accelerated = isPreviousBoardAccelerated(facts);
        // 调用 facts.tradeMode 读取当前选股模式。
        int tradeMode = facts.tradeMode();
        // 调用 facts.lbcs 读取买入日前已经封住的连板高度。
        int previousBoardHeight = facts.lbcs();
        // 调用 facts.yesterdayKlineState 读取前一交易日K线形态。
        int yesterdayKlineState = facts.yesterdayKlineState();
        // 调用 facts.twoDaysAgoKlineState 读取前两个交易日K线形态。
        int twoDaysAgoKlineState = facts.twoDaysAgoKlineState();
        // 调用 facts.yesterdayVolumeState 读取前一交易日量能状态。
        int yesterdayVolumeState = facts.yesterdayVolumeState();
        // 调用 facts.twoDaysAgoVolumeState 读取前两个交易日量能状态。
        int twoDaysAgoVolumeState = facts.twoDaysAgoVolumeState();
        boolean firstBoardKlineStateGate = (tradeMode == 2 || tradeMode == 3) && previousBoardHeight == 1 && yesterdayKlineState != 1;
        // 调用 isPositiveKlineStateSumBelow 判断接力二板的K线形态和是否满足竞价门槛。
        boolean relayTwoBoardKlineStateGate = tradeMode == 1 && previousBoardHeight == 2 && !isPositiveKlineStateSumBelow(yesterdayKlineState, twoDaysAgoKlineState, 4);
        boolean requiredVolumeStateMissing = (previousBoardHeight == 1 && yesterdayVolumeState < -1)
                || (previousBoardHeight == 2 && (yesterdayVolumeState < -1 || twoDaysAgoVolumeState < -1));
        boolean secondBoardVolumeStateGate = previousBoardHeight == 1 && yesterdayVolumeState == -1;
        boolean thirdBoardVolumeStateGate = previousBoardHeight == 2 && yesterdayVolumeState + twoDaysAgoVolumeState < -1;
        String reason;
        if (firstBoardKlineStateGate) {
            reason = "首板K线状态不等于1，禁止二板隔夜与开盘集合竞价买入";
        } else if (relayTwoBoardKlineStateGate) {
            reason = "首板与二板K线状态和必须小于4，禁止三板隔夜与开盘集合竞价买入，09:35 前只观察";
        } else if (requiredVolumeStateMissing) {
            reason = "历史量能状态数据不足，禁止隔夜与开盘集合竞价买入";
        } else if (secondBoardVolumeStateGate) {
            reason = "首板量能状态小于0，禁止二板隔夜与开盘集合竞价买入";
        } else if (thirdBoardVolumeStateGate) {
            reason = "首板与二板量能状态和小于-1，禁止三板隔夜与开盘集合竞价买入";
        } else {
            reason = null;
        }
        boolean openingAuctionBuyAllowed = !firstBoardKlineStateGate && !relayTwoBoardKlineStateGate && !requiredVolumeStateMissing
                && !secondBoardVolumeStateGate && !thirdBoardVolumeStateGate;
        // 调用 facts.initialMarketValue 读取启动流通市值并划分市值层级。
        MarketCapTier marketCapTier = facts.initialMarketValue() < SMALL_CAP_UPPER_EXCLUSIVE_WAN ? MarketCapTier.SMALL_CAP : MarketCapTier.NORMAL_CAP;
        return new TradeExecutionProfile(previousBoardHeight, previousBoardHeight + 1, marketCapTier, accelerated, openingAuctionBuyAllowed,
                relayTwoBoardKlineStateGate ? AFTER_ACCELERATION_BUY_TIME : 0, reason);
    }

    private static boolean isPositiveKlineStateSumBelow(
            int first, int second, int exclusiveUpperBound) {
        return first > 0 && second > 0 && first + second < exclusiveUpperBound;
    }

    private static boolean isPreviousBoardAccelerated(TradeStaticFacts facts) {
        // 调用 facts.yesterdayKlineState 读取前一交易日K线形态。
        int yesterdayKlineState = facts.yesterdayKlineState();
        // 调用 facts.yesterdayAmplitude 读取前一交易日振幅。
        double yesterdayAmplitude = facts.yesterdayAmplitude();
        // 调用 facts.yesterdayTurnover 读取前一交易日换手率。
        double yesterdayTurnover = facts.yesterdayTurnover();
        // 调用 isAcceleratedBoard 判断前一涨停板是否属于加速板。
        return isAcceleratedBoard(yesterdayKlineState, yesterdayAmplitude, yesterdayTurnover);
    }

    /**
     * 二板加速的稳定静态判定，回测机会编排与逐笔模板共用同一口径。
     * 三组条件相互独立：一字板、低振幅/低换手 T 字板、以及整体低换手板。
     */
    public static boolean isAcceleratedBoard(int klineState, double amplitude, double turnover) {
        if (klineState == 3) return true;
        return (amplitude >= 0 && amplitude < 3D)
                || (klineState == 2 && turnover >= 0 && turnover < 18D)
                || (turnover >= 0 && turnover < 15D);
    }

    public enum MarketCapTier {
        SMALL_CAP,
        NORMAL_CAP
    }
}
