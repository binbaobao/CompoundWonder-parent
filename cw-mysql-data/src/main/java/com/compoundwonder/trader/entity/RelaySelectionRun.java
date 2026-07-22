package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 一轮连板触发与日 K 理论结果研究任务。 */
@Data
@TableName("relay_selection_run")
public class RelaySelectionRun {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sourceRunId;
    private Integer runType;
    private String strategyVersion;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDate lastCompletedDate;
    private String parameterSnapshot;
    private String runRemark;
    private Integer triggerCount;
    private Integer emptyPositionCount;
    private Integer rawCandidateCount;
    private Integer eligibleCandidateCount;
    private Integer selectedCandidateCount;
    private Integer touchedLimitUpCount;
    private Integer sealedLimitUpCount;
    private Integer brokenLimitUpCount;
    private Integer theoreticalWinCount;
    private Integer dailyOpportunityCount;
    private Integer dailyBestCapturedCount;
    private BigDecimal touchRate;
    private BigDecimal sealRate;
    private BigDecimal breakRate;
    private BigDecimal theoreticalWinRate;
    private BigDecimal dailyBestCaptureRate;
    private BigDecimal theoreticalReturnCaptureRate;
    private BigDecimal averageTheoreticalMaxReturnRate;
    private BigDecimal medianTheoreticalMaxReturnRate;
    private BigDecimal averageDailyBestReturnRate;
    private String metricDetail;
    private String errorMessage;
    private LocalDateTime startedTime;
    private LocalDateTime finishedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
