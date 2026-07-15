package com.compoundwonder.core.processor.evaluator;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorSellTest {

    @Test
    void allSellRulesHaveUniqueCodes() {
        List<Integer> sellRuleCodes = Arrays.stream(RuleConstant.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getName().startsWith("SELL_"))
                .map(ConditionEvaluatorSellTest::readRuleCode)
                .toList();

        assertEquals(29, sellRuleCodes.size());
        assertEquals(sellRuleCodes.size(), sellRuleCodes.stream().distinct().count());
    }

    @Test
    void lowOpenWeakeningUsesPreviousPriceIncreaseInsteadOfRawPrice() {
        OrderBook orderBook = averageStrategyOrderBook(2);
        orderBook.updatePrice(0, 0, 1_000, 93_000_000);
        orderBook.updatePrice(0, 0, 940, 100_000_000);

        int index = 10;
        orderBook.avgPrice[index - 3] = 980;
        orderBook.avgPrice[index - 2] = 970;
        orderBook.avgPrice[index - 1] = 960;
        orderBook.avgPrice[index] = 950;
        orderBook.price[index - 3] = 980;
        orderBook.price[index - 2] = 960;
        orderBook.price[index - 1] = 940;
        orderBook.price[index] = 930;

        RuleRecord record = new RuleRecord();

        assertTrue(ConditionEvaluatorSell.averagePriceSellStrategy(index, orderBook, record));
        assertEquals(201, record.ruleCode);
    }

    @Test
    void latePeakDrawdownUsesPreviousPriceIncreaseInsteadOfRawPrice() {
        OrderBook orderBook = averageStrategyOrderBook(1);
        orderBook.updatePrice(0, 0, 1_000, 93_000_000);
        orderBook.updatePrice(0, 0, 1_090, 100_000_000);
        orderBook.updatePrice(0, 0, 940, 140_000_000);

        int index = 40;
        orderBook.avgPrice[index - 3] = 980;
        orderBook.avgPrice[index - 2] = 970;
        orderBook.avgPrice[index - 1] = 950;
        orderBook.avgPrice[index] = 940;
        orderBook.price[index - 3] = 970;
        orderBook.price[index - 2] = 950;
        orderBook.price[index - 1] = 940;
        orderBook.price[index] = 940;

        RuleRecord record = new RuleRecord();

        assertTrue(ConditionEvaluatorSell.averagePriceSellStrategy(index, orderBook, record));
        assertEquals(205, record.ruleCode);
    }

    @Test
    void limitUpStageUsesDifferentCodesForDifferentRules() throws ReflectiveOperationException {
        OrderBook smallCap = limitUpStrategyOrderBook(100_000);
        smallCap.setLbcs(4);
        smallCap.setLimitUpBuyAmount(11_000);
        setField(smallCap, "turnoverRate", 10.0);
        setField(smallCap, "changePercent", -2.0);
        setField(smallCap, "lastSealAmount", 20_000L);

        RuleRecord smallCapRecord = new RuleRecord();
        assertTrue(ConditionEvaluatorSell.evaluate(smallCap, smallCapRecord));
        assertEquals(101, smallCapRecord.ruleCode);

        OrderBook afternoon = limitUpStrategyOrderBook(140_000);
        afternoon.setTwoDaysTurnover(20);
        setField(afternoon, "turnoverRate", 20.0);
        setField(afternoon, "changePercent", -4.0);
        setField(afternoon, "time", 120_000_000);

        RuleRecord afternoonRecord = new RuleRecord();
        assertTrue(ConditionEvaluatorSell.evaluate(afternoon, afternoonRecord));
        assertEquals(102, afternoonRecord.ruleCode);
    }

    private OrderBook averageStrategyOrderBook(int lbcs) {
        OrderBook orderBook = new OrderBook("600000", 10_000_000L, 10.00, 500_000L);
        orderBook.setInitialMarketValue(150_000);
        orderBook.setLbcs(lbcs);
        return orderBook;
    }

    private OrderBook limitUpStrategyOrderBook(int marketValue) {
        OrderBook orderBook = new OrderBook("600000", 10_000_000L, 10.00, 500_000L);
        orderBook.setInitialMarketValue(marketValue);
        orderBook.updatePrice(0, 0, orderBook.getLimitUpPrice(), 100_000_000);
        return orderBook;
    }

    private void setField(OrderBook orderBook, String name, Object value) throws ReflectiveOperationException {
        Field field = OrderBook.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(orderBook, value);
    }

    private static int readRuleCode(Field field) {
        try {
            return field.getInt(null);
        } catch (IllegalAccessException e) {
            throw new AssertionError("无法读取卖出规则编号: " + field.getName(), e);
        }
    }
}
