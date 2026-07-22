package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeStaticFacts;

/**
 * 根据盘前静态事实编译出的执行约束。板位和市值只作为事实，不再决定 Handler 类型。
 */
public record TradeExecutionProfile(
        int previousBoardHeight,
        int targetBoardHeight,
        MarketCapTier marketCapTier,
        boolean previousBoardAccelerated,
        boolean openingAuctionBuyAllowed,
        int earliestContinuousBuyTime,
        String openingAuctionBlockReason) {

    public static final int NORMAL_FIRST_BOARD_AUCTION_CAP_WAN = 160_000;
    public static final int AFTER_ACCELERATION_BUY_TIME = 93_500_000;
    private static final int SMALL_CAP_UPPER_EXCLUSIVE_WAN = 119_999;

    public static TradeExecutionProfile from(TradeStaticFacts facts) {
        if (facts == null) throw new IllegalArgumentException("静态交易事实不能为空");
        boolean accelerated = isPreviousBoardAccelerated(facts);
        boolean relayAccelerationGate = facts.tradeMode() == 1
                && facts.lbcs() == 2 && accelerated;
        boolean normalFirstBoardCapGate = facts.tradeMode() == 2
                && facts.lbcs() == 1
                && facts.initialMarketValue() >= NORMAL_FIRST_BOARD_AUCTION_CAP_WAN;
        String reason = relayAccelerationGate
                ? "二板加速后禁止集合竞价买入，09:35 前只观察"
                : normalFirstBoardCapGate
                ? "普通首板启动市值达到 16 亿，不执行集合竞价一字板买入"
                : null;
        return new TradeExecutionProfile(
                facts.lbcs(), facts.lbcs() + 1,
                facts.initialMarketValue() < SMALL_CAP_UPPER_EXCLUSIVE_WAN
                        ? MarketCapTier.SMALL_CAP : MarketCapTier.NORMAL_CAP,
                accelerated, !relayAccelerationGate && !normalFirstBoardCapGate,
                relayAccelerationGate ? AFTER_ACCELERATION_BUY_TIME : 0,
                reason);
    }

    private static boolean isPreviousBoardAccelerated(TradeStaticFacts facts) {
        return isAcceleratedBoard(facts.yesterdayKlineState(),
                facts.yesterdayAmplitude(), facts.yesterdayTurnover());
    }

    /** 二板加速的稳定静态判定，回测机会编排与逐笔模板共用同一口径。 */
    public static boolean isAcceleratedBoard(int klineState, double amplitude, double turnover) {
        if (klineState == 3) return true;
        return amplitude >= 0 && amplitude < 3D
                || klineState == 2 && turnover >= 0 && turnover < 18D
                || turnover >= 0 && turnover < 15D;
    }

    public enum MarketCapTier {
        SMALL_CAP,
        NORMAL_CAP
    }
}
