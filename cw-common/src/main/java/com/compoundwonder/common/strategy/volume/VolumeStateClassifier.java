package com.compoundwonder.common.strategy.volume;

/**
 * 根据当日换手率、振幅、K 线形态和最近 200 根有效 K 线最大换手率划分量能状态。
 *
 * <p>返回值固定为：{@code -1} 缩量、{@code 0} 正常量、{@code 1} 放量或暴量。
 * 历史最大换手率不包含当天；超过 70% 时按 70% 参与计算。</p>
 */
public final class VolumeStateClassifier {

    public static final int SHRINK_VOLUME = -1;
    public static final int NORMAL_VOLUME = 0;
    public static final int EXPAND_VOLUME = 1;

    private static final int ENTITY_LIMIT_UP_KLINE_STATE = 1;
    private static final int ONE_WORD_LIMIT_UP_KLINE_STATE = 3;
    private static final double ENTITY_LIMIT_UP_NORMAL_MIN_AMPLITUDE = 6D;
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
     *   <li>实体涨停板振幅严格大于 6% 时直接返回正常量。</li>
     *   <li>当日换手率达到历史最大换手率的 150%，或达到 35%，返回放量。</li>
     *   <li>当日换手率严格大于历史最大换手率的 50%，且未达到放量线，返回正常量。</li>
     *   <li>其余情况返回缩量。</li>
     * </ul>
     *
     * @param turnoverRate 当日换手率，单位为百分比，例如 12.5 表示 12.5%
     * @param amplitude 当日振幅，单位为百分比，例如 6.5 表示 6.5%
     * @param klineState K 线形态；1 表示实体涨停，3 表示一字涨停
     * @param historicalMaxTurnoverRate200 最近 200 根有效 K 线最大换手率，不包含当天
     * @return -1 缩量、0 正常量、1 放量或暴量
     */
    public static int classify(double turnoverRate, double amplitude,
                               int klineState,
                               double historicalMaxTurnoverRate200) {
        // 调用 validate 校验当日换手率、振幅和历史最大换手率。
        validate(turnoverRate, amplitude, historicalMaxTurnoverRate200);

        if (klineState == ONE_WORD_LIMIT_UP_KLINE_STATE) {
            return SHRINK_VOLUME;
        }
        if (klineState == ENTITY_LIMIT_UP_KLINE_STATE && amplitude > ENTITY_LIMIT_UP_NORMAL_MIN_AMPLITUDE) {
            return NORMAL_VOLUME;
        }

        // 调用 Math.min 将历史最大换手率限制在 70% 以内。
        double effectiveHistoricalMax = Math.min(
                historicalMaxTurnoverRate200, MAX_HISTORICAL_TURNOVER_RATE);
        // 调用 Math.min 取历史比例阈值与 35% 固定阈值中的较小值。
        double burstThreshold = Math.min(
                effectiveHistoricalMax * BURST_MIN_RATIO,
                ABSOLUTE_BURST_TURNOVER_RATE);

        if (turnoverRate >= burstThreshold) {
            return EXPAND_VOLUME;
        }
        if (turnoverRate > effectiveHistoricalMax * NORMAL_MIN_RATIO_EXCLUSIVE) {
            return NORMAL_VOLUME;
        }
        return SHRINK_VOLUME;
    }

    private static void validate(double turnoverRate, double amplitude,
                                 double historicalMaxTurnoverRate200) {
        // 调用 Double.isFinite 校验当日换手率是否为有限数。
        if (!Double.isFinite(turnoverRate) || turnoverRate < 0D) {
            throw new IllegalArgumentException("当日换手率必须是大于等于 0 的有限数");
        }
        // 调用 Double.isFinite 校验当日振幅是否为有限数。
        if (!Double.isFinite(amplitude) || amplitude < 0D) {
            throw new IllegalArgumentException("当日振幅必须是大于等于 0 的有限数");
        }
        // 调用 Double.isFinite 校验历史最大换手率是否为有限数。
        if (!Double.isFinite(historicalMaxTurnoverRate200) || historicalMaxTurnoverRate200 <= 0D) {
            throw new IllegalArgumentException(
                    "200 日历史最大换手率必须是大于 0 的有限数");
        }
    }
}
