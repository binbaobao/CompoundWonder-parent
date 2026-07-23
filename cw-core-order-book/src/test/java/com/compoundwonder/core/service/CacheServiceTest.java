package com.compoundwonder.core.service;

import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.MarketSessionSpec;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.StrategyExecutionSession;
import com.compoundwonder.core.engine.StrategySessionKey;
import com.compoundwonder.core.engine.TradeExecutionState;
import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.constant.MarketEnum;
import com.compoundwonder.util.SymbolUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CacheServiceTest {

    @Test
    void updatesPreOpenPriceLimitsOnRegisteredOrderBook() {
        CacheService cacheService = new CacheService();
        int symbolId = SymbolUtil.fastSymbolToInt("600000");
        OrderBook orderBook = new OrderBook("600000", 1_000_000L, 10.00, 500_000L);
        orderBook.setTransactionStatus(1);
        cacheService.put(symbolId, orderBook);

        cacheService.updatePreOpenPriceLimits("600000", 9.50, 10.45, 8.55, "浦发银行");

        assertSame(orderBook, cacheService.get(symbolId).orderBook());
        assertEquals("浦发银行", orderBook.getSecurityName());
        assertEquals(950, orderBook.getClosePrice());
        assertEquals(1045, orderBook.getLimitUpPrice());
        assertEquals(855, orderBook.getLimitDownPrice());
        assertEquals(1, orderBook.getTransactionStatus());
    }

    @Test
    void qinLongCodesChecksEveryIndependentStrategySession() {
        String symbol = "600000";
        int symbolId = SymbolUtil.fastSymbolToInt(symbol);
        MarketSessionSpec spec = new MarketSessionSpec(
                symbol, "浦发银行", MarketEnum.SH, "2025-01-02",
                1_000_000L, 1_000, 1_100, 900);
        OrderBookSession marketSession = new OrderBookSession(
                spec, new OrderBook(spec.limitUpPrice(), spec.limitDownPrice()));
        TradeStaticFacts firstBoardFacts = facts(2, 1);
        TradeStaticFacts relayFacts = facts(1, 2);
        marketSession.registerStrategy(new StrategyExecutionSession(
                new StrategySessionKey("model-2", "MODEL_2", symbol, "2025-01-02"),
                marketSession, firstBoardFacts, template(firstBoardFacts),
                new TradeExecutionState(-1)));
        marketSession.registerStrategy(new StrategyExecutionSession(
                new StrategySessionKey("model-1", "MODEL_1", symbol, "2025-01-02"),
                marketSession, relayFacts, template(relayFacts),
                new TradeExecutionState(1)));
        CacheService cacheService = new CacheService();
        cacheService.put(symbolId, marketSession);

        assertEquals(List.of(symbol), cacheService.getQinLongCodes());
    }

    private TradeStaticFacts facts(int tradeMode, int lbcs) {
        return new TradeStaticFacts(
                tradeMode, lbcs, 500_000L, 50D, 180_000,
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
