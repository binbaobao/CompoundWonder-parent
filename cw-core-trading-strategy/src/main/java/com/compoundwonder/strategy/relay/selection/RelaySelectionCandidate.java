package com.compoundwonder.strategy.relay.selection;

/**
 * 连板接力核心选股需要的完整候选指标。
 *
 * @param consecutiveLimitUpDays 选股日连续涨停天数，只接受 2 板或 3 板
 * @param twoAcceleratedShrinkVolumeLimitUps 本轮是否至少出现两根加速缩量板
 * @param province 公司所属省份
 * @param currentPrice 选股日收盘价，单位：元
 * @param startMarketCap 本轮首板前一交易日收盘流通市值，单位：万元
 * @param startPrice 本轮首板前一交易日收盘价，单位：元
 * @param currentTurnoverRate 选股日换手率，单位：%
 * @param currentTurnover 选股日成交额，单位：万元
 * @param currentAmplitude 选股日振幅，单位：%
 * @param nonStMonthCount 上次摘帽或上市至选股日的非 ST 自然月数
 * @param listingMonthCount 上市至选股日的自然月数
 * @param maxTurnoverRate 本轮前最近 200 根有效 K 线最大换手率，单位：%
 * @param highestConsecutiveLimitUpDays 本轮前最近 200 根有效 K 线最高连板数
 * @param priorNinetyDayHighestConsecutiveLimitUpDays 本轮前 90 个自然日最高连板数
 * @param priorNinetyDayMaxTurnoverRate 本轮前 90 个自然日最大换手率，单位：%
 * @param historicalMaxVolume 本轮前最近 200 根有效 K 线最大成交量，单位：股
 * @param maxVolumeDayTurnoverRate 最大成交量日换手率，单位：%
 * @param maxVolumeDayTurnover 最大成交量日成交额，单位：万元
 * @param abnormalKlineStateCount 18 个月窗口内排除本次连板后的非正常 K 线数
 * @param priorTwentyDayAbnormalKlineStateCount 本轮连板前 20 个交易日的非正常 K 线数
 * @param fiveDayAmplitude 包含选股日在内的 5 日复权振幅，单位：%
 * @param tenDayChangeRate 10 日复权涨跌幅，单位：%
 */
public record RelaySelectionCandidate(
        Integer consecutiveLimitUpDays,
        boolean twoAcceleratedShrinkVolumeLimitUps,
        String province,
        Double currentPrice,
        Double startMarketCap,
        Double startPrice,
        Double currentTurnoverRate,
        Double currentTurnover,
        Double currentAmplitude,
        Integer nonStMonthCount,
        Integer listingMonthCount,
        Double maxTurnoverRate,
        Integer highestConsecutiveLimitUpDays,
        Integer priorNinetyDayHighestConsecutiveLimitUpDays,
        Double priorNinetyDayMaxTurnoverRate,
        Long historicalMaxVolume,
        Double maxVolumeDayTurnoverRate,
        Double maxVolumeDayTurnover,
        Integer abnormalKlineStateCount,
        Integer priorTwentyDayAbnormalKlineStateCount,
        Double fiveDayAmplitude,
        Double tenDayChangeRate) {
}
