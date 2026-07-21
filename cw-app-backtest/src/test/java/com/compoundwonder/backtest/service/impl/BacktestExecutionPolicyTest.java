package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BacktestExecutionPolicyTest {

    @Test
    void shanghaiBuyRequiresMoreThanFourHundredAndFiftyMilliseconds() {
        RuleRecordDTO boundary = buyRecord("600001", 100000000, 100000450);
        RuleRecordDTO fillable = buyRecord("600001", 100000000, 100000451);

        assertFalse(BacktestExecutionPolicy.isIntradayBuyFillable(boundary));
        assertTrue(BacktestExecutionPolicy.isIntradayBuyFillable(fillable));
    }

    @Test
    void shenzhenBuyRequiresMoreThanEightyMilliseconds() {
        RuleRecordDTO boundary = buyRecord("000001", 95959950, 100000030);
        RuleRecordDTO fillable = buyRecord("000001", 95959950, 100000031);

        assertFalse(BacktestExecutionPolicy.isIntradayBuyFillable(boundary));
        assertTrue(BacktestExecutionPolicy.isIntradayBuyFillable(fillable));
    }

    @Test
    void limitUpBuyIsFillableWhenReplayLaterBreaksBelowLimitUp() {
        RuleRecordDTO buy = buyRecord("600876", 93_457_270, 0);

        assertTrue(BacktestExecutionPolicy.isIntradayBuyFillable(buy, 993, 1_036));
    }

    @Test
    void sealedLimitUpStillRequiresMarketDelay() {
        RuleRecordDTO unfillable = buyRecord("600876", 93_457_270, 0);

        assertFalse(BacktestExecutionPolicy.isIntradayBuyFillable(unfillable, 1_036, 1_036));
    }

    @Test
    void overnightBuyRequiresQueueHeadAfterNineFifteenAndOneSecond() {
        assertFalse(BacktestExecutionPolicy.isOvernightBuyFillable(91501000));
        assertTrue(BacktestExecutionPolicy.isOvernightBuyFillable(91501001));
    }

    @Test
    void overnightBuyAlsoFillsWhenDailyTurnoverExceedsFortyMillionYuan() {
        assertFalse(BacktestExecutionPolicy.isOvernightBuyFillable(91501000, 4000D));
        assertTrue(BacktestExecutionPolicy.isOvernightBuyFillable(91501000, 4000.01D));
    }

    @Test
    void modelTwoOvernightBuyRequiresQueueHeadBeforeTwoThirty() {
        assertFalse(BacktestExecutionPolicy.isModelTwoOvernightBuyFillable(0));
        assertTrue(BacktestExecutionPolicy.isModelTwoOvernightBuyFillable(142959999));
        assertFalse(BacktestExecutionPolicy.isModelTwoOvernightBuyFillable(143000000));
        assertFalse(BacktestExecutionPolicy.isModelTwoOvernightBuyFillable(145824770));
    }

    @Test
    void choosesEarliestFillableBuyAndIgnoresUnfillableEarlierSignal() {
        RuleRecordDTO unfillable = buyRecord("600001", 100000000, 100000400);
        RuleRecordDTO later = buyRecord("000001", 100001000, 100001200);
        RuleRecordDTO earliestFillable = buyRecord("600002", 100000500, 100001100);

        RuleRecordDTO selected = BacktestExecutionPolicy.findEarliestFillableBuy(
                List.of(unfillable, later, earliestFillable)).orElseThrow();

        assertEquals("600002", selected.getSymbol());
    }

    private RuleRecordDTO buyRecord(String symbol, int time, int lastOrderTime) {
        RuleRecordDTO record = new RuleRecordDTO();
        record.setActionType(RuleConstant.TRADING_MODE_BUY);
        record.setSymbol(symbol);
        record.setTime(time);
        record.setLastOrderTime(lastOrderTime);
        return record;
    }
}
