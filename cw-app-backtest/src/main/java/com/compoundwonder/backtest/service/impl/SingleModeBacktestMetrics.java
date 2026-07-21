package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.service.model.SingleModeBacktestSummary;
import com.compoundwonder.backtest.service.model.SingleModeBoardStat;
import com.compoundwonder.trader.entity.SingleModeBacktestSample;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** 单模式样本的纯统计口径。所有比率统一返回百分数。 */
final class SingleModeBacktestMetrics {
    private SingleModeBacktestMetrics() { }

    static SingleModeBacktestSummary summarize(List<SingleModeBacktestSample> samples) {
        long total = samples.size();
        long bought = samples.stream().filter(s -> s.getBuyDate() != null).count();
        long closed = samples.stream().filter(s -> Integer.valueOf(4).equals(s.getStatus())).count();
        long open = samples.stream().filter(s -> Integer.valueOf(3).equals(s.getStatus())).count();
        long noBuy = samples.stream().filter(s -> Integer.valueOf(2).equals(s.getStatus())).count();
        long errors = samples.stream().filter(s -> Integer.valueOf(5).equals(s.getStatus())).count();
        long processed = closed + open + noBuy + errors;
        long wins = samples.stream().filter(s -> Integer.valueOf(4).equals(s.getStatus()))
                .filter(s -> value(s.getReturnRate()).signum() > 0).count();
        List<SingleModeBacktestSample> closedSamples = samples.stream()
                .filter(s -> Integer.valueOf(4).equals(s.getStatus())).toList();
        List<SingleModeBacktestSample> boughtSamples = samples.stream()
                .filter(s -> s.getBuyDate() != null).toList();
        SingleModeBoardStat next = boardStats(samples).stream()
                .filter(stat -> stat.fromBoard() == 1).findFirst()
                .orElse(new SingleModeBoardStat(1, 0, 0, 0, 0,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return new SingleModeBacktestSummary(total, processed, bought, closed, open, noBuy,
                errors, wins, percent(bought, total), percent(wins, closed),
                average(closedSamples, true), average(boughtSamples, false),
                next.touchRate(), next.sealRate(), next.breakRate());
    }

    static List<SingleModeBoardStat> boardStats(List<SingleModeBacktestSample> samples) {
        int maxBoard = samples.stream().map(SingleModeBacktestSample::getMaxTouchedBoards)
                .filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).max().orElse(2);
        List<SingleModeBoardStat> result = new ArrayList<>();
        for (int from = 1; from < Math.max(2, maxBoard); from++) {
            final int board = from;
            int eligible = (int) samples.stream().filter(s -> intValue(s.getMaxSealedBoards()) >= board).count();
            int touched = (int) samples.stream().filter(s -> intValue(s.getMaxTouchedBoards()) >= board + 1).count();
            int sealed = (int) samples.stream().filter(s -> intValue(s.getMaxSealedBoards()) >= board + 1).count();
            int broken = Math.max(0, touched - sealed);
            result.add(new SingleModeBoardStat(board, eligible, touched, sealed, broken,
                    percent(touched, eligible), percent(sealed, touched), percent(broken, touched)));
        }
        return List.copyOf(result);
    }

    private static BigDecimal average(List<SingleModeBacktestSample> samples, boolean actual) {
        List<BigDecimal> values = samples.stream()
                .map(s -> actual ? s.getReturnRate() : s.getPotentialMaxReturnRate())
                .filter(java.util.Objects::nonNull).toList();
        if (values.isEmpty()) return BigDecimal.ZERO;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
    }

    static BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static int intValue(Integer value) { return value == null ? 0 : value; }
}
