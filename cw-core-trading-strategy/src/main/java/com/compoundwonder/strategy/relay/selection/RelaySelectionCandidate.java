package com.compoundwonder.strategy.relay.selection;

/** 连板接力核心选股需要的完整候选指标。 */
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
