package com.compoundwonder.strategy.firstboard.trade;

import com.compoundwonder.constant.ConstantUtil;

/** 普通首板模式的上海早盘集合竞价买入与配套撤单场景；返回 0 表示不触发。 */
final class ShanghaiAuctionBuyEvaluator {
    private ShanghaiAuctionBuyEvaluator() {
    }
    static int evaluateBuy(int time, int price, int limitUpPrice, long totalBuyVolume,
                           long totalSellVolume, long requiredBuyVolume, long limitUpBuyAmount) {
        if (time > ConstantUtil.TIME_925 || price != limitUpPrice
                || totalBuyVolume <= requiredBuyVolume / 3) {
            return 0;
        }
        boolean sellPressureEligible = totalSellVolume * 100.0 / totalBuyVolume <= 40
                || limitUpBuyAmount > 15_000;
        return sellPressureEligible && totalBuyVolume > requiredBuyVolume ? 2 : 0;
    }
    static int evaluateCancel(int price, int limitUpPrice, long totalBuyVolume,
                              long totalSellVolume, long requiredBuyVolume) {
        if (price != limitUpPrice) {
            return 1;
        }
        return totalBuyVolume <= requiredBuyVolume
                || totalSellVolume * 100.0 / totalBuyVolume > 40 ? 2 : 0;
    }
}
