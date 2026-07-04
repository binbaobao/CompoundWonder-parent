package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.dto.StockDayQuotationPoint;
import com.compoundwonder.hxdata.entity.StockDailyEntity;

import java.util.List;

/**
 * 股票日 K 服务。
 * 作用：把华鑫日 K 行情转换成 stock_daily 记录并批量落库。
 */
public interface StockDailyService extends IService<StockDailyEntity> {

    /**
     * 替换指定股票的日 K 历史数据。
     * 处理逻辑：先删除该股票旧日 K，再按交易日期升序计算衍生字段并批量保存。
     */
    int replaceStockDaily(String stockCode, List<StockDayQuotationPoint> quotations);
}
