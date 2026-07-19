package com.compoundwonder.strategy.relay.selection;

import java.util.List;
import java.util.Objects;

/**
 * 连板接力独立核心选股规则。
 *
 * <p>严格通道与冰点 3/4 板通道共用同一入口；数据库模块只准备指标并根据本类结果落库。</p>
 */
public final class RelaySelectionPolicy {

    private RelaySelectionPolicy() {
    }

    /**
     * 执行连板接力过滤，并按市场高度选择严格通道或冰点 3/4 板通道。
     *
     * @param candidate 已完成数据准备的 2 板或 3 板候选
     * @param todayMaxLbc 选股日全市场最高连板数
     * @param allowIcePoint 是否允许在最高 3/4 板时进入宽松通道；弱 5 板兜底必须传 {@code false}
     * @return 是否通过、最终分数以及首个拒绝层或通过层的明细
     */
    public static Decision evaluate(RelaySelectionCandidate candidate,
                                    int todayMaxLbc, boolean allowIcePoint) {
        if (candidate == null) return Decision.rejected("数据完整性", "候选为空");
        int priorTwenty = Objects.requireNonNullElse(
                candidate.priorTwentyDayAbnormalKlineStateCount(), 0);
        if (priorTwenty >= 4) {
            return Decision.rejected("前20日非正常K线次数",
                    "actual=" + priorTwenty + ", required<4");
        }
        if (candidate.twoAcceleratedShrinkVolumeLimitUps()) {
            return Decision.rejected("加速缩量板",
                    "本轮至少2根加速缩量板：首板判断一字板或振幅<3%，后续板增加换手率<15%");
        }
        if (candidate.priorNinetyDayMaxTurnoverRate() == null) {
            return Decision.rejected("连板筹码过滤-数据完整性", "缺少90日历史最大换手率");
        }
        if (candidate.priorNinetyDayMaxTurnoverRate() > 35) {
            return Decision.rejected("连板筹码过滤-90日历史最大换手",
                    "actual=" + candidate.priorNinetyDayMaxTurnoverRate() + "%, required<=35%");
        }
        double maxTurnover = Objects.requireNonNullElse(candidate.maxTurnoverRate(), 0D);
        int nonStMonths = Objects.requireNonNullElse(candidate.nonStMonthCount(), 0);
        int listingMonths = Objects.requireNonNullElse(candidate.listingMonthCount(), 0);
        if (maxTurnover <= 25 && nonStMonths < 18 && nonStMonths < listingMonths) {
            return Decision.rejected("历史换手与非ST月份", "maxTurnoverRate=" + maxTurnover
                    + ", nonStMonthCount=" + nonStMonths + ", listingMonthCount=" + listingMonths);
        }
        Decision recent = evaluateRecentPattern(candidate);
        if (!recent.passed()) return recent;

        int lbc = Objects.requireNonNullElse(candidate.consecutiveLimitUpDays(), 0);
        if (allowIcePoint && (todayMaxLbc == 3 || todayMaxLbc == 4)
                && (lbc == 2 || lbc == 3)) {
            Decision ice = evaluateIcePoint(candidate);
            return ice.passed()
                    ? Decision.passed(calculateSelectionScore(candidate), "冰点3/4板", ice.detail())
                    : ice;
        }

        double startPrice = Objects.requireNonNullElse(candidate.startPrice(), 0D);
        if (startPrice <= 3 && Objects.requireNonNullElse(candidate.startMarketCap(), 0D) < 250_000) {
            return Decision.rejected("启动价格", "actual=" + startPrice + ", required>3");
        }
        int score = calculateSelectionScore(candidate);
        if (score < 15) {
            return Decision.rejected("选股评分", "actual=" + score + ", required>=15");
        }
        Decision chip = evaluateChip(candidate);
        return chip.passed() ? Decision.passed(score, chip.layer(), chip.detail()) : chip;
    }

    private static Decision evaluateRecentPattern(RelaySelectionCandidate candidate) {
        if (candidate.consecutiveLimitUpDays() == null || candidate.fiveDayAmplitude() == null
                || candidate.tenDayChangeRate() == null) {
            return Decision.rejected("近期形态-数据完整性", "缺少连板数、5日振幅或10日涨跌幅");
        }
        int lbc = candidate.consecutiveLimitUpDays();
        double amplitude = candidate.fiveDayAmplitude();
        double change = candidate.tenDayChangeRate();
        if (lbc == 3) {
            if (amplitude > 48) return Decision.rejected("3连板近期形态-5日振幅",
                    "actual=" + amplitude + "%, required<=48%");
            if (change >= 50) return Decision.rejected("3连板近期形态-10日涨跌幅",
                    "actual=" + change + "%, required<50%");
            return Decision.passed(0, "近期形态", "candidateLbc=3");
        }
        if (lbc == 2) {
            if (amplitude >= 34) return Decision.rejected("2连板近期形态-5日振幅",
                    "actual=" + amplitude + "%, required<34%");
            if (change >= 35) return Decision.rejected("2连板近期形态-10日涨跌幅",
                    "actual=" + change + "%, required<35%");
            if (change <= 11.5) return Decision.rejected("2连板近期形态-10日涨跌幅",
                    "actual=" + change + "%, required>11.5%");
            return Decision.passed(0, "近期形态", "candidateLbc=2");
        }
        return Decision.rejected("候选连板数", "actual=" + lbc + ", required=2或3");
    }

    private static Decision evaluateHistoricalHardLimits(RelaySelectionCandidate candidate) {
        if (candidate.maxTurnoverRate() == null
                || candidate.highestConsecutiveLimitUpDays() == null
                || candidate.priorNinetyDayHighestConsecutiveLimitUpDays() == null) {
            return Decision.rejected("筹码数据完整性", "缺少历史最大换手率或历史最高板指标");
        }
        if (candidate.maxTurnoverRate() > 55) {
            return Decision.rejected("历史最大换手",
                    "actual=" + candidate.maxTurnoverRate() + "%, required<=55%");
        }
        if (candidate.highestConsecutiveLimitUpDays() > 5) {
            return Decision.rejected("200根K线历史最高板",
                    "actual=" + candidate.highestConsecutiveLimitUpDays() + ", required<=5");
        }
        if (candidate.priorNinetyDayHighestConsecutiveLimitUpDays() >= 3) {
            return Decision.rejected("90日历史最高板", "actual="
                    + candidate.priorNinetyDayHighestConsecutiveLimitUpDays() + ", required<3");
        }
        return Decision.passed(0, "历史筹码硬规则", "passed");
    }

    private static Decision evaluateIcePoint(RelaySelectionCandidate candidate) {
        Decision hard = evaluateHistoricalHardLimits(candidate);
        if (!hard.passed()) return Decision.rejected("冰点3/4板-" + hard.layer(), hard.detail());
        if (candidate.startMarketCap() == null || candidate.currentPrice() == null
                || candidate.currentAmplitude() == null
                || candidate.maxVolumeDayTurnoverRate() == null) {
            return Decision.rejected("冰点3/4板数据完整性",
                    "缺少启动市值、价格、当日振幅或最大成交量日换手率");
        }
        if (candidate.startMarketCap() >= 440_000) return Decision.rejected("冰点3/4板-启动流通市值",
                "actual=" + candidate.startMarketCap() + "万元, required<440000万元");
        if (candidate.currentPrice() >= 45) return Decision.rejected("冰点3/4板-当日价格",
                "actual=" + candidate.currentPrice() + "元, required<45元");
        if (candidate.maxVolumeDayTurnoverRate() >= 50) return Decision.rejected(
                "冰点3/4板-最大成交量日换手率",
                "actual=" + candidate.maxVolumeDayTurnoverRate() + "%, required<50%");
        if (candidate.currentAmplitude() >= 15) return Decision.rejected("冰点3/4板-当日振幅",
                "actual=" + candidate.currentAmplitude() + "%, required<15%");
        if (candidate.startMarketCap() < 130_000) {
            return Decision.passed(0, "13亿以下冰点3/4板", "passed");
        }
        if (candidate.currentTurnoverRate() == null || candidate.currentTurnover() == null
                || candidate.maxVolumeDayTurnover() == null) {
            return Decision.rejected("冰点3/4板数据完整性",
                    "13至44亿档缺少当日换手率、当日成交额或最大成交量日成交额");
        }
        if (candidate.currentTurnoverRate() >= 50) return Decision.rejected("冰点3/4板-当日换手率",
                "actual=" + candidate.currentTurnoverRate() + "%, required<50%");
        if (candidate.currentTurnover() >= 250_000) return Decision.rejected("冰点3/4板-当日成交额",
                "actual=" + candidate.currentTurnover() + "万元, required<250000万元");
        if (candidate.maxVolumeDayTurnover() >= 300_000) return Decision.rejected(
                "冰点3/4板-最大成交量日成交额",
                "actual=" + candidate.maxVolumeDayTurnover() + "万元, required<300000万元");
        return Decision.passed(0, "13至44亿冰点3/4板", "passed");
    }

    private static Decision evaluateChip(RelaySelectionCandidate candidate) {
        Decision hard = evaluateHistoricalHardLimits(candidate);
        if (!hard.passed()) return Decision.rejected("筹码过滤-" + hard.layer(), hard.detail());
        if (candidate.startMarketCap() == null || candidate.currentPrice() == null) {
            return Decision.rejected("筹码数据完整性", "缺少启动市值或当前价格");
        }
        double cap = candidate.startMarketCap();
        double turnover = candidate.maxTurnoverRate();
        double price = candidate.currentPrice();
        if (matchesMarketCapTurnoverPriceBand(cap, turnover, price)) {
            return Decision.passed(0, "筹码过滤-市值换手价格阶梯", "passed");
        }
        if (candidate.historicalMaxVolume() == null) {
            return Decision.rejected("筹码数据完整性", "普通阶梯未通过且缺少历史最大成交量");
        }
        double chipAmount = candidate.historicalMaxVolume() * price / 10_000D;
        if (turnover < 20 && price < 17.5 && chipAmount < 75_800) {
            return Decision.passed(0, "筹码过滤-低换手低筹码金额特殊通道",
                    "maxVolumeChipAmount=" + chipAmount + "万元");
        }
        return Decision.rejected("筹码过滤-市值换手价格阶梯及特殊通道",
                "startMarketCap=" + cap + "万元, maxTurnoverRate=" + turnover
                        + "%, currentPrice=" + price + "元, maxVolumeChipAmount=" + chipAmount + "万元");
    }

    private static boolean matchesMarketCapTurnoverPriceBand(double cap, double turnover, double price) {
        if (cap <= 93_000) return turnover < 55;
        if (cap <= 106_000) return turnover < 50;
        if (cap <= 120_000) return turnover < 46;
        if (cap <= 138_800) return turnover < 44;
        if (cap <= 151_000) return (turnover < 43 && price < 25) || (turnover < 50 && price < 20);
        if (cap <= 168_000) return turnover < 39 && price < 22;
        if (cap <= 187_000) return turnover < 35 && price < 20;
        if (cap <= 200_000) return turnover < 30 && price < 20;
        if (cap <= 208_000) return turnover < 27 && price < 18.5;
        if (cap <= 220_000) return turnover < 25 && price < 17;
        if (cap <= 250_000) return turnover < 25 && price < 16;
        return false;
    }

    /**
     * 计算连板候选分数，并扣除超过市值豁免数量的异常 K 线次数。
     *
     * @param candidate 连板候选指标
     * @return 不小于 0 的最终分数
     */
    public static int calculateSelectionScore(RelaySelectionCandidate candidate) {
        int score = scoreStartMarketCap(candidate.startMarketCap())
                + scoreMaxTurnover(candidate.maxTurnoverRate())
                + scoreStartPrice(candidate.startPrice())
                + scoreProvince(candidate.province())
                + scoreCurrentTurnover(candidate.currentTurnoverRate())
                + scoreConsecutiveLimitUpDays(candidate.consecutiveLimitUpDays());
        int abnormal = Objects.requireNonNullElse(candidate.abnormalKlineStateCount(), 0);
        int noDeduction = candidate.startMarketCap() == null ? 0
                : Math.max(0, 27 - (int) Math.round(candidate.startMarketCap() / 10_000D));
        return Math.max(0, score - Math.max(0, abnormal - noDeduction));
    }

    private static int scoreStartMarketCap(Double v) {
        if (v == null || v > 200_000) return 0;
        if (v <= 81_000) return 30;
        if (v <= 95_000) return interpolate(v, 81_000, 95_000, 30, 20);
        if (v <= 150_000) return interpolate(v, 95_000, 150_000, 20, 10);
        return interpolate(v, 150_000, 200_000, 10, 0);
    }

    private static int scoreMaxTurnover(Double v) {
        if (v == null || v > 55) return 0;
        if (v <= 15) return 25;
        if (v <= 25) return interpolate(v, 15, 25, 25, 20);
        if (v <= 37.5) return interpolate(v, 25, 37.5, 20, 15);
        return interpolate(v, 37.5, 55, 15, 0);
    }

    private static int scoreStartPrice(Double v) {
        if (v == null || v < 3.3 || v > 19.5) return 0;
        if (v <= 4) return 10;
        if (v <= 10) return 15;
        if (v <= 12.5) return interpolate(v, 10, 12.5, 15, 10);
        if (v <= 15.5) return interpolate(v, 12.5, 15.5, 10, 7);
        return interpolate(v, 15.5, 19.5, 7, 0);
    }

    private static int scoreProvince(String province) {
        if (province == null || province.length() < 2) return 0;
        String p = province.substring(0, 2);
        if (List.of("江苏", "浙江", "广东", "上海", "深圳").contains(p)) return 10;
        if (List.of("山东", "湖南", "湖北", "安徽").contains(p)) return 7;
        if (List.of("吉林", "辽宁", "黑龙江", "四川").contains(p)) return 3;
        return 0;
    }

    private static int scoreCurrentTurnover(Double v) {
        if (v == null || v > 55) return 0;
        if (v <= 17) return 10;
        if (v <= 30) return 7;
        return interpolate(v, 30, 55, 7, 2);
    }

    private static int scoreConsecutiveLimitUpDays(Integer v) {
        if (Integer.valueOf(2).equals(v)) return 5;
        if (Integer.valueOf(3).equals(v)) return 15;
        return 0;
    }

    private static int interpolate(double value, double min, double max, int minScore, int maxScore) {
        return (int) Math.round(minScore + (value - min) * (maxScore - minScore) / (max - min));
    }

    /**
     * 连板接力策略判定结果。
     *
     * @param passed 是否通过当前通道全部过滤
     * @param score 通过时的最终选股分数，拒绝时固定为 0
     * @param layer 通过或首个拒绝条件所属层级
     * @param detail 用于选股过滤日志的指标明细
     */
    public record Decision(boolean passed, int score, String layer, String detail) {
        private static Decision passed(int score, String layer, String detail) {
            return new Decision(true, score, layer, detail);
        }

        private static Decision rejected(String layer, String detail) {
            return new Decision(false, 0, layer, detail);
        }
    }
}
