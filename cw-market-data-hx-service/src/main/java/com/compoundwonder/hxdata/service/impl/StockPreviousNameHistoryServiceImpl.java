package com.compoundwonder.hxdata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockPreviousNameHistory;
import com.compoundwonder.hxdata.mapper.StockPreviousNameHistoryMapper;
import com.compoundwonder.hxdata.service.StockPreviousNameHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 股票曾用名历史服务实现。
 * 作用：封装当前名、指定日期名称、时间区间名称历史的查询规则。
 */
@Service
public class StockPreviousNameHistoryServiceImpl extends ServiceImpl<StockPreviousNameHistoryMapper, StockPreviousNameHistory> implements StockPreviousNameHistoryService {

    /**
     * 查询股票当前使用名称。
     * 实现逻辑：查询指定股票代码下 end_date 为空的记录。
     */
    @Override
    public Optional<StockPreviousNameHistory> findCurrentName(String stockCode) {
        return Optional.ofNullable(getOne(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                .eq(StockPreviousNameHistory::getStockCode, stockCode)
                .isNull(StockPreviousNameHistory::getEndDate)
                .last("LIMIT 1")));
    }

    /**
     * 查询指定日期股票使用名称。
     * 实现逻辑：查询指定日期落在 start_date 和 end_date 区间内的记录。
     */
    @Override
    public Optional<StockPreviousNameHistory> findNameByDate(String stockCode, LocalDate tradeDate) {
        return Optional.ofNullable(getOne(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                .eq(StockPreviousNameHistory::getStockCode, stockCode)
                .le(StockPreviousNameHistory::getStartDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockPreviousNameHistory::getEndDate)
                        .or()
                        .ge(StockPreviousNameHistory::getEndDate, tradeDate))
                .last("LIMIT 1")));
    }

    /**
     * 按时间区间查询股票名称历史。
     * 实现逻辑：查询名称使用区间和传入查询区间有交集的所有记录，并按开始日期升序返回。
     */
    @Override
    public List<StockPreviousNameHistory> findNamesByDateRange(String stockCode, LocalDate startDate, LocalDate endDate) {
        return list(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                .eq(StockPreviousNameHistory::getStockCode, stockCode)
                .le(StockPreviousNameHistory::getStartDate, endDate)
                .and(wrapper -> wrapper.isNull(StockPreviousNameHistory::getEndDate)
                        .or()
                        .ge(StockPreviousNameHistory::getEndDate, startDate))
                .orderByAsc(StockPreviousNameHistory::getStartDate));
    }
}
