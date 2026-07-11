package com.compoundwonder.hxdata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.dto.ASharePreviousNamePoint;
import com.compoundwonder.hxdata.entity.StockPreviousNameHistory;
import com.compoundwonder.hxdata.mapper.StockPreviousNameHistoryMapper;
import com.compoundwonder.hxdata.service.StockPreviousNameHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 股票曾用名历史服务实现。
 * 作用：封装当前名、指定日期名称、时间区间名称历史的查询规则。
 */
@Service
@DS("market")
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

    /**
     * 合并每日曾用名变更点。
     * 实现逻辑：按股票代码和开始日期排序处理；重复记录跳过；有旧 open 名称时关闭到新开始日前一天；没有旧 open 名称时直接插入新记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mergeDailyNameChanges(List<ASharePreviousNamePoint> points) {
        List<ASharePreviousNamePoint> sortedPoints = points.stream()
                .filter(point -> point.getStockCode() != null && !point.getStockCode().isBlank())
                .filter(point -> point.getStockName() != null && !point.getStockName().isBlank())
                .filter(point -> point.getStartDate() != null)
                .sorted(Comparator.comparing(ASharePreviousNamePoint::getStockCode)
                        .thenComparing(ASharePreviousNamePoint::getStartDate))
                .toList();
        int insertCount = 0;

        for (ASharePreviousNamePoint point : sortedPoints) {
            StockPreviousNameHistory sameStart = findByStockAndStartDate(point.getStockCode(), point.getStartDate());
            if (sameStart != null) {
                updateSameStartNameIfNeeded(sameStart, point);
                continue;
            }

            StockPreviousNameHistory current = findCurrentOpenName(point.getStockCode());
            if (current != null && !current.getStartDate().equals(point.getStartDate())) {
                current.setEndDate(point.getStartDate().minusDays(1));
                updateById(current);
            }

            StockPreviousNameHistory history = new StockPreviousNameHistory();
            history.setStockCode(point.getStockCode());
            history.setStockName(point.getStockName());
            history.setStartDate(point.getStartDate());
            history.setEndDate(null);
            save(history);
            insertCount++;
        }

        return insertCount;
    }

    /**
     * 查询相同股票和开始日期的记录。
     * 作用：保证每日任务重复执行时不会插入重复曾用名记录。
     */
    private StockPreviousNameHistory findByStockAndStartDate(String stockCode, LocalDate startDate) {
        return getOne(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                .eq(StockPreviousNameHistory::getStockCode, stockCode)
                .eq(StockPreviousNameHistory::getStartDate, startDate)
                .last("LIMIT 1"));
    }

    /**
     * 必要时更新同开始日期记录的名称。
     * 作用：处理接口重跑或源数据修正，避免同一股票同一天出现两条 open 名称。
     */
    private void updateSameStartNameIfNeeded(StockPreviousNameHistory sameStart, ASharePreviousNamePoint point) {
        if (sameStart.getStockName().equals(point.getStockName())) {
            return;
        }

        sameStart.setStockName(point.getStockName());
        updateById(sameStart);
    }

    /**
     * 查询指定股票当前 open 名称记录。
     * 作用：用于每日变更时关闭旧区间。
     */
    private StockPreviousNameHistory findCurrentOpenName(String stockCode) {
        return getOne(Wrappers.<StockPreviousNameHistory>lambdaQuery()
                .eq(StockPreviousNameHistory::getStockCode, stockCode)
                .isNull(StockPreviousNameHistory::getEndDate)
                .orderByDesc(StockPreviousNameHistory::getStartDate)
                .last("LIMIT 1"));
    }
}
