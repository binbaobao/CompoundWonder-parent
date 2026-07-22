package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;

/** 解析触发点所需的三日市场高度、周期占领者与最高板股票。 */
public record RelayTriggerContext(MarketEmotionData today,
                                  MarketEmotionData yesterday,
                                  MarketEmotionData dayBeforeYesterday,
                                  String yesterdayHighestStockCode,
                                  String dayBeforeYesterdayHighestStockCode) {
}
