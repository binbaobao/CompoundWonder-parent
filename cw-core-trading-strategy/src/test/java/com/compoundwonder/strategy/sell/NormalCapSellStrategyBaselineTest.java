package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.eight_to_nine.EightToNineNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.five_to_six.FiveToSixNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.four_to_five.FourToFiveNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.high_board.HighBoardNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.seven_to_eight.SevenToEightNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.six_to_seven.SixToSevenNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.three_to_four.ThreeToFourNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.two_to_three.TwoToThreeNormalCapSellStrategy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalCapSellStrategyBaselineTest {

    @Test
    void keepsTwoToThreeAfternoonShrinkingBoardFromSuhaoHongyeOn2025_01_13() {
        Map<String, Object> values = values(2);
        values.put("getInitialMarketValue", 143_336);
        values.put("getTime", 141_742_980);
        values.put("getTurnoverRate", 11.460674833619576D);
        values.put("getTwoDaysTurnover", 10D);
        values.put("getChangePercent", -3.94D);
        values.put("getLimitUpBuyAmount", 9_668L);

        assertRule(new TwoToThreeNormalCapSellStrategy(), values,
                RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD);
    }

    @Test
    void keepsThreeToFourAfternoonShrinkingBoardFromMoenElectricOn2025_11_07() {
        Map<String, Object> values = values(3);
        values.put("getInitialMarketValue", 160_000);
        values.put("getTime", 144_833_830);
        values.put("getTurnoverRate", 20D);
        values.put("getTwoDaysTurnover", 20D);
        values.put("getChangePercent", -4D);

        assertRule(new ThreeToFourNormalCapSellStrategy(), values,
                RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD);
    }

    @Test
    void keepsFiveToSixMorningWeakSealFromJinaoboOn2025_01_16() {
        Map<String, Object> values = values(5);
        values.put("getTime", 111_458_210);
        values.put("getTurnoverRate", 40.00039506921119D);
        values.put("getChangePercent", -4.1D);
        values.put("getLimitUpBuyAmount", 570L);

        assertRule(new FiveToSixNormalCapSellStrategy(), values,
                RuleConstant.SELL_LIMIT_UP_MORNING_HIGH_TURNOVER_WEAK_SEAL);
    }

    @Test
    void keepsSixToSevenHighTurnoverWeakSealFromYimingMedicineOn2025_06_12() {
        Map<String, Object> values = values(6);
        values.put("getTime", 111_628_840);
        values.put("getTurnoverRate", 46D);
        values.put("getChangePercent", -4D);
        values.put("getLastSealAmount", 2_000L);

        assertRule(new SixToSevenNormalCapSellStrategy(), values,
                RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_SEAL_WEAKENING);
    }

    @Test
    void keepsSevenToEightGapShrinkingFromJinyaoMedicineOn2026_04_08() {
        Map<String, Object> values = values(7);
        values.put("getTime", 94_227_270);
        values.put("getOpenIncrease", 8D);
        values.put("getTurnoverRate", 5D);
        values.put("getYesterdayTurnover", 20D);
        values.put("getChangePercent", -6D);

        assertRule(new SevenToEightNormalCapSellStrategy(), values,
                RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING);
    }

    @Test
    void removesUnusedTwoToThreeMultiBreakBranch() {
        Map<String, Object> values = values(2);
        values.put("getTime", 100_000_000);
        values.put("getTurnoverRate", 55D);
        values.put("getStatus", 21);
        values.put("getAmplitude", 16D);

        assertNoRule(new TwoToThreeNormalCapSellStrategy(), values);
    }

    @Test
    void removesUnusedThreeToFourConsecutiveHighTurnoverBranch() {
        assertNoRule(new ThreeToFourNormalCapSellStrategy(), consecutiveHighTurnoverValues(3));
    }

    @Test
    void removesUnusedFiveToSixConsecutiveHighTurnoverBranch() {
        assertNoRule(new FiveToSixNormalCapSellStrategy(), consecutiveHighTurnoverValues(5));
    }

    @Test
    void removesUnusedSixToSevenConsecutiveHighTurnoverBranch() {
        assertNoRule(new SixToSevenNormalCapSellStrategy(), consecutiveHighTurnoverValues(6));
    }

    @Test
    void removesUnusedSevenToEightConsecutiveHighTurnoverBranch() {
        assertNoRule(new SevenToEightNormalCapSellStrategy(), consecutiveHighTurnoverValues(7));
    }

    @Test
    void fourToFiveUsesConfirmedAverageHeightFallbackInsteadOfRemainingEmpty() {
        Map<String, Object> fourToFive = values(4);
        fourToFive.put("getAverageLimitUpHeight", 4);
        fourToFive.put("getLastLimitUptime", 93_100_000);
        fourToFive.put("getLastSealAmount", 2_000L);
        fourToFive.put("getTurnoverRate", 20D);
        fourToFive.put("getChangePercent", -2D);

        assertRule(new FourToFiveNormalCapSellStrategy(), fourToFive,
                RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL);
    }

    @Test
    void fourToFiveDoesNotApplyAfternoonFallbackToZhongshuiFisheryOn2025_11_20() {
        Map<String, Object> values = values(4);
        values.put("getInitialMarketValue", 130_070);
        values.put("getTime", 141_604_740);
        values.put("getTurnoverRate", 23.50655601460691D);
        values.put("getTwoDaysTurnover", 20D);
        values.put("getChangePercent", -4.17D);

        assertNoRule(new FourToFiveNormalCapSellStrategy(), values);
    }

    @Test
    void eightToNineUsesConfirmedHighBoardGapFallbackInsteadOfRemainingEmpty() {
        Map<String, Object> eightToNine = values(8);
        eightToNine.put("getOpenIncrease", 8D);
        eightToNine.put("getTurnoverRate", 5D);
        eightToNine.put("getYesterdayTurnover", 20D);
        eightToNine.put("getChangePercent", -6D);

        assertRule(new EightToNineNormalCapSellStrategy(), eightToNine,
                RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING);
    }

    @Test
    void highBoardUsesConfirmedAfternoonShrinkingFallbackInsteadOfRemainingEmpty() {
        Map<String, Object> highBoard = values(9);
        highBoard.put("getTime", 140_000_000);
        highBoard.put("getTurnoverRate", 20D);
        highBoard.put("getTwoDaysTurnover", 20D);
        highBoard.put("getChangePercent", -4D);

        assertRule(new HighBoardNormalCapSellStrategy(), highBoard,
                RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD);
    }

    private static Map<String, Object> consecutiveHighTurnoverValues(int lbcs) {
        Map<String, Object> values = values(lbcs);
        values.put("getTime", 140_000_000);
        values.put("getTurnoverRate", 41D);
        values.put("getYesterdayTurnover", 41D);
        return values;
    }

    private static Map<String, Object> values(int lbcs) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "600000");
        values.put("getInitialMarketValue", 150_000);
        values.put("getLbcs", lbcs);
        values.put("getTime", 100_000_000);
        values.put("getLastPrice", 1_000);
        values.put("getLimitUpPrice", 1_000);
        values.put("getLimitUpBuyAmount", 1_000L);
        values.put("getLastSealAmount", 10_000L);
        values.put("getStatus", 1);
        values.put("getIncrease", 10D);
        return values;
    }

    private static void assertRule(BoardSellStrategy strategy, Map<String, Object> values,
                                   int expectedRule) {
        CapturedRule record = new CapturedRule();
        assertTrue(strategy.evaluateOrderBook(market(values), record));
        assertEquals(expectedRule, record.ruleCode);
    }

    private static void assertNoRule(BoardSellStrategy strategy, Map<String, Object> values) {
        assertFalse(strategy.evaluateOrderBook(market(values), new CapturedRule()));
    }

    private static TradeMarketState market(Map<String, Object> values) {
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value != null) return value;
                    return switch (method.getReturnType().getName()) {
                        case "int" -> 0;
                        case "long" -> 0L;
                        case "double" -> 0D;
                        case "boolean" -> false;
                        default -> null;
                    };
                });
    }

    private static final class CapturedRule implements TradeRuleRecord {
        private int ruleCode;

        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
            this.ruleCode = ruleCode;
        }
    }
}
