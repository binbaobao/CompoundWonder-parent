package com.compoundwonder.trader.service.impl;

import com.compoundwonder.trader.dto.StockSelectionAssistDTO;

/**
 * 连板冰点三板过滤器。
 *
 * <p>仅供“今日市场最高板为 3 板，且候选股也是 3 连板”场景调用。
 * 冰点三板绕过普通的市值、历史最大换手和价格组合阶梯，但继续遵守
 * 55% 历史最大换手、200 根 K 线历史最高板和 90 日历史最高板硬规则。</p>
 */
final class IcePointThreeBoardFilter {

    /** 冰点三板允许的启动流通市值上限，单位：万元，即 33 亿元。 */
    private static final double MAX_START_MARKET_CAP = 330_000D;

    /** 小市值和中等市值分界线，单位：万元，即 13 亿元。 */
    private static final double SMALL_MARKET_CAP_BOUNDARY = 130_000D;

    /** 冰点三板允许的选股日收盘价上限，单位：元。 */
    private static final double MAX_CURRENT_PRICE = 45D;

    /** 所有冰点三板的当日振幅上限，单位：%。 */
    private static final double MAX_CURRENT_AMPLITUDE = 15D;

    /** 所有冰点三板的 5 日复权振幅上限，单位：%。 */
    private static final double MAX_FIVE_DAY_AMPLITUDE = 45D;

    /** 最大成交量日换手率上限，单位：%。 */
    private static final double MAX_VOLUME_DAY_TURNOVER_RATE = 50D;

    /** 中等市值冰点三板的当日换手率上限，单位：%。 */
    private static final double MID_CAP_MAX_CURRENT_TURNOVER_RATE = 50D;

    /** 中等市值冰点三板的当日成交额上限，单位：万元，即 15 亿元。 */
    private static final double MID_CAP_MAX_CURRENT_TURNOVER = 150_000D;

    /** 中等市值冰点三板最大成交量日成交额上限，单位：万元，即 30 亿元。 */
    private static final double MID_CAP_MAX_VOLUME_DAY_TURNOVER = 300_000D;

    /** 13 亿元以下冰点三板的 10 日涨幅上限，单位：%。 */
    private static final double SMALL_CAP_MAX_TEN_DAY_CHANGE_RATE = 45D;

    /** 13 至 33 亿元冰点三板的 10 日涨幅上限，单位：%。 */
    private static final double MID_CAP_MAX_TEN_DAY_CHANGE_RATE = 55D;

    private IcePointThreeBoardFilter() {
    }

    /**
     * 按启动流通市值分档判断冰点三板是否可以进入候选池。
     */
    static Decision evaluate(StockSelectionAssistDTO assist) {
        if (assist == null
                || assist.getStartMarketCap() == null
                || assist.getCurrentPrice() == null
                || assist.getCurrentAmplitude() == null
                || assist.getSelectionAmplitude() == null
                || assist.getTenDayChangeRate() == null
                || assist.getMaxVolumeDayTurnoverRate() == null) {
            return Decision.rejected("冰点三板数据完整性",
                    "缺少启动市值、价格、振幅、10日涨幅或最大成交量日换手率");
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

        if (startMarketCap >= MAX_START_MARKET_CAP) {
            return Decision.rejected("启动流通市值",
                    "actual=" + startMarketCap + "万元, required<330000万元");
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
        if (fiveDayAmplitude >= MAX_FIVE_DAY_AMPLITUDE) {
            return Decision.rejected("5日振幅", "actual=" + fiveDayAmplitude + "%, required<45%");
        }

        if (startMarketCap < SMALL_MARKET_CAP_BOUNDARY) {
            if (tenDayChangeRate >= SMALL_CAP_MAX_TEN_DAY_CHANGE_RATE) {
                return Decision.rejected("10日涨幅",
                        "actual=" + tenDayChangeRate + "%, required<45%, marketCapBand=<13亿");
            }
            return Decision.passed("13亿以下冰点三板", commonDetail(assist));
        }

        if (assist.getCurrentTurnoverRate() == null
                || assist.getCurrentTurnover() == null
                || assist.getMaxVolumeDayTurnover() == null) {
            return Decision.rejected("冰点三板数据完整性",
                    "13至33亿档缺少当日换手率、当日成交额或最大成交量日成交额");
        }
        if (assist.getCurrentTurnoverRate() >= MID_CAP_MAX_CURRENT_TURNOVER_RATE) {
            return Decision.rejected("当日换手率",
                    "actual=" + assist.getCurrentTurnoverRate() + "%, required<50%");
        }
        if (assist.getCurrentTurnover() >= MID_CAP_MAX_CURRENT_TURNOVER) {
            return Decision.rejected("当日成交额",
                    "actual=" + assist.getCurrentTurnover() + "万元, required<150000万元");
        }
        if (assist.getMaxVolumeDayTurnover() >= MID_CAP_MAX_VOLUME_DAY_TURNOVER) {
            return Decision.rejected("最大成交量日成交额",
                    "actual=" + assist.getMaxVolumeDayTurnover() + "万元, required<300000万元");
        }
        if (tenDayChangeRate >= MID_CAP_MAX_TEN_DAY_CHANGE_RATE) {
            return Decision.rejected("10日涨幅",
                    "actual=" + tenDayChangeRate + "%, required<55%, marketCapBand=13至33亿");
        }
        return Decision.passed("13至33亿冰点三板", commonDetail(assist));
    }

    private static String commonDetail(StockSelectionAssistDTO assist) {
        return "startMarketCap=" + assist.getStartMarketCap() + "万元"
                + ", currentPrice=" + assist.getCurrentPrice() + "元"
                + ", currentAmplitude=" + assist.getCurrentAmplitude() + "%"
                + ", fiveDayAmplitude=" + assist.getSelectionAmplitude() + "%"
                + ", tenDayChangeRate=" + assist.getTenDayChangeRate() + "%"
                + ", maxTurnoverRate=" + assist.getMaxTurnoverRate() + "%"
                + ", maxVolumeDayTurnoverRate=" + assist.getMaxVolumeDayTurnoverRate() + "%";
    }

    /**
     * 冰点三板过滤结果。
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
