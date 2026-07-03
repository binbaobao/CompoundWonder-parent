package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.dto.FreeFloatSharePoint;
import com.compoundwonder.hxdata.entity.StockFreeFloatShareHistory;

import java.util.List;

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
}
