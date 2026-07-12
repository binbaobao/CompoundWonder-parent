package com.compoundwonder.trader.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.dto.StockEmotionCycleDailyDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;

import java.util.List;

/**
 * 股票情绪周期每日记录服务。
 * 作用：提供 stock_emotion_cycle_daily 表的基础读写能力。
 */
public interface StockEmotionCycleDailyService extends IService<StockEmotionCycleDaily> {

    /**
     * 如果发生情绪周期的改变，修改情绪周期前面占领
     * @param dto
     * @param stockDailyList
     */
    void updateStockEmotionCycleDaily(StockEmotionCycleDailyDTO dto, List<StockDailyEntity> stockDailyList);
}
