package com.compoundwonder.strategy.smallcapfirstboard.selection;

/** 小市值首板核心选股需要的完整候选指标。 */
public record SmallCapFirstBoardSelectionCandidate(
        Double startMarketCap,
        Double maxTurnoverRate,
        Integer highestConsecutiveLimitUpDays,
        Integer abnormalKlineStateCount,
        Integer priorTwentyDayAbnormalKlineStateCount,
        Double threeDayAmplitude,
        Double tenDayChangeRate) {
}
