package com.compoundwonder.strategy.smallcapfirstboard.selection;

import java.util.Objects;

/**
 * 小市值首板独立选股规则。
 *
 * <p>这里只判断已经由数据模块准备好的指标，不查询数据库、不创建盯盘任务。</p>
 */
public final class SmallCapFirstBoardSelectionPolicy {

    /** 小市值首板独占启动流通市值严格小于 119999 万元的候选。 */
    public static final double MAX_START_MARKET_CAP_EXCLUSIVE = 119_999D;

    private SmallCapFirstBoardSelectionPolicy() {
    }

    public static Decision evaluate(SmallCapFirstBoardSelectionCandidate candidate) {
        if (candidate == null || candidate.startMarketCap() == null) {
            return Decision.rejected("模式市值归属", "缺少启动流通市值");
        }
        double startMarketCap = candidate.startMarketCap();
        if (startMarketCap >= MAX_START_MARKET_CAP_EXCLUSIVE) {
            return Decision.rejected("模式市值归属", "actual=" + startMarketCap
                    + "万元, required<119999万元; 普通首板与小市值首板互不回退");
        }
        double maxTurnoverRate = Objects.requireNonNullElse(candidate.maxTurnoverRate(), 0D);
        if (maxTurnoverRate > 30D) {
            return Decision.rejected("200根K线历史最大换手率",
                    "actual=" + maxTurnoverRate + "%, required<=30%");
        }
        int highestBoard = Objects.requireNonNullElse(
                candidate.highestConsecutiveLimitUpDays(), 0);
        if (highestBoard >= 3) {
            return Decision.rejected("200根K线历史最高板",
                    "actual=" + highestBoard + ", required<3");
        }
        int priorTwentyAbnormal = Objects.requireNonNullElse(
                candidate.priorTwentyDayAbnormalKlineStateCount(), 0);
        if (priorTwentyAbnormal >= 4) {
            return Decision.rejected("前20日非正常K线次数",
                    "actual=" + priorTwentyAbnormal + ", required<4");
        }
        int abnormalCount = Objects.requireNonNullElse(candidate.abnormalKlineStateCount(), 0);
        if (abnormalCount > 25) {
            return Decision.rejected("18个月非正常状态次数",
                    "actual=" + abnormalCount + ", required<=25");
        }
        double amplitude = Objects.requireNonNullElse(candidate.threeDayAmplitude(), 0D);
        if (amplitude >= 20D) {
            return Decision.rejected("3日振幅", "actual=" + amplitude + ", required<20");
        }
        double tenDayChangeRate = Objects.requireNonNullElse(candidate.tenDayChangeRate(), 0D);
        if (tenDayChangeRate <= -2D || tenDayChangeRate >= 25D) {
            return Decision.rejected("10日涨跌幅", "actual=" + tenDayChangeRate
                    + ", required=(-2,25)");
        }
        return Decision.passed(scoreStartMarketCap(startMarketCap));
    }

    private static int scoreStartMarketCap(double value) {
        if (value > 200_000) return 0;
        if (value <= 81_000) return 30;
        if (value <= 95_000) return interpolate(value, 81_000, 95_000, 30, 20);
        if (value <= 150_000) return interpolate(value, 95_000, 150_000, 20, 10);
        return interpolate(value, 150_000, 200_000, 10, 0);
    }

    private static int interpolate(double value, double min, double max,
                                   int minScore, int maxScore) {
        return (int) Math.round(minScore
                + (value - min) * (maxScore - minScore) / (max - min));
    }

    public record Decision(boolean passed, int score, String layer, String detail) {
        private static Decision passed(int score) {
            return new Decision(true, score, "小市值首板核心规则", "score=" + score);
        }

        private static Decision rejected(String layer, String detail) {
            return new Decision(false, 0, layer, detail);
        }
    }
}
