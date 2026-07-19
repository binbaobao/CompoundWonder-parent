package com.compoundwonder.strategy.firstboard.selection;

import java.util.List;
import java.util.Objects;

/**
 * 普通首板独立核心选股规则。
 *
 * <p>数据模块只负责准备指标；模式归属、形态、评分和筹码阶梯全部在本类完成。</p>
 */
public final class FirstBoardSelectionPolicy {

    /** 普通首板独占启动流通市值大于等于 119999 万元的候选。 */
    public static final double MIN_START_MARKET_CAP = 119_999D;

    private FirstBoardSelectionPolicy() {
    }

    /**
     * 依次执行普通首板的模式归属、异常 K 线、近期形态、评分和筹码过滤。
     *
     * @param candidate 已完成数据准备的普通首板候选
     * @return 是否通过、最终分数以及首个拒绝层或通过层的明细
     */
    public static Decision evaluate(FirstBoardSelectionCandidate candidate) {
        if (candidate == null || candidate.startMarketCap() == null
                || candidate.currentPrice() == null) {
            return Decision.rejected("数据完整性", "缺少启动市值或当前价格");
        }
        double startMarketCap = candidate.startMarketCap();
        if (startMarketCap < MIN_START_MARKET_CAP) {
            return Decision.rejected("模式市值归属", "actual=" + startMarketCap
                    + "万元, required>=119999万元; 小市值股票不得回退普通首板");
        }
        int priorTwentyAbnormal = Objects.requireNonNullElse(
                candidate.priorTwentyDayAbnormalKlineStateCount(), 0);
        if (priorTwentyAbnormal >= 4) {
            return Decision.rejected("前20日非正常K线次数",
                    "actual=" + priorTwentyAbnormal + ", required<4");
        }
        int abnormalCount = Objects.requireNonNullElse(candidate.abnormalKlineStateCount(), 0);
        if (abnormalCount > 20) {
            return Decision.rejected("18个月非正常状态次数",
                    "actual=" + abnormalCount + ", required<=20");
        }
        double maxTurnoverRate = Objects.requireNonNullElse(candidate.maxTurnoverRate(), 0D);
        int nonStMonthCount = Objects.requireNonNullElse(candidate.nonStMonthCount(), 0);
        int listingMonthCount = Objects.requireNonNullElse(candidate.listingMonthCount(), 0);
        if (maxTurnoverRate <= 25 && nonStMonthCount < 18
                && nonStMonthCount < listingMonthCount) {
            return Decision.rejected("历史换手与非ST月份",
                    "maxTurnoverRate=" + maxTurnoverRate + ", nonStMonthCount="
                            + nonStMonthCount + ", listingMonthCount=" + listingMonthCount);
        }
        double threeDayAmplitude = Objects.requireNonNullElse(candidate.threeDayAmplitude(), 0D);
        if (threeDayAmplitude >= 20) {
            return Decision.rejected("3日振幅",
                    "actual=" + threeDayAmplitude + ", required<20");
        }
        double tenDayChangeRate = Objects.requireNonNullElse(candidate.tenDayChangeRate(), 0D);
        if (tenDayChangeRate <= -2 || tenDayChangeRate >= 25) {
            return Decision.rejected("10日涨跌幅",
                    "actual=" + tenDayChangeRate + ", required=(-2,25)");
        }
        double startPrice = Objects.requireNonNullElse(candidate.startPrice(), 0D);
        if (startPrice <= 3) {
            return Decision.rejected("启动价格", "actual=" + startPrice + ", required>3");
        }
        int score = calculateSelectionScore(candidate);
        if (score <= 30) {
            return Decision.rejected("选股评分", "actual=" + score + ", required>30");
        }
        Decision chipDecision = evaluateChip(candidate);
        return chipDecision.passed()
                ? Decision.passed(score, chipDecision.layer(), chipDecision.detail())
                : chipDecision;
    }

    private static Decision evaluateChip(FirstBoardSelectionCandidate candidate) {
        if (candidate.maxTurnoverRate() == null
                || candidate.highestConsecutiveLimitUpDays() == null
                || candidate.priorNinetyDayHighestConsecutiveLimitUpDays() == null) {
            return Decision.rejected("筹码数据完整性", "缺少历史最大换手率或历史最高板指标");
        }
        double maxTurnoverRate = candidate.maxTurnoverRate();
        int twoHundredKlineHighest = candidate.highestConsecutiveLimitUpDays();
        int ninetyDayHighest = candidate.priorNinetyDayHighestConsecutiveLimitUpDays();
        if (maxTurnoverRate > 55) {
            return Decision.rejected("筹码过滤-历史最大换手",
                    "actual=" + maxTurnoverRate + "%, required<=55%");
        }
        if (twoHundredKlineHighest > 5) {
            return Decision.rejected("筹码过滤-200根K线历史最高板",
                    "actual=" + twoHundredKlineHighest + ", required<=5");
        }
        if (ninetyDayHighest >= 3) {
            return Decision.rejected("筹码过滤-90日历史最高板",
                    "actual=" + ninetyDayHighest + ", required<3");
        }

        double startMarketCap = candidate.startMarketCap();
        double currentPrice = candidate.currentPrice();
        if (matchesMarketCapTurnoverPriceBand(startMarketCap, maxTurnoverRate, currentPrice)) {
            return Decision.passed(0, "筹码过滤-市值换手价格阶梯",
                    commonDetail(startMarketCap, maxTurnoverRate, currentPrice));
        }
        if (candidate.historicalMaxVolume() == null) {
            return Decision.rejected("筹码数据完整性", "普通阶梯未通过且缺少历史最大成交量");
        }
        double maxVolumeChipAmount = candidate.historicalMaxVolume() * currentPrice / 10_000D;
        if (maxTurnoverRate < 20 && currentPrice < 17.5 && maxVolumeChipAmount < 75_800) {
            return Decision.passed(0, "筹码过滤-低换手低筹码金额特殊通道",
                    commonDetail(startMarketCap, maxTurnoverRate, currentPrice)
                            + ", maxVolumeChipAmount=" + maxVolumeChipAmount + "万元");
        }
        return Decision.rejected("筹码过滤-市值换手价格阶梯及特殊通道",
                commonDetail(startMarketCap, maxTurnoverRate, currentPrice)
                        + ", specialMaxVolumeChipAmount=" + maxVolumeChipAmount + "万元");
    }

    private static boolean matchesMarketCapTurnoverPriceBand(
            double marketCap, double turnover, double price) {
        if (marketCap <= 93_000) return turnover < 55;
        if (marketCap <= 106_000) return turnover < 50;
        if (marketCap <= 120_000) return turnover < 46;
        if (marketCap <= 138_800) return turnover < 44;
        if (marketCap <= 151_000) return (turnover < 43 && price < 25)
                || (turnover < 50 && price < 20);
        if (marketCap <= 168_000) return turnover < 39 && price < 22;
        if (marketCap <= 187_000) return turnover < 35 && price < 20;
        if (marketCap <= 200_000) return turnover < 30 && price < 20;
        if (marketCap <= 208_000) return turnover < 27 && price < 18.5;
        if (marketCap <= 220_000) return turnover < 25 && price < 17;
        if (marketCap <= 250_000) return turnover < 25 && price < 16;
        return false;
    }

    private static int calculateSelectionScore(FirstBoardSelectionCandidate candidate) {
        int score = scoreStartMarketCap(candidate.startMarketCap())
                + scoreMaxTurnover(candidate.maxTurnoverRate())
                + scoreStartPrice(candidate.startPrice())
                + scoreProvince(candidate.province())
                + scoreCurrentTurnover(candidate.currentTurnoverRate());
        int abnormalCount = Objects.requireNonNullElse(candidate.abnormalKlineStateCount(), 0);
        int noDeductionCount = Math.max(0,
                27 - (int) Math.round(candidate.startMarketCap() / 10_000D));
        return Math.max(0, score - Math.max(0, abnormalCount - noDeductionCount));
    }

    private static int scoreStartMarketCap(Double value) {
        if (value == null || value > 200_000) return 0;
        if (value <= 81_000) return 30;
        if (value <= 95_000) return interpolate(value, 81_000, 95_000, 30, 20);
        if (value <= 150_000) return interpolate(value, 95_000, 150_000, 20, 10);
        return interpolate(value, 150_000, 200_000, 10, 0);
    }

    private static int scoreMaxTurnover(Double value) {
        if (value == null || value > 55) return 0;
        if (value <= 15) return 25;
        if (value <= 25) return interpolate(value, 15, 25, 25, 20);
        if (value <= 37.5) return interpolate(value, 25, 37.5, 20, 15);
        return interpolate(value, 37.5, 55, 15, 0);
    }

    private static int scoreStartPrice(Double value) {
        if (value == null || value < 3.3 || value > 19.5) return 0;
        if (value <= 4) return 10;
        if (value <= 10) return 15;
        if (value <= 12.5) return interpolate(value, 10, 12.5, 15, 10);
        if (value <= 15.5) return interpolate(value, 12.5, 15.5, 10, 7);
        return interpolate(value, 15.5, 19.5, 7, 0);
    }

    private static int scoreProvince(String province) {
        if (province == null || province.length() < 2) return 0;
        String prefix = province.substring(0, 2);
        if (List.of("江苏", "浙江", "广东", "上海", "深圳").contains(prefix)) return 10;
        if (List.of("山东", "湖南", "湖北", "安徽").contains(prefix)) return 7;
        if (List.of("吉林", "辽宁", "黑龙江", "四川").contains(prefix)) return 3;
        return 0;
    }

    private static int scoreCurrentTurnover(Double value) {
        if (value == null || value > 55) return 0;
        if (value <= 17) return 10;
        if (value <= 30) return 7;
        return interpolate(value, 30, 55, 7, 2);
    }

    private static int interpolate(double value, double min, double max,
                                   int minScore, int maxScore) {
        return (int) Math.round(minScore
                + (value - min) * (maxScore - minScore) / (max - min));
    }

    private static String commonDetail(double marketCap, double turnover, double price) {
        return "startMarketCap=" + marketCap + "万元, maxTurnoverRate="
                + turnover + "%, currentPrice=" + price + "元";
    }

    /**
     * 普通首板策略判定结果。
     *
     * @param passed 是否通过全部过滤
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
