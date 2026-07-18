package com.compoundwonder.backtest.orderbook.data.clickhouse;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClickHouseLevel2QueryModelTest {

    @Test
    void estimatesUncompressedMarketFieldPayload() {
        ClickHouseMarketRow row = new ClickHouseMarketRow(
                "603567", "1", LocalDate.of(2026, 7, 16), 93001001,
                LocalDateTime.of(2026, 7, 16, 9, 30, 1, 1_000_000),
                10F, 9F, 9.5F, 10.1F, 9.4F, 0F, 9.9F, 10F,
                100L, 1_000L, 200L, 300L, 10L,
                new float[10], new float[10], new long[10], new long[10]);

        assertEquals(249L, row.estimatedPayloadBytes());
    }

    @Test
    void estimatesUncompressedOrderAndTransactionFieldPayload() {
        ClickHouseOrderRow order = new ClickHouseOrderRow(
                "603567", LocalDate.of(2026, 7, 16), 93001001,
                LocalDateTime.of(2026, 7, 16, 9, 30, 1, 1_000_000),
                "A", "1", 10F, 100L, 123L);
        ClickHouseTransRow trans = new ClickHouseTransRow(
                "603567", LocalDate.of(2026, 7, 16), 93001001,
                LocalDateTime.of(2026, 7, 16, 9, 30, 1, 1_000_000),
                "T", 10F, 100L, 123L, 124L);

        assertEquals(34L, order.estimatedPayloadBytes());
        assertEquals(37L, trans.estimatedPayloadBytes());
    }

    @Test
    void queryResultAggregatesEveryReturnedRowWithoutLimit() {
        ClickHouseOrderRow row = new ClickHouseOrderRow(
                "603567", LocalDate.of(2026, 7, 16), 93001001,
                LocalDateTime.of(2026, 7, 16, 9, 30, 1),
                "A", "1", 10F, 100L, 123L);

        ClickHouseQueryResult<ClickHouseOrderRow> result =
                ClickHouseQueryResult.of("stock.order", List.of(row, row), 1_500_000L);

        assertEquals(2, result.rowCount());
        assertEquals(68L, result.estimatedPayloadBytes());
        assertEquals(1.5D, result.elapsedMillis(), 0.000001D);
    }

    @Test
    void level2QueriesDoNotContainReturnLimit() {
        assertFalse(ClickHouseLevel2QueryService.MARKET_QUERY_SQL.toLowerCase().contains("limit"));
        assertFalse(ClickHouseLevel2QueryService.ORDER_QUERY_SQL.toLowerCase().contains("limit"));
        assertFalse(ClickHouseLevel2QueryService.TRANS_QUERY_SQL.toLowerCase().contains("limit"));
    }
}
