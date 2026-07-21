package com.compoundwonder.strategy.smallcapfirstboard.selection;

/**
 * 小市值首板核心选股需要的完整候选指标。
 *
 * @param startMarketCap 首板前一交易日收盘流通市值，单位：万元
 * @param currentPrice 选股日首板涨停收盘价，单位：元
 * @param maxTurnoverRate 首板前最近 200 根有效 K 线最大换手率，单位：%
 * @param highestConsecutiveLimitUpDays 首板前最近 200 根有效 K 线最高连板数
 * @param abnormalKlineStateCount 18 个月窗口内排除本次首板后的非正常 K 线数
 * @param priorTwentyDayAbnormalKlineStateCount 本次首板前 20 个交易日的非正常 K 线数
 * @param threeDayAmplitude 包含选股日在内的 3 日复权振幅，单位：%
 * @param tenDayChangeRate 10 日复权涨跌幅，单位：%
 */
public record SmallCapFirstBoardSelectionCandidate(
        Double startMarketCap,
        Double currentPrice,
        Double maxTurnoverRate,
        Integer highestConsecutiveLimitUpDays,
        Integer abnormalKlineStateCount,
        Integer priorTwentyDayAbnormalKlineStateCount,
        Double threeDayAmplitude,
        Double tenDayChangeRate) {
}
