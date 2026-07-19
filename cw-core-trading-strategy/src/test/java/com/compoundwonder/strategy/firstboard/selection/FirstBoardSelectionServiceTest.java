package com.compoundwonder.strategy.firstboard.selection;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirstBoardSelectionServiceTest {

    @Test
    void calculatesThreeDayAmplitudeIncludingCurrentDay() {
        List<StockDailyData> dailyList = List.of(
                daily("2026-07-09", 7.0, 10.1, 0),
                daily("2026-07-10", 8.0, 10.2, 0),
                daily("2026-07-13", 9.0, 10.3, 0),
                daily("2026-07-14", 9.4, 10.4, 0),
                daily("2026-07-15", 9.2, 10.8, 0));

        assertEquals(20D,
                FirstBoardSelectionService.calculateThreeDayAdjustedAmplitude(dailyList),
                0.000001);
    }

    @Test
    void skipsCurrentFirstBoardBeforeCountingPriorTwentyDays() {
        List<StockDailyData> descendingDailyList = new ArrayList<>();
        for (int i = 0; i <= 21; i++) {
            int state = i == 0 || (i >= 1 && i <= 3) || i == 21 ? 1 : 0;
            descendingDailyList.add(daily(
                    LocalDate.of(2026, 7, 15).minusDays(i).toString(), 1D, 1D, state));
        }

        assertEquals(3, FirstBoardSelectionService
                .countPriorTwentyDayAbnormalKlineState(descendingDailyList));
    }

    @Test
    void sameScorePrefersLowerPriceAndKeepsTopThreeLimit() {
        List<SelectionTaskData> tasks = new ArrayList<>(List.of(
                task("600001", 50), task("600002", 50), task("600003", 60)));

        FirstBoardSelectionService.sortSelectionTasks(tasks, Map.of(
                "600001", 20D, "600002", 10D, "600003", 30D));

        assertEquals(List.of("600003", "600002", "600001"),
                tasks.stream().map(SelectionTaskData::getStockCode).toList());
        assertEquals(3, FirstBoardSelectionService.TASK_LIMIT);
    }

    private StockDailyData daily(String tradeDate, double low, double close, int state) {
        StockDailyData daily = new StockDailyData();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setAdjustLowPrice(low);
        daily.setAdjustClosePrice(close);
        daily.setKlineState(state);
        return daily;
    }

    private SelectionTaskData task(String stockCode, int score) {
        SelectionTaskData task = new SelectionTaskData();
        task.setStockCode(stockCode);
        task.setLimitUpScore(score);
        return task;
    }
}
