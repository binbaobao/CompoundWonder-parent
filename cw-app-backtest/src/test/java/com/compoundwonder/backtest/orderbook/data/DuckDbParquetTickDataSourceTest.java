package com.compoundwonder.backtest.orderbook.data;

import com.compoundwonder.core.engine.TickData;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DuckDbParquetTickDataSourceTest {

    @Test
    void decodesLegacyPackedTickFields() {
        TickData tick = DuckDbParquetTickDataSource.decodeTick(
                1600000, 121, 93000000, 88, 1050, 200, 101, 202);

        assertEquals(1, tick.dataType);
        assertEquals(2, tick.direction);
        assertEquals(1, tick.type);
        assertEquals(0, tick.amount);
        assertEquals(101, tick.buyerOrderId);
        assertEquals(202, tick.sellerOrderId);
    }

    @Test
    void preservesShanghaiLegacyTypeTenCorrection() {
        TickData tick = DuckDbParquetTickDataSource.decodeTick(
                1600001, 100, 93000000, 88, 1050, 100, 0, 0);

        assertEquals(1, tick.dataType);
        assertEquals(0, tick.direction);
        assertEquals(10, tick.type);
    }

    @Test
    void skipsAmountCalculationForLargeNonTradeQuantity() {
        TickData tick = DuckDbParquetTickDataSource.decodeTick(
                1_000_001, 121, 93000000, 88, 1000, 300_000_001, 101, 202);

        assertEquals(0, tick.amount);
    }

    @Test
    void calculatesAmountForTradeUsingExistingIntegerUnitConvention() {
        TickData tick = DuckDbParquetTickDataSource.decodeTick(
                1_000_001, 200, 93000000, 88, 1050, 200, 101, 202);

        assertEquals(2100, tick.amount);
    }

    @Test
    void replaysRealParquetWhenLocalFixtureExists() {
        Path dataDirectory = Path.of(System.getProperty("user.home"), "Documents", "lev2data");
        assumeTrue(Files.isRegularFile(dataDirectory.resolve("2026-07-14.parquet")));
        DuckDbParquetTickDataSource dataSource = new DuckDbParquetTickDataSource(dataDirectory.toString());
        AtomicLong consumed = new AtomicLong();

        long replayed = dataSource.replay(LocalDate.of(2026, 7, 14), "600664", tick -> {
            assertEquals(1_600_664, tick.symbolId);
            consumed.incrementAndGet();
        });

        assertTrue(replayed > 0);
        assertEquals(replayed, consumed.get());
    }
}
