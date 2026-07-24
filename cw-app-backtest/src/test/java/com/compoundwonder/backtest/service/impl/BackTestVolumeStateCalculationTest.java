package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.hxdata.entity.StockDailyEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackTestVolumeStateCalculationTest {

    @Test
    void calculatesEachBoardDayFromItsOwnEarlierTurnoverWindow() {
        // 调用 List.of 和 daily 构造按交易日倒序排列的日K数据。
        List<StockDailyEntity> dailyRows = List.of(daily(0D, 0D, 0), daily(10D, 6D, 1), daily(40D, 6D, 1), daily(20D, 6D, 0));

        // 调用 calculateVolumeState 计算首板日量能状态。
        int firstBoardVolumeState = BackTestTradeService.calculateVolumeState(dailyRows, 1);
        // 调用 calculateVolumeState 计算二板日量能状态。
        int secondBoardVolumeState = BackTestTradeService.calculateVolumeState(dailyRows, 2);

        // 调用 assertEquals 验证首板日只使用其之前的历史最大换手。
        assertEquals(-1, firstBoardVolumeState);
        // 调用 assertEquals 验证二板日排除更新的首板日并使用自己的历史窗口。
        assertEquals(1, secondBoardVolumeState);
    }

    @Test
    void includesTheTwoHundredthEarlierKlineInHistoricalMaximum() {
        List<StockDailyEntity> dailyRows = new ArrayList<>();
        // 调用 dailyRows.add 和 daily 添加回测日、首板日及待计算的二板日。
        dailyRows.add(daily(0D, 0D, 0));
        dailyRows.add(daily(10D, 6D, 1));
        dailyRows.add(daily(20D, 6D, 1));
        for (int index = 0; index < 199; index++) {
            // 调用 dailyRows.add 和 daily 添加前199根普通历史K线。
            dailyRows.add(daily(10D, 6D, 0));
        }
        // 调用 dailyRows.add 和 daily 添加第200根高换手历史K线。
        dailyRows.add(daily(40D, 6D, 0));

        // 调用 calculateVolumeState 计算二板日量能状态。
        int volumeState = BackTestTradeService.calculateVolumeState(dailyRows, 2);

        // 调用 assertEquals 验证第200根历史K线进入最大换手计算。
        assertEquals(-1, volumeState);
    }

    @Test
    void returnsUnavailableWhenNoEarlierValidTurnoverExists() {
        // 调用 List.of 和 daily 构造缺少更早历史K线的数据。
        List<StockDailyEntity> dailyRows = List.of(daily(0D, 0D, 0), daily(10D, 6D, 1));

        // 调用 calculateVolumeState 计算缺少历史最大换手的首板日。
        int volumeState = BackTestTradeService.calculateVolumeState(dailyRows, 1);

        // 调用 assertEquals 验证缺少历史数据时返回不可用状态。
        assertEquals(-2, volumeState);
    }

    private StockDailyEntity daily(double turnoverRate, double amplitude, int klineState) {
        StockDailyEntity daily = new StockDailyEntity();
        // 调用 setTurnoverRate 设置当日换手率。
        daily.setTurnoverRate(turnoverRate);
        // 调用 setAmplitude 设置当日振幅。
        daily.setAmplitude(amplitude);
        // 调用 setKlineState 设置当日K线形态。
        daily.setKlineState(klineState);
        return daily;
    }
}
