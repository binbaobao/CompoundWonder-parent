package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 一次不受仓位约束的单模式全样本回测任务。 */
@Data
@TableName("single_mode_backtest_run")
public class SingleModeBacktestRun {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer tradeMode;
    /** 1 运行中，2 完成，3 失败。 */
    private Integer status;
    private LocalDate lastCompletedDate;
    private Integer totalSamples;
    private Integer processedSamples;
    private Integer boughtSamples;
    private Integer closedSamples;
    private String errorMessage;
    private LocalDateTime startedTime;
    private LocalDateTime finishedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
