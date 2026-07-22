package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 一个选股日的连板触发事实、候选汇总和理论最优结果。 */
@Data
@TableName("relay_selection_trigger_record")
public class RelaySelectionTriggerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long runId;
    private LocalDate recommendDate;
    private LocalDate tradeDate;
    private String mainTriggerType;
    private String effectiveTriggerType;
    private Integer todayHighestBoard;
    private Integer yesterdayHighestBoard;
    private Integer dayBeforeHighestBoard;
    private LocalDate referenceDate;
    private String referenceStockCode;
    private String referenceStockName;
    private Integer referenceBoard;
    private String referenceDominantStockCode;
    private String currentDominantStockCode;
    private String weakFiveStockCode;
    private String weakFiveStockName;
    private String weakFiveReason;
    private String selectionPlan;
    private Integer rawCandidateCount;
    private Integer eligibleCandidateCount;
    private Integer selectedCandidateCount;
    private Boolean isEmptyPosition;
    private Integer touchedLimitUpCount;
    private Integer sealedLimitUpCount;
    private Integer brokenLimitUpCount;
    private Integer theoreticalWinCount;
    private Long dailyBestCandidateRecordId;
    private String dailyBestStockCode;
    private String dailyBestStockName;
    private BigDecimal dailyBestReturnRate;
    private Long selectedBestCandidateRecordId;
    private String selectedBestStockCode;
    private BigDecimal selectedBestReturnRate;
    private Boolean isDailyBestCaptured;
    private BigDecimal theoreticalReturnCaptureRate;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
