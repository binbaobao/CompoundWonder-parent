package com.compoundwonder.dto;

import lombok.Data;

/**
 * Level2 回测工作台股票池行。
 */
@Data
public class Level2StockPoolDTO {
    private String code;
    private Integer symbolId;
    private String name;
    private String historyName;
    private String boardLabel;
    private String theme;
    private Integer strength;
    private String amount;
    private Double price;
    private Double resultRate;
    private String scope;

    private Double turnover;
    private Double turnoverRate;
    private Double changeRate;
    private Double amplitude;
    private Long volume;
    private Integer klineState;
    private Integer consecutiveLimitUpDays;
    private Integer lbc;

    private Integer zz;
    private Integer rz;
    private Integer st;
    private Integer historySt;
    private String province;
    private Integer safetyScore;
    private Integer blacklist;
}
