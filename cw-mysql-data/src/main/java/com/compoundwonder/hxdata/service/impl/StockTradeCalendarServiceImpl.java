package com.compoundwonder.hxdata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockTradeCalendar;
import com.compoundwonder.hxdata.mapper.StockTradeCalendarMapper;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 股票交易日历服务实现。
 * 作用：把华鑫接口返回的交易日期转换成数据库记录并批量落库。
 */
@Service
@DS("market")
public class StockTradeCalendarServiceImpl extends ServiceImpl<StockTradeCalendarMapper, StockTradeCalendar> implements StockTradeCalendarService {

    /**
     * 批量保存交易日期。
     * 实现逻辑：先按 trade_date 查询已存在记录，再分别更新旧记录和新增新记录。
     */
    @Override
    public int saveTradeDates(Collection<LocalDate> tradeDates) {
        List<LocalDate> distinctTradeDates = tradeDates.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctTradeDates.isEmpty()) {
            return 0;
        }

        List<StockTradeCalendar> exists = list(Wrappers.<StockTradeCalendar>lambdaQuery()
                .in(StockTradeCalendar::getTradeDate, distinctTradeDates));
        Set<LocalDate> existsDates = exists.stream()
                .map(StockTradeCalendar::getTradeDate)
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        exists.forEach(item -> item.setUpdatedTime(now));
        List<StockTradeCalendar> inserts = distinctTradeDates.stream()
                .filter(tradeDate -> !existsDates.contains(tradeDate))
                .map(tradeDate -> {
                    StockTradeCalendar item = new StockTradeCalendar();
                    item.setTradeDate(tradeDate);
                    item.setUpdatedTime(now);
                    return item;
                })
                .toList();

        if (!exists.isEmpty()) {
            updateBatchById(exists);
        }
        if (!inserts.isEmpty()) {
            saveBatch(inserts);
        }
        return distinctTradeDates.size();
    }

    /**
     * 判断指定日期是否为交易日。
     * 实现逻辑：交易日历表中存在该日期即视为交易日。
     */
    @Override
    public boolean isTradeDay(LocalDate tradeDate) {
        return count(Wrappers.<StockTradeCalendar>lambdaQuery()
                .eq(StockTradeCalendar::getTradeDate, tradeDate)) > 0;
    }

    /**
     * 查询指定日期之后的第一个交易日。
     */
    @Override
    public LocalDate findNextTradeDay(LocalDate tradeDate) {
        StockTradeCalendar nextTradeDay = getOne(Wrappers.<StockTradeCalendar>lambdaQuery()
                .select(StockTradeCalendar::getTradeDate)
                .gt(StockTradeCalendar::getTradeDate, tradeDate)
                .orderByAsc(StockTradeCalendar::getTradeDate)
                .last("LIMIT 1"));
        return nextTradeDay == null ? null : nextTradeDay.getTradeDate();
    }

    @Override
    public List<LocalDate> findTradeDays(LocalDate startDate, LocalDate endDate) {
        return list(Wrappers.<StockTradeCalendar>lambdaQuery()
                .select(StockTradeCalendar::getTradeDate)
                .ge(StockTradeCalendar::getTradeDate, startDate)
                .le(StockTradeCalendar::getTradeDate, endDate)
                .orderByAsc(StockTradeCalendar::getTradeDate))
                .stream()
                .map(StockTradeCalendar::getTradeDate)
                .distinct()
                .toList();
    }
}
