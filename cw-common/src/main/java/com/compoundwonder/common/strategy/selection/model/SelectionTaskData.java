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
    private LocalDateTime createdTime;
}
