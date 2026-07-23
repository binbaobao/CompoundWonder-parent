package com.compoundwonder.strategy.relay.selection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelayStrengthPolicyTest {

    @Test
    void strictRequiresCapBelowTwentyBillionTurnoverAtMostFortyAndStartPriceAboveThree() {
        assertTrue(RelaySelectionPolicy.evaluate(candidate(199_999D, 10D, 3.01D),
                RelaySelectionStrength.STRICT).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(200_000D, 10D, 3.01D),
                RelaySelectionStrength.STRICT).passed());
        assertTrue(RelaySelectionPolicy.evaluate(candidate(80_000D, 40D, 3.01D),
                RelaySelectionStrength.STRICT).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(80_000D, 40.01D, 3.01D),
                RelaySelectionStrength.STRICT).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(80_000D, 10D, 3D),
                RelaySelectionStrength.STRICT).passed());
    }

    @Test
    void normalHasNoStartPriceFloor() {
        assertTrue(RelaySelectionPolicy.evaluate(candidate(299_999D, 10D, 1D),
                RelaySelectionStrength.NORMAL).passed());
        assertTrue(RelaySelectionPolicy.evaluate(candidate(349_999D, 10D, 1D),
                RelaySelectionStrength.NORMAL).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(350_000D, 10D, 1D),
                RelaySelectionStrength.NORMAL).passed());
        assertTrue(RelaySelectionPolicy.evaluate(candidate(80_000D, 45D, 1D),
                RelaySelectionStrength.NORMAL).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(80_000D, 45.01D, 1D),
                RelaySelectionStrength.NORMAL).passed());
    }

    @Test
    void relaxedHasNoStartPriceFloor() {
        assertTrue(RelaySelectionPolicy.evaluate(candidate(499_999D, 10D, 1D),
                RelaySelectionStrength.RELAXED).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(500_000D, 10D, 1D),
                RelaySelectionStrength.RELAXED).passed());
        assertTrue(RelaySelectionPolicy.evaluate(candidate(80_000D, 50D, 1D),
                RelaySelectionStrength.RELAXED).passed());
        assertFalse(RelaySelectionPolicy.evaluate(candidate(80_000D, 50.01D, 1D),
                RelaySelectionStrength.RELAXED).passed());
    }

    @Test
    void calibrationSamplesPassTheirConfiguredStrength() {
        RelaySelectionCandidate chengFeiJiCheng = candidate(
                3, false, 22.28D, 293_752.8504D, 16.74D,
                25.5716D, 44_872_917L, 36.519765D, 38.299391D);
        RelaySelectionCandidate shouKaiGuFen = candidate(
                2, true, 3.19D, 322_414.4627D, 2.64D,
                15.0861D, 184_241_247L, 22.222102D, 16.849841D);
        RelaySelectionCandidate baoGuangGuFen = candidate(
                2, false, 15.25D, 232_198.4448D, 12.60D,
                23.2219D, 42_794_332L, 33.654764D, 26.766366D);
        RelaySelectionCandidate liXinNengYuan = candidate(
                3, false, 9.10D, 248_787.6839D, 6.84D,
                44.5608D, 162_078_588L, 42.856050D, 30.000279D);

        assertTrue(RelaySelectionPolicy.evaluate(chengFeiJiCheng,
                RelaySelectionStrength.RELAXED).passed());
        assertTrue(RelaySelectionPolicy.evaluate(shouKaiGuFen,
                RelaySelectionStrength.NORMAL).passed());
        assertTrue(RelaySelectionPolicy.evaluate(baoGuangGuFen,
                RelaySelectionStrength.NORMAL).passed());
        assertTrue(RelaySelectionPolicy.evaluate(liXinNengYuan,
                RelaySelectionStrength.RELAXED).passed());
    }

    @Test
    void twoBoardFiveDayAmplitudeStillRejectsThirtyFivePercentBoundary() {
        RelaySelectionCandidate candidate = candidate(
                2, false, 10D, 80_000D, 5D,
                10D, 1_000_000L, 35D, 25D);

        assertFalse(RelaySelectionPolicy.evaluate(candidate,
                RelaySelectionStrength.NORMAL).passed());
    }

    @Test
    void threeBoardStillRejectsTwoAcceleratedShrinkVolumeLimitUps() {
        RelaySelectionCandidate candidate = candidate(
                3, true, 10D, 80_000D, 5D,
                10D, 1_000_000L, 30D, 30D);

        assertFalse(RelaySelectionPolicy.evaluate(candidate,
                RelaySelectionStrength.RELAXED).passed());
    }

    @Test
    void threeBoardAcceleratedCandidateCanEnterBackupWhenEveryOtherRulePasses() {
        RelaySelectionCandidate candidate = candidate(
                3, true, 10D, 80_000D, 5D,
                10D, 1_000_000L, 30D, 30D);

        assertFalse(RelaySelectionPolicy.evaluate(candidate,
                RelaySelectionStrength.RELAXED).passed());
        assertTrue(RelaySelectionPolicy.evaluateThreeBoardAcceleratedBackup(
                candidate, RelaySelectionStrength.RELAXED).passed());
    }

    @Test
    void threeBoardAcceleratedBackupStillAppliesLaterRiskFilters() {
        RelaySelectionCandidate candidate = new RelaySelectionCandidate(
                3, true, "江苏", 10D, 80_000D, 5D,
                20D, 50_000D, 8D, 36, 60,
                10D, 2, 1, 35.01D,
                1_000_000L, 20D, 50_000D,
                0, 0, 30D, 30D);

        assertFalse(RelaySelectionPolicy.evaluateThreeBoardAcceleratedBackup(
                candidate, RelaySelectionStrength.RELAXED).passed());
    }

    @Test
    void twoBoardAcceleratedShrinkVolumeRequiresHistoricalTurnoverBelowTwenty() {
        RelaySelectionCandidate candidate = candidate(
                2, true, 10D, 80_000D, 5D,
                20D, 1_000_000L, 22.222102D, 16.849841D);

        assertFalse(RelaySelectionPolicy.evaluate(candidate,
                RelaySelectionStrength.NORMAL).passed());
    }

    @Test
    void calibratedChipChannelsRejectValuesOutsideFocusedBands() {
        RelaySelectionCandidate lowPriceButTurnoverBelowBand = candidate(
                3, false, 9.10D, 248_787.6839D, 6.84D,
                39.99D, 162_078_588L, 42.856050D, 30.000279D);
        RelaySelectionCandidate midCapButTurnoverBelowBand = candidate(
                3, false, 22.28D, 293_752.8504D, 16.74D,
                24.99D, 44_872_917L, 36.519765D, 38.299391D);
        RelaySelectionCandidate midCapButPriceBelowBand = candidate(
                3, false, 15.99D, 293_752.8504D, 16.74D,
                25.5716D, 44_872_917L, 36.519765D, 38.299391D);

        assertFalse(RelaySelectionPolicy.evaluate(lowPriceButTurnoverBelowBand,
                RelaySelectionStrength.RELAXED).passed());
        assertFalse(RelaySelectionPolicy.evaluate(midCapButTurnoverBelowBand,
                RelaySelectionStrength.RELAXED).passed());
        assertFalse(RelaySelectionPolicy.evaluate(midCapButPriceBelowBand,
                RelaySelectionStrength.RELAXED).passed());
    }

    private RelaySelectionCandidate candidate(double startMarketCap,
                                               double maxTurnoverRate,
                                               double startPrice) {
        return candidate(3, false, 10D, startMarketCap, startPrice,
                maxTurnoverRate, 1_000_000L, 30D, 30D);
    }

    private RelaySelectionCandidate candidate(int board,
                                               boolean acceleratedShrinkVolume,
                                               double currentPrice,
                                               double startMarketCap,
                                               double startPrice,
                                               double maxTurnoverRate,
                                               long historicalMaxVolume,
                                               double fiveDayAmplitude,
                                               double tenDayChangeRate) {
        return new RelaySelectionCandidate(
                board, acceleratedShrinkVolume, "江苏", currentPrice, startMarketCap, startPrice,
                20D, 50_000D, 8D, 36, 60,
                maxTurnoverRate, 2, 1, 30D,
                historicalMaxVolume, 20D, 50_000D,
                0, 0, fiveDayAmplitude, tenDayChangeRate);
    }
}
