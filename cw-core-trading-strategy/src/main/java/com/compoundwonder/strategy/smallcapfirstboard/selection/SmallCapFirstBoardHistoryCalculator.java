package com.compoundwonder.strategy.smallcapfirstboard.selection;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 小市值首板独立历史指标计算器。
 *
 * <p>排除上市最早 10 根 K 线后，只取首板前最近 100 根有效 K 线。小市值模式
 * 只关心该窗口的最大换手率和最高连板数，不借用普通首板筹码阶梯。</p>
 */
final class SmallCapFirstBoardHistoryCalculator {

    private SmallCapFirstBoardHistoryCalculator() {
    }

    /**
     * 计算小市值首板启动日前的独立历史硬指标。
     *
     * <p>排除上市最早 10 根 K 线后，只保留不晚于 {@code historyEndDate} 的最近
     * 100 根日 K。数据不足时返回空指标，由策略的数据完整性和默认值口径继续处理。</p>
     *
     * @param rawHistory 启动日前最多 100 根日 K
     * @param earliestStoredDailyList 数据库中最早的 11 根日 K
     * @param historyEndDate 本次首板启动日前一交易日
     * @return 历史最大换手率和最高连板数
     */
    static HistoricalMetrics calculate(List<StockDailyData> rawHistory,
                                       List<StockDailyData> earliestStoredDailyList,
                                       LocalDate historyEndDate) {
        if (rawHistory == null || earliestStoredDailyList == null || historyEndDate == null
                || earliestStoredDailyList.size() <= 10) {
            return new HistoricalMetrics(null, null);
        }
        LocalDate firstEligibleDate = earliestStoredDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .sorted(Comparator.comparing(StockDailyData::getTradeDate))
                .skip(10)
                .map(StockDailyData::getTradeDate)
                .findFirst()
                .orElse(null);
        if (firstEligibleDate == null) return new HistoricalMetrics(null, null);

        List<StockDailyData> eligibleHistory = rawHistory.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .filter(daily -> !daily.getTradeDate().isBefore(firstEligibleDate))
                .filter(daily -> !daily.getTradeDate().isAfter(historyEndDate))
                .sorted(Comparator.comparing(StockDailyData::getTradeDate).reversed())
                .limit(100)
                .toList();
        Double maxTurnoverRate = eligibleHistory.stream()
                .map(StockDailyData::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        Integer highestBoard = eligibleHistory.stream()
                .map(StockDailyData::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        return new HistoricalMetrics(maxTurnoverRate, highestBoard);
    }

    /**
     * 小市值首板历史硬指标。
     *
     * @param maxTurnoverRate 最近 100 根有效 K 线最大换手率，单位：%
     * @param highestBoard 最近 100 根有效 K 线最高连板数
     */
    record HistoricalMetrics(Double maxTurnoverRate, Integer highestBoard) {
    }
}
