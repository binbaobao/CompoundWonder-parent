package com.compoundwonder.strategy.sell;

/**
 * 卖出规则使用的启动流通市值分档。
 *
 * <p>市值单位统一为万元。119999 万元是业务确认的唯一分界：
 * 小于该值走小市值规则，大于等于该值走普通市值规则。</p>
 */
public enum SellMarketCapBand {
    SMALL_CAP,
    NORMAL_CAP;

    public static final int SMALL_CAP_UPPER_EXCLUSIVE_WAN = 119_999;

    public static SellMarketCapBand from(long initialMarketValue) {
        return initialMarketValue < SMALL_CAP_UPPER_EXCLUSIVE_WAN
                ? SMALL_CAP
                : NORMAL_CAP;
    }
}
