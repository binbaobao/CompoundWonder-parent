package com.compoundwonder.trader.selection.smallcapfirstboard;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.entity.StockWatchingTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmallCapFirstBoardSelectionServiceTest {

    @Test
    void calculatesOwnThreeDayAmplitudeIncludingCurrentDay() {
        List<StockDailyEntity> dailyList = List.of(
                daily("2026-07-13", 9.0, 10.3, 0),
                daily("2026-07-14", 9.4, 10.4, 0),
                daily("2026-07-15", 9.2, 10.8, 0));

        assertEquals(20D,
                SmallCapFirstBoardSelectionService.calculateThreeDayAdjustedAmplitude(dailyList),
                0.000001);
    }

    @Test
    void countsItsOwnPriorTwentyDayAbnormalKlines() {
        List<StockDailyEntity> descendingDailyList = new ArrayList<>();
        for (int i = 0; i <= 21; i++) {
            int state = i == 0 || (i >= 1 && i <= 3) || i == 21 ? 1 : 0;
            descendingDailyList.add(daily(
                    LocalDate.of(2026, 7, 15).minusDays(i).toString(), 1D, 1D, state));
        }

        assertEquals(3, SmallCapFirstBoardSelectionService
                .countPriorTwentyDayAbnormalKlineState(descendingDailyList));
    }

    @Test
    void sameScorePrefersLowerPriceAndKeepsIndependentTopTwoLimit() {
        List<StockWatchingTask> tasks = new ArrayList<>(List.of(
                task("600001", 30), task("600002", 30)));

        SmallCapFirstBoardSelectionService.sortSelectionTasks(tasks,
                Map.of("600001", 20D, "600002", 10D));

        assertEquals(List.of("600002", "600001"),
                tasks.stream().map(StockWatchingTask::getStockCode).toList());
        assertEquals(2, SmallCapFirstBoardSelectionService.TASK_LIMIT);
    }

    private StockDailyEntity daily(String tradeDate, double low, double close, int state) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setAdjustLowPrice(low);
        daily.setAdjustClosePrice(close);
        daily.setKlineState(state);
        return daily;
    }

    private StockWatchingTask task(String stockCode, int score) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(stockCode);
        task.setLimitUpScore(score);
        return task;
    }
}
