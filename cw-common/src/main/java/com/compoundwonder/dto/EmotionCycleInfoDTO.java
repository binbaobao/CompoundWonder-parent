package com.compoundwonder.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * 情绪周期
 *
 * @author chaobin
 * @since 1.0.0 2024-08-18
 */
@Data
public class EmotionCycleInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // "")
    private Long id;

    // "日期")
    private String date;

    // "涨停数")
    private Integer limitUpCount;

    // "连板数")
    private Integer consecutiveLimitUpCount;

    // "跌停数")
    private Integer limitDownCount;

    // "炸板数")
    private Integer explodeCount;

    // "最高板")
    private Integer highestLimitUp;


}
