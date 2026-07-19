package com.compoundwonder.trader.selection.firstboard;

import lombok.Data;

import java.time.LocalDate;

/**
 * 普通首板专用选股辅助对象。
 *
 * <p>字段只服务普通首板模式。即使字段含义与其他模式当前相似，也不复用 DTO，
 * 防止以后调整任一模式时无意改变另外两种模式。</p>
 */
@Data
public class FirstBoardSelectionAssist {
    /** 股票代码。 */
    private String stockCode;
    /** 股票名称。 */
    private String stockName;
    /** 推荐交易日。 */
    private LocalDate tradeDate;
    /** 当前连续涨停天数，普通首板固定为 1。 */
    private Integer consecutiveLimitUpDays;
    /** 省份。 */
    private String province;
    /** 选股日收盘价。 */
    private Double currentPrice;
    /** 首板前一交易日收盘流通市值，单位：万元。 */
    private Double startMarketCap;
    /** 首板前一交易日收盘价。 */
    private Double startPrice;
    /** 选股日换手率，单位：%。 */
    private Double currentTurnoverRate;
    /** 上次摘帽或上市至选股日的非 ST 自然月数。 */
    private Integer nonStMonthCount;
    /** 上市至选股日的自然月数。 */
    private Integer listingMonthCount;
    /** 首板前最近 200 根有效 K 线最大换手率，单位：%。 */
    private Double maxTurnoverRate;
    /** 首板前最近 200 根有效 K 线最高连板数。 */
    private Integer highestConsecutiveLimitUpDays;
    /** 首板前 90 个自然日最高连板数。 */
    private Integer priorNinetyDayHighestConsecutiveLimitUpDays;
    /** 首板前最近 200 根有效 K 线最大成交量，单位：股。 */
    private Long historicalMaxVolume;
    /** 18 个月窗口内排除本次首板后的非正常 K 线数。 */
    private Integer abnormalKlineStateCount;
    /** 本次首板前 20 个交易日的非正常 K 线数。 */
    private Integer priorTwentyDayAbnormalKlineStateCount;
    /** 包含选股日在内的 3 日复权振幅，单位：%。 */
    private Double threeDayAmplitude;
    /** 10 日复权涨跌幅，单位：%。 */
    private Double tenDayChangeRate;
}
