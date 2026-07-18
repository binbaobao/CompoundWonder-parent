package com.compoundwonder.backtest.orderbook.data.clickhouse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** stock.market 的一行十档快照数据。 */
public record ClickHouseMarketRow(
        String securityId,
        String exchangeId,
        LocalDate tradeDate,
        long tradeTime,
        LocalDateTime tradeTimeStamp,
        float lastPrice,
        float preClosePrice,
        float openPrice,
        float highestPrice,
        float lowestPrice,
        float iopv,
        float avgBidPrice,
        float avgAskPrice,
        long totalVolumeTrade,
        long totalValueTrade,
        long totalBidVolume,
        long totalAskVolume,
        long numTrades,
        float[] askPrices,
        float[] bidPrices,
        long[] askVolumes,
        long[] bidVolumes) implements ClickHousePayloadRow {

    @Override
    public long estimatedPayloadBytes() {
        return ClickHousePayloadRow.utf8Bytes(securityId)
                + 1L                       // ExchangeID FixedString(1)
                + 2L                       // TradeDate Date
                + 4L                       // TradeTime UInt32
                + 8L                       // TradeTimeStamp DateTime64
                + 8L * Float.BYTES         // 8 个 Float32 行情字段
                + 4L * Long.BYTES          // 4 个 UInt64 累计量字段
                + Integer.BYTES             // NumTrades UInt32
                + arrayLength(askPrices) * (long) Float.BYTES
                + arrayLength(bidPrices) * (long) Float.BYTES
                + arrayLength(askVolumes) * (long) Integer.BYTES
                + arrayLength(bidVolumes) * (long) Integer.BYTES;
    }

    private static int arrayLength(float[] values) {
        return values == null ? 0 : values.length;
    }

    private static int arrayLength(long[] values) {
        return values == null ? 0 : values.length;
    }
}
