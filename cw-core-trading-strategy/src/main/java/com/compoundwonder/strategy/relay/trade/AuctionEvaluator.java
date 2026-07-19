package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.constant.ConstantUtil;

/**
 * 连板接力独立集合竞价规则。
 *
 * <p>返回 0 表示不触发，非 0 返回原规则编号；本类不修改订单簿、不执行券商动作。</p>
 */
final class AuctionEvaluator {

    private AuctionEvaluator() {
    }

    static int evaluateShanghaiBuy(int time, int price, int limitUpPrice,
                                   long totalBuyVolume, long totalSellVolume,
                                   long requiredBuyVolume, long limitUpBuyAmount) {
        if (time > ConstantUtil.TIME_925 || price != limitUpPrice
                || totalBuyVolume <= requiredBuyVolume / 3) {
            return 0;
        }
        boolean sellPressureEligible = totalSellVolume * 100.0 / totalBuyVolume <= 40
                || limitUpBuyAmount > 15_000;
        return sellPressureEligible && totalBuyVolume > requiredBuyVolume ? 2 : 0;
    }

    static int evaluateShanghaiCancel(int price, int limitUpPrice,
                                      long totalBuyVolume, long totalSellVolume,
                                      long requiredBuyVolume) {
        if (price != limitUpPrice) return 1;
        return totalBuyVolume <= requiredBuyVolume
                || totalSellVolume * 100.0 / totalBuyVolume > 40 ? 2 : 0;
    }

    static int evaluateShenzhenBuy(byte dataType, int price, int limitUpPrice,
                                   int orderQuantity, long limitUpBuyVolume,
                                   long totalSellVolume, long requiredBuyVolume,
                                   long limitUpBuyAmount, long circulation) {
        if (totalSellVolume * 100.0 / limitUpBuyVolume > 40) return 0;
        boolean largeOrder = dataType == 1 && limitUpBuyAmount > 1_500
                && price == limitUpPrice
                && (orderQuantity > 900_000
                || orderQuantity / 100L * limitUpPrice / 10_000L > 600)
                && limitUpBuyVolume * 100.0 / circulation > 2;
        if (largeOrder) return 6;
        return limitUpBuyVolume > requiredBuyVolume ? 7 : 0;
    }

    static int evaluateShenzhenCancel(long limitUpBuyVolume, long totalSellVolume,
                                      long requiredBuyVolume) {
        return limitUpBuyVolume <= requiredBuyVolume
                || totalSellVolume * 100.0 / limitUpBuyVolume > 40 ? 2 : 0;
    }

    static int evaluateShenzhenSnapshotCancel(int price, int limitUpPrice) {
        return price != limitUpPrice ? 1 : 0;
    }

    static boolean evaluateClosingSell(int price, int limitUpPrice,
                                       long totalBuyVolume, long totalSellVolume) {
        return price < limitUpPrice || totalBuyVolume < totalSellVolume;
    }
}
