package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.compoundwonder.backtest.service.Level2MinuteBarService;
import com.compoundwonder.dto.Level2MinuteTickDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.util.SymbolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class Level2MinuteBarServiceImpl implements Level2MinuteBarService {

    private static final Path LEVEL2_DATA_DIR = Path.of(System.getProperty("user.home"), "Documents", "lev2data");

    @Autowired
    private StockDailyService stockDailyService;

    /**
     * 查询指定股票在指定交易日的 Level2 分时 tick 数据。
     * 实现逻辑：读取本地 parquet，按字段语义自动适配列名，并聚合集合竞价和连续竞价分时点。
     */
    @Override
    public List<Level2MinuteTickDTO> findMinuteBars(String stockCode, LocalDate tradeDate) {
        Path parquetPath = LEVEL2_DATA_DIR.resolve(tradeDate + ".parquet");
        if (!Files.exists(parquetPath)) {
            return List.of();
        }

        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        int handlerIndex = SymbolUtil.getHandlerIndex(symbolId);
        if (symbolId < 0 || handlerIndex < 0) {
            return List.of();
        }

        try (Connection connection = DriverManager.getConnection("jdbc:duckdb:");
             Statement statement = connection.createStatement()) {
            Map<String, String> columns = describeColumns(statement, parquetPath);
            QueryColumns queryColumns = resolveColumns(columns);
            String sql = buildQuery(parquetPath, queryColumns, stockCode, symbolId, handlerIndex);
            double limitUpPrice = findLimitUpPrice(stockCode, tradeDate, symbolId);
            return queryMinuteBars(statement, sql, tradeDate, limitUpPrice);
        } catch (SQLException e) {
            throw new IllegalStateException("DuckDB 查询 Level2 分时失败: " + e.getMessage(), e);
        }
    }

    private Map<String, String> describeColumns(Statement statement, Path parquetPath) throws SQLException {
        Map<String, String> columns = new LinkedHashMap<>();
        String sql = "DESCRIBE SELECT * FROM read_parquet('" + sqlLiteral(parquetPath.toString()) + "')";
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                columns.put(columnName.toLowerCase(Locale.ROOT), columnName);
            }
        }
        return columns;
    }

    private QueryColumns resolveColumns(Map<String, String> columns) {
        // parquet 字段名可能随导出程序变化，查询时先解析成统一语义列。
        return new QueryColumns(
                requireColumn(columns, "dataType", "data_type", "datatype", "type"),
                requireColumn(columns, "price", "lastPrice", "last_price", "matchPrice", "match_price", "currentPrice", "current_price"),
                requireColumn(columns, "time", "timestamp", "tradeTime", "trade_time", "updateTime", "update_time", "localTime", "local_time"),
                requireColumn(columns, "orderId", "order_id", "amount", "turnover"),
                requireColumn(columns, "quantity", "volume"),
                requireColumn(columns, "buyerOrderId", "buyer_order_id", "bidQuantity", "bid_quantity", "buyQuantity", "buy_quantity"),
                requireColumn(columns, "sellerOrderId", "seller_order_id", "askQuantity", "ask_quantity", "sellQuantity", "sell_quantity"),
                requireColumn(columns, "symbolId", "symbol_id", "internalId", "internal_id", "symbol", "stockCode", "stock_code", "securityId", "security_id", "securityID"),
                findColumn(columns, "handlerIndex", "handler_index", "exchange", "market", "marketType", "market_type")
        );
    }

    private String buildQuery(Path parquetPath, QueryColumns columns, String stockCode, int symbolId, int handlerIndex) {
        List<String> predicates = new ArrayList<>();
        predicates.add(quoteIdentifier(columns.dataType()) + " >= 400");
        predicates.add(quoteIdentifier(columns.price()) + " != 0");
        if (columns.symbol() != null) {
            String symbolColumn = quoteIdentifier(columns.symbol());
            predicates.add("(" + symbolColumn + " = " + symbolId + " OR " + symbolColumn + " = " + (symbolId - 1000000)
                    + " OR CAST(" + symbolColumn + " AS VARCHAR) = '" + sqlLiteral(stockCode) + "')");
        }
        if (columns.handlerIndex() != null) {
            predicates.add(quoteIdentifier(columns.handlerIndex()) + " = " + handlerIndex);
        }

        return "SELECT " +
                "*, " +
                quoteIdentifier(columns.dataType()) + " AS tick_data_type, " +
                quoteIdentifier(columns.time()) + " AS tick_time, " +
                quoteIdentifier(columns.price()) + " AS tick_price, " +
                quoteIdentifier(columns.orderId()) + " AS tick_amount, " +
                quoteIdentifier(columns.quantity()) + " AS tick_quantity, " +
                quoteIdentifier(columns.buyerOrderId()) + " AS tick_buyer_order_id, " +
                quoteIdentifier(columns.sellerOrderId()) + " AS tick_seller_order_id" +
                (columns.symbol() == null ? "" : ", " + quoteIdentifier(columns.symbol()) + " AS tick_symbol_id") +
                (columns.handlerIndex() == null ? "" : ", " + quoteIdentifier(columns.handlerIndex()) + " AS tick_handler_index") +
                " " +
                "FROM read_parquet('" + sqlLiteral(parquetPath.toString()) + "') " +
                "WHERE " + String.join(" AND ", predicates) + " " +
                "ORDER BY " + quoteIdentifier(columns.time());
    }

    private List<Level2MinuteTickDTO> queryMinuteBars(Statement statement, String sql, LocalDate tradeDate, double limitUpPrice) throws SQLException {
        List<Level2MinuteTickDTO> ticks = new ArrayList<>();
        Level2MinuteTickDTO pendingMinuteTick = null;
        String pendingMinute = null;
        try (ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Object rawTime = rs.getObject("tick_time");
                long timestamp = readTimestamp(rawTime, tradeDate);
                String tickTime = formatTradeTime(rawTime);
                double rawPrice = rs.getDouble("tick_price") / 100D;
                // 本地 Level2 存储里的 price 是卖一价，低于涨停价时按用户确认口径补一分钱显示。
                double price = rawPrice < limitUpPrice ? roundPrice(rawPrice + 0.01D) : rawPrice;
                Level2MinuteTickDTO dto = new Level2MinuteTickDTO();
                dto.setTimestamp(timestamp);
                dto.setTickTime(tickTime);
                dto.setDataType(rs.getInt("tick_data_type"));
                dto.setRawTime(rawTime);
                dto.setRawPrice(rawPrice);
                dto.setSellPrice(rawPrice);
                dto.setPrice(price);
                dto.setAmount(rs.getLong("tick_amount"));
                dto.setQuantity(rs.getLong("tick_quantity"));
                dto.setBuyerOrderId(rs.getLong("tick_buyer_order_id"));
                dto.setSellerOrderId(rs.getLong("tick_seller_order_id"));
                if (hasColumn(metaData, "tick_symbol_id")) {
                    dto.setSymbolId(toJsonValue(rs.getObject("tick_symbol_id")));
                }
                if (hasColumn(metaData, "tick_handler_index")) {
                    dto.setHandlerIndex(toJsonValue(rs.getObject("tick_handler_index")));
                }
                dto.setRawFields(readRawFields(rs, metaData));
                if (isAuctionTradeTime(tickTime)) {
                    flushPendingMinuteTick(ticks, pendingMinuteTick);
                    pendingMinuteTick = null;
                    pendingMinute = null;
                    ticks.add(dto);
                } else if (isContinuousTradeTime(tickTime)) {
                    String minute = tickTime.substring(0, 5);
                    if (pendingMinute != null && !pendingMinute.equals(minute)) {
                        ticks.add(pendingMinuteTick);
                    }
                    pendingMinute = minute;
                    pendingMinuteTick = dto;
                }
            }
        }
        flushPendingMinuteTick(ticks, pendingMinuteTick);
        return ticks;
    }

    private void flushPendingMinuteTick(List<Level2MinuteTickDTO> ticks, Level2MinuteTickDTO pendingMinuteTick) {
        if (pendingMinuteTick != null) {
            ticks.add(pendingMinuteTick);
        }
    }

    private boolean isAuctionTradeTime(String tickTime) {
        int second = tradeSecond(tickTime);
        return (second >= tradeSecond(9, 15, 0) && second <= tradeSecond(9, 25, 0))
                || (second >= tradeSecond(14, 57, 0) && second <= tradeSecond(15, 0, 0));
    }

    private boolean isContinuousTradeTime(String tickTime) {
        int second = tradeSecond(tickTime);
        return (second >= tradeSecond(9, 30, 0) && second <= tradeSecond(11, 30, 0))
                || (second >= tradeSecond(13, 0, 0) && second < tradeSecond(14, 57, 0));
    }

    private int tradeSecond(String tickTime) {
        if (tickTime == null || tickTime.length() < 5) {
            return -1;
        }
        try {
            int hour = Integer.parseInt(tickTime.substring(0, 2));
            int minute = Integer.parseInt(tickTime.substring(3, 5));
            int second = tickTime.length() >= 8 ? Integer.parseInt(tickTime.substring(6, 8)) : 0;
            return tradeSecond(hour, minute, second);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private int tradeSecond(int hour, int minute, int second) {
        return (hour * 60 + minute) * 60 + second;
    }

    private Map<String, Object> readRawFields(ResultSet rs, ResultSetMetaData metaData) throws SQLException {
        Map<String, Object> fields = new LinkedHashMap<>();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String label = metaData.getColumnLabel(i);
            if (label.startsWith("tick_")) {
                continue;
            }
            fields.put(label, toJsonValue(rs.getObject(i)));
        }
        return fields;
    }

    private boolean hasColumn(ResultSetMetaData metaData, String columnName) throws SQLException {
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private Object toJsonValue(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toString();
        }
        if (value instanceof Time time) {
            return time.toLocalTime().toString();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof java.time.LocalDateTime dateTime) {
            return dateTime.toString();
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof LocalTime time) {
            return time.toString();
        }
        return value;
    }

    private double findLimitUpPrice(String stockCode, LocalDate tradeDate, int symbolId) {
        StockDailyEntity daily = stockDailyService.getOne(new QueryWrapper<StockDailyEntity>()
                .eq("stock_code", stockCode)
                .eq("trade_date", tradeDate)
                .last("limit 1"));
        double prevClose = daily == null || daily.getPrevClose() == null ? 0D : daily.getPrevClose();
        if (prevClose <= 0) {
            return Double.MAX_VALUE;
        }
        int prefix = (symbolId - 1000000) / 10000;
        double multiplier = prefix == 30 || prefix == 68 ? 1.2D : 1.1D;
        return BigDecimal.valueOf(prevClose)
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private long readTimestamp(Object value, LocalDate tradeDate) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant().toEpochMilli();
        }
        if (value instanceof Time time) {
            return time.toLocalTime().atDate(tradeDate).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (value instanceof java.time.LocalDateTime dateTime) {
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (value instanceof Number number) {
            long raw = number.longValue();
            if (raw > 1_000_000_000_000L) {
                return raw;
            }
            if (raw > 1_000_000_000L) {
                return Instant.ofEpochSecond(raw).toEpochMilli();
            }
            return parseTradeTime(raw, tradeDate);
        }
        if (value != null) {
            try {
                return parseTradeTime(Long.parseLong(value.toString().replace(":", "")), tradeDate);
            } catch (NumberFormatException ignored) {
                return tradeDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
        }
        return tradeDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String formatTradeTime(Object value) {
        if (value instanceof Number number) {
            return formatTradeTime(number.longValue());
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalTime().withNano(0).toString();
        }
        if (value instanceof Time time) {
            return time.toLocalTime().withNano(0).toString();
        }
        if (value instanceof java.time.LocalDateTime dateTime) {
            return dateTime.toLocalTime().withNano(0).toString();
        }
        if (value instanceof LocalTime time) {
            return time.withNano(0).toString();
        }
        if (value != null) {
            try {
                return formatTradeTime(Long.parseLong(value.toString().replace(":", "")));
            } catch (NumberFormatException ignored) {
                return value.toString();
            }
        }
        return "";
    }

    private String formatTradeTime(long raw) {
        // 压缩时间形如 140539790，去掉后三位毫秒后展示为 14:05:39。
        String normalized = raw <= 235959
                ? String.format("%06d", raw)
                : String.format("%09d", raw).substring(0, 6);
        return normalized.substring(0, 2) + ":" + normalized.substring(2, 4) + ":" + normalized.substring(4, 6);
    }

    private long parseTradeTime(long raw, LocalDate tradeDate) {
        // 同一份压缩时间既要用于界面展示，也要合成当天 timestamp 供前端排序和定位。
        String normalized = raw <= 235959
                ? String.format("%06d", raw) + "000"
                : String.format("%09d", raw).substring(0, 6) + "000";
        int hour = Integer.parseInt(normalized.substring(0, 2));
        int minute = Integer.parseInt(normalized.substring(2, 4));
        int second = Integer.parseInt(normalized.substring(4, 6));
        int millis = Integer.parseInt(normalized.substring(6, 9));
        return LocalTime.of(hour, minute, second, millis * 1_000_000)
                .atDate(tradeDate)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private double roundPrice(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String requireColumn(Map<String, String> columns, String... candidates) {
        String column = findColumn(columns, candidates);
        if (column == null) {
            throw new IllegalStateException("Level2 parquet 缺少字段，候选: "
                    + String.join(", ", candidates)
                    + "，实际字段: "
                    + String.join(", ", columns.values()));
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

    private record QueryColumns(String dataType, String price, String time, String orderId, String quantity,
                                String buyerOrderId, String sellerOrderId, String symbol, String handlerIndex) {
    }
}
