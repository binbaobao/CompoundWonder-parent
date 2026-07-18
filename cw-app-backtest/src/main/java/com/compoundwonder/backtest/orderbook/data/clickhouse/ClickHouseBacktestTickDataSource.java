package com.compoundwonder.backtest.orderbook.data.clickhouse;

import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.util.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
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

    /** 上海 A/D 映射为限价委托/撤单；状态包 S 与实盘入口一致直接忽略。 */
    static TickData toOrderTick(ClickHouseOrderRow row, int symbolId, boolean shanghai) {
        byte type;
        if (shanghai) {
            if ("A".equals(row.tickType())) {
                type = 2;
            } else if ("D".equals(row.tickType())) {
                type = 10;
            } else {
                return null;
            }
        } else {
            type = parseShenzhenOrderType(row.tickType());
        }

        TickData tick = baseTick(symbolId, row.tradeTime());
        tick.dataType = 1;
        tick.direction = parseSide(row.side());
        tick.type = type;
        tick.orderId = toInt(row.no(), "委托号");
        tick.price = toPrice(row.price());
        tick.quantity = toInt(row.volume(), "委托数量");
        return tick;
    }

    /** 深圳 TickType=1 为成交，其余类型为撤单；上海 trans 表只承载成交。 */
    static TickData toTransactionTick(ClickHouseTransRow row, int symbolId, boolean shanghai) {
        TickData tick = baseTick(symbolId, row.tradeTime());
        tick.dataType = 2;
        tick.type = shanghai || "1".equals(row.tickType()) ? (byte) 0 : (byte) 1;
        tick.buyerOrderId = toInt(row.buyNo(), "买方委托号");
        tick.sellerOrderId = toInt(row.sellNo(), "卖方委托号");
        tick.orderId = tick.buyerOrderId == 0 ? tick.sellerOrderId : tick.buyerOrderId;
        tick.direction = shanghai ? 0 : (tick.buyerOrderId == 0 ? (byte) 2 : (byte) 1);
        tick.price = toPrice(row.price());
        tick.quantity = toInt(row.volume(), "成交数量");
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
        int time = toInt(row.tradeTime(), "快照时间");
        boolean auction = time < TIME_0930 || (time >= TIME_1457 && time < TIME_1500);
        long buyVolume;
        long sellVolume;
        long amount;
        float price;
        if (auction) {
            price = first(row.askPrices());
            buyVolume = firstTwo(row.bidVolumes());
            sellVolume = firstTwo(row.askVolumes());
            amount = 0;
        } else {
            price = first(row.bidPrices());
            buyVolume = first(row.bidVolumes());
            sellVolume = row.totalVolumeTrade();
            amount = row.totalValueTrade();
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

    private static long firstTwo(long[] values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        return values[0] + (values.length > 1 ? values[1] : 0L);
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
