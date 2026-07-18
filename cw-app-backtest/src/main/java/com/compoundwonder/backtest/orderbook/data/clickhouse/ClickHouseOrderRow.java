package com.compoundwonder.backtest.orderbook.data.clickhouse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** stock.order 的一行逐笔委托数据。 */
public record ClickHouseOrderRow(
        String securityId,
        LocalDate tradeDate,
        int tradeTime,
        LocalDateTime tradeTimeStamp,
        String tickType,
        String side,
        float price,
        long volume,
        long no) implements ClickHousePayloadRow {

    @Override
    public long estimatedPayloadBytes() {
        return ClickHousePayloadRow.utf8Bytes(securityId)
                + 2L                       // TradeDate Date
                + Integer.BYTES             // TradeTime Int32
                + 8L                       // TradeTimeStamp DateTime64
                + 1L                       // TickType FixedString(1)
                + 1L                       // Side FixedString(1)
                + Float.BYTES               // Price Float32
                + Integer.BYTES             // Volume UInt32
                + Integer.BYTES;            // No UInt32
    }
}
