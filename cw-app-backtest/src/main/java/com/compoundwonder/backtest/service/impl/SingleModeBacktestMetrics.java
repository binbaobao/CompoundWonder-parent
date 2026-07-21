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
        List<SingleModeBacktestSample> actualSamples = samples.stream()
                .filter(s -> positionType(s) == SingleModeBacktestSample.POSITION_ACTUAL).toList();
        List<SingleModeBacktestSample> virtualSamples = samples.stream()
                .filter(s -> positionType(s) == SingleModeBacktestSample.POSITION_VIRTUAL).toList();
        long bought = actualSamples.size();
        long closed = actualSamples.stream().filter(SingleModeBacktestMetrics::isClosed).count();
        long open = actualSamples.stream().filter(s -> Integer.valueOf(3).equals(s.getStatus())).count();
        long errors = samples.stream().filter(s -> Integer.valueOf(5).equals(s.getStatus())).count();
        long noBuy = samples.stream()
                .filter(s -> positionType(s) != SingleModeBacktestSample.POSITION_ACTUAL)
                .filter(s -> !Integer.valueOf(5).equals(s.getStatus())).count();
        long processed = samples.stream().filter(s -> !Integer.valueOf(1).equals(s.getStatus())).count();
        long wins = actualSamples.stream().filter(SingleModeBacktestMetrics::isClosed)
                .filter(s -> value(s.getReturnRate()).signum() > 0).count();
        List<SingleModeBacktestSample> closedSamples = actualSamples.stream()
                .filter(SingleModeBacktestMetrics::isClosed).toList();
        List<SingleModeBacktestSample> virtualClosed = virtualSamples.stream()
                .filter(SingleModeBacktestMetrics::isClosed).toList();
        long virtualWins = virtualClosed.stream()
                .filter(s -> value(s.getReturnRate()).signum() > 0).count();
        List<SingleModeBacktestSample> scenarioClosed = samples.stream()
                .filter(s -> positionType(s) != SingleModeBacktestSample.POSITION_NONE)
                .filter(SingleModeBacktestMetrics::isClosed).toList();
        long scenarioWins = scenarioClosed.stream()
                .filter(s -> value(s.getReturnRate()).signum() > 0).count();
        SingleModeBoardStat next = boardStats(samples).stream()
                .filter(stat -> stat.fromBoard() == 1).findFirst()
                .orElse(new SingleModeBoardStat(1, 0, 0, 0, 0,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return new SingleModeBacktestSummary(total, processed, bought, closed, open, noBuy,
                errors, wins, percent(bought, total), percent(wins, closed),
                averageReturn(closedSamples), averagePotential(actualSamples),
                next.touchRate(), next.sealRate(), next.breakRate(),
                virtualSamples.size(), virtualClosed.size(), virtualWins,
                percent(virtualWins, virtualClosed.size()), averageReturn(virtualClosed),
                percent(scenarioWins, scenarioClosed.size()), averageReturn(scenarioClosed),
                sealRate(actualSamples), sealRate(virtualSamples));
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

    private static BigDecimal averageReturn(List<SingleModeBacktestSample> samples) {
        List<BigDecimal> values = samples.stream()
                .map(SingleModeBacktestSample::getReturnRate)
                .filter(java.util.Objects::nonNull).toList();
        return average(values);
    }

    private static BigDecimal averagePotential(List<SingleModeBacktestSample> samples) {
        List<BigDecimal> values = samples.stream()
                .map(SingleModeBacktestSample::getPotentialMaxReturnRate)
                .filter(java.util.Objects::nonNull).toList();
        return average(values);
    }

    private static BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal sealRate(List<SingleModeBacktestSample> samples) {
        long sealed = samples.stream().filter(s -> intValue(s.getMaxSealedBoards()) >= 2).count();
        return percent(sealed, samples.size());
    }

    private static boolean isClosed(SingleModeBacktestSample sample) {
        return Integer.valueOf(4).equals(sample.getStatus());
    }

    private static int positionType(SingleModeBacktestSample sample) {
        if (sample.getPositionType() != null) return sample.getPositionType();
        return sample.getBuyDate() == null
                ? SingleModeBacktestSample.POSITION_NONE
                : SingleModeBacktestSample.POSITION_ACTUAL;
    }

    static BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static int intValue(Integer value) { return value == null ? 0 : value; }
}
