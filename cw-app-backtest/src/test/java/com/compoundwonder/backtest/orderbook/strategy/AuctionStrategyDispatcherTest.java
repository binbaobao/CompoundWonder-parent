package com.compoundwonder.backtest.orderbook.strategy;

import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionStrategyDispatcherTest {

    private final TradeStrategyDispatcher dispatcher = new TradeStrategyDispatcher();

    @Test
    void dispatchesEveryAuctionRuleThroughAllThreeIndependentModes() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook("000001", 100_000L, 10.00, 50_000L);
            orderBook.setTradeMode(tradeMode);
            orderBook.setInitialMarketValue(100_000);
            int limitUpPrice = orderBook.getLimitUpPrice();

            RuleRecord shanghaiBuy = new RuleRecord();
            TickData shanghaiBuyEvent = event((byte) 4, 91_900_000, limitUpPrice,
                    0, 0, 10_000, 1_000);
            assertTrue(dispatcher.evaluateShanghaiAuctionBuy(
                    orderBook, shanghaiBuyEvent, 91_900_000, shanghaiBuy));
            assertEquals(RuleConstant.TRADING_MODE_BUY, shanghaiBuy.actionType);
            assertEquals(2, shanghaiBuy.ruleCode);
            assertTrue(shanghaiBuy.remark.contains("买入 - 上午早盘竞价"));

            RuleRecord shanghaiPriceCancel = new RuleRecord();
            TickData shanghaiPriceCancelEvent = event((byte) 4, 91_956_500,
                    limitUpPrice - 1, 0, 0, 10_000, 1_000);
            assertTrue(dispatcher.evaluateShanghaiAuctionCancel(
                    orderBook, shanghaiPriceCancelEvent, 91_956_500,
                    shanghaiPriceCancel));
            assertEquals(RuleConstant.TRADING_MODE_CANCEL, shanghaiPriceCancel.actionType);
            assertEquals(1, shanghaiPriceCancel.ruleCode);
            assertTrue(shanghaiPriceCancel.remark.contains("不等于涨停价"));

            RuleRecord shanghaiVolumeCancel = new RuleRecord();
            TickData shanghaiVolumeCancelEvent = event((byte) 4, 91_956_500,
                    limitUpPrice, 0, 0, 5_000, 1_000);
            assertTrue(dispatcher.evaluateShanghaiAuctionCancel(
                    orderBook, shanghaiVolumeCancelEvent, 91_956_500,
                    shanghaiVolumeCancel));
            assertEquals(2, shanghaiVolumeCancel.ruleCode);

            OrderBook largeOrderBook = new OrderBook("000001", 100_000_000L, 10.00, 50_000_000L);
            largeOrderBook.setTradeMode(tradeMode);
            largeOrderBook.setInitialMarketValue(100_000);
            RuleRecord shenzhenLargeOrderBuy = new RuleRecord();
            TickData shenzhenLargeOrderEvent = event((byte) 1, 91_900_000,
                    limitUpPrice, 900_001, 12_345, 15_000_000, 500);
            assertTrue(dispatcher.evaluateShenzhenAuctionBuy(
                    largeOrderBook, shenzhenLargeOrderEvent, 91_900_000,
                    15_000_000, 500, shenzhenLargeOrderBuy));
            assertEquals(6, shenzhenLargeOrderBuy.ruleCode);
            assertTrue(shenzhenLargeOrderBuy.remark.contains("大单买"));

            RuleRecord shenzhenVolumeBuy = new RuleRecord();
            TickData shenzhenVolumeEvent = event((byte) 2, 91_900_000,
                    limitUpPrice, 1, 12_346, 5_001, 500);
            assertTrue(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook, shenzhenVolumeEvent, 91_900_000,
                    5_001, 500, shenzhenVolumeBuy));
            assertEquals(7, shenzhenVolumeBuy.ruleCode);
            assertTrue(shenzhenVolumeBuy.remark.contains("总买超过"));

            RuleRecord shenzhenOrderCancel = new RuleRecord();
            TickData shenzhenOrderCancelEvent = event((byte) 1, 91_952_000,
                    limitUpPrice, 1, 12_347, 4_000, 500);
            assertTrue(dispatcher.evaluateShenzhenAuctionCancel(
                    orderBook, shenzhenOrderCancelEvent, 91_952_000,
                    4_000, 500, shenzhenOrderCancel));
            assertEquals(2, shenzhenOrderCancel.ruleCode);

            RuleRecord shenzhenSnapshotCancel = new RuleRecord();
            TickData shenzhenSnapshotCancelEvent = event((byte) 4, 91_956_500,
                    limitUpPrice - 1, 0, 0, 0, 0);
            assertTrue(dispatcher.evaluateShenzhenSnapshotAuctionCancel(
                    orderBook, shenzhenSnapshotCancelEvent, 91_956_500,
                    shenzhenSnapshotCancel));
            assertEquals(1, shenzhenSnapshotCancel.ruleCode);

            RuleRecord shanghaiClosingSell = new RuleRecord();
            TickData shanghaiClosingEvent = event((byte) 4, 145_959_000,
                    limitUpPrice - 1, 0, 0, 10_000, 1_000);
            assertTrue(dispatcher.evaluateShanghaiClosingAuctionSell(
                    orderBook, shanghaiClosingEvent, 145_959_000,
                    shanghaiClosingSell));
            assertEquals(RuleConstant.TRADING_MODE_SELL, shanghaiClosingSell.actionType);
            assertEquals(1, shanghaiClosingSell.ruleCode);
            assertTrue(shanghaiClosingSell.remark.startsWith("卖出 - 尾盘"));

            RuleRecord shenzhenClosingSell = new RuleRecord();
            TickData shenzhenClosingEvent = event((byte) 4, 145_959_000,
                    limitUpPrice, 0, 0, 1_000, 2_000);
            assertTrue(dispatcher.evaluateShenzhenClosingAuctionSell(
                    orderBook, shenzhenClosingEvent, 145_959_000,
                    shenzhenClosingSell));
            assertEquals(1, shenzhenClosingSell.ruleCode);
            assertTrue(shenzhenClosingSell.remark.startsWith("尾盘"));

            OrderBook controlBook = new OrderBook("000001", 100_000L, 10.00, 50_000L);
            controlBook.setTradeMode(tradeMode);
            controlBook.setLbcs(2);
            controlBook.updatePrice(0, 0, 1_050, 93_100_000);
            controlBook.updatePrice(0, 0, 990, 93_800_000);
            assertTrue(dispatcher.shouldEnableFirstBoardTradingMode(controlBook));
            assertTrue(dispatcher.isContinuousBuyTimeAllowed(controlBook, 142_959_999));
            assertFalse(dispatcher.isContinuousBuyTimeAllowed(controlBook, 143_000_000));
        }
    }

    @Test
    void preservesShenzhenLargeMarketValueSellerOrderIdThreshold() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook("000001", 100_000L, 10.00, 50_000L);
            orderBook.setTradeMode(tradeMode);
            orderBook.setInitialMarketValue(120_000);

            TickData transaction = event((byte) 2, 91_900_000,
                    orderBook.getLimitUpPrice(), 1, 12_348, 1, 20_000);

            // 原 Handler 的大市值分支使用当前事件 sellerOrderId，而不是累计卖量 0。
            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook, transaction, 91_900_000,
                    10_000, 0, new RuleRecord()));
        }
    }

    private TickData event(byte dataType, int time, int price, int quantity,
                           int orderId, int buyerOrderId, int sellerOrderId) {
        TickData event = new TickData();
        event.dataType = dataType;
        event.time = time;
        event.price = price;
        event.quantity = quantity;
        event.orderId = orderId;
        event.buyerOrderId = buyerOrderId;
        event.sellerOrderId = sellerOrderId;
        return event;
    }
}
