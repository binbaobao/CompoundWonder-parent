package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票情绪周期每日记录。
 * 作用：记录全市场每日涨跌停、连板、涨跌家数及周期占领标的。
 */
@Data
@TableName("stock_emotion_cycle_daily")
public class StockEmotionCycleDaily {

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
     * 涨停家数。
     */
    private Integer limitUpCount;

    /**
     * 跌停家数。
     */
    private Integer limitDownCount;

    /**
     * 连板数。
     */
    private Integer consecutiveLimitUpCount;

    /**
     * 跌停数。
     */
    private Integer downLimitCount;

    /**
     * 占领周期股票代码。
     */
    private String dominantCycleStockCode;

    /**
     * 占领周期股票名称。
     */
    private String dominantCycleStockName;

    /**
     * 涨家数。
     */
    private Integer risingCount;

    /**
     * 跌家数。
     */
    private Integer fallingCount;

    /**
     * 全市场成交金额，单位：亿元。
     */
    private BigDecimal allMarketTurnoverAmount;

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;
}
