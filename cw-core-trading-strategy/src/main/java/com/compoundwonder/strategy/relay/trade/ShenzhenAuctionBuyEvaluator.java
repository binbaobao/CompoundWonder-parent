package com.compoundwonder.strategy.relay.trade;

/**
 * 连板模式的深圳早盘集合竞价场景。
 *
 * <p>使用逐笔委托识别隔夜买入，并同时处理逐笔与快照撤单。价格单位为分，
 * 数量单位为股，流通股本单位为股，金额单位为万元。</p>
 */
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

    static int evaluateCancel(long limitUpBuyVolume, long totalSellVolume,
                              long requiredBuyVolume) {
        return limitUpBuyVolume <= requiredBuyVolume
                || totalSellVolume * 100.0 / limitUpBuyVolume > 40 ? 2 : 0;
    }

    static int evaluateSnapshotCancel(int price, int limitUpPrice) {
        return price != limitUpPrice ? 1 : 0;
    }
}
