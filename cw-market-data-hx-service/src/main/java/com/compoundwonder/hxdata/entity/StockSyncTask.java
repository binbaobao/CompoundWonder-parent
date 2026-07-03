package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 股票历史数据同步任务实体。
 * 一只股票一条任务记录，用于防止流通股和日 K 历史同步中断后全部重跑。
 */
@Data
@TableName("stock_sync_task")
public class StockSyncTask {

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
     * 是否已完成自由流通股同步。
     */
    private Boolean freeFloatSynced;

    /**
     * 是否已完成日 K 同步。
     */
    private Boolean dailyKlineSynced;

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedTime;
}
