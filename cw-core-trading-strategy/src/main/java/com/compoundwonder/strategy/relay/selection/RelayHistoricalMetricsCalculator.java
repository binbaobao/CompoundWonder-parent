package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 连板模式的历史日 K 指标计算器。
 *
 * <p>只把数据库日 K 转换为策略模块需要的历史指标，不包含候选过滤和评分规则。</p>
 */
final class RelayHistoricalMetricsCalculator {

    private RelayHistoricalMetricsCalculator() {
    }

    /**
     * 计算本轮首板启动日前的连板筹码历史指标。
     *
     * <p>先排除上市最早 10 根 K 线，再从不晚于 {@code historicalEndDate} 的数据中
     * 取最近 200 根；90 日指标使用自然日窗口。数据不足时各指标返回 {@code null}，
     * 由选股策略的数据完整性层统一拒绝。</p>
     *
     * @param rawWindowDailyList 启动日前最多 200 根日 K
     * @param earliestStoredDailyList 数据库中最早的 11 根日 K
     * @param historicalEndDate 本轮首板启动日前一交易日
     * @return 最大换手、最大成交量、历史高度和 90 日指标集合
     */
    static HistoricalMetrics calculateHistoricalMetrics(
            List<StockDailyData> rawWindowDailyList,
            List<StockDailyData> earliestStoredDailyList,
            LocalDate historicalEndDate) {
        if (rawWindowDailyList == null || earliestStoredDailyList == null
                || historicalEndDate == null || earliestStoredDailyList.size() <= 10) {
            return new HistoricalMetrics(null, null, null, null, null, null, null);
        }

        LocalDate firstEligibleDate = earliestStoredDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .sorted(Comparator.comparing(StockDailyData::getTradeDate))
                .skip(10)
                .map(StockDailyData::getTradeDate)
                .findFirst()
                .orElse(null);
        if (firstEligibleDate == null) {
            return new HistoricalMetrics(null, null, null, null, null, null, null);
        }

        List<StockDailyData> eligibleHistory = rawWindowDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .filter(daily -> !daily.getTradeDate().isBefore(firstEligibleDate))
                .filter(daily -> !daily.getTradeDate().isAfter(historicalEndDate))
                .sorted(Comparator.comparing(StockDailyData::getTradeDate).reversed())
                .limit(200)
                .toList();
        LocalDate ninetyDayStartDate = historicalEndDate.minusDays(90);
        Double maxTurnoverRate = eligibleHistory.stream()
                .map(StockDailyData::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        StockDailyData maxVolumeDaily = eligibleHistory.stream()
                .filter(daily -> daily.getVolume() != null)
                .max(Comparator.comparing(StockDailyData::getVolume)
                        .thenComparing(StockDailyData::getTradeDate))
                .orElse(null);
        Long maxVolume = maxVolumeDaily == null ? null : maxVolumeDaily.getVolume();
        Integer twoHundredKlineHighestBoard = highestBoard(eligibleHistory);
        List<StockDailyData> ninetyDayHistory = eligibleHistory.stream()
                .filter(daily -> !daily.getTradeDate().isBefore(ninetyDayStartDate))
                .toList();
        Integer ninetyDayHighestBoard = highestBoard(ninetyDayHistory);
        Double ninetyDayMaxTurnoverRate = ninetyDayHistory.stream()
                .map(StockDailyData::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        return new HistoricalMetrics(
                maxTurnoverRate,
                maxVolume,
                twoHundredKlineHighestBoard,
                ninetyDayHighestBoard,
                ninetyDayMaxTurnoverRate,
                maxVolumeDaily == null ? null : maxVolumeDaily.getTurnoverRate(),
                maxVolumeDaily == null ? null : maxVolumeDaily.getTurnover());
    }

    private static Integer highestBoard(List<StockDailyData> dailyList) {
        return dailyList.stream()
                .map(StockDailyData::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /** 数据模块根据历史日 K 计算出的筹码指标集合。 */
    /**
     * 连板接力历史筹码指标集合。
     *
     * @param maxTurnoverRate 最近 200 根有效 K 线最大换手率，单位：%
     * @param maxVolume 最近 200 根有效 K 线最大成交量，单位：股
     * @param twoHundredKlineHighestBoard 最近 200 根有效 K 线最高连板数
     * @param ninetyDayHighestBoard 前 90 个自然日最高连板数
     * @param ninetyDayMaxTurnoverRate 前 90 个自然日最大换手率，单位：%
     * @param maxVolumeDayTurnoverRate 最大成交量日换手率，单位：%
     * @param maxVolumeDayTurnover 最大成交量日成交额，单位：万元
     */
    record HistoricalMetrics(Double maxTurnoverRate,
                             Long maxVolume,
                             Integer twoHundredKlineHighestBoard,
                             Integer ninetyDayHighestBoard,
                             Double ninetyDayMaxTurnoverRate,
                             Double maxVolumeDayTurnoverRate,
                             Double maxVolumeDayTurnover) {
    }
}
