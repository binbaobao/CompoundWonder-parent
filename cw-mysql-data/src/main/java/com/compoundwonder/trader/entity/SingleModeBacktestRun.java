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
    /** 固定复用选股结果时对应的源任务；普通按日期选股回测为空。 */
    private Long sourceRunId;
    /** 每轮优化的稳定版本标识，用于把代码提交与回测结果对应起来。 */
    private String strategyVersion;
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
