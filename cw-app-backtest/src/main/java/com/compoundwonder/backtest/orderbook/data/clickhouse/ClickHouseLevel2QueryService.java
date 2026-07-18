package com.compoundwonder.backtest.orderbook.data.clickhouse;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * ClickHouse Level2 三表查询服务。
 *
 * <p>每个查询都按 SecurityID 和 TradeDate 参数化过滤，不设置 LIMIT，并将 JDBC
 * ResultSet 全量映射为强类型 Java 对象。耗时包含服务端查询、网络传输和本地对象映射。</p>
 */
@Service
@DS("clickhouse")
public class ClickHouseLevel2QueryService {

    static final String MARKET_QUERY_SQL = """
            SELECT SecurityID, ExchangeID, TradeDate, TradeTime, TradeTimeStamp,
                   LastPrice, PreClosePrice, OpenPrice, HighestPrice, LowestPrice,
                   IOPV, AvgBidPrice, AvgAskPrice,
                   TotalVolumeTrade, TotalValueTrade, TotalBidVolume, TotalAskVolume, NumTrades,
                   AskPrices, BidPrices, AskVolumes, BidVolumes
            FROM stock.market
            WHERE SecurityID = ? AND TradeDate = ?
            ORDER BY TradeTime
            """;

    static final String ORDER_QUERY_SQL = """
            SELECT SecurityID, TradeDate, TradeTime, TradeTimeStamp,
                   TickType, Side, Price, Volume, No
            FROM stock.`order`
            WHERE SecurityID = ? AND TradeDate = ?
            ORDER BY TradeTime, No
            """;

    static final String TRANS_QUERY_SQL = """
            SELECT SecurityID, TradeDate, TradeTime, TradeTimeStamp,
                   TickType, Price, Volume, BuyNo, SellNo
            FROM stock.trans
            WHERE SecurityID = ? AND TradeDate = ?
            ORDER BY TradeTime, BuyNo, SellNo
            """;

    private final JdbcTemplate jdbcTemplate;

    public ClickHouseLevel2QueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 全量查询 stock.market 十档快照。 */
    public ClickHouseQueryResult<ClickHouseMarketRow> queryMarket(
            String securityId, LocalDate tradeDate) {
        long startNanos = System.nanoTime();
        List<ClickHouseMarketRow> rows = jdbcTemplate.query(
                MARKET_QUERY_SQL, ClickHouseLevel2QueryService::mapMarket,
                securityId, Date.valueOf(tradeDate));
        return ClickHouseQueryResult.of(
                "stock.market", rows, System.nanoTime() - startNanos);
    }

    /** 全量查询 stock.order 逐笔委托。 */
    public ClickHouseQueryResult<ClickHouseOrderRow> queryOrders(
            String securityId, LocalDate tradeDate) {
        long startNanos = System.nanoTime();
        List<ClickHouseOrderRow> rows = jdbcTemplate.query(
                ORDER_QUERY_SQL, ClickHouseLevel2QueryService::mapOrder,
                securityId, Date.valueOf(tradeDate));
        return ClickHouseQueryResult.of(
                "stock.order", rows, System.nanoTime() - startNanos);
    }

    /** 全量查询 stock.trans 逐笔成交。 */
    public ClickHouseQueryResult<ClickHouseTransRow> queryTransactions(
            String securityId, LocalDate tradeDate) {
        long startNanos = System.nanoTime();
        List<ClickHouseTransRow> rows = jdbcTemplate.query(
                TRANS_QUERY_SQL, ClickHouseLevel2QueryService::mapTrans,
                securityId, Date.valueOf(tradeDate));
        return ClickHouseQueryResult.of(
                "stock.trans", rows, System.nanoTime() - startNanos);
    }

    private static ClickHouseMarketRow mapMarket(ResultSet resultSet, int rowNum)
            throws SQLException {
        return new ClickHouseMarketRow(
                resultSet.getString("SecurityID"),
                resultSet.getString("ExchangeID"),
                resultSet.getDate("TradeDate").toLocalDate(),
                resultSet.getLong("TradeTime"),
                readLocalDateTime(resultSet, "TradeTimeStamp"),
                resultSet.getFloat("LastPrice"),
                resultSet.getFloat("PreClosePrice"),
                resultSet.getFloat("OpenPrice"),
                resultSet.getFloat("HighestPrice"),
                resultSet.getFloat("LowestPrice"),
                resultSet.getFloat("IOPV"),
                resultSet.getFloat("AvgBidPrice"),
                resultSet.getFloat("AvgAskPrice"),
                resultSet.getLong("TotalVolumeTrade"),
                resultSet.getLong("TotalValueTrade"),
                resultSet.getLong("TotalBidVolume"),
                resultSet.getLong("TotalAskVolume"),
                resultSet.getLong("NumTrades"),
                readFloatArray(resultSet, "AskPrices"),
                readFloatArray(resultSet, "BidPrices"),
                readUnsignedIntArray(resultSet, "AskVolumes"),
                readUnsignedIntArray(resultSet, "BidVolumes"));
    }

    private static ClickHouseOrderRow mapOrder(ResultSet resultSet, int rowNum)
            throws SQLException {
        return new ClickHouseOrderRow(
                resultSet.getString("SecurityID"),
                resultSet.getDate("TradeDate").toLocalDate(),
                resultSet.getInt("TradeTime"),
                readLocalDateTime(resultSet, "TradeTimeStamp"),
                resultSet.getString("TickType"),
                resultSet.getString("Side"),
                resultSet.getFloat("Price"),
                resultSet.getLong("Volume"),
                resultSet.getLong("No"));
    }

    private static ClickHouseTransRow mapTrans(ResultSet resultSet, int rowNum)
            throws SQLException {
        return new ClickHouseTransRow(
                resultSet.getString("SecurityID"),
                resultSet.getDate("TradeDate").toLocalDate(),
                resultSet.getInt("TradeTime"),
                readLocalDateTime(resultSet, "TradeTimeStamp"),
                resultSet.getString("TickType"),
                resultSet.getFloat("Price"),
                resultSet.getLong("Volume"),
                resultSet.getLong("BuyNo"),
                resultSet.getLong("SellNo"));
    }

    private static float[] readFloatArray(ResultSet resultSet, String columnName)
            throws SQLException {
        Object rawArray = readSqlArray(resultSet, columnName);
        if (rawArray == null) {
            return new float[0];
        }
        int length = Array.getLength(rawArray);
        float[] values = new float[length];
        for (int index = 0; index < length; index++) {
            values[index] = ((Number) Array.get(rawArray, index)).floatValue();
        }
        return values;
    }

    private static long[] readUnsignedIntArray(ResultSet resultSet, String columnName)
            throws SQLException {
        Object rawArray = readSqlArray(resultSet, columnName);
        if (rawArray == null) {
            return new long[0];
        }
        int length = Array.getLength(rawArray);
        long[] values = new long[length];
        for (int index = 0; index < length; index++) {
            values[index] = ((Number) Array.get(rawArray, index)).longValue();
        }
        return values;
    }

    private static Object readSqlArray(ResultSet resultSet, String columnName)
            throws SQLException {
        java.sql.Array sqlArray = resultSet.getArray(columnName);
        return sqlArray == null ? null : sqlArray.getArray();
    }

    private static LocalDateTime readLocalDateTime(ResultSet resultSet, String columnName)
            throws SQLException {
        Object value = resultSet.getObject(columnName);
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDateTime();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        throw new SQLException("不支持的 DateTime64 JDBC 类型: "
                + (value == null ? "null" : value.getClass().getName()));
    }
}
