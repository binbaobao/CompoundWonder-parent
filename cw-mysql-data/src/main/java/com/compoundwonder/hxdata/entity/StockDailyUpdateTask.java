package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票每日更新任务记录。
 * 作用：记录每个交易日盘前、盘后各步骤是否执行完成，便于任务中断后排查和补跑。
 */
@Data
@TableName("stock_daily_update_task")
public class StockDailyUpdateTask {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务日期。
     */
    private LocalDate taskDate;

    /**
     * 是否交易日。
     */
    private Boolean tradeDay;

    /**
     * 曾用名同步是否完成。
     */
    private Boolean previousNameSynced;

    /**
     * 新上市股票发现是否完成。
     */
    private Boolean newListingSynced;

    /**
     * 自由流通股本同步是否完成。
     */
    private Boolean freeFloatSynced;

    /**
     * 可转债当前状态同步是否完成。
     */
    private Boolean convertibleBondSynced;

    /**
     * 地域信息同步是否完成。
     */
    private Boolean regionSynced;

    /**
     * 融资融券标识维护是否完成。
     */
    private Boolean marginTradingSynced;

    /**
     * 日 K 同步是否完成。
     */
    private Boolean dailyKlineSynced;

    /**
     * 盘前任务是否完成。
     */
    private Boolean preOpenFinished;

    /**
     * 盘后任务是否完成。
     */
    private Boolean postCloseFinished;

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedTime;
}
