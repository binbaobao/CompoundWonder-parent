package com.compoundwonder.trader.service.impl;

import com.compoundwonder.trader.dto.StockSelectionAssistDTO;

/**
 * 连板冰点 3/4 板宽松通道过滤器。
 *
 * <p>当日市场最高板为 3 板或 4 板时，由选股服务将所有 2、3 连板候选交给本过滤器。
 * 本通道绕过普通的市值、历史最大换手和价格组合阶梯，但继续遵守
 * 55% 历史最大换手、200 根 K 线历史最高板和 90 日历史最高板硬规则。</p>
 */
final class IcePointThreeFourBoardFilter {

    /** 宽松通道允许的启动流通市值上限，单位：万元，即 44 亿元。 */
    private static final double MAX_START_MARKET_CAP = 440_000D;

    /** 小市值和中等市值分界线，单位：万元，即 13 亿元。 */
    private static final double SMALL_MARKET_CAP_BOUNDARY = 130_000D;

    /** 宽松通道允许的选股日收盘价上限，单位：元。 */
    private static final double MAX_CURRENT_PRICE = 45D;

    /** 宽松通道的当日振幅上限，单位：%。 */
    private static final double MAX_CURRENT_AMPLITUDE = 15D;

    /** 最大成交量日换手率上限，单位：%。 */
    private static final double MAX_VOLUME_DAY_TURNOVER_RATE = 50D;

    /** 13 至 44 亿元档的当日换手率上限，单位：%。 */
    private static final double MID_CAP_MAX_CURRENT_TURNOVER_RATE = 50D;

    /** 13 至 44 亿元档的当日成交额上限，单位：万元，即 25 亿元。 */
    private static final double MID_CAP_MAX_CURRENT_TURNOVER = 250_000D;

    /** 13 至 44 亿元档最大成交量日成交额上限，单位：万元，即 30 亿元。 */
    private static final double MID_CAP_MAX_VOLUME_DAY_TURNOVER = 300_000D;

    private IcePointThreeFourBoardFilter() {
    }

    /**
     * 按候选连板数和启动流通市值分档判断能否进入冰点 3/4 板宽松候选池。
     *
     * <p>2 连板以 35% 为涨幅基准，3 连板以 45% 为涨幅基准：
     * 5 日复权振幅和 13 亿以下的 10 日涨幅不得达到基准值；
     * 13 至 44 亿档的 10 日涨幅上限在基准值上增加 10 个百分点。</p>
     */
    static Decision evaluate(StockSelectionAssistDTO assist) {
        if (assist == null
                || assist.getConsecutiveLimitUpDays() == null
                || assist.getStartMarketCap() == null
                || assist.getCurrentPrice() == null
                || assist.getCurrentAmplitude() == null
                || assist.getSelectionAmplitude() == null
                || assist.getTenDayChangeRate() == null
                || assist.getMaxVolumeDayTurnoverRate() == null) {
            return Decision.rejected("冰点3/4板数据完整性",
                    "缺少连板数、启动市值、价格、振幅、10日涨幅或最大成交量日换手率");
        }

        int consecutiveLimitUpDays = assist.getConsecutiveLimitUpDays();
        if (consecutiveLimitUpDays != 2 && consecutiveLimitUpDays != 3) {
            return Decision.rejected("候选连板数",
                    "actual=" + consecutiveLimitUpDays + ", required=2或3");
        }

        StockChipFilter.Decision hardLimitDecision = StockChipFilter.evaluateHistoricalHardLimits(assist);
        if (!hardLimitDecision.passed()) {
            return Decision.rejected(hardLimitDecision.layer(), hardLimitDecision.detail());
        }

        double startMarketCap = assist.getStartMarketCap();
        double currentPrice = assist.getCurrentPrice();
        double currentAmplitude = assist.getCurrentAmplitude();
        double fiveDayAmplitude = assist.getSelectionAmplitude();
        double tenDayChangeRate = assist.getTenDayChangeRate();
        double maxVolumeDayTurnoverRate = assist.getMaxVolumeDayTurnoverRate();
        double allowedPriceIncrease = consecutiveLimitUpDays == 3 ? 45D : 35D;

        if (startMarketCap >= MAX_START_MARKET_CAP) {
            return Decision.rejected("启动流通市值",
                    "actual=" + startMarketCap + "万元, required<440000万元");
        }
        if (currentPrice >= MAX_CURRENT_PRICE) {
            return Decision.rejected("当日价格", "actual=" + currentPrice + "元, required<45元");
        }
        if (maxVolumeDayTurnoverRate >= MAX_VOLUME_DAY_TURNOVER_RATE) {
            return Decision.rejected("最大成交量日换手率",
                    "actual=" + maxVolumeDayTurnoverRate + "%, required<50%");
        }
        if (currentAmplitude >= MAX_CURRENT_AMPLITUDE) {
            return Decision.rejected("当日振幅", "actual=" + currentAmplitude + "%, required<15%");
        }
        if (fiveDayAmplitude >= allowedPriceIncrease) {
            return Decision.rejected("5日振幅",
                    "actual=" + fiveDayAmplitude + "%, required<" + allowedPriceIncrease
                            + "%, candidateLbc=" + consecutiveLimitUpDays);
        }

        if (startMarketCap < SMALL_MARKET_CAP_BOUNDARY) {
            if (tenDayChangeRate >= allowedPriceIncrease) {
                return Decision.rejected("10日涨幅",
                        "actual=" + tenDayChangeRate + "%, required<" + allowedPriceIncrease
                                + "%, marketCapBand=<13亿, candidateLbc=" + consecutiveLimitUpDays);
            }
            return Decision.passed("13亿以下冰点3/4板", commonDetail(assist));
        }

        if (assist.getCurrentTurnoverRate() == null
                || assist.getCurrentTurnover() == null
                || assist.getMaxVolumeDayTurnover() == null) {
            return Decision.rejected("冰点3/4板数据完整性",
                    "13至44亿档缺少当日换手率、当日成交额或最大成交量日成交额");
        }
        if (assist.getCurrentTurnoverRate() >= MID_CAP_MAX_CURRENT_TURNOVER_RATE) {
            return Decision.rejected("当日换手率",
                    "actual=" + assist.getCurrentTurnoverRate() + "%, required<50%");
        }
        if (assist.getCurrentTurnover() >= MID_CAP_MAX_CURRENT_TURNOVER) {
            return Decision.rejected("当日成交额",
                    "actual=" + assist.getCurrentTurnover() + "万元, required<250000万元");
        }
        if (assist.getMaxVolumeDayTurnover() >= MID_CAP_MAX_VOLUME_DAY_TURNOVER) {
            return Decision.rejected("最大成交量日成交额",
                    "actual=" + assist.getMaxVolumeDayTurnover() + "万元, required<300000万元");
        }

        double midCapMaxTenDayChangeRate = allowedPriceIncrease + 10D;
        if (tenDayChangeRate >= midCapMaxTenDayChangeRate) {
            return Decision.rejected("10日涨幅",
                    "actual=" + tenDayChangeRate + "%, required<" + midCapMaxTenDayChangeRate
                            + "%, marketCapBand=13至44亿, candidateLbc=" + consecutiveLimitUpDays);
        }
        return Decision.passed("13至44亿冰点3/4板", commonDetail(assist));
    }

    private static String commonDetail(StockSelectionAssistDTO assist) {
        return "candidateLbc=" + assist.getConsecutiveLimitUpDays()
                + ", startMarketCap=" + assist.getStartMarketCap() + "万元"
                + ", currentPrice=" + assist.getCurrentPrice() + "元"
                + ", currentAmplitude=" + assist.getCurrentAmplitude() + "%"
                + ", fiveDayAmplitude=" + assist.getSelectionAmplitude() + "%"
                + ", tenDayChangeRate=" + assist.getTenDayChangeRate() + "%"
                + ", maxTurnoverRate=" + assist.getMaxTurnoverRate() + "%"
                + ", maxVolumeDayTurnoverRate=" + assist.getMaxVolumeDayTurnoverRate() + "%";
    }

    /**
     * 冰点 3/4 板宽松通道过滤结果。
     *
     * @param passed 是否通过
     * @param layer  通过或被过滤的规则层级
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
}
