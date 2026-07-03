package com.compoundwonder.hxdata.service.impl;

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
}
