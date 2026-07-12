package com.compoundwonder.trader.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.trader.dto.StockEmotionCycleDailyDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.mapper.StockEmotionCycleDailyMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 股票情绪周期每日记录服务实现。
 */
@Service
@DS("trade")
public class StockEmotionCycleDailyServiceImpl extends ServiceImpl<StockEmotionCycleDailyMapper, StockEmotionCycleDaily> implements StockEmotionCycleDailyService {


    @Override
    public void updateStockEmotionCycleDaily(StockEmotionCycleDailyDTO dto, List<StockDailyEntity> stockDailyList) {

        // 今日最高板
        StockDailyEntity stockDailyEntity = stockDailyList.stream().filter(entity -> entity.getConsecutiveLimitUpDays().equals(dto.getHighestConsecutiveLimitUpDays())).findFirst().get();

        //查询前 最高减 3 个交易日
        List<StockEmotionCycleDaily> entityList = list(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .le(StockEmotionCycleDaily::getTradeDate, stockDailyEntity.getTradeDate())
                .orderByDesc(StockEmotionCycleDaily::getTradeDate)
                .last("limit " + (stockDailyEntity.getConsecutiveLimitUpDays() - 3)));
        Integer hisHighestLimitUp = entityList.stream().map(StockEmotionCycleDaily::getHighestConsecutiveLimitUpDays).max(Integer::compareTo).get();

        Integer highestConsecutiveLimitUpDays = dto.getHighestConsecutiveLimitUpDays();
        // 如果昨天的最高➕1 等于今天的最高，并且股票代码也是一样的就直接占领
        if (highestConsecutiveLimitUpDays >= 9 && highestConsecutiveLimitUpDays >= hisHighestLimitUp) {
            for (StockEmotionCycleDaily infoEntity : entityList) {
                infoEntity.setDominantCycleStockCode(stockDailyEntity.getStockCode());
                infoEntity.setDominantCycleStockName(stockDailyEntity.getStockName());
            }
            updateBatchById(entityList);
        } else {
            // 如果今天最高板是 7 板的开始做判断是不是空间龙
            for (StockEmotionCycleDaily infoEntity : entityList) {
                if (StrUtil.isBlank(infoEntity.getDominantCycleStockCode()) || infoEntity.getHighestConsecutiveLimitUpDays() < hisHighestLimitUp) {
                    infoEntity.setDominantCycleStockCode(stockDailyEntity.getStockCode());
                    infoEntity.setDominantCycleStockName(stockDailyEntity.getStockName());
                }
            }
            // 看看实际占领了多少区域
            long count = entityList.stream().filter(e -> e.getDominantCycleStockCode().equals(stockDailyEntity.getStockCode())).count();
            if (count == entityList.size()) {
                updateBatchById(entityList);
            }
        }
    }
}
