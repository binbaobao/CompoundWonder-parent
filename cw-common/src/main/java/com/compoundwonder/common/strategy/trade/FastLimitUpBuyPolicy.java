package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.util.CompactTimeUtil;

/**
 * 三种交易模式共用的快速冲板买入约束。
 */
public final class FastLimitUpBuyPolicy {

    /**
     * 从分钟收盘价首次达到 7% 到买入，至少要经历的交易分钟数。
     */
    private static final int MIN_MINUTES_AFTER_SEVEN_PERCENT = 8;
    /**
     * 发生过快速冲板后，重新允许买入所需的最低实时换手率，单位：%。
     */
    private static final double MIN_TURNOVER_AFTER_FAST_LIMIT_UP = 25.0;
    /**
     * 低位开盘的换手板允许在快速冲板炸开后重新判断，单位：%。
     */
    private static final double MAX_OPEN_INCREASE_FOR_FAST_LIMIT_UP_RECOVERY = 3.0;
    /**
     * 低位开盘换手板从首次达到 7% 到恢复买入的最长交易分钟数。
     */
    private static final int MAX_MINUTES_FOR_LOW_OPEN_RECOVERY = 15;

    private FastLimitUpBuyPolicy() {
    }

    /**
     * 当前分钟不参与历史判断，避免用尚未走完的封板分钟反推路径。首次达到 7% 后
     * 不足 8 个交易分钟直接拒绝；若这段窗口内已经完成过涨停分钟，高开超过 3%
     * 的加速板需等换手达到 25% 才恢复买入；开盘不高于 3% 且
     * 首次达到 7% 后不超过 15 个交易分钟的低位换手板，仍交给后续规则判断。
     *
     * @param market 当前交易会话的共享行情
     * @return 应拒绝本次买入返回 {@code true}
     */
    public static boolean shouldReject(TradeMarketState market) {
        int closePrice = market.getClosePrice();
        int currentMinuteIndex = CompactTimeUtil.calculateIndex(market.getTime());
        if (closePrice <= 0 || currentMinuteIndex <= 0) {
            return false;
        }
        int firstSevenPercentIndex = -1;
        for (int index = 0; index < currentMinuteIndex; index++) {
            int minutePrice = market.getMinutePriceAt(index);
            if (minutePrice > 0
                    && (minutePrice - closePrice) * 100.0 / closePrice >= 7.0) {
                firstSevenPercentIndex = index;
                break;
            }
        }
        if (firstSevenPercentIndex < 0) {
            return false;
        }
        if (currentMinuteIndex - firstSevenPercentIndex
                < MIN_MINUTES_AFTER_SEVEN_PERCENT) {
            return true;
        }
        if (market.getTurnoverRate() >= MIN_TURNOVER_AFTER_FAST_LIMIT_UP) {
            return false;
        }
        if (market.getOpenIncrease()
                <= MAX_OPEN_INCREASE_FOR_FAST_LIMIT_UP_RECOVERY
                && currentMinuteIndex - firstSevenPercentIndex
                <= MAX_MINUTES_FOR_LOW_OPEN_RECOVERY) {
            return false;
        }
        int fastWindowEnd = Math.min(
                currentMinuteIndex,
                firstSevenPercentIndex + MIN_MINUTES_AFTER_SEVEN_PERCENT);
        for (int index = firstSevenPercentIndex; index < fastWindowEnd; index++) {
            if (market.getMinutePriceAt(index) == market.getLimitUpPrice()) {
                return true;
            }
        }
        return false;
    }
}
