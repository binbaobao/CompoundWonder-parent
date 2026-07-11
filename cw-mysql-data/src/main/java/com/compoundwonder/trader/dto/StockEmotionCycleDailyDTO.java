package com.compoundwonder.trader.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 股票情绪周期每日聚合结果。
 * 作用：承接 stock_daily 按交易日聚合后写入 stock_emotion_cycle_daily 所需字段。
 */
@Data
public class StockEmotionCycleDailyDTO {

    /**
     * 交易日期。
     */
    private LocalDate tradeDate;

    /**
     * 涨停家数。
     */
    private Integer limitUpCount;

    /**
     * 跌停家数。
     */
    private Integer limitDownCount;

    /**
     * 连板数。
     */
    private Integer consecutiveLimitUpCount;

    /**
     * 最高连板。
     */
    private Integer highestConsecutiveLimitUpDays;

    /**
     * 炸板数量。
     */
    private Integer limitUpBrokenCount;

    /**
     * 跌停数。
     */
    private Integer downLimitCount;

    /**
     * 占领周期股票代码。
     */
    private String dominantCycleStockCode;

    /**
     * 占领周期股票名称。
     */
    private String dominantCycleStockName;

    /**
     * 涨家数。
     */
    private Integer risingCount;

    /**
     * 跌家数。
     */
    private Integer fallingCount;

    /**
     * 全市场成交金额，单位：亿元。
     */
    private BigDecimal allMarketTurnoverAmount;
}
