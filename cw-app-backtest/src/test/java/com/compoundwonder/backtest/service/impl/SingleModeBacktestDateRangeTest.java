package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleModeBacktestDateRangeTest {

    @Test
    void prependsPreviousTradeDateSoFirstExecutionDayKeepsCrossYearRecommendation() {
        List<LocalDate> executionDays = new ArrayList<>(List.of(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 6)));

        List<LocalDate> recommendationDays = SingleModeBacktestServiceImpl.recommendationDays(
                executionDays, LocalDate.of(2025, 12, 31));

        assertEquals(List.of(
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 6)), recommendationDays);
        assertEquals(2, executionDays.size());
    }

    @Test
    void keepsTheSelectedBoardAsTheIndependentSampleStartingHeight() {
        SelectionTaskData relay = new SelectionTaskData();
        relay.setConsecutiveLimitUpDays(3);
        SelectionTaskData firstBoard = new SelectionTaskData();
        firstBoard.setConsecutiveLimitUpDays(1);

        assertEquals(3, SingleModeBacktestServiceImpl.selectionBoard(relay));
        assertEquals(1, SingleModeBacktestServiceImpl.selectionBoard(firstBoard));
    }

    @Test
    void keepsOnlyTheFirstSelectionInEachConsecutiveRelayChain() {
        SingleModeBacktestServiceImpl.ConsecutiveRelaySelectionDeduplicator deduplicator =
                new SingleModeBacktestServiceImpl.ConsecutiveRelaySelectionDeduplicator();

        assertEquals(List.of("600001", "600002"), symbols(deduplicator.keepFirst(List.of(
                task("600001"), task("600002")))));
        assertEquals(List.of("600003"), symbols(deduplicator.keepFirst(List.of(
                task("600001"), task("600003")))));
        assertEquals(List.of(), symbols(deduplicator.keepFirst(List.of(task("600001")))));

        assertEquals(List.of(), symbols(deduplicator.keepFirst(List.of())));
        assertEquals(List.of("600001"), symbols(deduplicator.keepFirst(List.of(task("600001")))));
    }

    @Test
    void usesAnIndependentVersionForRelayBacktestIterations() {
        assertEquals("relay-model-001", SingleModeBacktestServiceImpl.strategyVersion(1));
        assertEquals("multi-model-009", SingleModeBacktestServiceImpl.strategyVersion(2));
        assertEquals("multi-model-009", SingleModeBacktestServiceImpl.strategyVersion(3));
    }

    private SelectionTaskData task(String symbol) {
        SelectionTaskData task = new SelectionTaskData();
        task.setStockCode(symbol);
        return task;
    }

    private List<String> symbols(List<SelectionTaskData> tasks) {
        return tasks.stream().map(SelectionTaskData::getStockCode).toList();
    }
}
