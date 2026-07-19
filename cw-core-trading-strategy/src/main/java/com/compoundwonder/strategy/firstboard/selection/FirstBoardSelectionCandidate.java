package com.compoundwonder.strategy.firstboard.selection;

/**
 * 普通首板核心选股需要的完整候选指标。
 *
 * @param startMarketCap 首板前一交易日收盘流通市值，单位：万元
 * @param currentPrice 选股日收盘价，单位：元
 * @param startPrice 首板前一交易日收盘价，单位：元
 * @param currentTurnoverRate 选股日换手率，单位：%
 * @param province 公司所属省份
 * @param nonStMonthCount 上次摘帽或上市至选股日的非 ST 自然月数
 * @param listingMonthCount 上市至选股日的自然月数
 * @param maxTurnoverRate 首板前最近 200 根有效 K 线最大换手率，单位：%
 * @param highestConsecutiveLimitUpDays 首板前最近 200 根有效 K 线最高连板数
 * @param priorNinetyDayHighestConsecutiveLimitUpDays 首板前 90 个自然日最高连板数
 * @param historicalMaxVolume 首板前最近 200 根有效 K 线最大成交量，单位：股
 * @param abnormalKlineStateCount 18 个月窗口内排除本次首板后的非正常 K 线数
 * @param priorTwentyDayAbnormalKlineStateCount 本次首板前 20 个交易日的非正常 K 线数
 * @param threeDayAmplitude 包含选股日在内的 3 日复权振幅，单位：%
 * @param tenDayChangeRate 10 日复权涨跌幅，单位：%
 */
public record FirstBoardSelectionCandidate(
        Double startMarketCap,
        Double currentPrice,
        Double startPrice,
        Double currentTurnoverRate,
        String province,
        Integer nonStMonthCount,
        Integer listingMonthCount,
        Double maxTurnoverRate,
        Integer highestConsecutiveLimitUpDays,
        Integer priorNinetyDayHighestConsecutiveLimitUpDays,
        Long historicalMaxVolume,
        Integer abnormalKlineStateCount,
        Integer priorTwentyDayAbnormalKlineStateCount,
        Double threeDayAmplitude,
        Double tenDayChangeRate) {
}
