package com.compoundwonder.trader.selection.relay;


/**
 * 连板候选近期形态过滤器。
 *
 * <p>该过滤器在正常连板选股和冰点 3/4 板宽松通道分流前统一执行。
 * 所有 2、3 连板候选共用这里维护的 5 日复权振幅和 10 日涨跌幅规则，
 * 后续调整近期形态边界时只修改本类。</p>
 */
final class RelayRecentPatternFilter {

    /** 3 连板允许的 5 日复权振幅上限，当前边界值 48% 可以通过。 */
    private static final double THREE_BOARD_MAX_FIVE_DAY_AMPLITUDE = 48D;

    /** 3 连板允许的 10 日涨跌幅上限，必须严格小于 50%。 */
    private static final double THREE_BOARD_MAX_TEN_DAY_CHANGE_RATE = 50D;

    /** 2 连板允许的 5 日复权振幅上限，必须严格小于 34%。 */
    private static final double TWO_BOARD_MAX_FIVE_DAY_AMPLITUDE = 34D;

    /** 2 连板允许的 10 日涨跌幅上限，必须严格小于 35%。 */
    private static final double TWO_BOARD_MAX_TEN_DAY_CHANGE_RATE = 35D;

    /** 2 连板允许的 10 日涨跌幅下限，必须严格大于 11.5%。 */
    private static final double TWO_BOARD_MIN_TEN_DAY_CHANGE_RATE = 11.5D;

    private RelayRecentPatternFilter() {
    }

    /**
     * 判断 2、3 连板候选的近期形态是否符合要求。
     */
    static Decision evaluate(RelaySelectionAssist assist) {
        if (assist == null
                || assist.getConsecutiveLimitUpDays() == null
                || assist.getFiveDayAmplitude() == null
                || assist.getTenDayChangeRate() == null) {
            return Decision.rejected("数据完整性", "缺少连板数、5日振幅或10日涨跌幅");
        }

        int consecutiveLimitUpDays = assist.getConsecutiveLimitUpDays();
        double fiveDayAmplitude = assist.getFiveDayAmplitude();
        double tenDayChangeRate = assist.getTenDayChangeRate();

        if (consecutiveLimitUpDays == 3) {
            if (fiveDayAmplitude > THREE_BOARD_MAX_FIVE_DAY_AMPLITUDE) {
                return Decision.rejected("5日振幅",
                        "actual=" + fiveDayAmplitude + "%, required<=48%, candidateLbc=3");
            }
            if (tenDayChangeRate >= THREE_BOARD_MAX_TEN_DAY_CHANGE_RATE) {
                return Decision.rejected("10日涨跌幅",
                        "actual=" + tenDayChangeRate + "%, required<50%, candidateLbc=3");
            }
            return Decision.passed(commonDetail(consecutiveLimitUpDays, fiveDayAmplitude, tenDayChangeRate));
        }

        if (consecutiveLimitUpDays == 2) {
            if (fiveDayAmplitude >= TWO_BOARD_MAX_FIVE_DAY_AMPLITUDE) {
                return Decision.rejected("5日振幅",
                        "actual=" + fiveDayAmplitude + "%, required<34%, candidateLbc=2");
            }
            if (tenDayChangeRate >= TWO_BOARD_MAX_TEN_DAY_CHANGE_RATE) {
                return Decision.rejected("10日涨跌幅",
                        "actual=" + tenDayChangeRate + "%, required<35%, candidateLbc=2");
            }
            if (tenDayChangeRate <= TWO_BOARD_MIN_TEN_DAY_CHANGE_RATE) {
                return Decision.rejected("10日涨跌幅",
                        "actual=" + tenDayChangeRate + "%, required>11.5%, candidateLbc=2");
            }
            return Decision.passed(commonDetail(consecutiveLimitUpDays, fiveDayAmplitude, tenDayChangeRate));
        }

        return Decision.rejected("候选连板数",
                "actual=" + consecutiveLimitUpDays + ", required=2或3");
    }

    private static String commonDetail(int consecutiveLimitUpDays,
                                       double fiveDayAmplitude,
                                       double tenDayChangeRate) {
        return "candidateLbc=" + consecutiveLimitUpDays
                + ", fiveDayAmplitude=" + fiveDayAmplitude + "%"
                + ", tenDayChangeRate=" + tenDayChangeRate + "%";
    }

    /**
     * 近期形态过滤结果。
     *
     * @param passed 是否通过
     * @param layer  通过或被过滤的规则层级
     * @param detail 参与判断的指标明细
     */
    record Decision(boolean passed, String layer, String detail) {

        private static Decision passed(String detail) {
            return new Decision(true, "近期形态", detail);
        }

        private static Decision rejected(String layer, String detail) {
            return new Decision(false, layer, detail);
        }
    }
}

