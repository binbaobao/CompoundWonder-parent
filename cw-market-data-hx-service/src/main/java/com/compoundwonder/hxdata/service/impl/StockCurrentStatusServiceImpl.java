package com.compoundwonder.hxdata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockCurrentStatus;
import com.compoundwonder.hxdata.entity.StockPreviousNameHistory;
import com.compoundwonder.hxdata.mapper.StockCurrentStatusMapper;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockPreviousNameHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 股票当前状态服务实现。
 * 作用：以曾用名历史表中的股票代码为基础，维护一只股票一条当前状态记录。
 */
@Service
public class StockCurrentStatusServiceImpl extends ServiceImpl<StockCurrentStatusMapper, StockCurrentStatus> implements StockCurrentStatusService {

    private final StockPreviousNameHistoryService stockPreviousNameHistoryService;

    /**
     * 创建股票当前状态服务。
     * 作用：注入曾用名历史服务，用它提供的股票代码作为当前状态初始化来源。
     */
    public StockCurrentStatusServiceImpl(StockPreviousNameHistoryService stockPreviousNameHistoryService) {
        this.stockPreviousNameHistoryService = stockPreviousNameHistoryService;
    }

    /**
     * 根据曾用名历史表初始化股票当前状态。
     * 实现逻辑：先查曾用名表去重股票代码，再排除当前状态表已存在代码，最后批量新增缺失记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initFromPreviousNameHistory() {
        List<String> stockCodes = stockPreviousNameHistoryService.list(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                        .select(StockPreviousNameHistory::getStockCode)
                        .isNotNull(StockPreviousNameHistory::getStockCode))
                .stream()
                .map(StockPreviousNameHistory::getStockCode)
                .distinct()
                .toList();
        if (stockCodes.isEmpty()) {
            return 0;
        }

        Set<String> existsStockCodes = list(Wrappers.<StockCurrentStatus>lambdaQuery()
                        .select(StockCurrentStatus::getStockCode)
                        .in(StockCurrentStatus::getStockCode, stockCodes))
                .stream()
                .map(StockCurrentStatus::getStockCode)
                .collect(Collectors.toSet());

        List<StockCurrentStatus> newStatuses = stockCodes.stream()
                .filter(stockCode -> !existsStockCodes.contains(stockCode))
                .map(this::createDefaultStatus)
                .toList();
        if (newStatuses.isEmpty()) {
            return 0;
        }

        saveBatch(newStatuses);
        return newStatuses.size();
    }

    /**
     * 确保指定股票存在当前状态记录。
     * 实现逻辑：已存在时不处理；不存在时按默认状态新增记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ensureStatus(String stockCode) {
        return ensureStatus(stockCode, true);
    }

    /**
     * 确保指定股票存在当前状态记录。
     * 实现逻辑：已存在时不处理；不存在时按传入融资融券默认值新增记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ensureStatus(String stockCode, boolean marginTrading) {
        long exists = count(Wrappers.<StockCurrentStatus>lambdaQuery()
                .eq(StockCurrentStatus::getStockCode, stockCode));
        if (exists > 0) {
            return false;
        }

        StockCurrentStatus status = createDefaultStatus(stockCode);
        status.setMarginTrading(marginTrading);
        return save(status);
    }

    /**
     * 关闭指定股票融资融券标识。
     * 实现逻辑：戴帽时关闭融资融券；摘帽不调用本方法，因此不会自动恢复。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableMarginTrading(String stockCode) {
        ensureStatus(stockCode, true);
        return update(Wrappers.<StockCurrentStatus>lambdaUpdate()
                .eq(StockCurrentStatus::getStockCode, stockCode)
                .set(StockCurrentStatus::getMarginTrading, false));
    }

    /**
     * 查询全部当前状态股票代码。
     * 实现逻辑：按股票代码升序返回，用于串行同步地域等当前属性。
     */
    @Override
    public List<String> listAllStockCodes() {
        return list(Wrappers.<StockCurrentStatus>lambdaQuery()
                .select(StockCurrentStatus::getStockCode)
                .isNotNull(StockCurrentStatus::getStockCode)
                .orderByAsc(StockCurrentStatus::getStockCode))
                .stream()
                .map(StockCurrentStatus::getStockCode)
                .distinct()
                .toList();
    }

    /**
     * 刷新可转债标识。
     * 实现逻辑：先重置全表 convertible_bond 为 false，再按有效转债正股代码批量置为 true。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshConvertibleBondFlags(Set<String> stockCodes) {
        update(Wrappers.<StockCurrentStatus>lambdaUpdate()
                .set(StockCurrentStatus::getConvertibleBond, false));
        if (stockCodes == null || stockCodes.isEmpty()) {
            return 0;
        }

        int updateCount = 0;
        for (String stockCode : stockCodes) {
            boolean updated = update(Wrappers.<StockCurrentStatus>lambdaUpdate()
                    .eq(StockCurrentStatus::getStockCode, stockCode)
                    .set(StockCurrentStatus::getConvertibleBond, true));
            if (updated) {
                updateCount++;
            }
        }
        return updateCount;
    }

    /**
     * 更新指定股票地域名称。
     * 实现逻辑：当前状态记录存在时直接更新；不存在时先创建默认记录再写入地域。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRegionName(String stockCode, String regionName) {
        ensureStatus(stockCode);
        return update(Wrappers.<StockCurrentStatus>lambdaUpdate()
                .eq(StockCurrentStatus::getStockCode, stockCode)
                .set(StockCurrentStatus::getRegionName, regionName));
    }

    /**
     * 创建默认当前状态对象。
     * 默认规则：新记录默认允许融资融券；可转债和地域等字段等待后续接口维护。
     */
    private StockCurrentStatus createDefaultStatus(String stockCode) {
        StockCurrentStatus status = new StockCurrentStatus();
        status.setStockCode(stockCode);
        status.setMarginTrading(true);
        status.setConvertibleBond(false);
        return status;
    }
}
