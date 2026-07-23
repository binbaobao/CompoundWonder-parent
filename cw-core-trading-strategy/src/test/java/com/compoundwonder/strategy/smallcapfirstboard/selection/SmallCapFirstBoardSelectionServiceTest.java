package com.compoundwonder.strategy.smallcapfirstboard.selection;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmallCapFirstBoardSelectionServiceTest {

    @Test
    void calculatesOwnThreeDayAmplitudeIncludingCurrentDay() {
        List<StockDailyData> dailyList = List.of(
                daily("2026-07-13", 9.0, 10.3, 0),
                daily("2026-07-14", 9.4, 10.4, 0),
                daily("2026-07-15", 9.2, 10.8, 0));

        assertEquals(20D,
                SmallCapFirstBoardSelectionService.calculateThreeDayAdjustedAmplitude(dailyList),
                0.000001);
    }

    @Test
    void countsItsOwnPriorTwentyDayAbnormalKlines() {
        List<StockDailyData> descendingDailyList = new ArrayList<>();
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
        List<SelectionTaskData> tasks = new ArrayList<>(List.of(
                task("600001", 30), task("600002", 30)));

        SmallCapFirstBoardSelectionService.sortSelectionTasks(tasks,
                Map.of("600001", 20D, "600002", 10D));

        assertEquals(List.of("600002", "600001"),
                tasks.stream().map(SelectionTaskData::getStockCode).toList());
        assertEquals(2, SmallCapFirstBoardSelectionService.TASK_LIMIT);
    }

    @Test
    void queriesExactlyOneHundredHistoryKlines() {
        LocalDate tradeDate = LocalDate.of(2026, 3, 17);
        StockDailyData current = qualifiedDaily("000020", tradeDate, 10D, 110_000D);
        StockDailyData previous = qualifiedDaily(
                "000020", tradeDate.minusDays(1), 9D, 100_000D);
        Set<Integer> requestedLimits = new HashSet<>();
        StockSelectionDataService dataService = (StockSelectionDataService) Proxy.newProxyInstance(
                StockSelectionDataService.class.getClassLoader(),
                new Class<?>[]{StockSelectionDataService.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "listDailyByTradeDate" -> List.of(current);
                    case "listConvertibleBondStockCodes" -> Set.of();
                    case "listLatestDaily" -> {
                        int limit = (Integer) args[2];
                        requestedLimits.add(limit);
                        yield limit == 23 ? List.of(current, previous) : List.of();
                    }
                    case "listEarliestDaily", "listDailyBetween" -> List.of();
                    case "findNextTradeDate" -> tradeDate.plusDays(1);
                    default -> null;
                });

        new SmallCapFirstBoardSelectionService(dataService).select(tradeDate);

        assertEquals(Set.of(23, 100), requestedLimits);
    }

    private StockDailyData daily(String tradeDate, double low, double close, int state) {
        StockDailyData daily = new StockDailyData();
        daily.setTradeDate(LocalDate.parse(tradeDate));
        daily.setAdjustLowPrice(low);
        daily.setAdjustClosePrice(close);
        daily.setKlineState(state);
        return daily;
    }

    private StockDailyData qualifiedDaily(String stockCode, LocalDate tradeDate,
                                          double close, double floatMarketCap) {
        StockDailyData daily = daily(tradeDate.toString(), close * 0.9, close, 1);
        daily.setStockCode(stockCode);
        daily.setStockName("深华发");
        daily.setFloatMarketCap(floatMarketCap);
        daily.setChangeRate(10D);
        daily.setConsecutiveLimitUpDays(1);
        daily.setAmplitude(10D);
        daily.setClosePrice(close);
        daily.setIsSt(false);
        return daily;
    }

    private SelectionTaskData task(String stockCode, int score) {
        SelectionTaskData task = new SelectionTaskData();
        task.setStockCode(stockCode);
        task.setLimitUpScore(score);
        return task;
    }
}
