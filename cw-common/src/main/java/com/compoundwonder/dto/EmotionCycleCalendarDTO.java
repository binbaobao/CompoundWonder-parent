package com.compoundwonder.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 情绪周期日历项
 *
 * @author chaobin
 * @since 1.0.0
 */
@Data
public class EmotionCycleCalendarDTO {

    private LocalDate date;

    private Integer limitUpCount;

    private Integer explodeCount;

    private Integer highestLimitUp;
}
