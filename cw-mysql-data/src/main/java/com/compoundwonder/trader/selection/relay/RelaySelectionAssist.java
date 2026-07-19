package com.compoundwonder.trader.selection.relay;

import lombok.Data;

import java.time.LocalDate;

/**
 * 连板接力模式专用选股辅助对象。
 *
 * <p>该对象完整承接连板严格通道、冰点 3/4 板通道和弱 5 板兜底需要的指标，
 * 不与两种首板模式共享。</p>
 */
@Data
public class RelaySelectionAssist {
    private String stockCode;
    private String stockName;
    private LocalDate tradeDate;
    private Integer consecutiveLimitUpDays;
    /** 本轮 2/3 板中是否至少出现两根加速缩量板。 */
    private boolean twoAcceleratedShrinkVolumeLimitUps;
    private String province;
    private Double currentPrice;
    /** 本轮首板前一交易日收盘流通市值，单位：万元。 */
    private Double startMarketCap;
    private Double startPrice;
    private Double currentTurnoverRate;
    /** 选股日成交额，单位：万元。 */
    private Double currentTurnover;
    /** 选股日振幅，单位：%。 */
    private Double currentAmplitude;
    private Integer nonStMonthCount;
    private Integer listingMonthCount;
    /** 本轮前最近 200 根有效 K 线最大换手率。 */
    private Double maxTurnoverRate;
    /** 本轮前最近 200 根有效 K 线最高连板数。 */
    private Integer highestConsecutiveLimitUpDays;
    /** 本轮前 90 个自然日最高连板数。 */
    private Integer priorNinetyDayHighestConsecutiveLimitUpDays;
    /** 本轮前 90 个自然日最大换手率。 */
    private Double priorNinetyDayMaxTurnoverRate;
    private Long historicalMaxVolume;
    private Double maxVolumeDayTurnoverRate;
    private Double maxVolumeDayTurnover;
    private Integer abnormalKlineStateCount;
    private Integer priorTwentyDayAbnormalKlineStateCount;
    /** 包含选股日在内的 5 日复权振幅。 */
    private Double fiveDayAmplitude;
    private Double tenDayChangeRate;
}
