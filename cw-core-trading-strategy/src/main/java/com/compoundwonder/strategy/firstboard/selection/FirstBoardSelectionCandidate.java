package com.compoundwonder.strategy.firstboard.selection;

/** 普通首板核心选股需要的完整候选指标。 */
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
