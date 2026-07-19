package com.compoundwonder.strategy.firstboard.trade;

/** 普通首板模式的深圳早盘集合竞价买入、逐笔撤单和快照撤单场景。 */
final class ShenzhenAuctionBuyEvaluator {
    private ShenzhenAuctionBuyEvaluator() {
    }
    static int evaluateBuy(byte dataType, int price, int limitUpPrice, int orderQuantity,
                           long limitUpBuyVolume, long totalSellVolume, long requiredBuyVolume,
                           long limitUpBuyAmount, long circulation) {
        if (totalSellVolume * 100.0 / limitUpBuyVolume > 40) {
            return 0;
        }
        boolean largeOrder = dataType == 1 && limitUpBuyAmount > 1_500
                && price == limitUpPrice
                && (orderQuantity > 900_000
                || orderQuantity / 100L * limitUpPrice / 10_000L > 600)
                && limitUpBuyVolume * 100.0 / circulation > 2;
        if (largeOrder) {
            return 6;
        }
        return limitUpBuyVolume > requiredBuyVolume ? 7 : 0;
    }
    static int evaluateCancel(long limitUpBuyVolume, long totalSellVolume, long requiredBuyVolume) {
        return limitUpBuyVolume <= requiredBuyVolume
                || totalSellVolume * 100.0 / limitUpBuyVolume > 40 ? 2 : 0;
    }
    static int evaluateSnapshotCancel(int price, int limitUpPrice) {
        return price != limitUpPrice ? 1 : 0;
    }
}
