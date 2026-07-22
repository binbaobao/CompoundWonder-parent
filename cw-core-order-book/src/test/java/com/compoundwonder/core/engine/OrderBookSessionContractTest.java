package com.compoundwonder.core.engine;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.constant.MarketEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookSessionContractTest {

    @Test
    void sessionOwnsStaticFactsAndExecutionStateOutsideHotOrderBook() {
        MarketSessionSpec spec = new MarketSessionSpec(
                "600000", "浦发银行", MarketEnum.SH, "2025-01-02",
                1_000_000L, 1_000, 1_100, 900);
        TradeStaticFacts facts = new TradeStaticFacts(
                2, 1, 500_000L, 50D, 100_000,
                18D, 20D, 22D, 0, 6, 0, 1, 2);
        TradeExecutionTemplate template = template(facts);

        OrderBook orderBook = new OrderBook(spec.limitUpPrice(), spec.limitDownPrice());
        TradeExecutionState executionState = new TradeExecutionState(1);
        OrderBookSession session = new OrderBookSession(
                spec, facts, orderBook, template, executionState);

        assertSame(orderBook, session.orderBook());
        assertSame(template, session.template());
        assertEquals("600000", session.getSymbol());
        assertEquals(2, session.getTradeMode());
        assertEquals(1, session.getLbcs());
        assertEquals(100_000, session.getInitialMarketValue());
        assertEquals(1, session.executionState().transactionStatus());

        session.executionState().transactionStatus(2);
        assertEquals(2, session.executionState().transactionStatus());
    }

    @Test
    void executionStateTracksShanghaiAuctionSnapshotsWithoutPollutingOrderBook() {
        TradeExecutionState state = new TradeExecutionState(-1);

        assertEquals(-1L, state.recordShanghaiAuctionBuyVolume(20_000L));
        assertEquals(20_000L, state.recordShanghaiAuctionBuyVolume(30_000L));
        assertEquals(-1, state.transactionStatus());
    }

    @Test
    void oneMarketBookHostsIndependentStrategySessions() {
        MarketSessionSpec spec = new MarketSessionSpec(
                "600000", "浦发银行", MarketEnum.SH, "2025-01-02",
                1_000_000L, 1_000, 1_100, 900);
        OrderBook orderBook = new OrderBook(spec.limitUpPrice(), spec.limitDownPrice());
        OrderBookSession marketSession = new OrderBookSession(spec, orderBook);

        TradeStaticFacts modelOneFacts = facts(1, 2, 180_000);
        TradeStaticFacts modelTwoFacts = facts(2, 1, 180_000);
        StrategyExecutionSession modelOne = new StrategyExecutionSession(
                new StrategySessionKey("run-45:model-1:600000:2025-01-02",
                        "MODEL_1", "600000", "2025-01-02"),
                marketSession, modelOneFacts, template(modelOneFacts),
                new TradeExecutionState(1));
        StrategyExecutionSession modelTwo = new StrategyExecutionSession(
                new StrategySessionKey("run-46:model-2:600000:2025-01-02",
                        "MODEL_2", "600000", "2025-01-02"),
                marketSession, modelTwoFacts, template(modelTwoFacts),
                new TradeExecutionState(-1));

        marketSession.registerStrategy(modelOne);
        marketSession.registerStrategy(modelTwo);

        assertEquals(2, marketSession.strategySessions().size());
        assertSame(orderBook, modelOne.orderBook());
        assertSame(orderBook, modelTwo.orderBook());
        assertEquals(1, modelOne.getTradeMode());
        assertEquals(2, modelTwo.getTradeMode());

        modelOne.executionState().beginBuyOrder();
        assertTrue(modelOne.executionState().isBuyOrderPending());
        assertFalse(modelTwo.executionState().isBuyOrderPending());
        assertTrue(modelTwo.executionState().isSellMonitoring());
    }

    @Test
    void executionStateSeparatesBuySellAndPositionLifecycles() {
        TradeExecutionState state = new TradeExecutionState(1);

        assertTrue(state.isBuyMonitoring());
        assertFalse(state.isSellMonitoring());
        assertEquals(PositionState.FLAT, state.positionState());

        state.beginBuyOrder();
        assertEquals(BuyExecutionState.ORDER_PENDING, state.buyState());
        assertEquals(2, state.transactionStatus());

        state.beginSellMonitoring();
        assertEquals(BuyExecutionState.INACTIVE, state.buyState());
        assertEquals(SellExecutionState.MONITORING, state.sellState());
        assertEquals(PositionState.HELD, state.positionState());
        assertEquals(-1, state.transactionStatus());
    }

    private TradeStaticFacts facts(int tradeMode, int lbcs, int initialMarketValue) {
        return new TradeStaticFacts(
                tradeMode, lbcs, 500_000L, 50D, initialMarketValue,
                18D, 20D, 22D, 0, 6, 0, 1, 2);
    }

    private TradeExecutionTemplate template(TradeStaticFacts facts) {
        return new TradeExecutionTemplate() {
            @Override public TradeStaticFacts facts() { return facts; }
            @Override public ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy() { return null; }
            @Override public ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy() { return null; }
            @Override public ContinuousBuyExecutor continuousBuy() { return null; }
            @Override public ContinuousSellExecutor continuousSell() { return null; }
            @Override public AveragePriceSellExecutor averagePriceSell() { return null; }
            @Override public ClosingAuctionSellExecutor closingAuctionSell() { return null; }
        };
    }
}
