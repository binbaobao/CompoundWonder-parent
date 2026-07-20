package com.compoundwonder.backtest.orderbook.data.clickhouse;

import com.compoundwonder.core.engine.TickData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClickHouseBacktestTickDataSourceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 7, 16);

    @Test
    void mapsShanghaiAddCancelAndIgnoresStatusRows() {
        TickData add = ClickHouseBacktestTickDataSource.toOrderTick(
                order("A", "1", 10.36F, 20_000, 101), 1_600_876, true);
        TickData cancel = ClickHouseBacktestTickDataSource.toOrderTick(
                order("D", "2", 10.36F, 5_000, 102), 1_600_876, true);
        TickData status = ClickHouseBacktestTickDataSource.toOrderTick(
                order("S", "0", 0F, 0, 0), 1_600_876, true);

        assertEquals(1, add.dataType);
        assertEquals(2, add.type);
        assertEquals(1, add.direction);
        assertEquals(1_036, add.price);
        assertEquals(20_000, add.quantity);
        assertEquals(101, add.orderId);
        assertEquals(10, cancel.type);
        assertEquals(2, cancel.direction);
        assertNull(status);
    }

    @Test
    void mapsShenzhenOrderTypesAndConvertsOrderZeroToCancellation() {
        TickData limitOrder = ClickHouseBacktestTickDataSource.toOrderTick(
                order("1", "1", 10.59F, 400, 59_697_075), 1_002_632, false);
        TickData marketOrder = ClickHouseBacktestTickDataSource.toOrderTick(
                order("2", "1", 0F, 30_000, 201), 1_001_388, false);
        TickData ownBestOrder = ClickHouseBacktestTickDataSource.toOrderTick(
                order("3", "2", 0F, 500, 202), 1_001_388, false);
        TickData buyCancel = ClickHouseBacktestTickDataSource.toOrderTick(
                order("0", "1", 0F, 400, 59_697_075), 1_002_632, false);
        TickData sellCancel = ClickHouseBacktestTickDataSource.toOrderTick(
                order("0", "2", 0F, 500, 59_697_076), 1_002_632, false);

        assertEquals(1, limitOrder.dataType);
        assertEquals(1, limitOrder.type);
        assertEquals(1_059, limitOrder.price);
        assertEquals(2, marketOrder.type);
        assertEquals(0, marketOrder.price);
        assertEquals(3, ownBestOrder.type);
        assertEquals(2, ownBestOrder.direction);
        assertEquals(2, buyCancel.dataType);
        assertEquals(1, buyCancel.type);
        assertEquals(1, buyCancel.direction);
        assertEquals(59_697_075, buyCancel.orderId);
        assertEquals(59_697_075, buyCancel.buyerOrderId);
        assertEquals(0, buyCancel.sellerOrderId);
        assertEquals(400, buyCancel.quantity);
        assertEquals(2, sellCancel.dataType);
        assertEquals(1, sellCancel.type);
        assertEquals(2, sellCancel.direction);
        assertEquals(0, sellCancel.buyerOrderId);
        assertEquals(59_697_076, sellCancel.sellerOrderId);
    }

    @Test
    void mapsShenzhenTransactionTradeAndCancellationTypes() {
        TickData trade = ClickHouseBacktestTickDataSource.toTransactionTick(
                trans("1", 8.25F, 2_000, 301, 302), 1_001_388, false);
        TickData cancel = ClickHouseBacktestTickDataSource.toTransactionTick(
                trans("0", 0F, 1_000, 0, 302), 1_001_388, false);

        assertEquals(2, trade.dataType);
        assertEquals(0, trade.type);
        assertEquals(1, trade.direction);
        assertEquals(16_500, trade.amount);
        assertEquals(301, trade.orderId);
        assertEquals(1, cancel.type);
        assertEquals(2, cancel.direction);
        assertEquals(302, cancel.orderId);
    }

    @Test
    void mapsMarketSnapshotsWithAuctionAndContinuousFieldSemantics() {
        TickData auction = ClickHouseBacktestTickDataSource.toMarketTick(
                market(91_501_000L, 12.00F, 11.99F, 800_000_000L, 9_000_000L,
                        new long[]{100, 200}, new long[]{300, 400}),
                1_600_876);
        TickData continuous = ClickHouseBacktestTickDataSource.toMarketTick(
                market(93_001_000L, 12.00F, 11.99F, 3_000_000_000L, 9_000_000L,
                        new long[]{100, 200}, new long[]{300, 400}),
                1_600_876);

        assertEquals(1_200, auction.price);
        assertEquals(0, auction.orderId);
        assertEquals(700, auction.buyerOrderId);
        assertEquals(300, auction.sellerOrderId);
        assertEquals(1_199, continuous.price);
        assertEquals(1, continuous.type);
        assertEquals(30_000_000, continuous.orderId);
        assertEquals(300, continuous.buyerOrderId);
        assertEquals(9_000_000, continuous.sellerOrderId);
    }

    @Test
    void replaysDeterministicallyByTimeThenOrderTradeSnapshot() {
        ClickHouseLevel2QueryService queryService = new StubQueryService(
                List.of(orderAt(93_000_001, "A", "1", 1)),
                List.of(transAt(93_000_001, "T", 10F, 1, 1, 2)),
                List.of(market(93_000_001L, 10F, 9.99F, 1, 1,
                        new long[]{1}, new long[]{1})));

        List<Byte> dataTypes = new ArrayList<>();
        long count = new ClickHouseBacktestTickDataSource(queryService)
                .replay(DATE, "603567", tick -> dataTypes.add(tick.dataType));

        assertEquals(3, count);
        assertEquals(List.of((byte) 1, (byte) 2, (byte) 4), dataTypes);
    }

    @Test
    void replaysShenzhenLimitOrderFollowedByOrderStreamCancellation() {
        ClickHouseLevel2QueryService queryService = new StubQueryService(
                List.of(
                        orderAt(145_559_350, "1", "1", 59_697_075, 10.59F, 400),
                        orderAt(145_655_210, "0", "1", 59_697_075, 0F, 400)),
                List.of(), List.of());

        List<TickData> ticks = new ArrayList<>();
        long count = new ClickHouseBacktestTickDataSource(queryService)
                .replay(DATE, "002632", ticks::add);

        assertEquals(2, count);
        assertEquals(1, ticks.get(0).dataType);
        assertEquals(1, ticks.get(0).type);
        assertEquals(59_697_075, ticks.get(0).orderId);
        assertEquals(2, ticks.get(1).dataType);
        assertEquals(1, ticks.get(1).type);
        assertEquals(59_697_075, ticks.get(1).buyerOrderId);
    }

    @Test
    void singleReplayDropsEveryTickWhenShenzhenCancellationCannotIdentifyOrder() {
        ClickHouseLevel2QueryService queryService = new StubQueryService(
                List.of(
                        orderAt(91_900_000, "1", "1", 101, 14.06F, 10_000),
                        orderAt(91_939_010, "0", "0", 0, 0F, 9_000)),
                List.of(transAt(93_100_000, "1", 14.06F, 100, 101, 201)),
                List.of(market(91_948_000L, 14.05F, 14.05F, 0, 0,
                        new long[]{100}, new long[]{100})));

        List<TickData> ticks = new ArrayList<>();
        long count = new ClickHouseBacktestTickDataSource(queryService)
                .replay(DATE, "002883", ticks::add);

        assertEquals(0, count);
        assertEquals(List.of(), ticks);
    }

    @Test
    void loadsAllDailySymbolsOnceAndReusesTheirTicksAcrossMultipleReplays() {
        StubQueryService queryService = new StubQueryService(List.of(
                batchOrder("002632", 145_559_350, "1", "1", 10.59F, 400, 59_697_075),
                batchOrder("002632", 145_655_210, "0", "1", 0F, 400, 59_697_075),
                batchOrder("603567", 93_000_001, "A", "1", 10F, 100, 101),
                batchTransaction("603567", 93_000_001, "T", 10F, 100, 101, 102),
                batchMarket("603567", 93_000_001, 10F, 9.99F,
                        1_000, 100, 10, 20, 30, 40)));
        ClickHouseBacktestTickDataSource source = new ClickHouseBacktestTickDataSource(queryService);

        var batch = source.loadDay(DATE, List.of("603567", "002632", "603567"));
        List<TickData> shanghaiFirstReplay = new ArrayList<>();
        List<TickData> shanghaiSecondReplay = new ArrayList<>();
        List<TickData> shenzhenReplay = new ArrayList<>();

        assertEquals(3, batch.replay("603567", shanghaiFirstReplay::add));
        assertEquals(3, batch.replay("603567", shanghaiSecondReplay::add));
        assertEquals(2, batch.replay("002632", shenzhenReplay::add));
        assertEquals(1, queryService.dailyQueryCount);
        assertEquals(List.of("002632", "603567"), queryService.requestedSymbols);
        assertEquals(List.of((byte) 1, (byte) 2, (byte) 4),
                shanghaiFirstReplay.stream().map(tick -> tick.dataType).toList());
        assertEquals(shanghaiFirstReplay.size(), shanghaiSecondReplay.size());
        assertEquals(1, shenzhenReplay.get(0).dataType);
        assertEquals(2, shenzhenReplay.get(1).dataType);
        assertEquals(59_697_075, shenzhenReplay.get(1).buyerOrderId);
    }

    @Test
    void dailyBatchDropsOnlyTheShenzhenSymbolWithUnidentifiableCancellation() {
        StubQueryService queryService = new StubQueryService(List.of(
                batchOrder("002883", 91_900_000, "1", "1", 14.06F, 10_000, 101),
                batchOrder("002883", 91_939_010, "0", "0", 0F, 9_000, 0),
                batchMarket("002883", 91_948_000, 14.05F, 14.05F,
                        0, 0, 100, 0, 100, 0),
                batchOrder("000001", 91_900_000, "1", "1", 10F, 100, 201),
                batchMarket("000001", 91_948_000, 10F, 10F,
                        0, 0, 100, 0, 100, 0)));
        ClickHouseBacktestTickDataSource source =
                new ClickHouseBacktestTickDataSource(queryService);

        var batch = source.loadDay(DATE, List.of("002883", "000001"));
        List<TickData> invalidSymbolTicks = new ArrayList<>();
        List<TickData> validSymbolTicks = new ArrayList<>();

        assertEquals(0, batch.replay("002883", invalidSymbolTicks::add));
        assertEquals(List.of(), invalidSymbolTicks);
        assertEquals(2, batch.replay("000001", validSymbolTicks::add));
    }

    private static ClickHouseOrderRow order(String tickType, String side, float price,
                                             long volume, long no) {
        return orderAt(93_000_001, tickType, side, no, price, volume);
    }

    private static ClickHouseOrderRow orderAt(int time, String tickType, String side, long no) {
        return orderAt(time, tickType, side, no, 10F, 100);
    }

    private static ClickHouseOrderRow orderAt(int time, String tickType, String side, long no,
                                               float price, long volume) {
        return new ClickHouseOrderRow("603567", DATE, time, timestamp(time),
                tickType, side, price, volume, no);
    }

    private static ClickHouseTransRow trans(String tickType, float price, long volume,
                                             long buyNo, long sellNo) {
        return transAt(93_000_001, tickType, price, volume, buyNo, sellNo);
    }

    private static ClickHouseTransRow transAt(int time, String tickType, float price,
                                               long volume, long buyNo, long sellNo) {
        return new ClickHouseTransRow("603567", DATE, time, timestamp(time),
                tickType, price, volume, buyNo, sellNo);
    }

    private static ClickHouseMarketRow market(long time, float ask1, float bid1,
                                               long totalValue, long totalVolume,
                                               long[] askVolumes, long[] bidVolumes) {
        return new ClickHouseMarketRow("603567", "1", DATE, time, timestamp((int) time),
                bid1, 9F, 9F, 10F, 9F, 0F, bid1, ask1,
                totalVolume, totalValue, 0, 0, 0,
                new float[]{ask1}, new float[]{bid1}, askVolumes, bidVolumes);
    }

    private static ClickHouseLevel2BatchRow batchOrder(
            String symbol, int time, String tickType, String side,
            float price, long volume, long orderNo) {
        return new ClickHouseLevel2BatchRow(symbol, time,
                ClickHouseLevel2BatchRow.EVENT_ORDER, tickType, side, price, volume,
                orderNo, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private static ClickHouseLevel2BatchRow batchTransaction(
            String symbol, int time, String tickType, float price,
            long volume, long buyNo, long sellNo) {
        return new ClickHouseLevel2BatchRow(symbol, time,
                ClickHouseLevel2BatchRow.EVENT_TRANSACTION, tickType, "", price, volume,
                0, buyNo, sellNo, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private static ClickHouseLevel2BatchRow batchMarket(
            String symbol, int time, float askPrice1, float bidPrice1,
            long totalValue, long totalVolume, long askVolume1, long askVolume2,
            long bidVolume1, long bidVolume2) {
        return new ClickHouseLevel2BatchRow(symbol, time,
                ClickHouseLevel2BatchRow.EVENT_MARKET, "", "", 0, 0,
                0, 0, 0, askPrice1, bidPrice1, askVolume1, askVolume2,
                bidVolume1, bidVolume2, totalVolume, totalValue);
    }

    private static LocalDateTime timestamp(int compactTime) {
        int millis = compactTime % 1_000;
        int hhmmss = compactTime / 1_000;
        return DATE.atTime(hhmmss / 10_000, hhmmss / 100 % 100,
                hhmmss % 100, millis * 1_000_000);
    }

    private static final class StubQueryService extends ClickHouseLevel2QueryService {
        private final ClickHouseQueryResult<ClickHouseOrderRow> orders;
        private final ClickHouseQueryResult<ClickHouseTransRow> transactions;
        private final ClickHouseQueryResult<ClickHouseMarketRow> markets;
        private final List<ClickHouseLevel2BatchRow> dailyRows;
        private int dailyQueryCount;
        private List<String> requestedSymbols = List.of();

        private StubQueryService(List<ClickHouseOrderRow> orders,
                                 List<ClickHouseTransRow> transactions,
                                 List<ClickHouseMarketRow> markets) {
            super(null);
            this.orders = ClickHouseQueryResult.of("stock.order", orders, 1);
            this.transactions = ClickHouseQueryResult.of("stock.trans", transactions, 1);
            this.markets = ClickHouseQueryResult.of("stock.market", markets, 1);
            this.dailyRows = List.of();
        }

        private StubQueryService(List<ClickHouseLevel2BatchRow> dailyRows) {
            super(null);
            this.orders = ClickHouseQueryResult.of("stock.order", List.of(), 1);
            this.transactions = ClickHouseQueryResult.of("stock.trans", List.of(), 1);
            this.markets = ClickHouseQueryResult.of("stock.market", List.of(), 1);
            this.dailyRows = dailyRows;
        }

        @Override
        public ClickHouseQueryResult<ClickHouseOrderRow> queryOrders(String securityId,
                                                                     LocalDate tradeDate) {
            return orders;
        }

        @Override
        public ClickHouseQueryResult<ClickHouseTransRow> queryTransactions(String securityId,
                                                                           LocalDate tradeDate) {
            return transactions;
        }

        @Override
        public ClickHouseQueryResult<ClickHouseMarketRow> queryMarket(String securityId,
                                                                      LocalDate tradeDate) {
            return markets;
        }

        @Override
        public ClickHouseDailyQueryResult streamDailyTicks(
                LocalDate tradeDate, List<String> securityIds,
                Consumer<ClickHouseLevel2BatchRow> rowConsumer) {
            dailyQueryCount++;
            requestedSymbols = List.copyOf(securityIds);
            dailyRows.forEach(rowConsumer);
            return new ClickHouseDailyQueryResult(dailyRows.size(), 1);
        }
    }
}
