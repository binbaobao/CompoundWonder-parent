package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.strategy.relay.selection.WeakFiveBoardFallbackPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeakFiveBoardFallbackPolicyTest {

    @Test
    void onlyEvaluatesUniqueFiveBoardWhenNormalRelaySelectionIsEmpty() {
        WeakFiveBoardFallbackPolicy.FiveBoardQuality fiveBoard = strongFiveBoard();

        assertFalse(WeakFiveBoardFallbackPolicy.evaluate(4, false, List.of(fiveBoard)).triggered());
        assertFalse(WeakFiveBoardFallbackPolicy.evaluate(5, true, List.of(fiveBoard)).triggered());
        assertFalse(WeakFiveBoardFallbackPolicy.evaluate(5, false, List.of()).triggered());
        assertFalse(WeakFiveBoardFallbackPolicy.evaluate(
                5, false, List.of(fiveBoard, strongFiveBoard())).triggered());
    }

    @Test
    void strongUniqueFiveBoardDoesNotTriggerFallbackAtInclusiveBoundaries() {
        WeakFiveBoardFallbackPolicy.FiveBoardQuality lowerBoundaryQuality =
                quality(450_000D, 45D, 2.5D, 2.5D);

        WeakFiveBoardFallbackPolicy.Decision lowerBoundary =
                WeakFiveBoardFallbackPolicy.evaluate(5, false, List.of(lowerBoundaryQuality));
        assertFalse(lowerBoundary.triggered());

        WeakFiveBoardFallbackPolicy.FiveBoardQuality upperBoundaryQuality =
                quality(450_000D, 45D, 13D, 30D);
        WeakFiveBoardFallbackPolicy.Decision upperBoundary =
                WeakFiveBoardFallbackPolicy.evaluate(5, false, List.of(upperBoundaryQuality));
        assertFalse(upperBoundary.triggered());
    }

    @Test
    void anyWeakQualitySignalTriggersStrictTwoBoardFallback() {
        assertTriggered(450_000.01D, 30D, 8D, 10D, "当日流通市值");
        assertTriggered(300_000D, 45.01D, 8D, 10D, "当日换手率");
        assertTriggered(300_000D, 30D, 13.01D, 10D, "当日振幅过大");
    }

    @Test
    void oneWordOrLowAmplitudeFiveBoardIsNotWeakByItself() {
        WeakFiveBoardFallbackPolicy.Decision decision =
                WeakFiveBoardFallbackPolicy.evaluate(
                        5, false, List.of(quality(300_000D, 30D, 0D, 10D)));

        assertFalse(decision.triggered());
        assertEquals("唯一5板质量合格", decision.layer());
    }

    @Test
    void startPriceDoesNotMakeTheUniqueFiveBoardWeak() {
        WeakFiveBoardFallbackPolicy.Decision lowPrice =
                WeakFiveBoardFallbackPolicy.evaluate(
                        5, false, List.of(quality(300_000D, 30D, 8D, 2D)));
        WeakFiveBoardFallbackPolicy.Decision highPrice =
                WeakFiveBoardFallbackPolicy.evaluate(
                        5, false, List.of(quality(300_000D, 30D, 8D, 35D)));

        assertFalse(lowPrice.triggered());
        assertFalse(highPrice.triggered());
        assertEquals("唯一5板质量合格", lowPrice.layer());
        assertEquals("唯一5板质量合格", highPrice.layer());
    }

    @Test
    void missingFiveBoardQualityDataDoesNotStartSubjectiveFallback() {
        WeakFiveBoardFallbackPolicy.FiveBoardQuality fiveBoard =
                new WeakFiveBoardFallbackPolicy.FiveBoardQuality("600001", 300_000D, 30D, null, 10D);

        WeakFiveBoardFallbackPolicy.Decision decision =
                WeakFiveBoardFallbackPolicy.evaluate(5, false, List.of(fiveBoard));

        assertFalse(decision.triggered());
        assertEquals("数据完整性", decision.layer());
    }

    @Test
    void missingStartPriceDoesNotBlockOtherWeakSignals() {
        WeakFiveBoardFallbackPolicy.FiveBoardQuality fiveBoard =
                new WeakFiveBoardFallbackPolicy.FiveBoardQuality("600001", 460_000D, 30D, 8D, null);

        WeakFiveBoardFallbackPolicy.Decision decision =
                WeakFiveBoardFallbackPolicy.evaluate(5, false, List.of(fiveBoard));

        assertTrue(decision.triggered());
        assertEquals("当日流通市值", decision.layer());
    }

    private void assertTriggered(double currentMarketCap,
                                 double currentTurnoverRate,
                                 double currentAmplitude,
                                 double startPrice,
                                 String expectedLayer) {
        WeakFiveBoardFallbackPolicy.FiveBoardQuality fiveBoard =
                quality(currentMarketCap, currentTurnoverRate, currentAmplitude, startPrice);

        WeakFiveBoardFallbackPolicy.Decision decision =
                WeakFiveBoardFallbackPolicy.evaluate(5, false, List.of(fiveBoard));

        assertTrue(decision.triggered());
        assertEquals(expectedLayer, decision.layer());
    }

    private WeakFiveBoardFallbackPolicy.FiveBoardQuality strongFiveBoard() {
        return quality(300_000D, 30D, 8D, 10D);
    }

    private WeakFiveBoardFallbackPolicy.FiveBoardQuality quality(double currentMarketCap,
                                                                 double currentTurnoverRate,
                                                                 double currentAmplitude,
                                                                 double startPrice) {
        return new WeakFiveBoardFallbackPolicy.FiveBoardQuality(
                "600001", currentMarketCap, currentTurnoverRate, currentAmplitude, startPrice);
    }
}
