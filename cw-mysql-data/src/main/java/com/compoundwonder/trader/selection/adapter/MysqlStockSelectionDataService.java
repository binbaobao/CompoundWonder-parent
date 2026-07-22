package com.compoundwonder.trader.selection.adapter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockCurrentStatusData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.hxdata.entity.StockCurrentStatus;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockConvertibleBondHistoryService;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** MySQL 选股原始数据适配器，不包含任何过滤、评分或模式判断。 */
@Service
public class MysqlStockSelectionDataService implements StockSelectionDataService {

    private final StockDailyService stockDailyService;
    private final StockCurrentStatusService stockCurrentStatusService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final StockConvertibleBondHistoryService stockConvertibleBondHistoryService;
    private final StockTradeCalendarService stockTradeCalendarService;

    public MysqlStockSelectionDataService(
            StockDailyService stockDailyService,
            StockCurrentStatusService stockCurrentStatusService,
            StockEmotionCycleDailyService stockEmotionCycleDailyService,
            StockConvertibleBondHistoryService stockConvertibleBondHistoryService,
            StockTradeCalendarService stockTradeCalendarService) {
        this.stockDailyService = stockDailyService;
        this.stockCurrentStatusService = stockCurrentStatusService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
        this.stockConvertibleBondHistoryService = stockConvertibleBondHistoryService;
        this.stockTradeCalendarService = stockTradeCalendarService;
    }

    @Override
    public List<StockDailyData> listDailyByTradeDate(LocalDate tradeDate) {
        return mapDaily(stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)));
    }

    @Override
    public List<StockDailyData> listLatestDaily(String stockCode, LocalDate endDate, int limit) {
        if (stockCode == null || endDate == null || limit <= 0) return List.of();
        return mapDaily(stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, endDate)
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT " + limit)));
    }

    @Override
    public List<StockDailyData> listDailyBetween(String stockCode, LocalDate startDate,
                                                LocalDate endDate) {
        if (stockCode == null || startDate == null || endDate == null) return List.of();
        return mapDaily(stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode)
                .ge(StockDailyEntity::getTradeDate, startDate)
                .le(StockDailyEntity::getTradeDate, endDate)
                .orderByDesc(StockDailyEntity::getTradeDate)));
    }

    @Override
    public List<StockDailyData> listEarliestDaily(String stockCode, LocalDate endDate, int limit) {
        if (stockCode == null || endDate == null || limit <= 0) return List.of();
        return mapDaily(stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, endDate)
                .orderByAsc(StockDailyEntity::getTradeDate)
                .last("LIMIT " + limit)));
    }

    @Override
    public LocalDate findLatestStDate(String stockCode, LocalDate beforeDate) {
        StockDailyEntity daily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockCode)
                .lt(StockDailyEntity::getTradeDate, beforeDate)
                .eq(StockDailyEntity::getIsSt, true)
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 1"));
        return daily == null ? null : daily.getTradeDate();
    }

    @Override
    public LocalDate findFirstTradeDate(String stockCode, LocalDate endDate) {
        StockDailyEntity daily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, endDate)
                .orderByAsc(StockDailyEntity::getTradeDate)
                .last("LIMIT 1"));
        return daily == null ? null : daily.getTradeDate();
    }

    @Override
    public StockCurrentStatusData findCurrentStatus(String stockCode) {
        StockCurrentStatus status = stockCurrentStatusService.getOne(
                Wrappers.<StockCurrentStatus>lambdaQuery()
                        .eq(StockCurrentStatus::getStockCode, stockCode)
                        .last("LIMIT 1"));
        return status == null ? null
                : new StockCurrentStatusData(status.getStockCode(), status.getRegionName());
    }

    @Override
    public List<MarketEmotionData> listLatestMarketEmotion(LocalDate endDate, int limit) {
        return stockEmotionCycleDailyService.list(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                        .le(StockEmotionCycleDaily::getTradeDate, endDate)
                        .orderByDesc(StockEmotionCycleDaily::getTradeDate)
                        .last("LIMIT " + limit))
                .stream()
                .map(entity -> new MarketEmotionData(entity.getTradeDate(),
                        entity.getHighestConsecutiveLimitUpDays(),
                        entity.getDominantCycleStockCode()))
                .toList();
    }

    @Override
    public Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) {
        return stockConvertibleBondHistoryService.listTradableStockCodes(tradeDate);
    }

    @Override
    public LocalDate findNextTradeDate(LocalDate recommendDate) {
        LocalDate date = recommendDate.plusDays(1);
        for (int i = 0; i < 15; i++) {
            if (stockTradeCalendarService.isTradeDay(date)) return date;
            date = date.plusDays(1);
        }
        // 保留老选股逻辑的容错口径：交易日历不完整时回退到下一个自然日。
        return recommendDate.plusDays(1);
    }

    @Override
    public List<LocalDate> listTradeDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return List.of();
        }
        return stockTradeCalendarService.findTradeDays(startDate, endDate);
    }

    private List<StockDailyData> mapDaily(List<StockDailyEntity> entities) {
        return entities.stream().map(this::mapDaily).toList();
    }

    private StockDailyData mapDaily(StockDailyEntity entity) {
        StockDailyData data = new StockDailyData();
        data.setStockCode(entity.getStockCode());
        data.setStockName(entity.getStockName());
        data.setIsSt(entity.getIsSt());
        data.setTradeDate(entity.getTradeDate());
        data.setOpenPrice(entity.getOpenPrice());
        data.setHighPrice(entity.getHighPrice());
        data.setLowPrice(entity.getLowPrice());
        data.setClosePrice(entity.getClosePrice());
        data.setPrevClose(entity.getPrevClose());
        data.setAdjustPreClosePrice(entity.getAdjustPreClosePrice());
        data.setAdjustOpenPrice(entity.getAdjustOpenPrice());
        data.setAdjustHighPrice(entity.getAdjustHighPrice());
        data.setAdjustLowPrice(entity.getAdjustLowPrice());
        data.setAdjustClosePrice(entity.getAdjustClosePrice());
        data.setAdjustFactor(entity.getAdjustFactor());
        data.setVolume(entity.getVolume());
        data.setTurnover(entity.getTurnover());
        data.setTurnoverRate(entity.getTurnoverRate());
        data.setChangeRate(entity.getChangeRate());
        data.setAmplitude(entity.getAmplitude());
        data.setFloatMarketCap(entity.getFloatMarketCap());
        data.setFloatShares(entity.getFloatShares());
        data.setKlineState(entity.getKlineState());
        data.setConsecutiveLimitUpDays(entity.getConsecutiveLimitUpDays());
        return data;
    }
}
