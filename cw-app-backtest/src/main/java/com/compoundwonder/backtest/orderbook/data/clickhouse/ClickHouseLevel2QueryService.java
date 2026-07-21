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
import java.util.function.Consumer;

/**
 * ClickHouse Level2 三表查询服务。
 *
 * <p>每个查询都按 SecurityID 和 TradeDate 参数化过滤，不设置 LIMIT，并将 JDBC
 * ResultSet 全量映射为强类型 Java 对象。耗时包含服务端查询、网络传输和本地对象映射。</p>
 */
@Service
@DS("clickhouse")
public class ClickHouseLevel2QueryService {

    private static final String DAILY_BATCH_ORDER_SELECT = """
            SELECT SecurityID,
                   toUInt32(TradeTime) AS TradeTime,
                   toUInt8(0) AS EventSource,
                   toString(TickType) AS TickType,
                   toString(Side) AS Side,
                   toFloat32(Price) AS Price,
                   toUInt64(Volume) AS Volume,
                   toUInt64(No) AS OrderNo,
                   toUInt64(0) AS BuyNo,
                   toUInt64(0) AS SellNo,
                   toFloat32(0) AS AskPrice1,
                   toFloat32(0) AS BidPrice1,
                   toUInt64(0) AS AskVolume1,
                   toUInt64(0) AS AskVolume2,
                   toUInt64(0) AS BidVolume1,
                   toUInt64(0) AS BidVolume2,
                   toUInt64(0) AS TotalVolumeTrade,
                   toUInt64(0) AS TotalValueTrade,
                   toUInt8(0) AS EventPriority,
                   toUInt64(No) AS PrimarySortNo,
                   toUInt64(CASE
                       WHEN TickType = 'D' OR (TickType = '0' AND Price = 0) THEN 1
                       WHEN TickType IN ('A', '0', '1', '2', '3') THEN 0
                       ELSE 2
                   END) AS SecondarySortNo
            FROM stock.`order`
            WHERE TradeDate = ? AND SecurityID IN (%s)
              AND TickType IN ('A', 'D', '0', '1', '2', '3')
            """;

    private static final String DAILY_BATCH_TRANS_SELECT = """
            SELECT SecurityID,
                   toUInt32(TradeTime) AS TradeTime,
                   toUInt8(1) AS EventSource,
                   toString(TickType) AS TickType,
                   '' AS Side,
                   toFloat32(Price) AS Price,
                   toUInt64(Volume) AS Volume,
                   toUInt64(0) AS OrderNo,
                   toUInt64(BuyNo) AS BuyNo,
                   toUInt64(SellNo) AS SellNo,
                   toFloat32(0) AS AskPrice1,
                   toFloat32(0) AS BidPrice1,
                   toUInt64(0) AS AskVolume1,
                   toUInt64(0) AS AskVolume2,
                   toUInt64(0) AS BidVolume1,
                   toUInt64(0) AS BidVolume2,
                   toUInt64(0) AS TotalVolumeTrade,
                   toUInt64(0) AS TotalValueTrade,
                   toUInt8(1) AS EventPriority,
                   toUInt64(BuyNo) AS PrimarySortNo,
                   toUInt64(SellNo) AS SecondarySortNo
            FROM stock.trans
            WHERE TradeDate = ? AND SecurityID IN (%s)
            """;

    private static final String DAILY_BATCH_MARKET_SELECT = """
            SELECT SecurityID,
                   toUInt32(TradeTime) AS TradeTime,
                   toUInt8(2) AS EventSource,
                   '' AS TickType,
                   '' AS Side,
                   toFloat32(0) AS Price,
                   toUInt64(0) AS Volume,
                   toUInt64(0) AS OrderNo,
                   toUInt64(0) AS BuyNo,
                   toUInt64(0) AS SellNo,
                   toFloat32(AskPrices[1]) AS AskPrice1,
                   toFloat32(BidPrices[1]) AS BidPrice1,
                   toUInt64(AskVolumes[1]) AS AskVolume1,
                   toUInt64(AskVolumes[2]) AS AskVolume2,
                   toUInt64(BidVolumes[1]) AS BidVolume1,
                   toUInt64(BidVolumes[2]) AS BidVolume2,
                   toUInt64(TotalVolumeTrade) AS TotalVolumeTrade,
                   toUInt64(TotalValueTrade) AS TotalValueTrade,
                   toUInt8(2) AS EventPriority,
                   toUInt64(0) AS PrimarySortNo,
                   toUInt64(0) AS SecondarySortNo
            FROM stock.market
            WHERE TradeDate = ? AND SecurityID IN (%s)
            """;

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
            -- 沪深可能在同一毫秒、同一订单号同时保存新增和撤单。
            -- 上海新增、深圳有效价格委托必须先于上海 D、深圳零价 0 撤单，避免幽灵委托。
            ORDER BY TradeTime, No,
                     CASE
                         WHEN TickType = 'D' OR (TickType = '0' AND Price = 0) THEN 1
                         WHEN TickType IN ('A', '0', '1', '2', '3') THEN 0
                         ELSE 2
                     END
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

    /**
     * 使用一条 UNION ALL 查询流式读取当天全部候选股票的必要 Level2 字段。
     *
     * <p>排序先按股票分组，再按毫秒时间排序；同一毫秒固定委托、成交、快照的顺序。
     * 委托内部继续按订单号排序，并保证上海新增、深圳有效价格委托在同订单号的
     * 上海 D、深圳零价 0 撤单之前，避免撤单先到导致幽灵委托。</p>
     */
    @DS("clickhouse")
    public ClickHouseDailyQueryResult streamDailyTicks(
            LocalDate tradeDate, List<String> securityIds,
            Consumer<ClickHouseLevel2BatchRow> rowConsumer) {
        if (securityIds.isEmpty()) {
            return new ClickHouseDailyQueryResult(0, 0);
        }
        String sql = buildDailyBatchQuerySql(securityIds.size());
        long startNanos = System.nanoTime();
        long[] rowCount = {0};
        jdbcTemplate.query(sql, preparedStatement -> {
            int parameterIndex = 1;
            for (int tableIndex = 0; tableIndex < 3; tableIndex++) {
                preparedStatement.setDate(parameterIndex++, Date.valueOf(tradeDate));
                for (String securityId : securityIds) {
                    preparedStatement.setString(parameterIndex++, securityId);
                }
            }
            preparedStatement.setFetchSize(10_000);
        }, resultSet -> {
            rowConsumer.accept(mapDailyBatchRow(resultSet));
            rowCount[0]++;
        });
        return new ClickHouseDailyQueryResult(rowCount[0], System.nanoTime() - startNanos);
    }

    /** 构建与股票数量匹配的参数化 UNION ALL SQL。 */
    static String buildDailyBatchQuerySql(int securityCount) {
        if (securityCount <= 0) {
            throw new IllegalArgumentException("批量查询股票数量必须大于 0");
        }
        String placeholders = String.join(", ", java.util.Collections.nCopies(securityCount, "?"));
        return """
                SELECT SecurityID, TradeTime, EventSource, TickType, Side, Price, Volume,
                       OrderNo, BuyNo, SellNo, AskPrice1, BidPrice1,
                       AskVolume1, AskVolume2, BidVolume1, BidVolume2,
                       TotalVolumeTrade, TotalValueTrade
                FROM (
                %s
                UNION ALL
                %s
                UNION ALL
                %s
                )
                ORDER BY SecurityID, TradeTime, EventPriority, PrimarySortNo, SecondarySortNo
                """.formatted(
                DAILY_BATCH_ORDER_SELECT.formatted(placeholders),
                DAILY_BATCH_TRANS_SELECT.formatted(placeholders),
                DAILY_BATCH_MARKET_SELECT.formatted(placeholders));
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

    private static ClickHouseLevel2BatchRow mapDailyBatchRow(ResultSet resultSet)
            throws SQLException {
        return new ClickHouseLevel2BatchRow(
                resultSet.getString("SecurityID"),
                resultSet.getInt("TradeTime"),
                resultSet.getByte("EventSource"),
                resultSet.getString("TickType"),
                resultSet.getString("Side"),
                resultSet.getFloat("Price"),
                resultSet.getLong("Volume"),
                resultSet.getLong("OrderNo"),
                resultSet.getLong("BuyNo"),
                resultSet.getLong("SellNo"),
                resultSet.getFloat("AskPrice1"),
                resultSet.getFloat("BidPrice1"),
                resultSet.getLong("AskVolume1"),
                resultSet.getLong("AskVolume2"),
                resultSet.getLong("BidVolume1"),
                resultSet.getLong("BidVolume2"),
                resultSet.getLong("TotalVolumeTrade"),
                resultSet.getLong("TotalValueTrade"));
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
