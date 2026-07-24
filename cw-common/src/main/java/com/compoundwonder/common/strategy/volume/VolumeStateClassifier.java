package com.compoundwonder.common.strategy.volume;

/**
 * 根据当日换手率、K 线形态和最近 200 根有效 K 线最大换手率划分量能状态。
 *
 * <p>返回值固定为：{@code -1} 缩量、{@code 0} 正常量、{@code 1} 放量或暴量。
 * 历史最大换手率不包含当天；超过 70% 时按 70% 参与计算。</p>
 */
public final class VolumeStateClassifier {

    public static final int SHRINK_VOLUME = -1;
    public static final int NORMAL_VOLUME = 0;
    public static final int EXPAND_VOLUME = 1;

    private static final int ONE_WORD_LIMIT_UP_KLINE_STATE = 3;
    private static final double MAX_HISTORICAL_TURNOVER_RATE = 70D;
    private static final double NORMAL_MIN_RATIO_EXCLUSIVE = 0.5D;
    private static final double BURST_MIN_RATIO = 1.5D;
    private static final double ABSOLUTE_BURST_TURNOVER_RATE = 35D;

    private VolumeStateClassifier() {
    }

    /**
     * 计算当日量能状态。
     *
     * <ul>
     *   <li>一字涨停直接返回缩量，不再判断换手率。</li>
     *   <li>当日换手率达到历史最大换手率的 150%，或达到 35%，返回放量。</li>
     *   <li>当日换手率严格大于历史最大换手率的 50%，且未达到放量线，返回正常量。</li>
     *   <li>其余情况返回缩量。</li>
     * </ul>
     *
     * @param turnoverRate 当日换手率，单位为百分比，例如 12.5 表示 12.5%
     * @param klineState K 线形态；3 表示一字涨停
     * @param historicalMaxTurnoverRate200 最近 200 根有效 K 线最大换手率，不包含当天
     * @return -1 缩量、0 正常量、1 放量或暴量
     */
    public static int classify(double turnoverRate, int klineState,
                               double historicalMaxTurnoverRate200) {
        validate(turnoverRate, historicalMaxTurnoverRate200);

        if (klineState == ONE_WORD_LIMIT_UP_KLINE_STATE) {
            return SHRINK_VOLUME;
        }

        double effectiveHistoricalMax = Math.min(
                historicalMaxTurnoverRate200, MAX_HISTORICAL_TURNOVER_RATE);
        double burstThreshold = Math.min(
                effectiveHistoricalMax * BURST_MIN_RATIO,
                ABSOLUTE_BURST_TURNOVER_RATE);

        if (turnoverRate >= burstThreshold) {
            return EXPAND_VOLUME;
        }
        if (turnoverRate
                > effectiveHistoricalMax * NORMAL_MIN_RATIO_EXCLUSIVE) {
            return NORMAL_VOLUME;
        }
        return SHRINK_VOLUME;
    }

    private static void validate(double turnoverRate,
                                 double historicalMaxTurnoverRate200) {
        if (!Double.isFinite(turnoverRate) || turnoverRate < 0D) {
            throw new IllegalArgumentException("当日换手率必须是大于等于 0 的有限数");
        }
        if (!Double.isFinite(historicalMaxTurnoverRate200)
                || historicalMaxTurnoverRate200 <= 0D) {
            throw new IllegalArgumentException(
                    "200 日历史最大换手率必须是大于 0 的有限数");
        }
    }
}
