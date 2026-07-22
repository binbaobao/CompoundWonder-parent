package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelayTheoreticalOutcomeCalculatorTest {

    @Test
    void noTouchOnDPlusOneCreatesNoTheoreticalTrade() {
        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5),
                List.of(daily("2026-01-05", 0, 10, 10.5, 10.2)));

        assertEquals(1, outcome.status());
        assertFalse(outcome.touchedLimitUp());
        assertNull(outcome.breakDate());
        assertNull(outcome.theoreticalMaxReturnRate());
    }

    @Test
    void dPlusOneTouchedThenBrokeUsesZeroGrossMaximumReturn() {
        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5),
                List.of(daily("2026-01-05", 11, 10, 11, 10.5)));

        assertEquals(3, outcome.status());
        assertTrue(outcome.touchedLimitUp());
        assertFalse(outcome.sealedLimitUp());
        assertEquals(LocalDate.of(2026, 1, 5), outcome.breakDate());
        assertEquals(0, outcome.theoreticalMaxReturnRate().signum());
    }

    @Test
    void observesSealedBoardsUntilFirstNonLimitCloseAndSellsAtThatDaysHigh() {
        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5), List.of(
                        daily("2026-01-05", 1, 10, 11, 11),
                        daily("2026-01-06", 2, 11, 12.1, 12.1),
                        daily("2026-01-07", 11, 12.1, 13.0, 12.4)));

        assertEquals(3, outcome.status());
        assertEquals(2, outcome.postSelectionSealedDays());
        assertEquals(LocalDate.of(2026, 1, 7), outcome.breakDate());
        assertEquals("0.18181818", outcome.theoreticalMaxReturnRate().toPlainString());
        assertTrue(outcome.theoreticalWin());
    }

    @Test
    void remainsPendingWhenLastStoredDayStillSealsLimitUp() {
        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5), List.of(
                        daily("2026-01-05", 1, 10, 11, 11),
                        daily("2026-01-06", 1, 11, 12.1, 12.1)));

        assertEquals(2, outcome.status());
        assertNull(outcome.breakDate());
    }

    @Test
    void missingDPlusOneDailyDoesNotTreatLaterDailyAsBuyDay() {
        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5),
                List.of(daily("2026-01-06", 1, 10, 11, 11)));

        assertEquals(4, outcome.status());
        assertFalse(outcome.touchedLimitUp());
        assertNull(outcome.buyDaily());
    }

    @Test
    void buyLimitUsesDPlusOneStStatusInsteadOfSelectionDayStatus() {
        StockDailyData buyDaily = daily("2026-01-05", 11, 10, 10.5, 10.2);
        buyDaily.setIsSt(true);

        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5), List.of(buyDaily));

        assertEquals("10.5", outcome.buyLimitPrice().toPlainString());
        assertEquals(0, outcome.theoreticalMaxReturnRate().signum());
    }

    @Test
    void touchedBuyCostUsesActualLimitHighAcrossCorporateActions() {
        StockDailyData buyDaily = daily("2026-01-05", 11, 10, 10.5, 10.2);
        buyDaily.setIsSt(false);

        RelayTheoreticalOutcome outcome = RelayTheoreticalOutcomeCalculator.evaluate(
                "600001", false, LocalDate.of(2026, 1, 5), List.of(buyDaily));

        assertEquals("10.5", outcome.buyLimitPrice().toPlainString());
        assertEquals(0, outcome.theoreticalMaxReturnRate().signum());
    }

    private StockDailyData daily(String date, int state, double previousClose,
                                 double high, double close) {
        StockDailyData daily = new StockDailyData();
        daily.setTradeDate(LocalDate.parse(date));
        daily.setKlineState(state);
        daily.setPrevClose(previousClose);
        daily.setOpenPrice(previousClose);
        daily.setHighPrice(high);
        daily.setLowPrice(previousClose);
        daily.setClosePrice(close);
        return daily;
    }
}
