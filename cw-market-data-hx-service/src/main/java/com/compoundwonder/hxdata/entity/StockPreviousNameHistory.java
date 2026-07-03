package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 股票曾用名历史实体。
 * 表示股票在不同时间区间内使用过的证券简称。
 */
@Data
@TableName("stock_previous_name_history")
public class StockPreviousNameHistory {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 股票名称。
     */
    private String stockName;

    /**
     * 名称开始使用日期。
     */
    private LocalDate startDate;

    /**
     * 名称结束使用日期。
     */
    private LocalDate endDate;
}
