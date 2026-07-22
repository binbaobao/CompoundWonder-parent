package com.compoundwonder.core.service;

import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.util.SymbolUtil;
import org.junit.jupiter.api.Test;

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
}
