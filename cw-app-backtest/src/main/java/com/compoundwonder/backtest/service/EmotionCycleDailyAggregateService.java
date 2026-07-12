package com.compoundwonder.backtest.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.trader.dto.StockEmotionCycleDailyDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 情绪周期每日聚合服务。
 * 作用：在回测应用内把市场日 K 聚合结果写入回测/交易研究库。
 */
@Service
public class EmotionCycleDailyAggregateService {

    private final StockDailyService stockDailyService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;

    public EmotionCycleDailyAggregateService(StockDailyService stockDailyService, StockEmotionCycleDailyService stockEmotionCycleDailyService) {
        this.stockDailyService = stockDailyService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
    }

    /**
     * 聚合并保存指定交易日情绪周期记录。
     * 处理逻辑：按交易日期查询既有记录，存在则更新，不存在则插入。
     */
    public StockEmotionCycleDaily aggregateAndSave(LocalDate tradeDate) {
        StockEmotionCycleDailyDTO dto = stockDailyService.buildEmotionCycleDaily(tradeDate);
        StockEmotionCycleDaily existing = stockEmotionCycleDailyService.getOne(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .eq(StockEmotionCycleDaily::getTradeDate, tradeDate)
                .last("LIMIT 1"));

        StockEmotionCycleDaily entity = existing == null ? new StockEmotionCycleDaily() : existing;
        if (entity.getCreatedTime() == null) {
            entity.setCreatedTime(LocalDateTime.now());
        }
        fillEntity(entity, dto);
        stockEmotionCycleDailyService.saveOrUpdate(entity);

        Integer highestConsecutiveLimitUpDays = dto.getHighestConsecutiveLimitUpDays();
        // 最高板大于等于 7 板才有周期，而且最高板有且只有一个
        // 如果有多个 7板以上的 最高板 先不确定周期
        if (highestConsecutiveLimitUpDays >= 7 ) {
            List<StockDailyEntity> stockDailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                    .select(StockDailyEntity::getStockCode,
                            StockDailyEntity::getStockName,
                            StockDailyEntity::getTradeDate,
                            StockDailyEntity::getIsSt,
                            StockDailyEntity::getKlineState,
                            StockDailyEntity::getConsecutiveLimitUpDays,
                            StockDailyEntity::getChangeRate,
                            StockDailyEntity::getTurnover)
                    .eq(StockDailyEntity::getTradeDate, tradeDate));
            if (stockDailyList.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays().equals(highestConsecutiveLimitUpDays)).count() == 1){
                stockEmotionCycleDailyService.updateStockEmotionCycleDaily(dto, stockDailyList);
            }
        }
        return entity;
    }

    /**
     * 把聚合 DTO 映射到情绪周期实体。
     */
    private void fillEntity(StockEmotionCycleDaily entity, StockEmotionCycleDailyDTO dto) {
        entity.setTradeDate(dto.getTradeDate());
        entity.setLimitUpCount(dto.getLimitUpCount());
        entity.setLimitDownCount(dto.getLimitDownCount());
        entity.setConsecutiveLimitUpCount(dto.getConsecutiveLimitUpCount());
        entity.setHighestConsecutiveLimitUpDays(dto.getHighestConsecutiveLimitUpDays());
        entity.setLimitUpBrokenCount(dto.getLimitUpBrokenCount());
        entity.setDownLimitCount(dto.getDownLimitCount());
        entity.setDominantCycleStockCode(dto.getDominantCycleStockCode());
        entity.setDominantCycleStockName(dto.getDominantCycleStockName());
        entity.setRisingCount(dto.getRisingCount());
        entity.setFallingCount(dto.getFallingCount());
        entity.setAllMarketTurnoverAmount(dto.getAllMarketTurnoverAmount());
    }
}
