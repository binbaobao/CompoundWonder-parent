package com.compoundwonder.dto;

import lombok.Data;

/**
 * 前端图表 K 线/分时柱数据。
 */
@Data
public class Level2ChartBarDTO {
    private String date;
    private Long timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double prevClose;
    private Long volume;
    private Double turnover;
    private Double turnoverRate;
    private Double amplitude;
    private Double changeRate;
    private Double floatMarketCap;
    private Double totalMarketCap;
    private Long floatShares;
    private Long totalShares;
    private Integer klineState;
    private Integer consecutiveLimitUpDays;
}
