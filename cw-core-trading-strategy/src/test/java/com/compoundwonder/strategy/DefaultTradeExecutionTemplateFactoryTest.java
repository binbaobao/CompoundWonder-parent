package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultTradeExecutionTemplateFactoryTest {

    @Test
    void compilesSixStageExecutorsOnceFromStaticFacts() {
        TradeStaticFacts facts = new TradeStaticFacts(
                3, 4, 1_000_000L, 35D, 90_000,
                18D, 20D, 22D, 0, 6, 0, 1, 2);

        TradeExecutionTemplate template =
                new DefaultTradeExecutionTemplateFactory().compile(facts);

        assertSame(facts, template.facts());
        assertNotNull(template.shanghaiOpeningAuctionBuy());
        assertNotNull(template.shenzhenOpeningAuctionBuy());
        assertNotNull(template.continuousBuy());
        assertNotNull(template.continuousSell());
        assertNotNull(template.averagePriceSell());
        assertNotNull(template.closingAuctionSell());
    }
}
