package com.compoundwonder.strategy.selection;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockCurrentStatusData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class DefaultStockSelectionServiceTest {

    @Test
    void selectsSmallCapFirstBoardWithoutWritingWatchingTasks() {
        StockSelectionDataService dataService = new EmptySelectionDataService();
        LocalDate date = LocalDate.of(2026, 1, 5);

        DefaultStockSelectionService service = new DefaultStockSelectionService(dataService);

        assertEquals(List.of(), service.select(date, TradeMode.SMALL_CAP_FIRST_BOARD));
    }

    @Test
    void rejectsModesNotEnabledByThisSingleModeBacktestRelease() {
        DefaultStockSelectionService service = new DefaultStockSelectionService(
                new EmptySelectionDataService());

        assertThrows(UnsupportedOperationException.class,
                () -> service.select(LocalDate.of(2026, 1, 5), TradeMode.RELAY_LIMIT_UP));
        assertThrows(UnsupportedOperationException.class,
                () -> service.select(LocalDate.of(2026, 1, 5), TradeMode.FIRST_BOARD));
    }

    private static final class EmptySelectionDataService implements StockSelectionDataService {
        @Override public List<StockDailyData> listDailyByTradeDate(LocalDate tradeDate) { return List.of(); }
        @Override public List<StockDailyData> listLatestDaily(String stockCode, LocalDate endDate, int limit) { return List.of(); }
        @Override public List<StockDailyData> listDailyBetween(String stockCode, LocalDate startDate, LocalDate endDate) { return List.of(); }
        @Override public List<StockDailyData> listEarliestDaily(String stockCode, LocalDate endDate, int limit) { return List.of(); }
        @Override public LocalDate findLatestStDate(String stockCode, LocalDate beforeDate) { return null; }
        @Override public LocalDate findFirstTradeDate(String stockCode, LocalDate endDate) { return null; }
        @Override public StockCurrentStatusData findCurrentStatus(String stockCode) { return null; }
        @Override public List<MarketEmotionData> listLatestMarketEmotion(LocalDate endDate, int limit) { return List.of(); }
        @Override public Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) { return Set.of(); }
        @Override public LocalDate findNextTradeDate(LocalDate recommendDate) { return recommendDate.plusDays(1); }
    }
}
