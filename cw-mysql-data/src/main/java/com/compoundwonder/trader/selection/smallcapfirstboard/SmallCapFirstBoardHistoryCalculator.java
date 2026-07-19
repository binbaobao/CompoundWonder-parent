package com.compoundwonder.trader.selection.smallcapfirstboard;

import com.compoundwonder.hxdata.entity.StockDailyEntity;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 小市值首板独立历史指标计算器。
 *
 * <p>排除上市最早 10 根 K 线后，只取首板前最近 200 根有效 K 线。小市值模式
 * 只关心该窗口的最大换手率和最高连板数，不借用普通首板筹码阶梯。</p>
 */
final class SmallCapFirstBoardHistoryCalculator {

    private SmallCapFirstBoardHistoryCalculator() {
    }

    static HistoricalMetrics calculate(List<StockDailyEntity> rawHistory,
                                       List<StockDailyEntity> earliestStoredDailyList,
                                       LocalDate historyEndDate) {
        if (rawHistory == null || earliestStoredDailyList == null || historyEndDate == null
                || earliestStoredDailyList.size() <= 10) {
            return new HistoricalMetrics(null, null);
        }
        LocalDate firstEligibleDate = earliestStoredDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .skip(10)
                .map(StockDailyEntity::getTradeDate)
                .findFirst()
                .orElse(null);
        if (firstEligibleDate == null) return new HistoricalMetrics(null, null);

        List<StockDailyEntity> eligibleHistory = rawHistory.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .filter(daily -> !daily.getTradeDate().isBefore(firstEligibleDate))
                .filter(daily -> !daily.getTradeDate().isAfter(historyEndDate))
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate).reversed())
                .limit(200)
                .toList();
        Double maxTurnoverRate = eligibleHistory.stream()
                .map(StockDailyEntity::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        Integer highestBoard = eligibleHistory.stream()
                .map(StockDailyEntity::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        return new HistoricalMetrics(maxTurnoverRate, highestBoard);
    }

    record HistoricalMetrics(Double maxTurnoverRate, Integer highestBoard) {
    }
}
