package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeStaticFacts;

/**
 * 根据盘前静态事实编译出的跨交易所执行约束。板位和市值只作为事实，不再决定 Handler 类型。
 *
 * <p>本层目前只阻断 Model 1 二板加速后的集合竞价，并推迟到 09:35。上海、深圳各自
 * 的启动市值竞价上限仍归具体竞价规则所有，禁止在这里再增加统一市值门槛。</p>
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
        boolean accelerated = isPreviousBoardAccelerated(facts);
        boolean relayAccelerationGate = facts.tradeMode() == 1
                && facts.lbcs() == 2 && accelerated;
        String reason = relayAccelerationGate
                ? "二板加速后禁止集合竞价买入，09:35 前只观察"
                : null;
        return new TradeExecutionProfile(
                facts.lbcs(), facts.lbcs() + 1,
                facts.initialMarketValue() < SMALL_CAP_UPPER_EXCLUSIVE_WAN
                        ? MarketCapTier.SMALL_CAP : MarketCapTier.NORMAL_CAP,
                accelerated, !relayAccelerationGate,
                relayAccelerationGate ? AFTER_ACCELERATION_BUY_TIME : 0,
                reason);
    }

    private static boolean isPreviousBoardAccelerated(TradeStaticFacts facts) {
        return isAcceleratedBoard(facts.yesterdayKlineState(),
                facts.yesterdayAmplitude(), facts.yesterdayTurnover());
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
