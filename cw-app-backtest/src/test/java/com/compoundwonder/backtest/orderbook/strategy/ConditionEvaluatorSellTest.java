package com.compoundwonder.backtest.orderbook.strategy;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorSellTest {

    private final TradeStrategyDispatcher dispatcher = new TradeStrategyDispatcher();

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

        assertTrue(dispatcher.evaluateAveragePriceSell(index, orderBook, record));
        assertEquals(201, record.ruleCode);
    }

    @Test
    void latePeakDrawdownUsesPreviousPriceIncreaseInsteadOfRawPrice() {
        OrderBook orderBook = averageStrategyOrderBook(1);
        orderBook.updatePrice(0, 0, 1_060, 93_000_000);
        orderBook.updatePrice(0, 0, 1_090, 100_000_000);
        orderBook.updatePrice(0, 0, 940, 140_000_000);

        int index = 40;
        orderBook.avgPrice[index - 3] = 980;
        orderBook.avgPrice[index - 2] = 970;
        orderBook.avgPrice[index - 1] = 950;
        orderBook.avgPrice[index] = 950;
        // 最近 15 分钟内形成“开盘高点 -> 回落 -> 二次冲高 -> 再回落”完整结构。
        orderBook.price[index - 10] = 940;
        orderBook.price[index - 5] = 1_090;
        orderBook.price[index - 3] = 970;
        orderBook.price[index - 2] = 950;
        orderBook.price[index - 1] = 940;
        orderBook.price[index] = 940;

        RuleRecord record = new RuleRecord();

        assertTrue(dispatcher.evaluateAveragePriceSell(index, orderBook, record));
        assertEquals(205, record.ruleCode);
    }

    @Test
    void smallCapDispatcherKeepsNanjingPortThreeToFourRule() throws ReflectiveOperationException {
        OrderBook smallCap = limitUpStrategyOrderBook(110_633);
        smallCap.setLbcs(3);
        smallCap.setLimitUpBuyAmount(2_042);
        setField(smallCap, "turnoverRate", 54.38365946976607);
        setField(smallCap, "time", 100_055_630);
        setField(smallCap, "status", 39);

        RuleRecord smallCapRecord = new RuleRecord();
        assertTrue(dispatcher.evaluateSell(smallCap, smallCapRecord));
        assertEquals(103, smallCapRecord.ruleCode);
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
