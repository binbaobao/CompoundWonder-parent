package com.compoundwonder.hxdata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.dto.FreeFloatSharePoint;
import com.compoundwonder.hxdata.entity.StockFreeFloatShareHistory;
import com.compoundwonder.hxdata.mapper.StockFreeFloatShareHistoryMapper;
import com.compoundwonder.hxdata.service.StockFreeFloatShareHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 股票自由流通股本历史区间服务实现。
 * 作用：把每天一条的接口数据按股本变化压缩成区间数据。
 */
@Service
@DS("market")
public class StockFreeFloatShareHistoryServiceImpl extends ServiceImpl<StockFreeFloatShareHistoryMapper, StockFreeFloatShareHistory> implements StockFreeFloatShareHistoryService {

    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");

    /**
     * 保存指定股票的自由流通股本历史区间。
     * 实现逻辑：先把自由流通股相同的连续日期压缩成区间，确认有数据后再替换旧历史。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceStockHistory(String stockCode, List<FreeFloatSharePoint> points) {
        List<StockFreeFloatShareHistory> histories = buildHistories(stockCode, points);
        if (histories.isEmpty()) {
            return 0;
        }

        remove(Wrappers.<StockFreeFloatShareHistory>lambdaQuery()
                .eq(StockFreeFloatShareHistory::getStockCode, stockCode));
        saveBatch(histories, 1000);
        return histories.size();
    }

    /**
     * 查询指定日期有效的自由流通股本区间。
     * 实现逻辑：start_date 小于等于交易日，且 end_date 为空或大于等于交易日。
     */
    @Override
    public Optional<StockFreeFloatShareHistory> findByTradeDate(String stockCode, LocalDate tradeDate) {
        return Optional.ofNullable(getOne(Wrappers.<StockFreeFloatShareHistory>lambdaQuery()
                .eq(StockFreeFloatShareHistory::getStockCode, stockCode)
                .le(StockFreeFloatShareHistory::getStartDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockFreeFloatShareHistory::getEndDate)
                        .or()
                        .ge(StockFreeFloatShareHistory::getEndDate, tradeDate))
                .last("LIMIT 1")));
    }

    /**
     * 按时间区间查询有效的自由流通股本历史。
     * 实现逻辑：查询自由流通股区间和传入查询区间有交集的所有记录，并按开始日期升序返回。
     */
    @Override
    public List<StockFreeFloatShareHistory> findByDateRange(String stockCode, LocalDate startDate, LocalDate endDate) {
        return list(Wrappers.<StockFreeFloatShareHistory>lambdaQuery()
                .eq(StockFreeFloatShareHistory::getStockCode, stockCode)
                .le(StockFreeFloatShareHistory::getStartDate, endDate)
                .and(wrapper -> wrapper.isNull(StockFreeFloatShareHistory::getEndDate)
                        .or()
                        .ge(StockFreeFloatShareHistory::getEndDate, startDate))
                .orderByAsc(StockFreeFloatShareHistory::getStartDate));
    }

    /**
     * 增量合并自由流通股本变化点。
     * 实现逻辑：按股票和日期升序处理；股本不变时跳过，股本变化时关闭当前开区间并新增新区间。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mergeChangePoints(List<FreeFloatSharePoint> points) {
        List<FreeFloatSharePoint> sortedPoints = points.stream()
                .filter(point -> point.getStockCode() != null && !point.getStockCode().isBlank())
                .filter(point -> point.getChangeDate() != null)
                .filter(point -> point.getFreeSharesTenThousand() != null)
                .sorted(Comparator.comparing(FreeFloatSharePoint::getStockCode)
                        .thenComparing(FreeFloatSharePoint::getChangeDate))
                .toList();
        if (sortedPoints.isEmpty()) {
            return 0;
        }

        Map<String, StockFreeFloatShareHistory> currentHistoryCache = new LinkedHashMap<>();
        int changeCount = 0;
        for (FreeFloatSharePoint point : sortedPoints) {
            StockFreeFloatShareHistory current = currentHistoryCache.computeIfAbsent(point.getStockCode(), this::findCurrentHistory);
            Long freeShares = toShares(point.getFreeSharesTenThousand());

            if (current == null) {
                StockFreeFloatShareHistory created = createHistory(point.getStockCode(), freeShares, point);
                save(created);
                currentHistoryCache.put(point.getStockCode(), created);
                changeCount++;
                continue;
            }

            if (point.getChangeDate().isBefore(current.getStartDate())) {
                continue;
            }

            if (point.getChangeDate().isEqual(current.getStartDate())) {
                if (!Objects.equals(current.getFreeShares(), freeShares)) {
                    current.setFreeShares(freeShares);
                    current.setAnnouncementDate(point.getAnnouncementDate());
                    updateById(current);
                    changeCount++;
                }
                continue;
            }

            if (Objects.equals(current.getFreeShares(), freeShares)) {
                continue;
            }

            current.setEndDate(point.getChangeDate().minusDays(1));
            updateById(current);

            StockFreeFloatShareHistory created = createHistory(point.getStockCode(), freeShares, point);
            save(created);
            currentHistoryCache.put(point.getStockCode(), created);
            changeCount++;
        }

        return changeCount;
    }

    /**
     * 查询当前仍有效的自由流通股本区间。
     */
    private StockFreeFloatShareHistory findCurrentHistory(String stockCode) {
        return getOne(Wrappers.<StockFreeFloatShareHistory>lambdaQuery()
                .eq(StockFreeFloatShareHistory::getStockCode, stockCode)
                .isNull(StockFreeFloatShareHistory::getEndDate)
                .orderByDesc(StockFreeFloatShareHistory::getStartDate)
                .last("LIMIT 1"));
    }

    /**
     * 构建自由流通股本历史区间。
     * 实现逻辑：按生效日期升序处理，股本变化时关闭上一段区间并开启新段区间。
     */
    private List<StockFreeFloatShareHistory> buildHistories(String stockCode, List<FreeFloatSharePoint> points) {
        List<FreeFloatSharePoint> sortedPoints = points.stream()
                .filter(point -> point.getChangeDate() != null)
                .filter(point -> point.getFreeSharesTenThousand() != null)
                .sorted(Comparator.comparing(FreeFloatSharePoint::getChangeDate))
                .toList();

        List<StockFreeFloatShareHistory> histories = new ArrayList<>();
        StockFreeFloatShareHistory current = null;
        for (FreeFloatSharePoint point : sortedPoints) {
            Long freeShares = toShares(point.getFreeSharesTenThousand());
            if (current == null) {
                current = createHistory(stockCode, freeShares, point);
                histories.add(current);
                continue;
            }

            if (Objects.equals(current.getFreeShares(), freeShares)) {
                continue;
            }

            current.setEndDate(point.getChangeDate().minusDays(1));
            current = createHistory(stockCode, freeShares, point);
            histories.add(current);
        }

        return histories;
    }

    /**
     * 创建自由流通股本历史区间。
     * 默认区间结束日期为空，表示当前区间仍然有效。
     */
    private StockFreeFloatShareHistory createHistory(String stockCode, Long freeShares, FreeFloatSharePoint point) {
        StockFreeFloatShareHistory history = new StockFreeFloatShareHistory();
        history.setStockCode(stockCode);
        history.setFreeShares(freeShares);
        history.setStartDate(point.getChangeDate());
        history.setAnnouncementDate(point.getAnnouncementDate());
        return history;
    }

    /**
     * 把接口返回的万股转换成股。
     * 转换规则：万股乘以 10000，四舍五入成整数股。
     */
    private Long toShares(BigDecimal freeSharesTenThousand) {
        return freeSharesTenThousand.multiply(TEN_THOUSAND)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
