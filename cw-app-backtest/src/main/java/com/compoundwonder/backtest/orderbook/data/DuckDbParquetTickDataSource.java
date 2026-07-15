package com.compoundwonder.backtest.orderbook.data;

import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.util.SymbolUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 使用内存 DuckDB 直接扫描本地 Parquet 的回测 Tick 数据源。
 */
@Component
@ConditionalOnProperty(name = "backtest.tick-data-source", havingValue = "duckdb", matchIfMissing = true)
public class DuckDbParquetTickDataSource implements BacktestTickDataSource {

    private final Path dataDirectory;

    public DuckDbParquetTickDataSource(
            @Value("${backtest.level2-data-dir:${user.home}/Documents/lev2data}") String dataDirectory) {
        this.dataDirectory = Path.of(dataDirectory);
    }

    @Override
    public long replay(LocalDate tradeDate, String stockCode, Consumer<TickData> tickConsumer) {
        validateStockCode(stockCode);
        Path parquetPath = dataDirectory.resolve(tradeDate + ".parquet");
        if (!Files.isRegularFile(parquetPath)) {
            throw new IllegalArgumentException("Level2 Parquet 文件不存在: " + parquetPath);
        }

        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        int handlerIndex = SymbolUtil.getHandlerIndex(symbolId);
        try (Connection connection = DriverManager.getConnection("jdbc:duckdb:");
             Statement statement = connection.createStatement()) {
            TickColumns columns = resolveColumns(describeColumns(statement, parquetPath));
            String sql = buildReplaySql(parquetPath, columns, stockCode, symbolId, handlerIndex);
            return streamTicks(statement, sql, tickConsumer);
        } catch (SQLException e) {
            throw new IllegalStateException("DuckDB 读取回测 Tick 失败: " + e.getMessage(), e);
        }
    }

    private Map<String, String> describeColumns(Statement statement, Path parquetPath) throws SQLException {
        Map<String, String> columns = new LinkedHashMap<>();
        String sql = "DESCRIBE SELECT * FROM read_parquet('" + sqlLiteral(parquetPath.toString()) + "')";
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("column_name");
                columns.put(columnName.toLowerCase(Locale.ROOT), columnName);
            }
        }
        return columns;
    }

    private TickColumns resolveColumns(Map<String, String> columns) {
        return new TickColumns(
                requireColumn(columns, "symbolId", "symbol_id", "internalId", "internal_id", "symbol", "stockCode", "stock_code"),
                requireColumn(columns, "dataType", "data_type", "datatype"),
                requireColumn(columns, "time", "timestamp", "tradeTime", "trade_time", "updateTime", "update_time"),
                requireColumn(columns, "orderId", "order_id"),
                requireColumn(columns, "price", "matchPrice", "match_price"),
                requireColumn(columns, "quantity", "volume"),
                requireColumn(columns, "buyerOrderId", "buyer_order_id", "buyNo", "buy_no"),
                requireColumn(columns, "sellerOrderId", "seller_order_id", "sellNo", "sell_no"),
                findColumn(columns, "handlerIndex", "handler_index", "exchange", "market"));
    }

    private String buildReplaySql(Path parquetPath, TickColumns columns, String stockCode,
                                  int symbolId, int handlerIndex) {
        String symbol = quoteIdentifier(columns.symbol());
        StringBuilder where = new StringBuilder("CAST(")
                .append(symbol)
                .append(" AS VARCHAR) IN ('")
                .append(symbolId)
                .append("', '")
                .append(symbolId - 1_000_000)
                .append("', '")
                .append(sqlLiteral(stockCode))
                .append("')");
        if (columns.handlerIndex() != null) {
            where.append(" AND ")
                    .append(quoteIdentifier(columns.handlerIndex()))
                    .append(" = ")
                    .append(handlerIndex);
        }

        return "SELECT "
                + symbol + " AS tick_symbol_id, "
                + quoteIdentifier(columns.dataType()) + " AS tick_data_type, "
                + quoteIdentifier(columns.time()) + " AS tick_time, "
                + quoteIdentifier(columns.orderId()) + " AS tick_order_id, "
                + quoteIdentifier(columns.price()) + " AS tick_price, "
                + quoteIdentifier(columns.quantity()) + " AS tick_quantity, "
                + quoteIdentifier(columns.buyerOrderId()) + " AS tick_buyer_order_id, "
                + quoteIdentifier(columns.sellerOrderId()) + " AS tick_seller_order_id "
                + "FROM read_parquet('" + sqlLiteral(parquetPath.toString()) + "') "
                + "WHERE " + where + " "
                + "ORDER BY " + quoteIdentifier(columns.time()) + ", "
                + quoteIdentifier(columns.dataType()) + ", "
                + quoteIdentifier(columns.orderId());
    }

    private long streamTicks(Statement statement, String sql, Consumer<TickData> tickConsumer) throws SQLException {
        long count = 0;
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                TickData tick = decodeTick(
                        resultSet.getInt("tick_symbol_id"),
                        resultSet.getInt("tick_data_type"),
                        resultSet.getInt("tick_time"),
                        resultSet.getInt("tick_order_id"),
                        resultSet.getInt("tick_price"),
                        resultSet.getInt("tick_quantity"),
                        resultSet.getInt("tick_buyer_order_id"),
                        resultSet.getInt("tick_seller_order_id"));
                tickConsumer.accept(tick);
                count++;
            }
        }
        return count;
    }

    static TickData decodeTick(int symbolId, int combinedDataType, int time, int orderId,
                               int price, int quantity, int buyerOrderId, int sellerOrderId) {
        int type = combinedDataType % 10;
        if (combinedDataType < 400 && type == 0 && symbolId > 1_600_000) {
            type = 10;
        }
        int withoutType = combinedDataType / 10;

        TickData tick = new TickData();
        tick.symbolId = symbolId;
        tick.dataType = (byte) (withoutType / 10);
        tick.direction = (byte) (withoutType % 10);
        tick.type = (byte) type;
        tick.time = time;
        tick.orderId = orderId;
        tick.price = price;
        tick.quantity = quantity;
        tick.amount = Math.toIntExact(quantity / 100L * price);
        tick.buyerOrderId = buyerOrderId;
        tick.sellerOrderId = sellerOrderId;
        return tick;
    }

    private void validateStockCode(String stockCode) {
        if (stockCode == null || !stockCode.matches("(?:60|00)\\d{4}")) {
            throw new IllegalArgumentException("当前回测只支持 60/00 沪深主板股票: " + stockCode);
        }
    }

    private String requireColumn(Map<String, String> columns, String... candidates) {
        String column = findColumn(columns, candidates);
        if (column == null) {
            throw new IllegalStateException("Level2 Parquet 缺少字段，候选: "
                    + String.join(", ", candidates) + "，实际字段: " + String.join(", ", columns.values()));
        }
        return column;
    }

    private String findColumn(Map<String, String> columns, String... candidates) {
        for (String candidate : candidates) {
            String column = columns.get(candidate.toLowerCase(Locale.ROOT));
            if (column != null) {
                return column;
            }
        }
        return null;
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private String sqlLiteral(String value) {
        return value.replace("'", "''");
    }

    private record TickColumns(String symbol, String dataType, String time, String orderId, String price,
                               String quantity, String buyerOrderId, String sellerOrderId, String handlerIndex) {
    }
}
