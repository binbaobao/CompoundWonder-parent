package com.compoundwonder.backtest.service.model;

import java.math.BigDecimal;

/** 单模式全样本回测汇总。 */
public record SingleModeBacktestSummary(
        long totalSamples, long processedSamples, long boughtSamples, long closedSamples,
        long openSamples, long noBuySamples, long errorSamples, long winSamples,
        BigDecimal buyRate, BigDecimal closeWinRate, BigDecimal averageReturnRate,
        BigDecimal averagePotentialMaxReturnRate, BigDecimal nextBoardTouchRate,
        BigDecimal nextBoardSealRate, BigDecimal nextBoardBreakRate,
        long virtualSamples, long virtualClosedSamples, long virtualWinSamples,
        BigDecimal virtualCloseWinRate, BigDecimal virtualAverageReturnRate,
        BigDecimal scenarioCloseWinRate, BigDecimal scenarioAverageReturnRate,
        BigDecimal actualEntrySealRate, BigDecimal virtualEntrySealRate) {
}
