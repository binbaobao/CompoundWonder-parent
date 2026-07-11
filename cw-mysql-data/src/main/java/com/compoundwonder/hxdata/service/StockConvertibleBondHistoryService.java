package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.dto.ConvertibleBondDescriptionPoint;
import com.compoundwonder.hxdata.dto.ConvertibleBondIssuancePoint;
import com.compoundwonder.hxdata.entity.StockConvertibleBondHistory;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 股票可转债历史服务。
 * 作用：维护正股和可转债关系，以及可转债上市到退市的生命周期区间。
 */
public interface StockConvertibleBondHistoryService extends IService<StockConvertibleBondHistory> {

    /**
     * 保存或更新可转债发行关系。
     */
    boolean saveOrUpdateIssuance(ConvertibleBondIssuancePoint point);

    /**
     * 根据可转债基本资料补全生命周期字段。
     */
    boolean fillDescription(ConvertibleBondDescriptionPoint point);

    /**
     * 查询还需要补全基本资料的转债代码。
     * 处理逻辑：上市日期、退市日期、到期日期或存续余额缺失时，认为需要补全。
     */
    List<String> listPendingDescriptionBondCodes(int limit);

    /**
     * 查询指定日期已经上市且尚未退市的转债正股代码。
     * 处理逻辑：startDate 必须存在且不晚于目标日期，endDate 为空或不早于目标日期，发行失败的不计入。
     */
    Set<String> listTradableStockCodes(LocalDate tradeDate);
}
