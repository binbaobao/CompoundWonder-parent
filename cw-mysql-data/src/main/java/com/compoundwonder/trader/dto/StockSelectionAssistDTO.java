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
     * 本轮连续涨停中一字板的数量，按当前连板数向前回看统计。
     */
    private Integer consecutiveOneWordLimitUpDays;

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
     * 启动价格，本轮首板前一交易日收盘价。
     */
    private Double startPrice;

    /**
     * 选股当日换手率。
     */
    private Double currentTurnoverRate;

    /**
     * 非 ST 月份数，上次摘帽或新上市至今的自然月数。
     */
    private Integer nonStMonthCount;

    /**
     * 新上市至选股日期的自然月数。
     */
    private Integer listingMonthCount;

    /**
     * 最大换手率。
     */
    private Double maxTurnoverRate;

    /**
     * 最高板。
     */
    private Integer highestConsecutiveLimitUpDays;

    /**
     * 近 3 个自然月内的最高板。
     */
    private Integer recentThreeMonthHighestConsecutiveLimitUpDays;

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

    /**
     * 按选股评分公式计算的总分，基础满分 100 分，异常状态次数超出 10 次后扣分。
     */
    private Integer score;
}
