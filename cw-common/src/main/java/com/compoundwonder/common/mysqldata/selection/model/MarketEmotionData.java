package com.compoundwonder.common.mysqldata.selection.model;

import java.time.LocalDate;

/** 连板选股读取的每日市场高度事实。 */
public record MarketEmotionData(LocalDate tradeDate,
                                Integer highestConsecutiveLimitUpDays,
                                String dominantCycleStockCode) {
}
