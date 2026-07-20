package com.compoundwonder.backtest.orderbook.strategy;

import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.constant.ConstantUtil;
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
                    orderBook, shanghaiBuyEvent, 0, 91_900_000, shanghaiBuy));
            assertEquals(RuleConstant.TRADING_MODE_BUY, shanghaiBuy.actionType);
            assertEquals(2, shanghaiBuy.ruleCode);
            assertTrue(shanghaiBuy.remark.contains("上海早盘竞价封单绝对强度"));

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
                    true, 15_000_000, 500, shenzhenLargeOrderBuy));
            assertEquals(6, shenzhenLargeOrderBuy.ruleCode);
            assertTrue(shenzhenLargeOrderBuy.remark.contains("涨停大单"));

            RuleRecord shenzhenVolumeBuy = new RuleRecord();
            TickData shenzhenVolumeEvent = event((byte) 2, 91_900_000,
                    limitUpPrice, 1, 12_346, 5_001, 500);
            assertTrue(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook, shenzhenVolumeEvent, 91_900_000,
                    false, 5_001, 500, shenzhenVolumeBuy));
            assertEquals(7, shenzhenVolumeBuy.ruleCode);
            assertTrue(shenzhenVolumeBuy.remark.contains("封单绝对强度"));

            RuleRecord shenzhenSnapshotCancel = new RuleRecord();
            TickData shenzhenSnapshotCancelEvent = event((byte) 4, 91_956_500,
                    limitUpPrice - 1, 0, 0, 0, 0);
            assertTrue(dispatcher.evaluateShenzhenSnapshotAuctionCancel(
                    orderBook, shenzhenSnapshotCancelEvent, 91_956_500,
                    10_000, 1_000, shenzhenSnapshotCancel));
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
    void shanghaiAuctionSupportsAbsoluteStrengthAndSnapshotGrowthBelowTwoBillion() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook("600001", 100_000_000L, 10.00, 20_000_000L);
            orderBook.setTradeMode(tradeMode);
            orderBook.setInitialMarketValue(199_999);
            int limitUpPrice = orderBook.getLimitUpPrice();

            // 绝对强度阈值为 min(流通股 5%=500万股, 最大成交量 20%=400万股)。
            RuleRecord absoluteStrength = new RuleRecord();
            assertTrue(dispatcher.evaluateShanghaiAuctionBuy(
                    orderBook,
                    event((byte) 4, 91_900_000, limitUpPrice,
                            0, 0, 4_000_001, 1_600_000),
                    3_000_000, 91_900_000, absoluteStrength));
            assertEquals(2, absoluteStrength.ruleCode);

            // 买量从 200万增加到 360万：增量超过流通股 1.5%，且当前买量超过流通股 3%。
            RuleRecord snapshotGrowth = new RuleRecord();
            assertTrue(dispatcher.evaluateShanghaiAuctionBuy(
                    orderBook,
                    event((byte) 4, 91_903_000, limitUpPrice,
                            0, 0, 3_600_000, 2_000_000),
                    2_000_000, 91_903_000, snapshotGrowth));
            assertEquals(3, snapshotGrowth.ruleCode);

            // 上一张快照买量为 0 也是有效基准；只用 -1 表示本交易日尚无上一张快照。
            RuleRecord growthFromZero = new RuleRecord();
            assertTrue(dispatcher.evaluateShanghaiAuctionBuy(
                    orderBook,
                    event((byte) 4, 91_906_000, limitUpPrice,
                            0, 0, 3_100_000, 2_000_000),
                    0, 91_906_000, growthFromZero));
            assertEquals(3, growthFromZero.ruleCode);
        }
    }

    @Test
    void shanghaiAuctionUsesStrictBoundariesAndRejectsTwoBillionStartupValue() {
        OrderBook orderBook = new OrderBook("600001", 100_000_000L, 10.00, 20_000_000L);
        orderBook.setTradeMode(1);
        orderBook.setInitialMarketValue(199_999);
        int limitUpPrice = orderBook.getLimitUpPrice();

        // 买量等于最低阈值不算“超过”。
        assertFalse(dispatcher.evaluateShanghaiAuctionBuy(
                orderBook,
                event((byte) 4, 91_900_000, limitUpPrice,
                        0, 0, 4_000_000, 1_000_000),
                4_000_000, 91_900_000, new RuleRecord()));

        // 已撮合卖量等于买量 40% 不算“小于 40%”。
        assertFalse(dispatcher.evaluateShanghaiAuctionBuy(
                orderBook,
                event((byte) 4, 91_900_000, limitUpPrice,
                        0, 0, 5_000_000, 2_000_000),
                5_000_000, 91_900_000, new RuleRecord()));

        // 启动市值达到 20 亿元时，两种竞价买入规则都不能执行。
        orderBook.setInitialMarketValue(200_000);
        assertFalse(dispatcher.evaluateShanghaiAuctionBuy(
                orderBook,
                event((byte) 4, 91_900_000, limitUpPrice,
                        0, 0, 8_000_000, 100_000),
                1_000_000, 91_900_000, new RuleRecord()));
    }

    @Test
    void shanghaiAuctionCancelsWhenPriceLeavesLimitUpOrAbsoluteStrengthFails() {
        OrderBook orderBook = new OrderBook("600001", 100_000_000L, 10.00, 20_000_000L);
        orderBook.setTradeMode(1);
        orderBook.setInitialMarketValue(199_999);
        int limitUpPrice = orderBook.getLimitUpPrice();

        RuleRecord priceCancel = new RuleRecord();
        assertTrue(dispatcher.evaluateShanghaiAuctionCancel(
                orderBook,
                event((byte) 4, 91_956_500, limitUpPrice - 1,
                        0, 0, 8_000_000, 100_000),
                91_956_500, priceCancel));
        assertEquals(1, priceCancel.ruleCode);

        RuleRecord strengthCancel = new RuleRecord();
        assertTrue(dispatcher.evaluateShanghaiAuctionCancel(
                orderBook,
                event((byte) 4, 91_956_500, limitUpPrice,
                        0, 0, 4_000_000, 1_000_000),
                91_956_500, strengthCancel));
        assertEquals(2, strengthCancel.ruleCode);

        assertFalse(dispatcher.evaluateShanghaiAuctionCancel(
                orderBook,
                event((byte) 4, 91_956_500, limitUpPrice,
                        0, 0, 4_000_001, 1_000_000),
                91_956_500, new RuleRecord()));
    }

    @Test
    void shenzhenAuctionUsesShanghaiAbsoluteStrengthFormulaBelowTwoBillion() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook(
                    "000001", 100_000_000L, 10.00, 20_000_000L);
            orderBook.setTradeMode(tradeMode);
            orderBook.setInitialMarketValue(199_999);
            int limitUpPrice = orderBook.getLimitUpPrice();

            // 最低要求取 min(流通股本 5%=500万股, 最大成交量 20%=400万股)。
            RuleRecord absoluteStrength = new RuleRecord();
            assertTrue(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 2, 91_900_000, limitUpPrice,
                            0, 12_348, 0, 0),
                    91_900_000, false, 4_000_001, 1_599_999,
                    absoluteStrength));
            assertEquals(7, absoluteStrength.ruleCode);

            // 买量等于最低要求，或者全部卖量等于买量 40%，均不满足严格边界。
            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 2, 91_900_000, limitUpPrice,
                            0, 12_349, 0, 0),
                    91_900_000, false, 4_000_000, 1_000_000,
                    new RuleRecord()));
            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 2, 91_900_000, limitUpPrice,
                            0, 12_350, 0, 0),
                    91_900_000, false, 5_000_000, 2_000_000,
                    new RuleRecord()));

            // 启动流通市值达到 20 亿元时，两种深圳集合竞价买入都不执行。
            orderBook.setInitialMarketValue(200_000);
            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 2, 91_900_000, limitUpPrice,
                            0, 12_351, 0, 0),
                    91_900_000, false, 8_000_000, 100_000,
                    new RuleRecord()));

            // 低价股大单分档本身不限制市值，仍必须被深圳竞价统一的 20 亿元硬门槛挡住。
            OrderBook lowPriceOrderBook = new OrderBook(
                    "000001", 100_000_000L, 8.00, 20_000_000L);
            lowPriceOrderBook.setTradeMode(tradeMode);
            lowPriceOrderBook.setInitialMarketValue(200_000);
            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    lowPriceOrderBook,
                    event((byte) 1, 91_900_000,
                            lowPriceOrderBook.getLimitUpPrice(),
                            888_800, 12_352, 0, 0),
                    91_900_000, true, 888_800, 888_799,
                    new RuleRecord()));
        }
    }

    @Test
    void shenzhenLargeOrderIsIndependentFromAbsoluteStrengthButStillRequiresLimitUpPrice() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook(
                    "000001", 100_000_000L, 10.00, 20_000_000L);
            orderBook.setTradeMode(tradeMode);
            orderBook.setInitialMarketValue(149_999);
            int limitUpPrice = orderBook.getLimitUpPrice();

            // 卖量接近涨停买量，绝对强度不成立；有效涨停价大单仍是独立买入条件。
            RuleRecord largeOrder = new RuleRecord();
            assertTrue(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 1, 91_900_000, limitUpPrice,
                            900_001, 12_352, 0, 0),
                    91_900_000, true, 900_001, 900_000,
                    largeOrder));
            assertEquals(6, largeOrder.ruleCode);

            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 1, 91_900_000, limitUpPrice - 1,
                            900_001, 12_353, 0, 0),
                    91_900_000, false, 900_001, 900_000,
                    new RuleRecord()));

            // 09:25 批量到达的是集合竞价结果，不能再产生竞价买入信号。
            assertFalse(dispatcher.evaluateShenzhenAuctionBuy(
                    orderBook,
                    event((byte) 1, ConstantUtil.TIME_925, limitUpPrice,
                            900_001, 12_354, 0, 0),
                    ConstantUtil.TIME_925, true, 900_001, 100,
                    new RuleRecord()));
        }
    }

    @Test
    void shenzhenOrderBookAndSnapshotCancelUseExactlyTheSameAbsoluteStrengthFormulaAsBuy() {
        for (int tradeMode = 1; tradeMode <= 3; tradeMode++) {
            OrderBook orderBook = new OrderBook(
                    "000001", 100_000_000L, 10.00, 20_000_000L);
            orderBook.setTradeMode(tradeMode);
            orderBook.setInitialMarketValue(199_999);
            int limitUpPrice = orderBook.getLimitUpPrice();
            TickData snapshot = event((byte) 4, 91_956_500,
                    limitUpPrice, 0, 0, 0, 0);
            TickData orderBookEvent = event((byte) 2, 91_956_500,
                    limitUpPrice, 0, 0, 0, 0);

            RuleRecord weakStrength = new RuleRecord();
            assertTrue(dispatcher.evaluateShenzhenSnapshotAuctionCancel(
                    orderBook, snapshot, 91_956_500,
                    4_000_000, 1_000_000, weakStrength));
            assertEquals(2, weakStrength.ruleCode);

            RuleRecord orderBookWeakStrength = new RuleRecord();
            assertTrue(dispatcher.evaluateShenzhenAuctionCancel(
                    orderBook, orderBookEvent, 91_956_500,
                    4_000_000, 1_000_000, orderBookWeakStrength));
            assertEquals(2, orderBookWeakStrength.ruleCode);
            assertTrue(orderBookWeakStrength.remark.contains("逐笔订单簿"));

            // 与买入完全相同的严格强度边界成立时，两种触发源都不能产生撤单。
            assertFalse(dispatcher.evaluateShenzhenSnapshotAuctionCancel(
                    orderBook, snapshot, 91_956_500,
                    4_000_001, 1_599_999, new RuleRecord()));
            assertFalse(dispatcher.evaluateShenzhenAuctionCancel(
                    orderBook, orderBookEvent, 91_956_500,
                    4_000_001, 1_599_999, new RuleRecord()));
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
