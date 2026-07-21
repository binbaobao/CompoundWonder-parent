package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.service.model.SingleModeBoardStat;
import com.compoundwonder.trader.entity.SingleModeBacktestSample;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleModeBacktestMetricsTest {
    @Test
    void separatesTouchSealAndBreakRatesForEveryPromotion() {
        SingleModeBacktestSample tenBoards = sample(10, 10);
        SingleModeBacktestSample fiveBoardBreak = sample(5, 6);
        SingleModeBacktestSample firstBoardOnly = sample(1, 1);

        List<SingleModeBoardStat> stats = SingleModeBacktestMetrics.boardStats(
                List.of(tenBoards, fiveBoardBreak, firstBoardOnly));

        SingleModeBoardStat oneToTwo = stats.get(0);
        assertEquals(3, oneToTwo.eligibleCount());
        assertEquals(2, oneToTwo.touchCount());
        assertEquals(2, oneToTwo.sealedCount());
        assertEquals("66.666667", oneToTwo.touchRate().toPlainString());

        SingleModeBoardStat fiveToSix = stats.get(4);
        assertEquals(2, fiveToSix.eligibleCount());
        assertEquals(2, fiveToSix.touchCount());
        assertEquals(1, fiveToSix.sealedCount());
        assertEquals(1, fiveToSix.breakCount());
        assertEquals("50.000000", fiveToSix.sealRate().toPlainString());
    }

    @Test
    void separatesActualAndVirtualSellPerformance() {
        SingleModeBacktestSample actualWin = closedSample(
                SingleModeBacktestSample.POSITION_ACTUAL, "12.000000");
        SingleModeBacktestSample actualLoss = closedSample(
                SingleModeBacktestSample.POSITION_ACTUAL, "-4.000000");
        SingleModeBacktestSample virtualWin = closedSample(
                SingleModeBacktestSample.POSITION_VIRTUAL, "20.000000");

        var summary = SingleModeBacktestMetrics.summarize(
                List.of(actualWin, actualLoss, virtualWin));

        assertEquals(2, summary.boughtSamples());
        assertEquals(2, summary.closedSamples());
        assertEquals(1, summary.virtualSamples());
        assertEquals(1, summary.virtualClosedSamples());
        assertEquals("50.000000", summary.closeWinRate().toPlainString());
        assertEquals("4.000000", summary.averageReturnRate().toPlainString());
        assertEquals("100.000000", summary.virtualCloseWinRate().toPlainString());
        assertEquals("20.000000", summary.virtualAverageReturnRate().toPlainString());
    }

    @Test
    void summarizesTheFirstPromotionFromEachSamplesSelectedBoard() {
        SingleModeBacktestSample twoBoardSealedToThree = sample(3, 3);
        twoBoardSealedToThree.setSelectionBoard(2);
        SingleModeBacktestSample threeBoardBrokeAtFour = sample(3, 4);
        threeBoardBrokeAtFour.setSelectionBoard(3);
        SingleModeBacktestSample threeBoardUntouched = sample(3, 3);
        threeBoardUntouched.setSelectionBoard(3);

        var summary = SingleModeBacktestMetrics.summarize(
                List.of(twoBoardSealedToThree, threeBoardBrokeAtFour, threeBoardUntouched));

        assertEquals("66.666667", summary.nextBoardTouchRate().toPlainString());
        assertEquals("50.000000", summary.nextBoardSealRate().toPlainString());
        assertEquals("50.000000", summary.nextBoardBreakRate().toPlainString());
    }

    private SingleModeBacktestSample sample(int sealed, int touched) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setMaxSealedBoards(sealed);
        sample.setMaxTouchedBoards(touched);
        return sample;
    }

    private SingleModeBacktestSample closedSample(int positionType, String returnRate) {
        SingleModeBacktestSample sample = sample(2, 2);
        sample.setPositionType(positionType);
        sample.setStatus(4);
        sample.setBuyDate(java.time.LocalDate.of(2026, 1, 2));
        sample.setSellDate(java.time.LocalDate.of(2026, 1, 5));
        sample.setReturnRate(new BigDecimal(returnRate));
        return sample;
    }
}
