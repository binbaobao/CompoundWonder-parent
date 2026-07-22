package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;

import java.util.List;
import java.util.Objects;

/** 按“高度压制 -> 第二次补充 -> 断板当天”的顺序解析唯一主触发点。 */
public final class RelayTriggerResolver {

    private RelayTriggerResolver() {
    }

    public static RelaySelectionPlan resolve(RelayTriggerContext context) {
        if (context == null || context.today() == null || context.yesterday() == null
                || context.dayBeforeYesterday() == null) {
            return RelaySelectionPlan.none("缺少连续三个交易日的市场情绪数据");
        }

        int todayHeight = height(context.today());
        int yesterdayHeight = height(context.yesterday());
        int dayBeforeHeight = height(context.dayBeforeYesterday());

        if (todayHeight >= 2 && todayHeight <= 4) {
            List<RelayBoardPlan> boards = todayHeight == 2
                    ? List.of(new RelayBoardPlan(2, RelaySelectionStrength.RELAXED))
                    : List.of(new RelayBoardPlan(3, RelaySelectionStrength.RELAXED),
                            new RelayBoardPlan(2, RelaySelectionStrength.NORMAL));
            return new RelaySelectionPlan(RelaySelectionTrigger.HEIGHT_SUPPRESSION,
                    boards, 3, "当日最高板=" + todayHeight);
        }

        if (todayHeight >= 5 && dayBeforeHeight >= 5
                && yesterdayHeight <= dayBeforeHeight
                && isValidBreakSource(context.dayBeforeYesterday(), dayBeforeHeight,
                context.dayBeforeYesterdayHighestStockCode())) {
            return new RelaySelectionPlan(RelaySelectionTrigger.HIGH_TO_LOW_SECOND,
                    List.of(new RelayBoardPlan(3, RelaySelectionStrength.NORMAL)),
                    3, "前一交易日完成高度" + dayBeforeHeight + "断板后的第二次补充");
        }

        if (todayHeight >= 5 && yesterdayHeight >= 5
                && todayHeight <= yesterdayHeight
                && isValidBreakSource(context.yesterday(), yesterdayHeight,
                context.yesterdayHighestStockCode())) {
            return new RelaySelectionPlan(RelaySelectionTrigger.HIGH_TO_LOW_BREAK,
                    List.of(new RelayBoardPlan(3, RelaySelectionStrength.STRICT),
                            new RelayBoardPlan(2, RelaySelectionStrength.STRICT)),
                    3, "当日确认高度" + yesterdayHeight + "断板");
        }

        return RelaySelectionPlan.none("未命中高度压制或有效高切低");
    }

    private static boolean isValidBreakSource(MarketEmotionData sourceDay,
                                              int sourceHeight,
                                              String sourceHighestStockCode) {
        if (sourceHeight < 7) {
            return true;
        }
        return sourceHighestStockCode != null
                && !sourceHighestStockCode.isBlank()
                && Objects.equals(sourceHighestStockCode,
                sourceDay.dominantCycleStockCode());
    }

    private static int height(MarketEmotionData emotion) {
        return Objects.requireNonNullElse(emotion.highestConsecutiveLimitUpDays(), 0);
    }
}
