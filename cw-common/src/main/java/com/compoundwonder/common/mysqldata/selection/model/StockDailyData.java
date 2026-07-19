package com.compoundwonder.common.mysqldata.selection.model;

import lombok.Data;

import java.time.LocalDate;

/** 选股策略需要的原始日 K 数据，不包含 MyBatis 映射信息。 */
@Data
public class StockDailyData {
    private String stockCode;
    private String stockName;
    private Boolean isSt;
    private LocalDate tradeDate;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Double prevClose;
    private Double adjustPreClosePrice;
    private Double adjustOpenPrice;
    private Double adjustHighPrice;
    private Double adjustLowPrice;
    private Double adjustClosePrice;
    private Double adjustFactor;
    private Long volume;
    private Double turnover;
    private Double turnoverRate;
    private Double changeRate;
    private Double amplitude;
    private Double floatMarketCap;
    private Long floatShares;
    private Integer klineState;
    private Integer consecutiveLimitUpDays;
}
