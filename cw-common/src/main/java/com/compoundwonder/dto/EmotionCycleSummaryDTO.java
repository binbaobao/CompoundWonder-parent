package com.compoundwonder.dto;

import lombok.Data;

/**
 * 前端情绪周期摘要。
 */
@Data
public class EmotionCycleSummaryDTO {
    private String date;
    private Integer limitUpCount;
    private Integer yesterdayLimitUpCount;
    private Integer consecutiveLimitUpCount;
    private Integer explodeCount;
    private Integer limitDownCount;
    private Integer highestLimitUp;
    private Double consecutiveRate;
    private Double explodeRate;
    private String leaderCode;
    private String leaderName;
}
