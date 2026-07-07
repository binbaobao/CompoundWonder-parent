package com.compoundwonder.hxdata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.dto.ConvertibleBondDescriptionPoint;
import com.compoundwonder.hxdata.dto.ConvertibleBondIssuancePoint;
import com.compoundwonder.hxdata.entity.StockConvertibleBondHistory;
import com.compoundwonder.hxdata.mapper.StockConvertibleBondHistoryMapper;
import com.compoundwonder.hxdata.service.StockConvertibleBondHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 股票可转债历史服务实现。
 * 作用：按 bond_code + market 唯一维护转债和正股对应关系，并逐步补全生命周期字段。
 */
@Service
public class StockConvertibleBondHistoryServiceImpl extends ServiceImpl<StockConvertibleBondHistoryMapper, StockConvertibleBondHistory> implements StockConvertibleBondHistoryService {

    /**
     * 保存或更新可转债发行关系。
     * 实现逻辑：只保存已经有关联正股且已有上市日期的转债；上市日期为空说明当前不可交易，先不入库。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateIssuance(ConvertibleBondIssuancePoint point) {
        if (point == null || isBlank(point.getStockCode()) || isBlank(point.getBondCode()) || isBlank(point.getMarket()) || point.getListDate() == null) {
            return false;
        }

        StockConvertibleBondHistory history = findByBondCodeAndMarket(point.getBondCode(), point.getMarket());
        if (history == null) {
            history = new StockConvertibleBondHistory();
            history.setBondCode(point.getBondCode());
            history.setMarket(point.getMarket());
        }

        history.setStockCode(point.getStockCode());
        history.setBondName(point.getBondName());
        history.setStartDate(point.getListDate());
        return saveOrUpdate(history);
    }

    /**
     * 根据可转债基本资料补全生命周期字段。
     * 实现逻辑：按 bond_code + market 找到已存在发行关系后补充上市日期、退市日期、到期日期、失效标识和存续余额；不存在时不新增。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fillDescription(ConvertibleBondDescriptionPoint point) {
        if (point == null || isBlank(point.getBondCode()) || isBlank(point.getMarket())) {
            return false;
        }

        StockConvertibleBondHistory history = findByBondCodeAndMarket(point.getBondCode(), point.getMarket());
        if (history == null) {
            return false;
        }

        if (!isBlank(point.getBondName())) {
            history.setBondName(point.getBondName());
        }
        history.setStartDate(point.getStartDate());
        history.setEndDate(point.getEndDate());
        history.setMaturityDate(point.getMaturityDate());
        history.setFailure(point.getFailure());
        history.setOutstandingBalance(point.getOutstandingBalance());
        return saveOrUpdate(history);
    }

    /**
     * 查询还需要补全基本资料的转债代码。
     * 实现逻辑：只要生命周期字段有缺失，就取转债代码；同一代码跨市场复用时去重，避免重复查询。
     */
    @Override
    public List<String> listPendingDescriptionBondCodes(int limit) {
        return list(Wrappers.<StockConvertibleBondHistory>lambdaQuery()
                .select(StockConvertibleBondHistory::getBondCode)
                .isNotNull(StockConvertibleBondHistory::getStockCode)
                .and(wrapper -> wrapper.isNull(StockConvertibleBondHistory::getStartDate)
                        .or()
                        .isNull(StockConvertibleBondHistory::getEndDate)
                        .or()
                        .isNull(StockConvertibleBondHistory::getMaturityDate)
                        .or()
                        .isNull(StockConvertibleBondHistory::getOutstandingBalance))
                .orderByAsc(StockConvertibleBondHistory::getBondCode))
                .stream()
                .map(StockConvertibleBondHistory::getBondCode)
                .distinct()
                .limit(limit)
                .toList();
    }

    /**
     * 查询指定日期已经上市且尚未退市的转债正股代码。
     * 实现逻辑：只有 start_date 已存在并且不晚于目标日期，才认为当前可交易；未上市转债不刷新到当前状态。
     */
    @Override
    public Set<String> listTradableStockCodes(LocalDate tradeDate) {
        return list(Wrappers.<StockConvertibleBondHistory>lambdaQuery()
                .select(StockConvertibleBondHistory::getStockCode)
                .isNotNull(StockConvertibleBondHistory::getStockCode)
                .ne(StockConvertibleBondHistory::getStockCode, "")
                .isNotNull(StockConvertibleBondHistory::getStartDate)
                .le(StockConvertibleBondHistory::getStartDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockConvertibleBondHistory::getEndDate)
                        .or()
                        .ge(StockConvertibleBondHistory::getEndDate, tradeDate))
                .and(wrapper -> wrapper.isNull(StockConvertibleBondHistory::getFailure)
                        .or()
                        .eq(StockConvertibleBondHistory::getFailure, 0)))
                .stream()
                .map(StockConvertibleBondHistory::getStockCode)
                .collect(Collectors.toSet());
    }

    /**
     * 按转债代码和市场查询记录。
     */
    private StockConvertibleBondHistory findByBondCodeAndMarket(String bondCode, String market) {
        return getOne(Wrappers.<StockConvertibleBondHistory>lambdaQuery()
                .eq(StockConvertibleBondHistory::getBondCode, bondCode)
                .eq(StockConvertibleBondHistory::getMarket, market)
                .last("LIMIT 1"));
    }

    /**
     * 判断字符串是否为空。
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
