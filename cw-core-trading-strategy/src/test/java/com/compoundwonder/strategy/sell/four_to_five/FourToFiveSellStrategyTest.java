package com.compoundwonder.strategy.sell.four_to_five;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FourToFiveSellStrategyTest {

    @Test
    void keepsSmallCapOneWordWeakeningFromXinjuNetworkOn2025_02_05() {
        Map<String, Object> values = baseValues(89_217, 4);
        values.put("getTime", 100_615_400);
        values.put("getLastPrice", 3_071);
        values.put("getLimitUpPrice", 3_071);
        values.put("getLimitUpBuyAmount", 1_334L);
        values.put("getStatus", 1);
        values.put("getChangePercent", -3.3D);
        values.put("getTurnoverRate", 19.392967438893887D);
        values.put("getTwoDaysTurnover", 20D);
        values.put("getYesterdayTurnover", 20D);
        values.put("getOneWordLimitUp", 2);
        values.put("getLastSealAmount", 2_000L);
        values.put("getAmplitude", 1D);
        values.put("getOpenIncrease", 9D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new FourToFiveSmallCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING, rule.ruleCode);
        assertEquals(3_071, rule.price);
    }

    @Test
    void keepsAverageHeightWeakSealFromMeibangSharesOn2026_02_25() {
        Map<String, Object> values = baseValues(79_362, 4);
        values.put("getTime", 93_306_880);
        values.put("getLastPrice", 3_469);
        values.put("getLimitUpPrice", 3_469);
        values.put("getLimitUpBuyAmount", 4_063L);
        values.put("getStatus", 1);
        values.put("getChangePercent", -3.89D);
        values.put("getTurnoverRate", 3.402270895030144D);
        values.put("getLastSealAmount", 4_063L);
        values.put("getAverageLimitUpHeight", 4);
        values.put("getAmplitude", 5D);

        CapturedRule rule = new CapturedRule();

        assertTrue(new FourToFiveSmallCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_WEAK_SEAL, rule.ruleCode);
        assertEquals(3_469, rule.price);
    }

    @Test
    void keepsAveragePriceWeakeningFromNankuangGroupOn2025_07_24() {
        int calculateIndex = 10;
        Map<String, Object> values = baseValues(80_000, 4);
        values.put("getTime", 140_000_000);
        values.put("getTurnoverRate", 60D);
        values.put("getTurnover", 1_000_000_000L);
        values.put("getClosePrice", 2_076);
        values.put("getOpenIncrease", 2D);
        values.put("getIncrease", -4.234841193455245D);
        values.put("getOneWordLimitUp", 1);
        values.put("getAveragePriceAt", (IntUnaryOperator) index -> switch (index) {
            case 7 -> 2_080;
            case 8 -> 2_050;
            case 9 -> 2_010;
            default -> 1_990;
        });
        values.put("getMinutePriceAt", (IntUnaryOperator) index -> index == calculateIndex ? 1_988 : 2_000);

        CapturedRule rule = new CapturedRule();

        assertTrue(new FourToFiveSmallCapSellStrategy()
                .evaluateAveragePrice(calculateIndex, market(values), rule));
        assertEquals(RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING, rule.ruleCode);
        assertEquals(1_988, rule.price);
    }

    @Test
    void normalCapDoesNotUseRulesWithoutAnyBaselineSample() {
        Map<String, Object> values = baseValues(120_000, 4);
        values.put("getLastPrice", 1_100);
        values.put("getLimitUpPrice", 1_100);
        values.put("getLimitUpBuyAmount", 11_000L);
        values.put("getChangePercent", -2D);
        values.put("getTurnoverRate", 10D);
        values.put("getLastSealAmount", 20_000L);

        CapturedRule rule = new CapturedRule();

        assertFalse(new FourToFiveNormalCapSellStrategy().evaluateOrderBook(market(values), rule));
        assertEquals(0, rule.ruleCode);
    }

    private static Map<String, Object> baseValues(int marketValue, int lbcs) {
        Map<String, Object> values = new HashMap<>();
        values.put("getSymbol", "600000");
        values.put("getInitialMarketValue", marketValue);
        values.put("getLbcs", lbcs);
        return values;
    }

    private static TradeMarketState market(Map<String, Object> values) {
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value instanceof IntUnaryOperator operator) {
                        return operator.applyAsInt((Integer) args[0]);
                    }
                    if (value != null) {
                        return value;
                    }
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
        private int price;

        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
            this.ruleCode = ruleCode;
            this.price = price;
        }
    }
}
