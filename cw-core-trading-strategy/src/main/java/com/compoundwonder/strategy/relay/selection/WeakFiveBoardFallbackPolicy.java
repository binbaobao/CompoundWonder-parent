package com.compoundwonder.strategy.relay.selection;

import java.util.List;

/**
 * 唯一弱 5 板的严格 2 板兜底策略。
 *
 * <p>该策略是一种主观卡位预判：当触发日前 10 个交易日的市场平均高度低于 6 板、
 * 市场只有一只 5 板、常规连板选股没有产生任何内存候选，且这只 5 板暴露出明显的
 * 持续性风险时，才允许重新观察当天的 2 板股票。</p>
 *
 * <p>这里不能把市场高度伪装成 4 板，也不能复用宽松强度。原因是弱 5 板次日仍可能继续涨停，
 * 此时低位 2 板仍然受到高位股压制；如果再放宽 2 板质地要求，很容易在竞争中炸板。
 * 因此本策略只负责判断是否启动兜底，具体候选必须继续执行完整的正常严格过滤。</p>
 */
public final class WeakFiveBoardFallbackPolicy {

    /** 前 10 个交易日平均高度必须严格低于 6 板。 */
    private static final double MAX_PREVIOUS_TEN_DAY_AVERAGE_HEIGHT = 6D;

    /** 触发兜底的当日流通市值下限，单位：万元，即超过 45 亿元视为体量过大。 */
    private static final double MAX_CURRENT_MARKET_CAP = 450_000D;

    /** 触发兜底的当日换手率下限，超过 45% 说明筹码分歧偏大。 */
    private static final double MAX_CURRENT_TURNOVER_RATE = 45D;

    /** 当日振幅超过 13%，说明分歧过大，不利于后续继续向上。 */
    private static final double MAX_CURRENT_AMPLITUDE = 13D;

    private WeakFiveBoardFallbackPolicy() {
    }

    /** 平均高度数据完整且严格低于 6 时，才继续读取和判断唯一 5 板质量。 */
    static boolean isAverageHeightAllowed(Double previousTenDayAverageHeight) {
        return previousTenDayAverageHeight != null
                && previousTenDayAverageHeight < MAX_PREVIOUS_TEN_DAY_AVERAGE_HEIGHT;
    }

    /**
     * 判断是否启动唯一弱 5 板的严格 2 板兜底。
     *
     * @param todayHighestLimitUp      当日市场最高连板数
     * @param hasNormalRecommendations 主触发流程是否已产生内存候选
     * @param previousTenDayAverageHeight 不含当日的前 10 个交易日市场最高板平均值
     * @param fiveBoardQualities       当天过滤 ST 后的 5 板质量快照
     * @return 是否启动严格 2 板兜底以及对应判断层级和明细
     */
    public static Decision evaluate(int todayHighestLimitUp,
                             boolean hasNormalRecommendations,
                             Double previousTenDayAverageHeight,
                             List<FiveBoardQuality> fiveBoardQualities) {
        if (todayHighestLimitUp != 5) {
            return Decision.notTriggered("市场最高板", "actual=" + todayHighestLimitUp + ", required=5");
        }
        if (hasNormalRecommendations) {
            return Decision.notTriggered("常规连板已有推荐", "内存候选不为空，不启动弱5板兜底");
        }
        if (previousTenDayAverageHeight == null) {
            return Decision.notTriggered("前10日平均高度", "缺少前10个交易日完整市场最高板数据");
        }
        if (!isAverageHeightAllowed(previousTenDayAverageHeight)) {
            return Decision.notTriggered("前10日平均高度",
                    "actual=" + previousTenDayAverageHeight + ", required<6");
        }
        if (fiveBoardQualities == null || fiveBoardQualities.size() != 1) {
            int actualCount = fiveBoardQualities == null ? 0 : fiveBoardQualities.size();
            return Decision.notTriggered("5板数量", "actual=" + actualCount + ", required=1");
        }

        FiveBoardQuality quality = fiveBoardQualities.get(0);
        if (quality == null
                || quality.currentMarketCap() == null
                || quality.currentTurnoverRate() == null
                || quality.currentAmplitude() == null) {
            return Decision.notTriggered("数据完整性",
                    "缺少5板股票、当日流通市值、当日换手率或当日振幅");
        }

        if (quality.currentMarketCap() > MAX_CURRENT_MARKET_CAP) {
            return Decision.triggered("当日流通市值", commonDetail(quality) + ", required<=450000万元");
        }
        if (quality.currentTurnoverRate() > MAX_CURRENT_TURNOVER_RATE) {
            return Decision.triggered("当日换手率", commonDetail(quality) + ", required<=45%");
        }
        if (quality.currentAmplitude() > MAX_CURRENT_AMPLITUDE) {
            return Decision.triggered("当日振幅过大", commonDetail(quality)
                    + ", required<=13%, reason=分歧过大，不利于后续继续上升");
        }
        return Decision.notTriggered("唯一5板质量合格", commonDetail(quality));
    }

    private static String commonDetail(FiveBoardQuality quality) {
        return "stockCode=" + quality.stockCode()
                + ", currentMarketCap=" + quality.currentMarketCap() + "万元"
                + ", currentTurnoverRate=" + quality.currentTurnoverRate() + "%"
                + ", currentAmplitude=" + quality.currentAmplitude() + "%"
                + ", startPrice=" + quality.startPrice() + "元";
    }

    /**
     * 唯一 5 板质量快照。
     * 当日指标来自 5 板日 K；启动价格仅保留用于研究明细，不参与弱势判断或数据完整性判断。
     */
    public record FiveBoardQuality(String stockCode,
                            Double currentMarketCap,
                            Double currentTurnoverRate,
                            Double currentAmplitude,
                            Double startPrice) {
    }

    /**
     * 弱 5 板兜底判定结果。
     *
     * @param triggered 是否启动严格 2 板兜底
     * @param layer     触发或未触发的判断层级
     * @param detail    判断明细
     */
    public record Decision(boolean triggered, String layer, String detail) {

        private static Decision triggered(String layer, String detail) {
            return new Decision(true, layer, detail);
        }

        private static Decision notTriggered(String layer, String detail) {
            return new Decision(false, layer, detail);
        }
    }
}
