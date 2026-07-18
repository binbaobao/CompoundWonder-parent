package com.compoundwonder.backtest.orderbook.data.clickhouse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** stock.trans 的一行逐笔成交数据。 */
public record ClickHouseTransRow(
        String securityId,
        LocalDate tradeDate,
        int tradeTime,
        LocalDateTime tradeTimeStamp,
        String tickType,
        float price,
        long volume,
        long buyNo,
        long sellNo) implements ClickHousePayloadRow {

    @Override
    public long estimatedPayloadBytes() {
        return ClickHousePayloadRow.utf8Bytes(securityId)
                + 2L                       // TradeDate Date
                + Integer.BYTES             // TradeTime Int32
                + 8L                       // TradeTimeStamp DateTime64
                + 1L                       // TickType FixedString(1)
                + Float.BYTES               // Price Float32
                + Integer.BYTES             // Volume UInt32
                + Integer.BYTES             // BuyNo UInt32
                + Integer.BYTES;            // SellNo UInt32
    }
}
