package com.compoundwonder.hxdata.dto;

import lombok.Data;

/**
 * 股票日 K 接口数据快照。
 * 作用：在华鑫异步回调中立刻复制字段，避免后续落库逻辑依赖 SDK 原生回调对象。
 */
@Data
public class StockDayQuotationPoint {

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 交易日期，格式 yyyyMMdd。
     */
    private String tradingDay;

    /**
     * 涨停价。
     */
    private Double limitPrice;

    /**
     * 跌停价。
     */
    private Double stoppingPrice;

    /**
     * 开盘价。
     */
    private Double openPrice;

    /**
     * 最高价。
     */
    private Double highPrice;

    /**
     * 最低价。
     */
    private Double lowPrice;

    /**
     * 收盘价。
     */
    private Double closePrice;

    /**
     * 昨收盘价。
     */
    private Double preClosePrice;

    /**
     * 复权昨收盘价。
     */
    private Double adjustPreClosePrice;

    /**
     * 复权开盘价。
     */
    private Double adjustOpenPrice;

    /**
     * 复权最高价。
     */
    private Double adjustHighPrice;

    /**
     * 复权最低价。
     */
    private Double adjustLowPrice;

    /**
     * 复权收盘价。
     */
    private Double adjustClosePrice;

    /**
     * 复权因子。
     */
    private Double adjustFactor;

    /**
     * 成交量，接口单位为手。
     */
    private Double volume;

    /**
     * 成交额，接口原始单位。
     */
    private Double turnover;

    /**
     * 涨跌幅。
     */
    private Double percentChange;
}
