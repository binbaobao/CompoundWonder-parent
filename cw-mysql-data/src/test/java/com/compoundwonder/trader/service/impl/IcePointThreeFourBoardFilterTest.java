package com.compoundwonder.trader.service.impl;

import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IcePointThreeFourBoardFilterTest {

    @Test
    void allowsMidCapThreeBoardAtExpandedRelaxedBoundaries() {
        StockSelectionAssistDTO assist = eligibleAssist(439_999.99D);
        assist.setMaxTurnoverRate(55D);
        assist.setCurrentPrice(44.99D);
        assist.setCurrentTurnoverRate(49.99D);
        assist.setCurrentTurnover(249_999.99D);
        assist.setCurrentAmplitude(14.99D);
        assist.setSelectionAmplitude(44.99D);
        assist.setTenDayChangeRate(54.99D);
        assist.setMaxVolumeDayTurnoverRate(49.99D);
        assist.setMaxVolumeDayTurnover(299_999.99D);

        IcePointThreeFourBoardFilter.Decision decision = IcePointThreeFourBoardFilter.evaluate(assist);

        assertTrue(decision.passed());
        assertEquals("13至44亿冰点3/4板", decision.layer());
    }

    @Test
    void keepsFiftyFivePercentHistoricalMaxTurnoverAsHardLimit() {
        StockSelectionAssistDTO assist = eligibleAssist(200_000D);
        assist.setMaxTurnoverRate(55.01D);

        IcePointThreeFourBoardFilter.Decision decision = IcePointThreeFourBoardFilter.evaluate(assist);

        assertFalse(decision.passed());
        assertEquals("历史最大换手", decision.layer());
    }

    @Test
    void keepsHistoricalBoardHeightRulesAsHardLimits() {
        StockSelectionAssistDTO twoHundredKlineBoundary = eligibleAssist(200_000D);
        twoHundredKlineBoundary.setHighestConsecutiveLimitUpDays(6);
        assertEquals("200根K线历史最高板",
                IcePointThreeFourBoardFilter.evaluate(twoHundredKlineBoundary).layer());

        StockSelectionAssistDTO ninetyDayBoundary = eligibleAssist(200_000D);
        ninetyDayBoundary.setPriorNinetyDayHighestConsecutiveLimitUpDays(3);
        assertEquals("90日历史最高板",
                IcePointThreeFourBoardFilter.evaluate(ninetyDayBoundary).layer());
    }

    @Test
    void smallCapThreeBoardDoesNotApplyMidCapLiquidityLimits() {
        StockSelectionAssistDTO assist = eligibleAssist(129_999.99D);
        assist.setCurrentTurnoverRate(80D);
        assist.setCurrentTurnover(500_000D);
        assist.setMaxVolumeDayTurnover(500_000D);
        assist.setTenDayChangeRate(44.99D);

        IcePointThreeFourBoardFilter.Decision decision = IcePointThreeFourBoardFilter.evaluate(assist);

        assertTrue(decision.passed());
        assertEquals("13亿以下冰点3/4板", decision.layer());
    }

    @Test
    void midCapThreeBoardRejectsExpandedTurnoverAmountBoundaries() {
        StockSelectionAssistDTO currentTurnoverRateBoundary = eligibleAssist(200_000D);
        currentTurnoverRateBoundary.setCurrentTurnoverRate(50D);
        assertEquals("当日换手率",
                IcePointThreeFourBoardFilter.evaluate(currentTurnoverRateBoundary).layer());

        StockSelectionAssistDTO maxVolumeTurnoverRateBoundary = eligibleAssist(200_000D);
        maxVolumeTurnoverRateBoundary.setMaxVolumeDayTurnoverRate(50D);
        assertEquals("最大成交量日换手率",
                IcePointThreeFourBoardFilter.evaluate(maxVolumeTurnoverRateBoundary).layer());

        StockSelectionAssistDTO currentAmountBoundary = eligibleAssist(200_000D);
        currentAmountBoundary.setCurrentTurnover(250_000D);
        IcePointThreeFourBoardFilter.Decision currentAmountDecision =
                IcePointThreeFourBoardFilter.evaluate(currentAmountBoundary);
        assertFalse(currentAmountDecision.passed());
        assertEquals("当日成交额", currentAmountDecision.layer());

        StockSelectionAssistDTO maxVolumeAmountBoundary = eligibleAssist(200_000D);
        maxVolumeAmountBoundary.setMaxVolumeDayTurnover(300_000D);
        IcePointThreeFourBoardFilter.Decision maxVolumeAmountDecision =
                IcePointThreeFourBoardFilter.evaluate(maxVolumeAmountBoundary);
        assertFalse(maxVolumeAmountDecision.passed());
        assertEquals("最大成交量日成交额", maxVolumeAmountDecision.layer());
    }

    @Test
    void doesNotDuplicateRecentPatternRules() {
        StockSelectionAssistDTO assist = eligibleAssist(200_000D);
        assist.setSelectionAmplitude(99D);
        assist.setTenDayChangeRate(99D);

        IcePointThreeFourBoardFilter.Decision decision = IcePointThreeFourBoardFilter.evaluate(assist);

        assertTrue(decision.passed());
    }

    @Test
    void rejectsDailyAmplitudePriceAndMarketCapUpperBoundaries() {
        StockSelectionAssistDTO dailyAmplitudeBoundary = eligibleAssist(200_000D);
        dailyAmplitudeBoundary.setCurrentAmplitude(15D);
        assertFalse(IcePointThreeFourBoardFilter.evaluate(dailyAmplitudeBoundary).passed());

        StockSelectionAssistDTO priceBoundary = eligibleAssist(200_000D);
        priceBoundary.setCurrentPrice(45D);
        assertFalse(IcePointThreeFourBoardFilter.evaluate(priceBoundary).passed());

        StockSelectionAssistDTO marketCapBoundary = eligibleAssist(440_000D);
        assertFalse(IcePointThreeFourBoardFilter.evaluate(marketCapBoundary).passed());
    }

    @Test
    void rejectsCandidatesOutsideTwoAndThreeBoards() {
        StockSelectionAssistDTO fourBoard = eligibleAssist(200_000D);
        fourBoard.setConsecutiveLimitUpDays(4);

        IcePointThreeFourBoardFilter.Decision decision = IcePointThreeFourBoardFilter.evaluate(fourBoard);

        assertFalse(decision.passed());
        assertEquals("候选连板数", decision.layer());
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
