package com.compoundwonder.common.strategy.selection.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 一只股票通过某个模式选股后的中立任务结果。 */
@Data
public class SelectionTaskData {
    private String stockCode;
    private String stockName;
    private Integer limitUpScore;
    private Integer consecutiveLimitUpDays;
    private LocalDate recommendDate;
    private LocalDate tradeDate;
    private Integer tradeMode;
    /** 连板接力触发点；其他模式为空。 */
    private String selectionTrigger;
    /** 连板接力过滤强度；其他模式为空。 */
    private String selectionStrength;
    /** 生成该任务的选股规则版本。 */
    private String strategyVersion;
    /** 对应的研究运行 ID；正式选股可为空。 */
    private Long selectionRunId;
    /** 对应的连板候选审计记录 ID；未落审计表时为空。 */
    private Long relayCandidateRecordId;
    private LocalDateTime createdTime;
}
