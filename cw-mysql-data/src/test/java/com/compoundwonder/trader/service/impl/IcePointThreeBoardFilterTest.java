package com.compoundwonder.trader.service.impl;

import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IcePointThreeBoardFilterTest {

    @Test
    void allowsMidCapThreeBoardAtConfirmedRelaxedBoundaries() {
        StockSelectionAssistDTO assist = eligibleAssist(200_000D);
        assist.setMaxTurnoverRate(55D);
        assist.setCurrentPrice(44.99D);
        assist.setCurrentTurnoverRate(49.99D);
        assist.setCurrentTurnover(149_999.99D);
        assist.setCurrentAmplitude(14.99D);
        assist.setSelectionAmplitude(44.99D);
        assist.setTenDayChangeRate(54.99D);
        assist.setMaxVolumeDayTurnoverRate(49.99D);
        assist.setMaxVolumeDayTurnover(299_999.99D);

        IcePointThreeBoardFilter.Decision decision = IcePointThreeBoardFilter.evaluate(assist);

        assertTrue(decision.passed());
        assertEquals("13至33亿冰点三板", decision.layer());
    }

    @Test
    void keepsFiftyFivePercentHistoricalMaxTurnoverAsHardLimit() {
        StockSelectionAssistDTO assist = eligibleAssist(200_000D);
        assist.setMaxTurnoverRate(55.01D);

        IcePointThreeBoardFilter.Decision decision = IcePointThreeBoardFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("历史最大换手", decision.layer());
    }

    @Test
    void keepsHistoricalBoardHeightRulesAsHardLimits() {
        StockSelectionAssistDTO twoHundredKlineBoundary = eligibleAssist(200_000D);
        twoHundredKlineBoundary.setHighestConsecutiveLimitUpDays(6);
        assertEquals("200根K线历史最高板",
                IcePointThreeBoardFilter.evaluate(twoHundredKlineBoundary).layer());

        StockSelectionAssistDTO ninetyDayBoundary = eligibleAssist(200_000D);
        ninetyDayBoundary.setPriorNinetyDayHighestConsecutiveLimitUpDays(3);
        assertEquals("90日历史最高板",
                IcePointThreeBoardFilter.evaluate(ninetyDayBoundary).layer());
    }

    @Test
    void smallCapThreeBoardDoesNotApplyMidCapLiquidityLimits() {
        StockSelectionAssistDTO assist = eligibleAssist(129_999.99D);
        assist.setCurrentTurnoverRate(80D);
        assist.setCurrentTurnover(500_000D);
        assist.setMaxVolumeDayTurnover(500_000D);
        assist.setTenDayChangeRate(44.99D);

        IcePointThreeBoardFilter.Decision decision = IcePointThreeBoardFilter.evaluate(assist);

        assertTrue(decision.passed());
        assertEquals("13亿以下冰点三板", decision.layer());
    }

    @Test
    void midCapThreeBoardRejectsConfirmedTurnoverAmountBoundaries() {
        StockSelectionAssistDTO currentTurnoverRateBoundary = eligibleAssist(200_000D);
        currentTurnoverRateBoundary.setCurrentTurnoverRate(50D);
        assertEquals("当日换手率",
                IcePointThreeBoardFilter.evaluate(currentTurnoverRateBoundary).layer());

        StockSelectionAssistDTO maxVolumeTurnoverRateBoundary = eligibleAssist(200_000D);
        maxVolumeTurnoverRateBoundary.setMaxVolumeDayTurnoverRate(50D);
        assertEquals("最大成交量日换手率",
                IcePointThreeBoardFilter.evaluate(maxVolumeTurnoverRateBoundary).layer());

        StockSelectionAssistDTO currentAmountBoundary = eligibleAssist(200_000D);
        currentAmountBoundary.setCurrentTurnover(150_000D);
        IcePointThreeBoardFilter.Decision currentAmountDecision =
                IcePointThreeBoardFilter.evaluate(currentAmountBoundary);
        assertFalse(currentAmountDecision.passed());
        assertEquals("当日成交额", currentAmountDecision.layer());

        StockSelectionAssistDTO maxVolumeAmountBoundary = eligibleAssist(200_000D);
        maxVolumeAmountBoundary.setMaxVolumeDayTurnover(300_000D);
        IcePointThreeBoardFilter.Decision maxVolumeAmountDecision =
                IcePointThreeBoardFilter.evaluate(maxVolumeAmountBoundary);
        assertFalse(maxVolumeAmountDecision.passed());
        assertEquals("最大成交量日成交额", maxVolumeAmountDecision.layer());
    }

    @Test
    void appliesFiveDayAmplitudeAndMarketCapSpecificTenDayLimits() {
        StockSelectionAssistDTO fiveDayBoundary = eligibleAssist(200_000D);
        fiveDayBoundary.setSelectionAmplitude(45D);
        assertFalse(IcePointThreeBoardFilter.evaluate(fiveDayBoundary).passed());

        StockSelectionAssistDTO smallCapTenDayBoundary = eligibleAssist(120_000D);
        smallCapTenDayBoundary.setTenDayChangeRate(45D);
        assertFalse(IcePointThreeBoardFilter.evaluate(smallCapTenDayBoundary).passed());

        StockSelectionAssistDTO midCapTenDayBoundary = eligibleAssist(200_000D);
        midCapTenDayBoundary.setTenDayChangeRate(55D);
        assertFalse(IcePointThreeBoardFilter.evaluate(midCapTenDayBoundary).passed());
    }

    @Test
    void rejectsDailyAmplitudePriceAndMarketCapUpperBoundaries() {
        StockSelectionAssistDTO dailyAmplitudeBoundary = eligibleAssist(200_000D);
        dailyAmplitudeBoundary.setCurrentAmplitude(15D);
        assertFalse(IcePointThreeBoardFilter.evaluate(dailyAmplitudeBoundary).passed());

        StockSelectionAssistDTO priceBoundary = eligibleAssist(200_000D);
        priceBoundary.setCurrentPrice(45D);
        assertFalse(IcePointThreeBoardFilter.evaluate(priceBoundary).passed());

        StockSelectionAssistDTO marketCapBoundary = eligibleAssist(330_000D);
        assertFalse(IcePointThreeBoardFilter.evaluate(marketCapBoundary).passed());
    }

    private StockSelectionAssistDTO eligibleAssist(double startMarketCap) {
        StockSelectionAssistDTO assist = new StockSelectionAssistDTO();
        assist.setStockCode("600001");
        assist.setConsecutiveLimitUpDays(3);
        assist.setStartMarketCap(startMarketCap);
        assist.setCurrentPrice(20D);
        assist.setCurrentTurnoverRate(30D);
        assist.setCurrentTurnover(100_000D);
        assist.setCurrentAmplitude(10D);
        assist.setSelectionAmplitude(40D);
        assist.setTenDayChangeRate(40D);
        assist.setMaxTurnoverRate(40D);
        assist.setHighestConsecutiveLimitUpDays(5);
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(2);
        assist.setMaxVolumeDayTurnoverRate(40D);
        assist.setMaxVolumeDayTurnover(200_000D);
        return assist;
    }
}
