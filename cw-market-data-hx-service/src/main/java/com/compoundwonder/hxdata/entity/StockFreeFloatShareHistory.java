package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票自由流通股本历史区间实体。
 * 作用：用开闭区间记录自由流通股本变化，避免每天重复保存相同股本。
 */
@Data
@TableName("stock_free_float_share_history")
public class StockFreeFloatShareHistory {

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
     * 自由流通股本，单位：股。
     */
    private Long freeShares;

    /**
     * 区间开始日期。
     */
    private LocalDate startDate;

    /**
     * 区间结束日期，空值表示当前仍有效。
     */
    private LocalDate endDate;

    /**
     * 公告日期。
     */
    private LocalDate announcementDate;

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedTime;
}
