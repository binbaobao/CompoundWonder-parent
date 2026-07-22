package com.compoundwonder.strategy.relay.selection;

import lombok.Data;

import java.time.LocalDate;

/**
 * 连板接力模式专用选股辅助对象。
 *
 * <p>该对象完整承接连板三级强度和弱 5 板卡位需要的指标，
 * 不与两种首板模式共享。</p>
 */
@Data
public class RelaySelectionAssist {
    /** 股票代码。 */
    private String stockCode;
    /** 股票名称。 */
    private String stockName;
    /** 推荐交易日。 */
    private LocalDate tradeDate;
    /** 选股日连续涨停天数，只接受 2 板或 3 板。 */
    private Integer consecutiveLimitUpDays;
    /** 本轮 2/3 板中是否至少出现两根加速缩量板。 */
    private boolean twoAcceleratedShrinkVolumeLimitUps;
    /** 公司所属省份。 */
    private String province;
    /** 选股日收盘价，单位：元。 */
    private Double currentPrice;
    /** 本轮首板前一交易日收盘流通市值，单位：万元。 */
    private Double startMarketCap;
    /** 本轮首板前一交易日收盘价，单位：元。 */
    private Double startPrice;
    /** 选股日换手率，单位：%。 */
    private Double currentTurnoverRate;
    /** 选股日成交额，单位：万元。 */
    private Double currentTurnover;
    /** 选股日振幅，单位：%。 */
    private Double currentAmplitude;
    /** 上次摘帽或上市至选股日的非 ST 自然月数。 */
    private Integer nonStMonthCount;
    /** 上市至选股日的自然月数。 */
    private Integer listingMonthCount;
    /** 本轮前最近 200 根有效 K 线最大换手率。 */
    private Double maxTurnoverRate;
    /** 本轮前最近 200 根有效 K 线最高连板数。 */
    private Integer highestConsecutiveLimitUpDays;
    /** 本轮前 90 个自然日最高连板数。 */
    private Integer priorNinetyDayHighestConsecutiveLimitUpDays;
    /** 本轮前 90 个自然日最大换手率。 */
    private Double priorNinetyDayMaxTurnoverRate;
    /** 本轮前最近 200 根有效 K 线最大成交量，单位：股。 */
    private Long historicalMaxVolume;
    /** 最大成交量日换手率，单位：%。 */
    private Double maxVolumeDayTurnoverRate;
    /** 最大成交量日成交额，单位：万元。 */
    private Double maxVolumeDayTurnover;
    /** 18 个月窗口内排除本次连板后的非正常 K 线数。 */
    private Integer abnormalKlineStateCount;
    /** 本轮连板前 20 个交易日的非正常 K 线数。 */
    private Integer priorTwentyDayAbnormalKlineStateCount;
    /** 包含选股日在内的 5 日复权振幅。 */
    private Double fiveDayAmplitude;
    /** 10 日复权涨跌幅，单位：%。 */
    private Double tenDayChangeRate;
}
