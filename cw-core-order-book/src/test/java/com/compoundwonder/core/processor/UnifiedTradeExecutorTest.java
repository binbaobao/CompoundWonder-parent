package com.compoundwonder.core.processor;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.MarketEnum;
import com.compoundwonder.core.engine.MarketSessionSpec;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.TradeExecutionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedTradeExecutorTest {

    @Test
    void disablesBuyMonitoringOnlyWhenItHasNeverTouchedLimitUpByTenOClock() {
        assertFalse(UnifiedTradeExecutor.shouldDisableUntouchedBuySessions(
                ConstantUtil.TIME_1000 - 1, 0));
        assertTrue(UnifiedTradeExecutor.shouldDisableUntouchedBuySessions(
                ConstantUtil.TIME_1000, 0));
        assertFalse(UnifiedTradeExecutor.shouldDisableUntouchedBuySessions(
                ConstantUtil.TIME_1000, 1));
        assertFalse(UnifiedTradeExecutor.shouldDisableUntouchedBuySessions(
                ConstantUtil.TIME_1000, 2));
        assertFalse(UnifiedTradeExecutor.shouldDisableUntouchedBuySessions(
                ConstantUtil.TIME_1000, 4));
    }

    @Test
    void disablesRelayBuyMonitoringThatNeverTouchedLimitUpByTenOClock() {
        TradeExecutionState state = new TradeExecutionState(1);
        OrderBookSession session = buyMonitoringSession(1, 2, state);

        new UnifiedTradeExecutor(null)
                .disableUntouchedBuySessionsAtTen(session, ConstantUtil.TIME_1000);

        assertFalse(state.isBuyMonitoring());
    }

    private static OrderBookSession buyMonitoringSession(
            int tradeMode, int lbcs, TradeExecutionState state) {
        MarketSessionSpec spec = new MarketSessionSpec(
                "600000", "浦发银行", MarketEnum.SH, "2025-01-02",
                100_000_000L, 1_000, 1_100, 900);
        TradeStaticFacts facts = new TradeStaticFacts(
                tradeMode, lbcs, 30_000_000L, 30D, 100_000,
                18D, 20D, 22D, 0, 6, 0, 1, 1);
        TradeExecutionTemplate template = new TradeExecutionTemplate() {
            @Override public TradeStaticFacts facts() { return facts; }
            @Override public ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy() { return null; }
            @Override public ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy() { return null; }
            @Override public ContinuousBuyExecutor continuousBuy() { return null; }
            @Override public ContinuousSellExecutor continuousSell() { return null; }
            @Override public AveragePriceSellExecutor averagePriceSell() { return null; }
            @Override public ClosingAuctionSellExecutor closingAuctionSell() { return null; }
        };
        return new OrderBookSession(
                spec, facts, new OrderBook(spec.limitUpPrice(), spec.limitDownPrice()),
                template, state);
    }
}
