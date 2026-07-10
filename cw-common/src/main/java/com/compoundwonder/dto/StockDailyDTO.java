package com.compoundwonder.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票日K线数据表
 *
 * @author chaobin
 * @since 1.0.0 2025-12-17
 */
@Data
public class StockDailyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // "主键ID")
    private Long id;

    // "股票代码")
    private String stockCode;

    // "股票名称")
    private String stockName;

    // "交易日期")
    private Date tradeDate;

    // "开盘价")
    private BigDecimal openPrice;

    // "最高价")
    private BigDecimal highPrice;

    // "最低价")
    private BigDecimal lowPrice;

    // "收盘价")
    private BigDecimal closePrice;

    // "前收盘价")
    private BigDecimal prevClose;

    // "涨停价")
    private BigDecimal limitUpPrice;

    // "跌停价")
    private BigDecimal limitDownPrice;

    // "成交量(股)")
    private Long volume;

    // "成交额(万元)")
    private BigDecimal turnover;

    // "换手率(%)")
    private BigDecimal turnoverRate;

    // "涨跌幅(%)")
    private BigDecimal changeRate;

    // "振幅(%)")
    private BigDecimal amplitude;

    // "流通市值(万元)")
    private BigDecimal floatMarketCap;

    // "总市值(万元)")
    private BigDecimal totalMarketCap;

    // "流通股本(万股)")
    private Long floatShares;

    // "总股本(万股)")
    private Long totalShares;

    // "K线状态(-13到13)")
    private Integer klineState;

    // "连续涨停天数")
    private Integer consecutiveLimitUpDays;

    // "是否ST股票(0否1是)")
    private Integer isStock;

    // "创建时间")
    private Date createdAt;

    // "更新时间")
    private Date updatedAt;


}
