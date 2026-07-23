package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.common.strategy.trade.TradeTriggerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotSame(template.shanghaiOpeningAuctionBuy(),
                template.shenzhenOpeningAuctionBuy());
        assertNotNull(template.continuousBuy());
        assertNotNull(template.continuousSell());
        assertNotNull(template.averagePriceSell());
        assertNotNull(template.closingAuctionSell());
        assertTrue(template.supports(TradeTriggerType.CONTINUOUS_TICK));
        assertSame(template.triggerTypes(), template.triggerTypes());
    }

    @Test
    void templateCarriesBoardAndAuctionExecutionConstraintsWithoutOverridingExchangeCapRules() {
        TradeStaticFacts acceleratedRelay = new TradeStaticFacts(
                1, 2, 1_000_000L, 35D, 90_000,
                18D, 20D, 14.99D, 0, 6, 0, 1, 2,
                4D, 22D, 6D);
        TradeExecutionTemplate relayTemplate =
                new DefaultTradeExecutionTemplateFactory().compile(acceleratedRelay);

        assertEquals(2, relayTemplate.executionProfile().previousBoardHeight());
        assertEquals(3, relayTemplate.executionProfile().targetBoardHeight());
        assertEquals("SMALL_CAP", relayTemplate.executionProfile().marketCapTier().name());
        assertFalse(relayTemplate.executionProfile().openingAuctionBuyAllowed());
        assertEquals(93_500_000,
                relayTemplate.executionProfile().earliestContinuousBuyTime());

        TradeStaticFacts normalFirstBoardAtSixteenBillion = new TradeStaticFacts(
                2, 1, 1_000_000L, 35D, 160_000,
                18D, 20D, 22D, 0, 6, 0, 1, 2,
                8D, 30D, 9D);
        TradeExecutionTemplate firstBoardTemplate =
                new DefaultTradeExecutionTemplateFactory().compile(normalFirstBoardAtSixteenBillion);

        assertTrue(firstBoardTemplate.executionProfile().openingAuctionBuyAllowed());
    }
}
