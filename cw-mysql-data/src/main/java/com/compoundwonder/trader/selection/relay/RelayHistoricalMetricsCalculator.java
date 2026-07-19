package com.compoundwonder.trader.selection.relay;

import com.compoundwonder.hxdata.entity.StockDailyEntity;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 连板数据适配层的历史日 K 指标计算器。
 *
 * <p>只把数据库日 K 转换为策略模块需要的历史指标，不包含候选过滤和评分规则。</p>
 */
final class RelayHistoricalMetricsCalculator {

    private RelayHistoricalMetricsCalculator() {
    }

    static HistoricalMetrics calculateHistoricalMetrics(
            List<StockDailyEntity> rawWindowDailyList,
            List<StockDailyEntity> earliestStoredDailyList,
            LocalDate historicalEndDate) {
        if (rawWindowDailyList == null || earliestStoredDailyList == null
                || historicalEndDate == null || earliestStoredDailyList.size() <= 10) {
            return new HistoricalMetrics(null, null, null, null, null, null, null);
        }

        LocalDate firstEligibleDate = earliestStoredDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .skip(10)
                .map(StockDailyEntity::getTradeDate)
                .findFirst()
                .orElse(null);
        if (firstEligibleDate == null) {
            return new HistoricalMetrics(null, null, null, null, null, null, null);
        }

        List<StockDailyEntity> eligibleHistory = rawWindowDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .filter(daily -> !daily.getTradeDate().isBefore(firstEligibleDate))
                .filter(daily -> !daily.getTradeDate().isAfter(historicalEndDate))
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate).reversed())
                .limit(200)
                .toList();
        LocalDate ninetyDayStartDate = historicalEndDate.minusDays(90);
        Double maxTurnoverRate = eligibleHistory.stream()
                .map(StockDailyEntity::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        StockDailyEntity maxVolumeDaily = eligibleHistory.stream()
                .filter(daily -> daily.getVolume() != null)
                .max(Comparator.comparing(StockDailyEntity::getVolume)
                        .thenComparing(StockDailyEntity::getTradeDate))
                .orElse(null);
        Long maxVolume = maxVolumeDaily == null ? null : maxVolumeDaily.getVolume();
        Integer twoHundredKlineHighestBoard = highestBoard(eligibleHistory);
        List<StockDailyEntity> ninetyDayHistory = eligibleHistory.stream()
                .filter(daily -> !daily.getTradeDate().isBefore(ninetyDayStartDate))
                .toList();
        Integer ninetyDayHighestBoard = highestBoard(ninetyDayHistory);
        Double ninetyDayMaxTurnoverRate = ninetyDayHistory.stream()
                .map(StockDailyEntity::getTurnoverRate)
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

    private static Integer highestBoard(List<StockDailyEntity> dailyList) {
        return dailyList.stream()
                .map(StockDailyEntity::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /** 数据模块根据历史日 K 计算出的筹码指标集合。 */
    record HistoricalMetrics(Double maxTurnoverRate,
                             Long maxVolume,
                             Integer twoHundredKlineHighestBoard,
                             Integer ninetyDayHighestBoard,
                             Double ninetyDayMaxTurnoverRate,
                             Double maxVolumeDayTurnoverRate,
                             Double maxVolumeDayTurnover) {
    }
}
