package com.compoundwonder.trader.service.impl;

import java.util.List;

/**
 * 唯一弱 5 板的严格 2 板兜底策略。
 *
 * <p>该策略是一种主观卡位预判：当市场只有一只 5 板、常规连板选股没有产生任何内存候选，
 * 且这只 5 板暴露出明显的持续性风险时，才允许重新观察当天的 2 板股票。</p>
 *
 * <p>这里不能把市场高度伪装成 4 板，也不能复用冰点 3/4 板宽松通道。原因是弱 5 板次日仍可能继续涨停，
 * 此时低位 2 板仍然受到高位股压制；如果再放宽 2 板质地要求，很容易在竞争中炸板。
 * 因此本策略只负责判断是否启动兜底，具体候选必须继续执行完整的正常严格过滤。</p>
 */
final class WeakFiveBoardFallbackPolicy {

    /** 触发兜底的当日流通市值下限，单位：万元，即超过 45 亿元视为体量过大。 */
    private static final double MAX_CURRENT_MARKET_CAP = 450_000D;

    /** 触发兜底的当日换手率下限，超过 45% 说明筹码分歧偏大。 */
    private static final double MAX_CURRENT_TURNOVER_RATE = 45D;

    /** 当日振幅低于 2.5%，通常是一字板或大幅高开后缺少真实分歧。 */
    private static final double MIN_CURRENT_AMPLITUDE = 2.5D;

    /** 当日振幅超过 13%，说明分歧过大，不利于后续继续向上。 */
    private static final double MAX_CURRENT_AMPLITUDE = 13D;

    /** 本轮连板启动价格低于 2.5 元，视为价格过低。 */
    private static final double MIN_START_PRICE = 2.5D;

    /** 本轮连板启动价格超过 30 元，视为价格过高。 */
    private static final double MAX_START_PRICE = 30D;

    private WeakFiveBoardFallbackPolicy() {
    }

    /**
     * 判断是否启动唯一弱 5 板的严格 2 板兜底。
     *
     * @param todayHighestLimitUp      当日市场最高连板数
     * @param hasNormalRecommendations 常规连板严格/冰点流程是否已产生内存候选
     * @param fiveBoardQualities       当天过滤 ST 后的 5 板质量快照
     */
    static Decision evaluate(int todayHighestLimitUp,
                             boolean hasNormalRecommendations,
                             List<FiveBoardQuality> fiveBoardQualities) {
        if (todayHighestLimitUp != 5) {
            return Decision.notTriggered("市场最高板", "actual=" + todayHighestLimitUp + ", required=5");
        }
        if (hasNormalRecommendations) {
            return Decision.notTriggered("常规连板已有推荐", "内存候选不为空，不启动弱5板兜底");
        }
        if (fiveBoardQualities == null || fiveBoardQualities.size() != 1) {
            int actualCount = fiveBoardQualities == null ? 0 : fiveBoardQualities.size();
            return Decision.notTriggered("5板数量", "actual=" + actualCount + ", required=1");
        }

        FiveBoardQuality quality = fiveBoardQualities.get(0);
        if (quality == null
                || quality.currentMarketCap() == null
                || quality.currentTurnoverRate() == null
                || quality.currentAmplitude() == null
                || quality.startPrice() == null) {
            return Decision.notTriggered("数据完整性",
                    "缺少5板股票、当日流通市值、当日换手率、当日振幅或启动价格");
        }

        if (quality.currentMarketCap() > MAX_CURRENT_MARKET_CAP) {
            return Decision.triggered("当日流通市值", commonDetail(quality) + ", required<=450000万元");
        }
        if (quality.currentTurnoverRate() > MAX_CURRENT_TURNOVER_RATE) {
            return Decision.triggered("当日换手率", commonDetail(quality) + ", required<=45%");
        }
        if (quality.currentAmplitude() < MIN_CURRENT_AMPLITUDE) {
            return Decision.triggered("当日振幅过小", commonDetail(quality)
                    + ", required>=2.5%, reason=可能是一字板或大高开，未经历真实的分歧转一致");
        }
        if (quality.currentAmplitude() > MAX_CURRENT_AMPLITUDE) {
            return Decision.triggered("当日振幅过大", commonDetail(quality)
                    + ", required<=13%, reason=分歧过大，不利于后续继续上升");
        }
        if (quality.startPrice() < MIN_START_PRICE) {
            return Decision.triggered("启动价格过低", commonDetail(quality) + ", required>=2.5元");
        }
        if (quality.startPrice() > MAX_START_PRICE) {
            return Decision.triggered("启动价格过高", commonDetail(quality) + ", required<=30元");
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
     * 当日指标来自 5 板日 K；启动价格必须来自选股辅助对象，保持本轮首板前一交易日口径。
     */
    record FiveBoardQuality(String stockCode,
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
    record Decision(boolean triggered, String layer, String detail) {

        private static Decision triggered(String layer, String detail) {
            return new Decision(true, layer, detail);
        }

        private static Decision notTriggered(String layer, String detail) {
            return new Decision(false, layer, detail);
        }
    }
}
