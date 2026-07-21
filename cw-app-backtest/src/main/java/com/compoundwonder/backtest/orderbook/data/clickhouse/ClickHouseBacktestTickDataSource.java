package com.compoundwonder.backtest.orderbook.data.clickhouse;

import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.backtest.orderbook.data.BacktestDailyTickBatch;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.util.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * 使用 ClickHouse Level2 三张表构建统一 {@link TickData} 的回测数据源。
 *
 * <p>字段转换与实盘华鑫行情入口保持一致。同一毫秒内固定按逐笔委托、逐笔成交、
 * 三秒快照的顺序发布，延续旧回放按 dataType 排序的订单簿构建口径。</p>
 */
@Slf4j
@Component
public class ClickHouseBacktestTickDataSource implements BacktestTickDataSource {

    private static final int TIME_0930 = 93_000_000;
    private static final int TIME_1457 = 145_700_000;
    private static final int TIME_1500 = 150_000_000;

    private final ClickHouseLevel2QueryService queryService;

    public ClickHouseBacktestTickDataSource(ClickHouseLevel2QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public long replay(LocalDate tradeDate, String stockCode, Consumer<TickData> tickConsumer) {
        validateStockCode(stockCode);
        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        boolean shanghai = stockCode.startsWith("60");

        ClickHouseQueryResult<ClickHouseOrderRow> orderResult =
                queryService.queryOrders(stockCode, tradeDate);
        if (containsUnidentifiableShenzhenCancellation(stockCode, orderResult.rows())) {
            log.warn("丢弃无法重建订单簿的深圳回测数据 date={}, stockCode={}, "
                            + "reason=撤单缺少有效订单号或方向, orderRows={}",
                    tradeDate, stockCode, orderResult.rowCount());
            return 0;
        }
        ClickHouseQueryResult<ClickHouseTransRow> transResult =
                queryService.queryTransactions(stockCode, tradeDate);
        ClickHouseQueryResult<ClickHouseMarketRow> marketResult =
                queryService.queryMarket(stockCode, tradeDate);

        long replayed = replayMerged(symbolId, shanghai, orderResult.rows(),
                transResult.rows(), marketResult.rows(), tickConsumer);
        log.info("ClickHouse Level2 回放读取完成 date={}, stockCode={}, outputTicks={}, "
                        + "orderRows={}, transRows={}, marketRows={}, queryElapsedMs={}",
                tradeDate, stockCode, replayed, orderResult.rowCount(), transResult.rowCount(),
                marketResult.rowCount(), String.format("%.3f",
                        orderResult.elapsedMillis() + transResult.elapsedMillis() + marketResult.elapsedMillis()));
        return replayed;
    }

    /**
     * 一次查询并缓存当天全部候选股票的必要 Tick，供完整历史回测日内重复回放。
     *
     * <p>股票代码先去重并排序，使 SQL 参数和结果日志稳定。查询结果已经按股票、时间、
     * 委托/成交/快照优先级以及订单号排序，因此这里仅分组，不再进行内存排序。</p>
     */
    @Override
    public BacktestDailyTickBatch loadDay(LocalDate tradeDate, Collection<String> stockCodes) {
        List<String> requestedCodes = new TreeSet<>(stockCodes).stream()
                .peek(ClickHouseBacktestTickDataSource::validateStockCode)
                .toList();
        if (requestedCodes.isEmpty()) {
            return new InMemoryDailyTickBatch(tradeDate, Map.of());
        }

        Map<String, List<TickData>> mutableTicks = new LinkedHashMap<>(requestedCodes.size());
        for (String stockCode : requestedCodes) {
            mutableTicks.put(stockCode, new java.util.ArrayList<>());
        }
        Set<String> invalidSymbols = new HashSet<>();
        ClickHouseDailyQueryResult queryResult = queryService.streamDailyTicks(
                tradeDate, requestedCodes, row -> {
                    List<TickData> symbolTicks = mutableTicks.get(row.securityId());
                    if (symbolTicks == null) {
                        throw new IllegalStateException("ClickHouse 返回了未请求的股票: "
                                + row.securityId());
                    }
                    if (isUnidentifiableShenzhenCancellation(row)) {
                        symbolTicks.clear();
                        invalidSymbols.add(row.securityId());
                        return;
                    }
                    if (invalidSymbols.contains(row.securityId())) {
                        return;
                    }
                    TickData tick = toBatchTick(row);
                    if (tick != null) {
                        symbolTicks.add(tick);
                    }
                });

        if (!invalidSymbols.isEmpty()) {
            log.warn("丢弃无法重建订单簿的深圳回测数据 date={}, symbols={}, "
                            + "reason=撤单缺少有效订单号或方向",
                    tradeDate, invalidSymbols);
        }

        Map<String, List<TickData>> immutableTicks = new LinkedHashMap<>(mutableTicks.size());
        long outputTicks = 0;
        for (Map.Entry<String, List<TickData>> entry : mutableTicks.entrySet()) {
            List<TickData> ticks = List.copyOf(entry.getValue());
            immutableTicks.put(entry.getKey(), ticks);
            outputTicks += ticks.size();
        }
        log.info("ClickHouse 单日批量 Level2 读取完成 date={}, symbols={}, queryRows={}, "
                        + "outputTicks={}, queryElapsedMs={}",
                tradeDate, requestedCodes.size(), queryResult.rowCount(), outputTicks,
                String.format("%.3f", queryResult.elapsedMillis()));
        return new InMemoryDailyTickBatch(tradeDate, Map.copyOf(immutableTicks));
    }

    private long replayMerged(int symbolId, boolean shanghai,
                              List<ClickHouseOrderRow> orders,
                              List<ClickHouseTransRow> transactions,
                              List<ClickHouseMarketRow> markets,
                              Consumer<TickData> tickConsumer) {
        int orderIndex = 0;
        int transIndex = 0;
        int marketIndex = 0;
        long replayed = 0;

        while (orderIndex < orders.size()
                || transIndex < transactions.size()
                || marketIndex < markets.size()) {
            long orderTime = orderIndex < orders.size()
                    ? Integer.toUnsignedLong(orders.get(orderIndex).tradeTime()) : Long.MAX_VALUE;
            long transTime = transIndex < transactions.size()
                    ? Integer.toUnsignedLong(transactions.get(transIndex).tradeTime()) : Long.MAX_VALUE;
            long marketTime = marketIndex < markets.size()
                    ? markets.get(marketIndex).tradeTime() : Long.MAX_VALUE;

            TickData tick;
            if (orderTime <= transTime && orderTime <= marketTime) {
                tick = toOrderTick(orders.get(orderIndex++), symbolId, shanghai);
            } else if (transTime <= marketTime) {
                tick = toTransactionTick(transactions.get(transIndex++), symbolId, shanghai);
            } else {
                tick = toMarketTick(markets.get(marketIndex++), symbolId);
            }
            if (tick != null) {
                tickConsumer.accept(tick);
                replayed++;
            }
        }
        return replayed;
    }

    /**
     * 转换逐笔委托。
     *
     * <p>上海 A/D 映射为限价委托/撤单，状态包 S 与实盘入口一致直接忽略。
     * 深圳只有 TickType=0 且价格为 0 才是撤单，转换成核心 Handler 使用的
     * {@code dataType=2,type=1}；TickType=0 但价格有效时仍按逐笔委托入队。</p>
     */
    static TickData toOrderTick(ClickHouseOrderRow row, int symbolId, boolean shanghai) {
        return toOrderTick(row.tickType(), row.side(), row.price(), row.volume(), row.no(),
                row.tradeTime(), symbolId, shanghai);
    }

    private static TickData toOrderTick(String tickType, String side, float price,
                                        long volume, long orderNo, int tradeTime,
                                        int symbolId, boolean shanghai) {
        byte type;
        if (shanghai) {
            if ("A".equals(tickType)) {
                type = 2;
            } else if ("D".equals(tickType)) {
                type = 10;
            } else {
                return null;
            }
        } else {
            type = parseShenzhenOrderType(tickType);
        }

        TickData tick = baseTick(symbolId, tradeTime);
        tick.direction = parseSide(side);
        tick.orderId = toInt(orderNo, "委托号");
        tick.price = toPrice(price);
        tick.quantity = toInt(volume, "委托数量");
        if (!shanghai && type == 0 && tick.price == 0) {
            tick.dataType = 2;
            tick.type = 1;
            if (tick.direction == 1) {
                tick.buyerOrderId = tick.orderId;
            } else if (tick.direction == 2) {
                tick.sellerOrderId = tick.orderId;
            }
            return tick;
        }
        tick.dataType = 1;
        tick.type = type;
        return tick;
    }

    /** 深圳 TickType=1 为成交，其余类型为撤单；上海 trans 表只承载成交。 */
    static TickData toTransactionTick(ClickHouseTransRow row, int symbolId, boolean shanghai) {
        return toTransactionTick(row.tickType(), row.price(), row.volume(), row.buyNo(),
                row.sellNo(), row.tradeTime(), symbolId, shanghai);
    }

    private static TickData toTransactionTick(String tickType, float price, long volume,
                                              long buyNo, long sellNo, int tradeTime,
                                              int symbolId, boolean shanghai) {
        TickData tick = baseTick(symbolId, tradeTime);
        tick.dataType = 2;
        tick.type = shanghai || "1".equals(tickType) ? (byte) 0 : (byte) 1;
        tick.buyerOrderId = toInt(buyNo, "买方委托号");
        tick.sellerOrderId = toInt(sellNo, "卖方委托号");
        tick.orderId = tick.buyerOrderId == 0 ? tick.sellerOrderId : tick.buyerOrderId;
        tick.direction = shanghai ? 0 : (tick.buyerOrderId == 0 ? (byte) 2 : (byte) 1);
        tick.price = toPrice(price);
        tick.quantity = toInt(volume, "成交数量");
        // 保留核心订单簿既有的金额单位和 int 溢出语义；撤单不产生成交额。
        if (tick.type == 0) {
            tick.amount = tick.quantity / 100 * tick.price;
        }
        return tick;
    }

    /**
     * 将 market 十档快照转换成实盘 publishSnapshotData 使用的字段语义。
     * 集合竞价：卖一价、买一买二量、卖一卖二量；连续竞价：买一价、累计成交额、
     * 累计成交量和买一量。
     */
    public static TickData toMarketTick(ClickHouseMarketRow row, int symbolId) {
        return toMarketTick(toInt(row.tradeTime(), "快照时间"),
                first(row.askPrices()), first(row.bidPrices()),
                first(row.askVolumes()), second(row.askVolumes()),
                first(row.bidVolumes()), second(row.bidVolumes()),
                row.totalVolumeTrade(), row.totalValueTrade(), symbolId);
    }

    private static TickData toMarketTick(int time, float askPrice1, float bidPrice1,
                                         long askVolume1, long askVolume2,
                                         long bidVolume1, long bidVolume2,
                                         long totalVolumeTrade, long totalValueTrade,
                                         int symbolId) {
        boolean auction = time < TIME_0930 || (time >= TIME_1457 && time < TIME_1500);
        long buyVolume;
        long sellVolume;
        long amount;
        float price;
        if (auction) {
            price = askPrice1;
            buyVolume = bidVolume1 + bidVolume2;
            sellVolume = askVolume1 + askVolume2;
            amount = 0;
        } else {
            price = bidPrice1;
            buyVolume = bidVolume1;
            sellVolume = totalVolumeTrade;
            amount = totalValueTrade;
        }

        TickData tick = baseTick(symbolId, time);
        tick.dataType = 4;
        tick.price = toPrice(price);
        tick.buyerOrderId = clampToInt(buyVolume);
        tick.sellerOrderId = clampToInt(sellVolume);
        tick.quantity = tick.buyerOrderId;
        if (amount > Integer.MAX_VALUE) {
            tick.type = 1;
            tick.orderId = toInt(amount / 100L, "压缩后的累计成交额");
        } else {
            tick.type = 0;
            tick.orderId = (int) amount;
        }
        return tick;
    }

    /** 将 UNION ALL 的最小字段行转换为与单股票查询完全相同的 TickData。 */
    private static TickData toBatchTick(ClickHouseLevel2BatchRow row) {
        int symbolId = SymbolUtil.fastSymbolToInt(row.securityId());
        boolean shanghai = row.securityId().startsWith("60");
        return switch (row.eventSource()) {
            case ClickHouseLevel2BatchRow.EVENT_ORDER -> toOrderTick(
                    row.tickType(), row.side(), row.price(), row.volume(), row.orderNo(),
                    row.tradeTime(), symbolId, shanghai);
            case ClickHouseLevel2BatchRow.EVENT_TRANSACTION -> toTransactionTick(
                    row.tickType(), row.price(), row.volume(), row.buyNo(), row.sellNo(),
                    row.tradeTime(), symbolId, shanghai);
            case ClickHouseLevel2BatchRow.EVENT_MARKET -> toMarketTick(
                    row.tradeTime(), row.askPrice1(), row.bidPrice1(),
                    row.askVolume1(), row.askVolume2(),
                    row.bidVolume1(), row.bidVolume2(),
                    row.totalVolumeTrade(), row.totalValueTrade(), symbolId);
            default -> throw new IllegalArgumentException(
                    "无法识别的 ClickHouse Level2 事件来源: " + row.eventSource());
        };
    }

    /**
     * 判断单股票查询是否包含无法关联到原委托的深圳撤单。
     *
     * <p>这类旧数据没有足够字段恢复订单簿，不能把其余新增委托继续推入共享核心引擎，
     * 否则会把已经撤销的委托长期保留在盘口中并产生虚假买入信号。</p>
     */
    private static boolean containsUnidentifiableShenzhenCancellation(
            String stockCode, List<ClickHouseOrderRow> orders) {
        if (stockCode.startsWith("60")) {
            return false;
        }
        for (ClickHouseOrderRow row : orders) {
            if (isUnidentifiableShenzhenCancellation(
                    row.tickType(), row.price(), row.side(), row.no())) {
                return true;
            }
        }
        return false;
    }

    /** 批量查询版本的数据质量判断，在转换 TickData 之前执行。 */
    private static boolean isUnidentifiableShenzhenCancellation(
            ClickHouseLevel2BatchRow row) {
        return row.eventSource() == ClickHouseLevel2BatchRow.EVENT_ORDER
                && !row.securityId().startsWith("60")
                && isUnidentifiableShenzhenCancellation(
                        row.tickType(), row.price(), row.side(), row.orderNo());
    }

    /** 深圳撤单必须同时具备有效订单号和买卖方向，缺少任一字段都无法精确撤单。 */
    private static boolean isUnidentifiableShenzhenCancellation(
            String tickType, float price, String side, long orderNo) {
        return "0".equals(tickType) && price == 0F
                && (orderNo <= 0 || parseSide(side) == 0);
    }

    /** 内存批次中的 TickData 只读复用；Disruptor publish 会复制字段，不会修改对象。 */
    private record InMemoryDailyTickBatch(
            LocalDate tradeDate, Map<String, List<TickData>> ticksBySymbol)
            implements BacktestDailyTickBatch {

        @Override
        public Set<String> stockCodes() {
            return ticksBySymbol.keySet();
        }

        @Override
        public long replay(String stockCode, Consumer<TickData> tickConsumer) {
            List<TickData> ticks = ticksBySymbol.get(stockCode);
            if (ticks == null) {
                return 0;
            }
            ticks.forEach(tickConsumer);
            return ticks.size();
        }
    }

    private static TickData baseTick(int symbolId, int time) {
        TickData tick = new TickData();
        tick.symbolId = symbolId;
        tick.time = time;
        return tick;
    }

    private static byte parseShenzhenOrderType(String value) {
        if (value == null || value.length() != 1 || value.charAt(0) < '0' || value.charAt(0) > '9') {
            throw new IllegalArgumentException("无法识别的深圳委托类型: " + value);
        }
        return (byte) (value.charAt(0) - '0');
    }

    private static byte parseSide(String value) {
        if ("1".equals(value)) {
            return 1;
        }
        if ("2".equals(value)) {
            return 2;
        }
        return 0;
    }

    private static int toPrice(float price) {
        return Math.round(price * 100F);
    }

    private static float first(float[] values) {
        return values == null || values.length == 0 ? 0F : values[0];
    }

    private static long first(long[] values) {
        return values == null || values.length == 0 ? 0L : values[0];
    }

    private static long second(long[] values) {
        return values == null || values.length < 2 ? 0L : values[1];
    }

    private static int clampToInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static int toInt(long value, String field) {
        if (value < 0 || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(field + " 超出 TickData int 范围: " + value);
        }
        return (int) value;
    }

    private static void validateStockCode(String stockCode) {
        if (stockCode == null || !stockCode.matches("(?:60|00)\\d{4}")) {
            throw new IllegalArgumentException("当前回测只支持 60/00 沪深主板股票: " + stockCode);
        }
    }
}
