package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.backtest.orderbook.data.BacktestDailyTickBatch;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.entity.SingleModeBacktestRun;
import com.compoundwonder.trader.entity.SingleModeBacktestSample;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleModeRelayDeferredEntryTest {

    @Test
    void recognizesSecondBoardAccelerationWithStrictExistingBoundaries() {
        assertTrue(SingleModeBacktestServiceImpl.isAcceleratedSecondBoard(
                daily(3, 0D, 2D, 10D, 11D)));
        assertTrue(SingleModeBacktestServiceImpl.isAcceleratedSecondBoard(
                daily(1, 14.99D, 8D, 10D, 11D)));
        assertTrue(SingleModeBacktestServiceImpl.isAcceleratedSecondBoard(
                daily(1, 20D, 2.99D, 10D, 11D)));
        assertTrue(SingleModeBacktestServiceImpl.isAcceleratedSecondBoard(
                daily(2, 17.99D, 4D, 10D, 11D)));

        assertFalse(SingleModeBacktestServiceImpl.isAcceleratedSecondBoard(
                daily(1, 15D, 3D, 10D, 11D)));
        assertFalse(SingleModeBacktestServiceImpl.isAcceleratedSecondBoard(
                daily(2, 18D, 4D, 10D, 11D)));
    }

    @Test
    void defersOnlyAcceleratedTwoToThreeOneWordOrHighOpenFastBoards() {
        SingleModeBacktestSample twoToThree = relaySample(2);
        StockDailyEntity acceleratedSecondBoard = daily(3, 3D, 0D, 10D, 11D);
        StockDailyEntity oneWordThirdBoard = daily(3, 1D, 0D, 11D, 11D);

        assertTrue(SingleModeBacktestServiceImpl.shouldDeferRelayThirdBoardBuy(
                twoToThree, acceleratedSecondBoard, oneWordThirdBoard, 0));

        StockDailyEntity highOpenThirdBoard = daily(1, 20D, 8D, 10.6D, 10D);
        assertTrue(SingleModeBacktestServiceImpl.shouldDeferRelayThirdBoardBuy(
                twoToThree, acceleratedSecondBoard, highOpenThirdBoard,
                ConstantUtil.TIME_935 - 1));

        assertFalse(SingleModeBacktestServiceImpl.shouldDeferRelayThirdBoardBuy(
                twoToThree, acceleratedSecondBoard, highOpenThirdBoard,
                ConstantUtil.TIME_935));
        assertFalse(SingleModeBacktestServiceImpl.shouldDeferRelayThirdBoardBuy(
                relaySample(3), acceleratedSecondBoard, oneWordThirdBoard, 0));
        assertFalse(SingleModeBacktestServiceImpl.shouldDeferRelayThirdBoardBuy(
                twoToThree, daily(1, 20D, 6D, 10D, 11D), oneWordThirdBoard, 0));
    }

    @Test
    void usesTheFirstRealLimitUpTradeInsteadOfTheOvernightOrderTime() {
        BacktestDailyTickBatch ticks = new BacktestDailyTickBatch() {
            @Override
            public LocalDate tradeDate() {
                return LocalDate.of(2025, 3, 24);
            }

            @Override
            public Set<String> stockCodes() {
                return Set.of("600001");
            }

            @Override
            public long replay(String stockCode, Consumer<TickData> consumer) {
                consumer.accept(tick(92_500_000, 1, 1_100));
                consumer.accept(tick(93_100_000, 2, 1_099));
                consumer.accept(tick(93_200_000, 2, 1_100));
                consumer.accept(tick(93_300_000, 2, 1_100));
                return 4;
            }
        };

        assertEquals(93_200_000, SingleModeBacktestServiceImpl.firstLimitUpTradeTime(
                ticks, "600001", 1_100));
    }

    @Test
    void createsAThreeToFourOpportunityAfterDeferredThirdBoardSeals() {
        SingleModeBacktestRun run = new SingleModeBacktestRun();
        run.setId(43L);
        run.setTradeMode(TradeMode.RELAY_LIMIT_UP.code());
        SingleModeBacktestSample source = relaySample(2);
        source.setId(900L);
        source.setSourceSampleId(100L);
        source.setSymbol("600001");
        source.setSymbolName("递延样本");
        source.setRecommendDate(LocalDate.of(2025, 3, 20));
        source.setTradeDate(LocalDate.of(2025, 3, 21));
        source.setSelectionTrigger("HEIGHT_SUPPRESSION");
        source.setSelectionStrength("NORMAL");
        source.setLimitUpScore(88);
        source.setSelectionRunId(7L);
        source.setRelayCandidateRecordId(8L);

        SingleModeBacktestSample deferred = SingleModeBacktestServiceImpl.createDeferredRelaySample(
                run, source, LocalDate.of(2025, 3, 24));

        assertEquals(43L, deferred.getRunId());
        assertEquals(100L, deferred.getSourceSampleId());
        assertEquals("600001", deferred.getSymbol());
        assertEquals(LocalDate.of(2025, 3, 21), deferred.getRecommendDate());
        assertEquals(LocalDate.of(2025, 3, 24), deferred.getTradeDate());
        assertEquals(3, deferred.getSelectionBoard());
        assertEquals("DEFERRED_3_TO_4", deferred.getSelectionTrigger());
        assertTrue(deferred.getSelectionTrigger().length() <= 20,
                "延后机会标识需兼容既有 selection_trigger 字段长度");
        assertEquals("NORMAL", deferred.getSelectionStrength());
        assertEquals(SingleModeBacktestSample.POSITION_NONE, deferred.getPositionType());
        assertEquals(3, deferred.getMaxSealedBoards());
        assertEquals(3, deferred.getMaxTouchedBoards());
    }

    @Test
    void reopensOnlyUnfilledAcceleratedSamplesWhoseThirdBoardClosedSealed() {
        SingleModeBacktestSample unfilled = relaySample(2);
        unfilled.setPositionType(SingleModeBacktestSample.POSITION_VIRTUAL);
        StockDailyEntity acceleratedSecondBoard = daily(3, 3D, 0D, 10D, 11D);

        assertTrue(SingleModeBacktestServiceImpl.shouldCreateDeferredRelayOpportunity(
                unfilled, acceleratedSecondBoard, daily(2, 20D, 4D, 11D, 10D)));
        assertFalse(SingleModeBacktestServiceImpl.shouldCreateDeferredRelayOpportunity(
                unfilled, acceleratedSecondBoard, daily(11, 40D, 12D, 10.5D, 10D)));

        unfilled.setPositionType(SingleModeBacktestSample.POSITION_ACTUAL);
        assertFalse(SingleModeBacktestServiceImpl.shouldCreateDeferredRelayOpportunity(
                unfilled, acceleratedSecondBoard, daily(1, 20D, 4D, 11D, 10D)));
    }

    private SingleModeBacktestSample relaySample(int selectionBoard) {
        SingleModeBacktestSample sample = new SingleModeBacktestSample();
        sample.setTradeMode(TradeMode.RELAY_LIMIT_UP.code());
        sample.setSelectionBoard(selectionBoard);
        return sample;
    }

    private StockDailyEntity daily(int klineState, double turnoverRate, double amplitude,
                                   double open, double prevClose) {
        StockDailyEntity daily = new StockDailyEntity();
        daily.setKlineState(klineState);
        daily.setTurnoverRate(turnoverRate);
        daily.setAmplitude(amplitude);
        daily.setOpenPrice(open);
        daily.setPrevClose(prevClose);
        return daily;
    }

    private TickData tick(int time, int dataType, int price) {
        TickData tick = new TickData();
        tick.time = time;
        tick.dataType = (byte) dataType;
        tick.price = price;
        return tick;
    }
}
