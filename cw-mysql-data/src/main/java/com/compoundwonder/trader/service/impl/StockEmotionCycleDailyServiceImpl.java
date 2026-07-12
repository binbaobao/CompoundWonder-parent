package com.compoundwonder.trader.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.trader.dto.StockEmotionCycleDailyDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.mapper.StockEmotionCycleDailyMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 股票情绪周期每日记录服务实现。
 */
@Service
@DS("trade")
public class StockEmotionCycleDailyServiceImpl extends ServiceImpl<StockEmotionCycleDailyMapper, StockEmotionCycleDaily> implements StockEmotionCycleDailyService {

    private final StockDailyService stockDailyService;

    /**
     * 创建股票情绪周期每日记录服务。
     * 作用：注入日 K 服务，用于从市场日 K 聚合情绪周期基础指标。
     */
    public StockEmotionCycleDailyServiceImpl(StockDailyService stockDailyService) {
        this.stockDailyService = stockDailyService;
    }

    /**
     * 聚合并保存指定交易日情绪周期记录。
     * 处理逻辑：按交易日聚合日 K，存在则更新，不存在则插入；最高板达到周期条件时更新周期占领。
     */
    @Override
    public StockEmotionCycleDaily aggregateAndSave(LocalDate tradeDate) {
        StockEmotionCycleDailyDTO dto = stockDailyService.buildEmotionCycleDaily(tradeDate);
        StockEmotionCycleDaily existing = getOne(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .eq(StockEmotionCycleDaily::getTradeDate, tradeDate)
                .last("LIMIT 1"));

        StockEmotionCycleDaily entity = existing == null ? new StockEmotionCycleDaily() : existing;
        if (entity.getCreatedTime() == null) {
            entity.setCreatedTime(LocalDateTime.now());
        }
        fillEntity(entity, dto);
        saveOrUpdate(entity);

        Integer highestConsecutiveLimitUpDays = dto.getHighestConsecutiveLimitUpDays();
        // 最高板大于等于 7 板才有周期，而且最高板有且只有一个；如果多个 7 板以上最高板，先不确定周期。
        if (highestConsecutiveLimitUpDays != null && highestConsecutiveLimitUpDays >= 7) {
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
            long highestCount = stockDailyList.stream()
                    .filter(stockDaily -> highestConsecutiveLimitUpDays.equals(stockDaily.getConsecutiveLimitUpDays()))
                    .count();
            if (highestCount == 1) {
                updateStockEmotionCycleDaily(dto, stockDailyList);
            }
        }
        return entity;
    }


    /**
     * 如果发生情绪周期的改变，修改情绪周期前面占领。
     */
    @Override
    public void updateStockEmotionCycleDaily(StockEmotionCycleDailyDTO dto, List<StockDailyEntity> stockDailyList) {

        // 今日最高板
        StockDailyEntity stockDailyEntity = stockDailyList.stream()
                .filter(entity -> Objects.equals(entity.getConsecutiveLimitUpDays(), dto.getHighestConsecutiveLimitUpDays()))
                .findFirst()
                .orElse(null);
        if (stockDailyEntity == null || stockDailyEntity.getConsecutiveLimitUpDays() == null) {
            return;
        }

        //查询前 最高减 3 个交易日
        List<StockEmotionCycleDaily> entityList = list(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .le(StockEmotionCycleDaily::getTradeDate, stockDailyEntity.getTradeDate())
                .orderByDesc(StockEmotionCycleDaily::getTradeDate)
                .last("limit " + (stockDailyEntity.getConsecutiveLimitUpDays() - 3)));
        if (entityList.isEmpty()) {
            return;
        }
        Integer hisHighestLimitUp = entityList.stream()
                .map(StockEmotionCycleDaily::getHighestConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        Integer highestConsecutiveLimitUpDays = dto.getHighestConsecutiveLimitUpDays();
        // 如果昨天的最高➕1 等于今天的最高，并且股票代码也是一样的就直接占领
        if (highestConsecutiveLimitUpDays != null && highestConsecutiveLimitUpDays >= 9 && highestConsecutiveLimitUpDays >= hisHighestLimitUp) {
            for (StockEmotionCycleDaily infoEntity : entityList) {
                infoEntity.setDominantCycleStockCode(stockDailyEntity.getStockCode());
                infoEntity.setDominantCycleStockName(stockDailyEntity.getStockName());
            }
            updateBatchById(entityList);
        } else {
            // 如果今天最高板是 7 板的开始做判断是不是空间龙
            for (StockEmotionCycleDaily infoEntity : entityList) {
                Integer infoHighestLimitUp = infoEntity.getHighestConsecutiveLimitUpDays();
                if (StrUtil.isBlank(infoEntity.getDominantCycleStockCode()) || infoHighestLimitUp == null || infoHighestLimitUp < hisHighestLimitUp) {
                    infoEntity.setDominantCycleStockCode(stockDailyEntity.getStockCode());
                    infoEntity.setDominantCycleStockName(stockDailyEntity.getStockName());
                }
            }
            // 看看实际占领了多少区域
            long count = entityList.stream()
                    .filter(e -> Objects.equals(e.getDominantCycleStockCode(), stockDailyEntity.getStockCode()))
                    .count();
            if (count == entityList.size()) {
                updateBatchById(entityList);
            }
        }
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
