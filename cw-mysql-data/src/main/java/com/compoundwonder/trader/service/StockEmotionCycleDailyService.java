package com.compoundwonder.trader.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.dto.StockEmotionCycleDailyDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票情绪周期每日记录服务。
 * 作用：提供 stock_emotion_cycle_daily 表的基础读写能力。
 */
public interface StockEmotionCycleDailyService extends IService<StockEmotionCycleDaily> {

    /**
     * 聚合并保存指定交易日的情绪周期记录。
     * 作用：从日 K 聚合每日情绪指标，写入情绪周期表，并在满足条件时更新周期占领标的。
     */
    StockEmotionCycleDaily aggregateAndSave(LocalDate tradeDate);

    /**
     * 如果发生情绪周期的改变，修改情绪周期前面占领
     * @param dto
     * @param stockDailyList
     */
    void updateStockEmotionCycleDaily(StockEmotionCycleDailyDTO dto, List<StockDailyEntity> stockDailyList);

    /**
     * 查询一段历史区间内全市场最高连板的平均高度。
     *
     * @param startDateInclusive 起始日期，包含
     * @param endDateExclusive 结束日期，不包含，避免历史回测读取当日结果
     * @return 四舍五入后的平均高度；区间没有数据时返回 {@code null}
     */
    Integer queryRecentAverageLimitUpHeight(LocalDate startDateInclusive,
                                            LocalDate endDateExclusive);
}
