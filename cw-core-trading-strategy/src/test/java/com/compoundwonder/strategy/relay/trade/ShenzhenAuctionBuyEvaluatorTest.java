package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShenzhenAuctionBuyEvaluatorTest {

    @Test
    void rejectsAbsoluteStrengthEntryWhenAuctionSellVolumeIsBelowTwentyPercent() {
        CapturedRule record = new CapturedRule();

        boolean bought = ShenzhenAuctionBuyEvaluator.evaluateBuy(
                market(), event((byte) 2, (byte) 0, 0), 92_000_000,
                10_000_000L, 1_999_999L, record);

        assertFalse(bought);
    }

    @Test
    void keepsAbsoluteStrengthEntryAtTwentyPercentAuctionSellVolume() {
        CapturedRule record = new CapturedRule();

        boolean bought = ShenzhenAuctionBuyEvaluator.evaluateBuy(
                market(), event((byte) 2, (byte) 0, 0), 92_000_000,
                10_000_000L, 2_000_000L, record);

        assertTrue(bought);
        assertEquals(7, record.ruleCode);
    }

    @Test
    void rejectsLargeOrderEntryWhenAuctionSellVolumeIsBelowTwentyPercent() {
        CapturedRule record = new CapturedRule();

        boolean bought = ShenzhenAuctionBuyEvaluator.evaluateBuy(
                market(), event((byte) 1, (byte) 1, 888_800), 92_000_000,
                10_000_000L, 1_000_000L, record);

        assertFalse(bought);
    }

    @Test
    void keepsLargeOrderEntryAtTwentyPercentAuctionSellVolume() {
        CapturedRule record = new CapturedRule();

        boolean bought = ShenzhenAuctionBuyEvaluator.evaluateBuy(
                market(), event((byte) 1, (byte) 1, 888_800), 92_000_000,
                10_000_000L, 2_000_000L, record);

        assertTrue(bought);
        assertEquals(6, record.ruleCode);
    }

    private TradeMarketState market() {
        Map<String, Object> values = Map.of(
                "getSymbol", "002799",
                "getLimitUpPrice", 1_100,
                "getClosePrice", 1_000,
                "getCirculation", 100_000_000L,
                "getMaxVolume", 30_000_000L,
                "getInitialMarketValue", 120_000);
        return (TradeMarketState) Proxy.newProxyInstance(
                TradeMarketState.class.getClassLoader(),
                new Class<?>[]{TradeMarketState.class},
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value != null) return value;
                    return switch (method.getReturnType().getName()) {
                        case "int" -> 0;
                        case "long" -> 0L;
                        case "double" -> 0D;
                        case "boolean" -> false;
                        default -> null;
                    };
                });
    }

    private AuctionMarketEvent event(byte dataType, byte direction, int quantity) {
        return new AuctionMarketEvent() {
            @Override public byte getDataType() { return dataType; }
            @Override public byte getDirection() { return direction; }
            @Override public int getTime() { return 92_000_000; }
            @Override public int getPrice() { return 1_100; }
            @Override public int getQuantity() { return quantity; }
            @Override public int getOrderId() { return 123; }
            @Override public int getBuyerOrderId() { return 0; }
            @Override public int getSellerOrderId() { return 0; }
        };
    }

    private static final class CapturedRule implements TradeRuleRecord {
        private int ruleCode;

        @Override
        public void fill(int actionType, int ruleCode, String symbol, int time,
                         int price, double increase, String remark) {
            this.ruleCode = ruleCode;
        }
    }
}
