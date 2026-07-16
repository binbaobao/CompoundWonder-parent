package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.util.CompactTimeUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 回测成交判定规则。
 */
final class BacktestExecutionPolicy {

    static final int OVERNIGHT_FILL_TIME = 91_501_000;
    private static final int SHANGHAI_DELAY_MILLIS = 500;
    private static final int SHENZHEN_DELAY_MILLIS = 100;

    private BacktestExecutionPolicy() {
    }

    /**
     * 按规则时间加市场延迟后，判断是否仍早于回放结束时的队首委托时间。
     */
    static boolean isIntradayBuyFillable(RuleRecordDTO record) {
        if (record == null || record.getTime() == null || record.getLastOrderTime() == null
                || record.getSymbol() == null || record.getLastOrderTime() == 0) {
            return false;
        }
        int delayMillis = marketDelayMillis(record.getSymbol());
        int orderMillis = CompactTimeUtil.compactToMillis(record.getTime());
        int lastOrderMillis = CompactTimeUtil.compactToMillis(record.getLastOrderTime());
        return lastOrderMillis - orderMillis > delayMillis;
    }

    /**
     * 隔夜委托没有撤单时，只要最终队首委托晚于 09:15:01.000 即认为成交。
     */
    static boolean isOvernightBuyFillable(int lastOrderTime) {
        return lastOrderTime > OVERNIGHT_FILL_TIME;
    }

    /**
     * 隔夜委托满足队首时间，或者回测当日成交额超过 4000 万元时，均认为能够成交。
     *
     * @param dailyTurnoverInTenThousands 日 K 成交额，单位万元
     */
    static boolean isOvernightBuyFillable(int lastOrderTime, Double dailyTurnoverInTenThousands) {
        return isOvernightBuyFillable(lastOrderTime)
                || dailyTurnoverInTenThousands != null && dailyTurnoverInTenThousands > 4_000D;
    }

    /**
     * 先过滤不能成交的候选规则，再选择下单时间最早的一条。
     */
    static Optional<RuleRecordDTO> findEarliestFillableBuy(List<RuleRecordDTO> records) {
        return records.stream()
                .filter(record -> Integer.valueOf(RuleConstant.TRADING_MODE_BUY).equals(record.getActionType()))
                .filter(BacktestExecutionPolicy::isIntradayBuyFillable)
                .min(Comparator.comparingInt(RuleRecordDTO::getTime));
    }

    private static int marketDelayMillis(String symbol) {
        if (symbol.startsWith("60")) {
            return SHANGHAI_DELAY_MILLIS;
        }
        if (symbol.startsWith("00")) {
            return SHENZHEN_DELAY_MILLIS;
        }
        throw new IllegalArgumentException("当前回测只支持 60/00 沪深主板股票: " + symbol);
    }
}
