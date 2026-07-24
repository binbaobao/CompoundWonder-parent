package com.compoundwonder.common.strategy.volume;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VolumeStateClassifierTest {

    @Test
    void oneWordLimitUpAlwaysReturnsShrinkVolume() {
        assertEquals(-1, VolumeStateClassifier.classify(80D, 3, 40D));
    }

    @Test
    void exactHalfOfHistoricalMaximumStillReturnsShrinkVolume() {
        assertEquals(-1, VolumeStateClassifier.classify(30D, 1, 60D));
        assertEquals(-1, VolumeStateClassifier.classify(0D, 0, 60D));
    }

    @Test
    void aboveHalfAndBelowBurstThresholdReturnsNormalVolume() {
        assertEquals(0, VolumeStateClassifier.classify(30.01D, 1, 60D));
        assertEquals(0, VolumeStateClassifier.classify(29.99D, 2, 20D));
    }

    @Test
    void exactOneHundredFiftyPercentReturnsBurstVolume() {
        assertEquals(1, VolumeStateClassifier.classify(30D, 1, 20D));
    }

    @Test
    void thirtyFivePercentAndAboveReturnsBurstVolume() {
        assertEquals(1, VolumeStateClassifier.classify(35D, 1, 60D));
        assertEquals(1, VolumeStateClassifier.classify(75D, 1, 60D));
    }

    @Test
    void capsHistoricalMaximumAtSeventyPercent() {
        assertEquals(-1, VolumeStateClassifier.classify(34.99D, 1, 80D));
        assertEquals(1, VolumeStateClassifier.classify(35D, 1, 80D));
    }

    @ParameterizedTest(name = "turnover={0}, state={1}, max={2} => {3}")
    @CsvSource({
            // 历史最大换手较低时，150% 边界先于固定 35% 边界生效。
            "2.50,  1,   5.00, -1",
            "2.51,  1,   5.00,  0",
            "7.49,  1,   5.00,  0",
            "7.50,  1,   5.00,  1",
            "10.00, 2,  20.00, -1",
            "10.01, 2,  20.00,  0",
            "29.99, 2,  20.00,  0",
            "30.00, 2,  20.00,  1",

            // 历史最大换手较高时，固定 35% 边界先于 150% 边界生效。
            "15.00, 0,  30.00, -1",
            "15.01, 0,  30.00,  0",
            "34.99, 0,  30.00,  0",
            "35.00, 0,  30.00,  1",
            "30.00, 1,  60.00, -1",
            "30.01, 1,  60.00,  0",
            "34.99, 1,  60.00,  0",
            "35.00, 1,  60.00,  1",

            // 70% 封顶前后：69.99 仍保留极窄正常区，70 及以上没有正常区。
            "34.995, 1, 69.99, -1",
            "34.996, 1, 69.99,  0",
            "34.999, 1, 70.00, -1",
            "35.000, 1, 70.00,  1",
            "34.999, 1, 99.00, -1",
            "35.000, 1, 99.00,  1",

            // 只有正向一字涨停形态 3 无条件返回缩量。
            "0.00,   3, 20.00, -1",
            "20.00,  3, 20.00, -1",
            "80.00,  3, 20.00, -1",
            "20.00, -3, 20.00,  0"
    })
    void coversAllThresholdBands(double turnoverRate, int klineState,
                                 double historicalMaxTurnoverRate200,
                                 int expected) {
        assertEquals(expected, VolumeStateClassifier.classify(
                turnoverRate, klineState, historicalMaxTurnoverRate200));
    }

    @Test
    void onlyReturnsDeclaredStatesAcrossWideInputGrid() {
        for (double historicalMax = 0.5D; historicalMax <= 100D; historicalMax += 0.5D) {
            for (double turnover = 0D; turnover <= 100D; turnover += 0.5D) {
                int result = VolumeStateClassifier.classify(
                        turnover, 1, historicalMax);
                assertTrue(result == -1 || result == 0 || result == 1);
            }
        }
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> VolumeStateClassifier.classify(-0.01D, 1, 60D));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeStateClassifier.classify(10D, 1, 0D));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeStateClassifier.classify(Double.NaN, 1, 60D));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeStateClassifier.classify(
                        10D, 1, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeStateClassifier.classify(
                        Double.POSITIVE_INFINITY, 1, 60D));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeStateClassifier.classify(
                        10D, 3, Double.NaN));
    }
}
