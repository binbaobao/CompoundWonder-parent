package com.compoundwonder.trader.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.mapper.StockEmotionCycleDailyMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import org.springframework.stereotype.Service;

/**
 * 股票情绪周期每日记录服务实现。
 */
@Service
@DS("trade")
public class StockEmotionCycleDailyServiceImpl extends ServiceImpl<StockEmotionCycleDailyMapper, StockEmotionCycleDaily> implements StockEmotionCycleDailyService {
}
