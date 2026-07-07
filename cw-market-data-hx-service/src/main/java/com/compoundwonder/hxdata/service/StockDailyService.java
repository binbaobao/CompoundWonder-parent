package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.dto.StockDayQuotationPoint;
import com.compoundwonder.hxdata.entity.StockDailyEntity;

import java.time.LocalDate;
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

    /**
     * 保存指定交易日的全市场日 K。
     * 处理逻辑：按股票代码和交易日期覆盖当天数据，并基于前一条日 K 计算连板/断板。
     */
    int saveMarketDaily(LocalDate tradeDate, List<StockDayQuotationPoint> quotations);
}
