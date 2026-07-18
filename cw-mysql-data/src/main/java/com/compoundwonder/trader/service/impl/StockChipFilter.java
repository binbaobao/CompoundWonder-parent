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
 * <p>过滤顺序固定为：数据完整性、历史最大换手、200 根日 K 历史最高板、
 * 90 日历史最高板、市值换手价格阶梯、低换手低筹码金额特殊通道。</p>
 */
final class StockChipFilter {

    /** 历史最大换手率超过 55% 时，不论当前是几板都直接过滤。 */
    private static final double MAX_ALLOWED_HISTORICAL_TURNOVER_RATE = 55D;

    /** 本轮连续涨停前 200 根有效日 K 内允许出现的最高板数，超过 5 板才过滤。 */
    private static final int MAX_ALLOWED_TWO_HUNDRED_KLINE_BOARD = 5;

    /** 本轮连板前 90 个自然日内只要出现过 3 板就过滤。 */
    private static final int MAX_ALLOWED_NINETY_DAY_BOARD_EXCLUSIVE = 3;

    /** 连板候选在本轮连板前 90 个自然日内的历史最大换手率不得超过 35%。 */
    private static final double MAX_ALLOWED_RELAY_NINETY_DAY_TURNOVER_RATE = 35D;

    /** 特殊通道要求历史最大换手率严格低于 20%。 */
    private static final double SPECIAL_MAX_TURNOVER_RATE = 20D;

    /** 特殊通道要求选股涨停日收盘价严格低于 17.5 元。 */
    private static final double SPECIAL_MAX_CURRENT_PRICE = 17.5D;

    /** 特殊通道最大筹码金额，单位：万元，即 7.58 亿元。 */
    private static final double SPECIAL_MAX_CHIP_AMOUNT = 75_800D;

    private StockChipFilter() {
    }

    /**
     * 计算本轮连板前的历史筹码指标。
     *
     * <p>{@code rawWindowDailyList} 是本轮首板前一交易日之前的原始日 K；过滤新股
     * 最早 10 根数据后，再按交易日倒序保留最近 200 根作为筹码窗口。
     * {@code earliestStoredDailyList} 是数据库中该股票最早的 11 根日 K。以前 10 根日 K
     * 作为新股上市初期数据，不参与换手、成交量和历史板数统计。</p>
     *
     * <p>90 日最高板和 90 日最大换手率使用同一个自然日窗口，包含起止日期；
     * 历史最高达到 3 板由公共历史硬规则剔除，最大换手率超过 35% 仅由连板流程剔除。</p>
     */
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

    /**
     * 按固定层级执行筹码过滤，并返回命中的层级与指标明细。
     */
    static Decision evaluate(StockSelectionAssistDTO assist) {
        if (assist == null || assist.getStartMarketCap() == null || assist.getCurrentPrice() == null) {
            return Decision.rejected("筹码数据完整性", "缺少启动市值、当前价格或历史筹码指标");
        }

        Decision historicalHardLimitDecision = evaluateHistoricalHardLimits(assist);
        if (!historicalHardLimitDecision.passed()) {
            return historicalHardLimitDecision;
        }

        double startMarketCap = assist.getStartMarketCap();
        double currentPrice = assist.getCurrentPrice();
        double maxTurnoverRate = assist.getMaxTurnoverRate();

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
     * 执行普通首板、正常连板、冰点连板和弱 5 板兜底共同遵守的历史筹码硬规则。
     * 小市值首板补充分支使用独立的历史高度规则，不调用本方法。
     * 冰点 3/4 板宽松通道可以绕过普通市值换手价格阶梯，但不能绕过 55% 历史最大换手、
     * 200 根 K 线历史最高板和 90 日历史最高板限制。
     */
    static Decision evaluateHistoricalHardLimits(StockSelectionAssistDTO assist) {
        if (assist == null
                || assist.getMaxTurnoverRate() == null
                || assist.getHighestConsecutiveLimitUpDays() == null
                || assist.getPriorNinetyDayHighestConsecutiveLimitUpDays() == null) {
            return Decision.rejected("筹码数据完整性", "缺少历史最大换手率或历史最高板指标");
        }

        double maxTurnoverRate = assist.getMaxTurnoverRate();
        int twoHundredKlineHighest = assist.getHighestConsecutiveLimitUpDays();
        int ninetyDayHighest = assist.getPriorNinetyDayHighestConsecutiveLimitUpDays();
        if (maxTurnoverRate > MAX_ALLOWED_HISTORICAL_TURNOVER_RATE) {
            return Decision.rejected("历史最大换手",
                    "actual=" + maxTurnoverRate + "%, required<=55%");
        }
        if (twoHundredKlineHighest > MAX_ALLOWED_TWO_HUNDRED_KLINE_BOARD) {
            return Decision.rejected("200根K线历史最高板",
                    "actual=" + twoHundredKlineHighest + ", required<=5");
        }
        if (ninetyDayHighest >= MAX_ALLOWED_NINETY_DAY_BOARD_EXCLUSIVE) {
            return Decision.rejected("90日历史最高板",
                    "actual=" + ninetyDayHighest + ", required<3");
        }
        return Decision.passed("历史筹码硬规则",
                "maxTurnoverRate=" + maxTurnoverRate
                        + "%, twoHundredKlineHighest=" + twoHundredKlineHighest
                        + ", ninetyDayHighest=" + ninetyDayHighest);
    }

    /**
     * 执行连板候选专属的 90 日历史最大换手限制。
     *
     * <p>该规则在严格通道、冰点 3/4 板宽松通道和弱 5 板兜底分流前执行，
     * 因此任何连板后续通道都不能绕过；首板流程不调用本方法。</p>
     */
    static Decision evaluateRelayNinetyDayTurnoverLimit(StockSelectionAssistDTO assist) {
        if (assist == null || assist.getPriorNinetyDayMaxTurnoverRate() == null) {
            return Decision.rejected("筹码数据完整性", "缺少90日历史最大换手率");
        }

        double ninetyDayMaxTurnoverRate = assist.getPriorNinetyDayMaxTurnoverRate();
        if (ninetyDayMaxTurnoverRate > MAX_ALLOWED_RELAY_NINETY_DAY_TURNOVER_RATE) {
            return Decision.rejected("90日历史最大换手",
                    "actual=" + ninetyDayMaxTurnoverRate + "%, required<=35%");
        }
        return Decision.passed("90日历史最大换手",
                "actual=" + ninetyDayMaxTurnoverRate + "%, required<=35%");
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
            return (maxTurnoverRate < 43D && currentPrice < 25D) || (maxTurnoverRate < 50D && currentPrice < 20D);
        }
        if (startMarketCap <= 168_000D) {
            return maxTurnoverRate < 39D && currentPrice < 22D;
        }
        if (startMarketCap <= 187_000D) {
            return maxTurnoverRate < 35D && currentPrice < 20D;
        }
        if (startMarketCap <= 200_000D) {
            return maxTurnoverRate < 30D && currentPrice < 20D;
        }
        if (startMarketCap <= 208_000D) {
            return maxTurnoverRate < 27D && currentPrice < 18.5D;
        }
        if (startMarketCap <= 220_000D) {
            return maxTurnoverRate < 25D && currentPrice < 17D;
        }
        if (startMarketCap <= 250_000D) {
            return maxTurnoverRate < 25D && currentPrice < 16D;
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
     * @param maxTurnoverRate            最近 200 根有效日 K 的历史最大换手率，单位：%
     * @param maxVolume                  最近 200 根有效日 K 的历史最大成交量，单位：股
     * @param twoHundredKlineHighestBoard 最近 200 根有效日 K 的历史最高板
     * @param ninetyDayHighestBoard      90 个自然日历史最高板
     * @param ninetyDayMaxTurnoverRate   同一 90 个自然日窗口的历史最大换手率，单位：%
     * @param maxVolumeDayTurnoverRate   最大成交量日换手率，单位：%
     * @param maxVolumeDayTurnover       最大成交量日成交额，单位：万元
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
