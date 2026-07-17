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
     * 本轮连续涨停开始前 18 个自然月内的历史最大换手率；
     * 排除股票上市后最早 10 根日 K，不包含本轮连板。
     */
    private Double maxTurnoverRate;

    /**
     * 本轮连续涨停开始前 18 个自然月内的历史最高板；
     * 排除股票上市后最早 10 根日 K，不包含本轮连板。
     */
    private Integer highestConsecutiveLimitUpDays;

    /**
     * 本轮连续涨停开始前 90 个自然日内的历史最高板，不包含本轮连板。
     */
    private Integer priorNinetyDayHighestConsecutiveLimitUpDays;

    /**
     * 本轮连续涨停开始前 18 个自然月内的历史最大成交量，单位：股；
     * 排除股票上市后最早 10 根日 K，用于低换手、低筹码金额特殊通道。
     */
    private Long historicalMaxVolume;

    /**
     * 非正常状态次数，统计 klineState != 0 的交易日数量。
     */
    private Integer abnormalKlineStateCount;

    /**
     * 本轮连续涨停开始前 20 个交易日的非正常 K 线数量，不包含本轮连板；
     * 非正常 K 线口径为 klineState != 0。
     */
    private Integer priorTwentyDayAbnormalKlineStateCount;

    /**
     * 选股振幅：首板使用包含当日在内的 3 个交易日，连板使用 5 个交易日；
     * 以窗口内最低复权价为基准，计算到当日复权收盘价的涨幅。
     */
    private Double selectionAmplitude;

    /**
     * 10 日涨跌幅，使用复权收盘价计算。
     */
    private Double tenDayChangeRate;

    /**
     * 按选股评分公式计算的总分，基础满分 100 分，异常状态次数超出 10 次后扣分。
     */
    private Integer score;
}
