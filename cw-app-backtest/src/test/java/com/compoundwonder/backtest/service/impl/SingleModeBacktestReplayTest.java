package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.trader.entity.SingleModeBacktestSample;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleModeBacktestReplayTest {

    @Test
    void onlyReplaysTouchedOrActuallyBoughtSourceSamples() {
        SingleModeBacktestSample untouched = source("600001", 1, null);
        SingleModeBacktestSample touched = source("600002", 2, null);
        SingleModeBacktestSample actual = source("600003", 1, LocalDate.of(2025, 1, 3));

        List<SingleModeBacktestSample> eligible =
                SingleModeBacktestServiceImpl.replayCandidates(List.of(untouched, touched, actual));

        assertEquals(List.of("600002", "600003"),
                eligible.stream().map(SingleModeBacktestSample::getSymbol).toList());
    }

    @Test
    void preparesVirtualPositionAtPlannedLimitPriceAndKeepsMissReason() {
        LocalDate tradeDate = LocalDate.of(2025, 1, 2);
        SingleModeBacktestSample sample = source("600002", 2, null);
        sample.setTradeDate(tradeDate);
        sample.setNoBuyReason("隔夜委托排队未成交");
        StockDailyEntity daily = new StockDailyEntity();
        daily.setKlineState(2);
        BacktestReplayResult replay = new BacktestReplayResult(
                tradeDate, "600002", "测试股票", BacktestReplayMode.OVERNIGHT_BUY,
                List.of(), 1, 91_500_000, 1_234, 1_234, 10);

        SingleModeBacktestServiceImpl.prepareVirtualPosition(sample, daily, replay);

        assertEquals(SingleModeBacktestSample.POSITION_VIRTUAL, sample.getPositionType());
        assertEquals(tradeDate, sample.getBuyDate());
        assertEquals(1_234, sample.getBuyPrice());
        assertEquals(0, sample.getBuyRuleCode());
        assertEquals(2, sample.getBuyDayKlineState());
        assertEquals(3, sample.getStatus());
        assertEquals("隔夜委托排队未成交", sample.getNoBuyReason());
        assertTrue(sample.getBuyRemark().contains("卖出场景虚拟持仓"));
    }

    @Test
    void reusesActualBuySnapshotWithoutReplayingBuyOrderBook() {
        LocalDate tradeDate = LocalDate.of(2025, 1, 23);
        SingleModeBacktestSample source = source("605398", 2, tradeDate);
        source.setPositionType(SingleModeBacktestSample.POSITION_ACTUAL);
        source.setBuyTime(91_500_100);
        source.setBuyPrice(2_146);
        source.setBuyRuleCode(1);
        source.setBuyRemark("源任务真实成交");
        source.setBuyDayKlineState(3);
        SingleModeBacktestSample replay = source("605398", 2, null);
        replay.setTradeDate(tradeDate);
        StockDailyEntity daily = new StockDailyEntity();
        daily.setKlineState(3);
        daily.setHighPrice(21.46D);

        SingleModeBacktestServiceImpl.prepareReplayPosition(replay, source, daily);

        assertEquals(SingleModeBacktestSample.POSITION_ACTUAL, replay.getPositionType());
        assertEquals(tradeDate, replay.getBuyDate());
        assertEquals(91_500_100, replay.getBuyTime());
        assertEquals(2_146, replay.getBuyPrice());
        assertEquals(1, replay.getBuyRuleCode());
        assertEquals("源任务真实成交", replay.getBuyRemark());
    }

    @Test
    void createsVirtualSellScenarioFromTouchedDailyHighWithoutBuyReplay() {
        LocalDate tradeDate = LocalDate.of(2025, 1, 27);
        SingleModeBacktestSample source = source("603928", 7, null);
        source.setPositionType(SingleModeBacktestSample.POSITION_VIRTUAL);
        source.setBuyDate(tradeDate); // 重放结果本身也可能再次作为固定源，不能误判为真实成交。
        source.setNoBuyReason("隔夜涨停委托排队未成交");
        SingleModeBacktestSample replay = source("603928", 7, null);
        replay.setTradeDate(tradeDate);
        StockDailyEntity daily = new StockDailyEntity();
        daily.setKlineState(11);
        daily.setHighPrice(15.97D);

        SingleModeBacktestServiceImpl.prepareReplayPosition(replay, source, daily);

        assertEquals(SingleModeBacktestSample.POSITION_VIRTUAL, replay.getPositionType());
        assertEquals(tradeDate, replay.getBuyDate());
        assertEquals(1_597, replay.getBuyPrice());
        assertEquals(0, replay.getBuyRuleCode());
        assertEquals("隔夜涨停委托排队未成交", replay.getNoBuyReason());
        assertTrue(replay.getBuyRemark().contains("复用固定选股结果"));
    }

    @Test
    void skipsMissingHoldingDayAndContinuesSellLifecycle() {
        assertTrue(SingleModeBacktestServiceImpl.shouldSkipHoldingDay(null));
        assertFalse(SingleModeBacktestServiceImpl.shouldSkipHoldingDay(new StockDailyEntity()));
    }

    private SingleModeBacktestSample source(String symbol, int maxTouchedBoards, LocalDate buyDate) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setSymbol(symbol);
        sample.setMaxTouchedBoards(maxTouchedBoards);
        sample.setBuyDate(buyDate);
        return sample;
    }
}
