package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.orderbook.data.clickhouse.ClickHouseLevel2QueryService;
import com.compoundwonder.backtest.orderbook.data.clickhouse.ClickHouseMarketRow;
import com.compoundwonder.backtest.orderbook.data.clickhouse.ClickHouseQueryResult;
import com.compoundwonder.dto.Level2MinuteTickDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Level2MinuteBarServiceImplTest {

    private static final LocalDate DATE = LocalDate.of(2026, 7, 16);

    @Test
    void buildsAuctionTicksAndLastContinuousSnapshotPerMinuteFromClickHouse() {
        ClickHouseLevel2QueryService queryService = new StubQueryService(List.of(
                        market(91_500_000, 10.50F, 10.49F, 0, 0),
                        market(93_000_000, 10.51F, 10.50F, 1_000, 100),
                        market(93_030_000, 10.52F, 10.51F, 2_000, 200),
                        market(93_059_000, 10.53F, 10.52F, 3_000_000_000L, 300)));
        StockDailyEntity daily = new StockDailyEntity();
        daily.setPrevClose(10D);
        StockDailyService stockDailyService = (StockDailyService) Proxy.newProxyInstance(
                StockDailyService.class.getClassLoader(), new Class<?>[]{StockDailyService.class},
                (proxy, method, args) -> "getOne".equals(method.getName()) ? daily : null);

        List<Level2MinuteTickDTO> ticks = new Level2MinuteBarServiceImpl(queryService, stockDailyService)
                .findMinuteBars("603567", DATE);

        assertEquals(2, ticks.size());
        assertEquals("09:15:00", ticks.get(0).getTickTime());
        assertEquals(10.51D, ticks.get(0).getPrice());
        assertEquals(400, ticks.get(0).getDataType());
        assertEquals("09:30:59", ticks.get(1).getTickTime());
        assertEquals(10.52D, ticks.get(1).getRawPrice());
        assertEquals(30_000_000L, ticks.get(1).getAmount());
        assertEquals(401, ticks.get(1).getDataType());
        assertEquals(300L, ticks.get(1).getSellerOrderId());
        assertEquals(1_603_567, ticks.get(1).getSymbolId());
        assertEquals(0, ticks.get(1).getHandlerIndex());
    }

    private static ClickHouseMarketRow market(long time, float ask, float bid,
                                               long totalValue, long totalVolume) {
        int compact = (int) time;
        int hhmmss = compact / 1_000;
        int millis = compact % 1_000;
        LocalDateTime timestamp = DATE.atTime(hhmmss / 10_000, hhmmss / 100 % 100,
                hhmmss % 100, millis * 1_000_000);
        return new ClickHouseMarketRow("603567", "1", DATE, time, timestamp,
                bid, 10F, 10F, ask, bid, 0, bid, ask,
                totalVolume, totalValue, 0, 0, 0,
                new float[]{ask, ask + 0.01F}, new float[]{bid, bid - 0.01F},
                new long[]{10, 20}, new long[]{30, 40});
    }

    private static final class StubQueryService extends ClickHouseLevel2QueryService {
        private final ClickHouseQueryResult<ClickHouseMarketRow> result;

        private StubQueryService(List<ClickHouseMarketRow> rows) {
            super(null);
            this.result = ClickHouseQueryResult.of("stock.market", rows, 1);
        }

        @Override
        public ClickHouseQueryResult<ClickHouseMarketRow> queryMarket(String securityId,
                                                                      LocalDate tradeDate) {
            return result;
        }
    }
}
