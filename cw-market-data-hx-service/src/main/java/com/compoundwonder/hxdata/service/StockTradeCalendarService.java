package com.compoundwonder.hxdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compoundwonder.hxdata.entity.StockTradeCalendar;

import java.time.LocalDate;
import java.util.Collection;

/**
 * 股票交易日历服务。
 * 作用：封装交易日历的去重、批量保存和年度同步入口。
 */
public interface StockTradeCalendarService extends IService<StockTradeCalendar> {

    /**
     * 批量保存交易日期。
     * 表里存在的日期会刷新更新时间，不存在的日期会新增。
     */
    int saveTradeDates(Collection<LocalDate> tradeDates);
}
