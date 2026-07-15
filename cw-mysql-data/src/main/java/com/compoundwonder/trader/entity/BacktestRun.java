package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 一次连续日期区间回测任务。
 */
@Data
@TableName("backtest_run")
public class BacktestRun {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal initialCapital;
    private Integer shanghaiDelayMs;
    private Integer shenzhenDelayMs;
    private Integer overnightFillTime;
    private Integer status;
    private LocalDate lastCompletedDate;
    private BigDecimal finalAsset;
    private BigDecimal totalReturnRate;
    private String errorMessage;
    private LocalDateTime startedTime;
    private LocalDateTime finishedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
