package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.constant.ConstantUtil;

/**
 * 连板模式的上海早盘集合竞价场景。
 *
 * <p>只处理 09:25 前的隔夜买单和配套撤单；价格单位为分，数量单位为股，
 * 金额单位为万元。返回 0 表示不触发，非 0 为原有规则编号。</p>
 */
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
