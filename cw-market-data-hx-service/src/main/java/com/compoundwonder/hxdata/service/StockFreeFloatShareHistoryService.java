package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.dto.FreeFloatSharePoint;
import com.compoundwonder.hxdata.entity.StockFreeFloatShareHistory;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 股票自由流通股本历史区间服务。
 * 作用：把接口按日返回的自由流通股本压缩成变化区间并落库。
 */
public interface StockFreeFloatShareHistoryService extends IService<StockFreeFloatShareHistory> {

    /**
     * 保存指定股票的自由流通股本历史区间。
     * 处理逻辑：先清理该股票旧区间，再按自由流通股数变化压缩生成新区间。
     */
    int replaceStockHistory(String stockCode, List<FreeFloatSharePoint> points);

    /**
     * 查询指定日期有效的自由流通股本区间。
     */
    Optional<StockFreeFloatShareHistory> findByTradeDate(String stockCode, LocalDate tradeDate);

    /**
     * 按时间区间查询有效的自由流通股本历史。
     * 判断规则：自由流通股区间和查询区间存在交集即返回。
     */
    List<StockFreeFloatShareHistory> findByDateRange(String stockCode, LocalDate startDate, LocalDate endDate);
}
