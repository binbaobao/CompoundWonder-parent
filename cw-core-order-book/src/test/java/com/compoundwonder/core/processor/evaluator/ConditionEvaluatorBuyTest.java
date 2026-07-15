package com.compoundwonder.core.processor.evaluator;

import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.TickNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorBuyTest {

    @Test
    void matchesNineMillionYuanOrderForHigherPricedStock() {
        OrderBook orderBook = eligibleOrderBook(30.00, 180_000, 8_000);
        setLargestBuyOrder(orderBook, 300_000);

        RuleRecord record = evaluate(orderBook);

        assertEquals(11, record.ruleCode);
    }

    @Test
    void fallsThroughToSevenMillionYuanRuleForSmallerMarketValue() {
        OrderBook orderBook = eligibleOrderBook(20.00, 140_000, 8_000);
        setLargestBuyOrder(orderBook, 400_000);

        RuleRecord record = evaluate(orderBook);

        assertEquals(12, record.ruleCode);
    }

    @Test
    void lowPricedStockUsesLargeQuantityInsteadOfOrderAmount() {
        OrderBook orderBook = eligibleOrderBook(4.00, 80_000, 8_000);
        setLargestBuyOrder(orderBook, 900_000);

        RuleRecord record = evaluate(orderBook);

        assertEquals(13, record.ruleCode);
    }

    @Test
    void preservesRuleElevenPriorityWhenBothHighPriceRulesMatch() {
        OrderBook orderBook = eligibleOrderBook(11.00, 140_000, 8_000);
        setLargestBuyOrder(orderBook, 990_000);

        RuleRecord record = evaluate(orderBook);

        assertEquals(11, record.ruleCode);
    }

    @Test
    void matchesNormalSealRuleAtMarketValueSpecificThreshold() {
        OrderBook orderBook = eligibleOrderBook(15.00, 160_000, 2_001);

        RuleRecord record = evaluate(orderBook);

        assertEquals(14, record.ruleCode);
    }

    @Test
    void rejectsNormalSealAtStrictThresholdBoundary() {
        OrderBook orderBook = eligibleOrderBook(15.00, 160_000, 2_000);
        RuleRecord record = new RuleRecord();

        assertFalse(ConditionEvaluatorBuy.evaluate(orderBook, record));
        assertEquals(0, record.ruleCode);
    }

    @Test
    void rejectsEntryAtThirtySecondBoundaryAfterLimitUp() {
        OrderBook orderBook = eligibleOrderBook(30.00, 180_000, 0);
        TickNode limitUpBuy = new TickNode();
        limitUpBuy.setOrderId(1);
        limitUpBuy.setPrice(orderBook.getLimitUpPrice());
        limitUpBuy.setQuantity(1_000_000);
        limitUpBuy.setDirection((byte) 1);
        orderBook.addOrder(limitUpBuy);
        orderBook.updateLimitUpStatus();
        orderBook.updatePrice(0, 0, orderBook.getLimitUpPrice(), 100_030_000);
        orderBook.updateLimitUpStatus();
        setLargestBuyOrder(orderBook, 300_000);
        RuleRecord record = new RuleRecord();

        assertFalse(ConditionEvaluatorBuy.evaluate(orderBook, record));
    }

    private RuleRecord evaluate(OrderBook orderBook) {
        RuleRecord record = new RuleRecord();
        assertTrue(ConditionEvaluatorBuy.evaluate(orderBook, record));
        return record;
    }

    private OrderBook eligibleOrderBook(double closePrice, int marketValue, long sealAmount) {
        OrderBook orderBook = new OrderBook("600000", 1_000_000L, closePrice, 500_000L);
        orderBook.setInitialMarketValue(marketValue);
        orderBook.updatePrice(0, 130_000, orderBook.getLimitUpPrice(), 100_000_000);
        orderBook.setLimitUpBuyAmount(sealAmount);
        return orderBook;
    }

    private void setLargestBuyOrder(OrderBook orderBook, int quantity) {
        orderBook.buyMaxOrder.setPrice(orderBook.getLimitUpPrice());
        orderBook.buyMaxOrder.setQuantity(quantity);
        orderBook.buyMaxOrder.setDirection((byte) 1);
    }
}
