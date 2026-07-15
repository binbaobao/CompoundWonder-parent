package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 回测任务每日收盘后的账户与持仓快照。
 */
@Data
@TableName("backtest_daily_record")
public class BacktestDailyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long backtestRunId;
    private LocalDate tradeDate;
    private Long positionId;
    private Integer accountStatus;
    private String symbol;
    private String symbolName;
    private Integer quantity;
    private BigDecimal availableCash;
    private Integer closePrice;
    private BigDecimal positionMarketValue;
    private BigDecimal totalAsset;
    private BigDecimal dailyReturnRate;
    private BigDecimal cumulativeReturnRate;
    private BigDecimal positionReturnRate;
    private Integer klineState;
    private BigDecimal adjustFactor;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
