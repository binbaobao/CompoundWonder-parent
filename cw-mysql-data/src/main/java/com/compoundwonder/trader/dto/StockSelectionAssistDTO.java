package com.compoundwonder.trader.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 选股辅助对象。
 * 作用：承接涨停候选股的过滤、打分所需基础字段。
 */
@Data
public class StockSelectionAssistDTO {

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 股票名称。
     */
    private String stockName;

    /**
     * 交易日期。
     */
    private LocalDate tradeDate;

    /**
     * 连板次数。
     */
    private Integer consecutiveLimitUpDays;

    /**
     * 省份属性。
     */
    private String province;

    /**
     * 当日价格。
     */
    private Double currentPrice;

    /**
     * 启动市值，本轮首板涨停前一交易日的收盘流通市值，单位：万元。
     */
    private Double startMarketCap;

    /**
     * 非 ST 月份数，上次摘帽或新上市至今的自然月数。
     */
    private Integer nonStMonthCount;

    /**
     * 最大换手率。
     */
    private Double maxTurnoverRate;

    /**
     * 最高板。
     */
    private Integer highestConsecutiveLimitUpDays;

    /**
     * 非正常状态次数，统计 klineState != 0 的交易日数量。
     */
    private Integer abnormalKlineStateCount;

    /**
     * 5 日涨跌幅，使用复权收盘价计算。
     */
    private Double fiveDayChangeRate;

    /**
     * 10 日涨跌幅，使用复权收盘价计算。
     */
    private Double tenDayChangeRate;
}
