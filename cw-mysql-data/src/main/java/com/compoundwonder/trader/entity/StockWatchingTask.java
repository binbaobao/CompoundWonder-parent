package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票盯盘任务。
 * 作用：记录策略推荐后需要在指定交易日盯盘的股票。
 */
@Data
@TableName("stock_watching_task")
public class StockWatchingTask {

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
     * 涨停分数。
     */
    private Integer limitUpScore;

    /**
     * 连板次数。
     */
    private Integer consecutiveLimitUpDays;

    /**
     * 推荐日期。
     */
    private LocalDate recommendDate;

    /**
     * 交易日期。
     */
    private LocalDate tradeDate;

    /**
     * 交易模式：1 连板接力，2 普通首板，3 小市值首板。
     */
    private Integer tradeMode;

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;
}
