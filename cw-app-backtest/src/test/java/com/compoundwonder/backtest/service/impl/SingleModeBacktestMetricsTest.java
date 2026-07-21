package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.service.model.SingleModeBoardStat;
import com.compoundwonder.trader.entity.SingleModeBacktestSample;
import org.junit.jupiter.api.Test;

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

    private SingleModeBacktestSample sample(int sealed, int touched) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setMaxSealedBoards(sealed);
        sample.setMaxTouchedBoards(touched);
        return sample;
    }
}
