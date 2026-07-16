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
    void shanghaiBuyRequiresMoreThanFiveHundredMilliseconds() {
        RuleRecordDTO boundary = buyRecord("600001", 100000000, 100000500);
        RuleRecordDTO fillable = buyRecord("600001", 100000000, 100000501);

        assertFalse(BacktestExecutionPolicy.isIntradayBuyFillable(boundary));
        assertTrue(BacktestExecutionPolicy.isIntradayBuyFillable(fillable));
    }

    @Test
    void shenzhenBuyRequiresMoreThanOneHundredMilliseconds() {
        RuleRecordDTO boundary = buyRecord("000001", 95959950, 100000050);
        RuleRecordDTO fillable = buyRecord("000001", 95959950, 100000051);

        assertFalse(BacktestExecutionPolicy.isIntradayBuyFillable(boundary));
        assertTrue(BacktestExecutionPolicy.isIntradayBuyFillable(fillable));
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
