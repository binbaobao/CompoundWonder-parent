package com.compoundwonder.backtest.orderbook.data.clickhouse;

/** 单日多股票 Level2 流式查询统计。 */
record ClickHouseDailyQueryResult(long rowCount, long elapsedNanos) {

    double elapsedMillis() {
        return elapsedNanos / 1_000_000D;
    }
}
