package com.compoundwonder.strategy.sell;

/**
 * 收盘集合竞价卖出场景。
 *
 * <p>该场景与买入模式、昨日板高和启动市值无关。输入来自交易所集合竞价快照：
 * 撮合价未封住涨停，或者叫卖总量大于叫买总量时触发卖出。</p>
 */
public final class ClosingAuctionSellEvaluator {

    private ClosingAuctionSellEvaluator() {
    }

    /**
     * @param price 当前撮合价格，单位：分
     * @param limitUpPrice 当日涨停价，单位：分
     * @param totalBuyVolume 叫买总量，单位：股
     * @param totalSellVolume 叫卖总量，单位：股
     * @return 需要在收盘集合竞价卖出时返回 {@code true}
     */
    public static boolean evaluate(int price, int limitUpPrice,
                                   long totalBuyVolume, long totalSellVolume) {
        return price < limitUpPrice || totalBuyVolume < totalSellVolume;
    }
}
