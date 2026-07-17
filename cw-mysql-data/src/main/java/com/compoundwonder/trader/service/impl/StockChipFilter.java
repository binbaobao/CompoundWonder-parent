package com.compoundwonder.trader.service.impl;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.dto.StockSelectionAssistDTO;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 股票历史筹码过滤器。
 *
 * <p>该类负责从原始日 K 计算历史筹码指标，并根据选股辅助指标判断是否通过；
 * 类本身不读取数据库，便于独立验证每一层筹码规则。首板 30 亿、连板 50 亿的当日流通市值上限
 * 仍由候选查询负责，普通阶梯和特殊通道都不能绕过候选查询。</p>
 *
 * <p>过滤顺序固定为：数据完整性、历史最大换手、18 个月历史最高板、
 * 90 日历史最高板、市值换手价格阶梯、低换手低筹码金额特殊通道。</p>
 */
final class StockChipFilter {

    /** 历史最大换手率超过 50% 时，不论当前是几板都直接过滤。 */
    private static final double MAX_ALLOWED_HISTORICAL_TURNOVER_RATE = 50D;

    /** 本轮连板前 18 个月内允许出现的最高板数，超过 5 板才过滤。 */
    private static final int MAX_ALLOWED_EIGHTEEN_MONTH_BOARD = 5;

    /** 本轮连板前 90 个自然日内只要出现过 3 板就过滤。 */
    private static final int MAX_ALLOWED_NINETY_DAY_BOARD_EXCLUSIVE = 3;

    /** 特殊通道要求历史最大换手率严格低于 18%。 */
    private static final double SPECIAL_MAX_TURNOVER_RATE = 18D;

    /** 特殊通道要求选股涨停日收盘价严格低于 17.5 元。 */
    private static final double SPECIAL_MAX_CURRENT_PRICE = 17.5D;

    /** 特殊通道最大筹码金额，单位：万元，即 3.98 亿元。 */
    private static final double SPECIAL_MAX_CHIP_AMOUNT = 39_800D;

    private StockChipFilter() {
    }

    /**
     * 计算本轮连板前的历史筹码指标。
     *
     * <p>{@code rawWindowDailyList} 是本轮首板前一交易日往前 18 个自然月的原始日 K；
     * {@code earliestStoredDailyList} 是数据库中该股票最早的 11 根日 K。以前 10 根日 K
     * 作为新股上市初期数据，不参与换手、成交量和历史板数统计。老股票的最早 10 根数据
     * 位于 18 个月窗口之外，因此不会误删老股票窗口内的数据。</p>
     *
     * <p>90 日最高板使用自然日窗口，包含起止日期；历史最高达到 3 板即由过滤层剔除。</p>
     */
    static HistoricalMetrics calculateHistoricalMetrics(
            List<StockDailyEntity> rawWindowDailyList,
            List<StockDailyEntity> earliestStoredDailyList,
            LocalDate historicalEndDate) {
        if (rawWindowDailyList == null || earliestStoredDailyList == null
                || historicalEndDate == null || earliestStoredDailyList.size() <= 10) {
            return new HistoricalMetrics(null, null, null, null);
        }

        LocalDate firstEligibleDate = earliestStoredDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .skip(10)
                .map(StockDailyEntity::getTradeDate)
                .findFirst()
                .orElse(null);
        if (firstEligibleDate == null) {
            return new HistoricalMetrics(null, null, null, null);
        }

        List<StockDailyEntity> eligibleHistory = rawWindowDailyList.stream()
                .filter(daily -> daily.getTradeDate() != null)
                .filter(daily -> !daily.getTradeDate().isBefore(firstEligibleDate))
                .filter(daily -> !daily.getTradeDate().isAfter(historicalEndDate))
                .toList();
        LocalDate ninetyDayStartDate = historicalEndDate.minusDays(90);
        Double maxTurnoverRate = eligibleHistory.stream()
                .map(StockDailyEntity::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        Long maxVolume = eligibleHistory.stream()
                .map(StockDailyEntity::getVolume)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);
        Integer eighteenMonthHighestBoard = highestBoard(eligibleHistory);
        Integer ninetyDayHighestBoard = highestBoard(eligibleHistory.stream()
                .filter(daily -> !daily.getTradeDate().isBefore(ninetyDayStartDate))
                .toList());
        return new HistoricalMetrics(
                maxTurnoverRate, maxVolume, eighteenMonthHighestBoard, ninetyDayHighestBoard);
    }

    private static Integer highestBoard(List<StockDailyEntity> dailyList) {
        return dailyList.stream()
                .map(StockDailyEntity::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /**
     * 按固定层级执行筹码过滤，并返回命中的层级与指标明细。
     */
    static Decision evaluate(StockSelectionAssistDTO assist) {
        if (assist == null
                || assist.getStartMarketCap() == null
                || assist.getCurrentPrice() == null
                || assist.getMaxTurnoverRate() == null
                || assist.getHighestConsecutiveLimitUpDays() == null
                || assist.getPriorNinetyDayHighestConsecutiveLimitUpDays() == null) {
            return Decision.rejected("筹码数据完整性", "缺少启动市值、当前价格或历史筹码指标");
        }

        double startMarketCap = assist.getStartMarketCap();
        double currentPrice = assist.getCurrentPrice();
        double maxTurnoverRate = assist.getMaxTurnoverRate();
        int eighteenMonthHighest = assist.getHighestConsecutiveLimitUpDays();
        int ninetyDayHighest = assist.getPriorNinetyDayHighestConsecutiveLimitUpDays();

        if (maxTurnoverRate > MAX_ALLOWED_HISTORICAL_TURNOVER_RATE) {
            return Decision.rejected("历史最大换手",
                    "actual=" + maxTurnoverRate + "%, required<=50%");
        }
        if (eighteenMonthHighest > MAX_ALLOWED_EIGHTEEN_MONTH_BOARD) {
            return Decision.rejected("18个月历史最高板",
                    "actual=" + eighteenMonthHighest + ", required<=5");
        }
        if (ninetyDayHighest >= MAX_ALLOWED_NINETY_DAY_BOARD_EXCLUSIVE) {
            return Decision.rejected("90日历史最高板",
                    "actual=" + ninetyDayHighest + ", required<3");
        }

        if (matchesMarketCapTurnoverPriceBand(
                startMarketCap, maxTurnoverRate, currentPrice)) {
            return Decision.passed("市值换手价格阶梯",
                    commonDetail(startMarketCap, maxTurnoverRate, currentPrice));
        }

        Long historicalMaxVolume = assist.getHistoricalMaxVolume();
        if (historicalMaxVolume == null) {
            return Decision.rejected("筹码数据完整性", "普通阶梯未通过且缺少历史最大成交量");
        }
        double maxVolumeChipAmount = historicalMaxVolume * currentPrice / 10_000D;
        if (maxTurnoverRate < SPECIAL_MAX_TURNOVER_RATE
                && currentPrice < SPECIAL_MAX_CURRENT_PRICE
                && maxVolumeChipAmount < SPECIAL_MAX_CHIP_AMOUNT) {
            return Decision.passed("低换手低筹码金额特殊通道",
                    commonDetail(startMarketCap, maxTurnoverRate, currentPrice)
                            + ", maxVolumeChipAmount=" + maxVolumeChipAmount + "万元");
        }

        return Decision.rejected("市值换手价格阶梯及特殊通道",
                commonDetail(startMarketCap, maxTurnoverRate, currentPrice)
                        + ", specialMaxVolumeChipAmount=" + maxVolumeChipAmount + "万元");
    }

    /**
     * 按启动流通市值进入唯一对应的换手率、价格档位，档位之间互不回退。
     * 市值单位为万元，价格单位为元，换手率单位为百分比。
     */
    private static boolean matchesMarketCapTurnoverPriceBand(
            double startMarketCap, double maxTurnoverRate, double currentPrice) {
        if (startMarketCap <= 93_000D) {
            return maxTurnoverRate < 55D;
        }
        if (startMarketCap <= 106_000D) {
            return maxTurnoverRate < 50D;
        }
        if (startMarketCap <= 120_000D) {
            return maxTurnoverRate < 46D;
        }
        if (startMarketCap <= 138_800D) {
            return maxTurnoverRate < 44D;
        }
        if (startMarketCap <= 151_000D) {
            return maxTurnoverRate < 43D && currentPrice < 25D;
        }
        if (startMarketCap <= 168_000D) {
            return maxTurnoverRate < 39D && currentPrice < 22D;
        }
        if (startMarketCap <= 187_000D) {
            return maxTurnoverRate < 35D && currentPrice < 20D;
        }
        if (startMarketCap <= 208_000D) {
            return maxTurnoverRate < 27D && currentPrice < 17.5D;
        }
        if (startMarketCap <= 220_000D) {
            return maxTurnoverRate < 25D && currentPrice < 15D;
        }
        if (startMarketCap <= 250_000D) {
            return maxTurnoverRate < 20D && currentPrice < 15D;
        }
        return false;
    }

    private static String commonDetail(double startMarketCap,
                                       double maxTurnoverRate,
                                       double currentPrice) {
        return "startMarketCap=" + startMarketCap + "万元"
                + ", maxTurnoverRate=" + maxTurnoverRate + "%"
                + ", currentPrice=" + currentPrice + "元";
    }

    /**
     * 筹码判断结果。
     *
     * @param passed 是否通过筹码过滤
     * @param layer  通过或被过滤的具体层级，直接用于选股日志
     * @param detail 参与判断的指标明细
     */
    record Decision(boolean passed, String layer, String detail) {

        private static Decision passed(String layer, String detail) {
            return new Decision(true, layer, detail);
        }

        private static Decision rejected(String layer, String detail) {
            return new Decision(false, layer, detail);
        }
    }

    /**
     * 本轮连板开始前的历史筹码指标。
     *
     * @param maxTurnoverRate           18 个自然月历史最大换手率，单位：%
     * @param maxVolume                 18 个自然月历史最大成交量，单位：股
     * @param eighteenMonthHighestBoard 18 个自然月历史最高板
     * @param ninetyDayHighestBoard     90 个自然日历史最高板
     */
    record HistoricalMetrics(Double maxTurnoverRate,
                             Long maxVolume,
                             Integer eighteenMonthHighestBoard,
                             Integer ninetyDayHighestBoard) {
    }
}
