package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockPreviousNameHistory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 股票曾用名历史服务。
 * 作用：以曾用名历史表作为股票代码和股票名称的权威来源。
 */
public interface StockPreviousNameHistoryService extends IService<StockPreviousNameHistory> {

    /**
     * 查询股票当前使用名称。
     * 判断规则：end_date 为空的记录就是当前名称。
     */
    Optional<StockPreviousNameHistory> findCurrentName(String stockCode);

    /**
     * 查询指定日期股票使用名称。
     * 判断规则：start_date 小于等于查询日期，且 end_date 为空或大于等于查询日期。
     */
    Optional<StockPreviousNameHistory> findNameByDate(String stockCode, LocalDate tradeDate);

    /**
     * 按时间区间查询股票名称历史。
     * 判断规则：名称使用区间和查询区间存在交集即返回。
     */
    List<StockPreviousNameHistory> findNamesByDateRange(String stockCode, LocalDate startDate, LocalDate endDate);
}
