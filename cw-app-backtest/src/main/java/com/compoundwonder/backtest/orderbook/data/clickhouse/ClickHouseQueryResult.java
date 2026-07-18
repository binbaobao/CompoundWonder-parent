package com.compoundwonder.backtest.orderbook.data.clickhouse;

import java.util.List;

/** 一次 ClickHouse 全量查询的返回数据和本地映射统计。 */
public record ClickHouseQueryResult<T extends ClickHousePayloadRow>(
        String tableName,
        List<T> rows,
        int rowCount,
        long estimatedPayloadBytes,
        long elapsedNanos) {

    public static <T extends ClickHousePayloadRow> ClickHouseQueryResult<T> of(
            String tableName, List<T> rows, long elapsedNanos) {
        long payloadBytes = 0L;
        for (T row : rows) {
            payloadBytes += row.estimatedPayloadBytes();
        }
        return new ClickHouseQueryResult<>(
                tableName, rows, rows.size(), payloadBytes, elapsedNanos);
    }

    public double elapsedMillis() {
        return elapsedNanos / 1_000_000D;
    }

    public double estimatedPayloadMiB() {
        return estimatedPayloadBytes / 1024D / 1024D;
    }
}
