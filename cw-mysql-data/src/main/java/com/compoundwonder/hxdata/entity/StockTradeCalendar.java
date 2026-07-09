package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票交易日历实体。
 * 表里存在的日期即表示真实交易日，不存在的日期按非交易日处理。
 */
@Data
@TableName("stock_trade_calendar")
public class StockTradeCalendar {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 交易日期。
     */
    private LocalDate tradeDate;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedTime;
}
