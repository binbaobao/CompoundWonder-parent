package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockCurrentStatus;

import java.util.List;
import java.util.Set;

/**
 * 股票当前状态服务。
 * 作用：维护融资融券、可转债、地域等当前属性。
 */
public interface StockCurrentStatusService extends IService<StockCurrentStatus> {

    /**
     * 根据曾用名历史表初始化股票当前状态。
     * 处理逻辑：取曾用名历史表中的去重股票代码，缺失的当前状态记录才新增。
     */
    int initFromPreviousNameHistory();

    /**
     * 确保指定股票存在当前状态记录。
     */
    boolean ensureStatus(String stockCode);

    /**
     * 查询全部当前状态股票代码。
     */
    List<String> listAllStockCodes();

    /**
     * 刷新可转债标识。
     * 处理逻辑：先把全表可转债标识置为否，再把存在有效可转债的股票置为是。
     */
    int refreshConvertibleBondFlags(Set<String> stockCodes);

    /**
     * 更新指定股票地域名称。
     */
    boolean updateRegionName(String stockCode, String regionName);
}
