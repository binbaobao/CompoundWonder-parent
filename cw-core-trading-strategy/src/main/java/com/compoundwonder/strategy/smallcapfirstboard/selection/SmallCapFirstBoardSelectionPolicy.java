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
    /** 小市值首板要求选股日首板涨停收盘价不低于 4.5 元。 */
    public static final double MIN_FIRST_BOARD_LIMIT_PRICE_INCLUSIVE = 4.5D;
    /** 小市值首板最近 100 根 K 线历史最大换手率不得超过 60%。 */
    public static final double MAX_HISTORICAL_TURNOVER_RATE_INCLUSIVE = 60D;

    private SmallCapFirstBoardSelectionPolicy() {
    }

    /**
     * 依次执行小市值首板的市值与首板价格归属、历史换手、高度、异常 K 线和近期形态过滤。
     *
     * <p>本模式只按启动流通市值评分，不复用普通首板的换手和价格筹码阶梯。</p>
     *
     * @param candidate 已完成数据准备的小市值首板候选
     * @return 是否通过、启动市值分数以及首个拒绝层或通过层的明细
     */
    public static Decision evaluate(SmallCapFirstBoardSelectionCandidate candidate) {
        if (candidate == null || candidate.startMarketCap() == null) {
            return Decision.rejected("模式市值归属", "缺少启动流通市值");
        }
        double startMarketCap = candidate.startMarketCap();
        if (startMarketCap >= MAX_START_MARKET_CAP_EXCLUSIVE) {
            return Decision.rejected("模式市值归属", "actual=" + startMarketCap
                    + "万元, required<119999万元; 普通首板与小市值首板互不回退");
        }
        if (candidate.currentPrice() == null) {
            return Decision.rejected("首板涨停价格", "缺少选股日首板涨停收盘价");
        }
        double currentPrice = candidate.currentPrice();
        if (currentPrice < MIN_FIRST_BOARD_LIMIT_PRICE_INCLUSIVE) {
            return Decision.rejected("首板涨停价格", "actual=" + currentPrice
                    + "元, required>=4.5元");
        }
        double maxTurnoverRate = Objects.requireNonNullElse(candidate.maxTurnoverRate(), 0D);
        if (maxTurnoverRate > MAX_HISTORICAL_TURNOVER_RATE_INCLUSIVE) {
            return Decision.rejected("100根K线历史最大换手率",
                    "actual=" + maxTurnoverRate + "%, required<=60%");
        }
        int highestBoard = Objects.requireNonNullElse(
                candidate.highestConsecutiveLimitUpDays(), 0);
        if (highestBoard > 3) {
            return Decision.rejected("100根K线历史最高板",
                    "actual=" + highestBoard + ", required<3");
        }
//        int priorTwentyAbnormal = Objects.requireNonNullElse(
//                candidate.priorTwentyDayAbnormalKlineStateCount(), 0);
//        if (priorTwentyAbnormal >= 4) {
//            return Decision.rejected("前20日非正常K线次数",
//                    "actual=" + priorTwentyAbnormal + ", required<4");
//        }
//        int abnormalCount = Objects.requireNonNullElse(candidate.abnormalKlineStateCount(), 0);
//        if (abnormalCount > 25) {
//            return Decision.rejected("18个月非正常状态次数",
//                    "actual=" + abnormalCount + ", required<=25");
//        }
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

    /**
     * 小市值首板策略判定结果。
     *
     * @param passed 是否通过全部过滤
     * @param score 通过时的启动市值分数，拒绝时固定为 0
     * @param layer 通过或首个拒绝条件所属层级
     * @param detail 用于选股过滤日志的指标明细
     */
    public record Decision(boolean passed, int score, String layer, String detail) {
        private static Decision passed(int score) {
            return new Decision(true, score, "小市值首板核心规则", "score=" + score);
        }

        private static Decision rejected(String layer, String detail) {
            return new Decision(false, 0, layer, detail);
        }
    }
}
