package com.compoundwonder.trader.selection.smallcapfirstboard;

import lombok.Data;

import java.time.LocalDate;

/**
 * 小市值首板模式专用选股辅助对象。
 *
 * <p>本对象不与普通首板共享。小市值首板存在明显的操纵特征，历史高度、换手、
 * 异常 K 线和评分边界都由本模式独立维护。</p>
 */
@Data
public class SmallCapFirstBoardSelectionAssist {
    /** 股票代码。 */
    private String stockCode;
    /** 股票名称。 */
    private String stockName;
    /** 推荐交易日。 */
    private LocalDate tradeDate;
    /** 当前连续涨停天数，小市值首板固定为 1。 */
    private Integer consecutiveLimitUpDays;
    /** 选股日收盘价。 */
    private Double currentPrice;
    /** 首板前一交易日收盘流通市值，单位：万元。 */
    private Double startMarketCap;
    /** 首板前最近 200 根有效 K 线最大换手率，单位：%。 */
    private Double maxTurnoverRate;
    /** 首板前最近 200 根有效 K 线最高连板数。 */
    private Integer highestConsecutiveLimitUpDays;
    /** 18 个月窗口内排除本次首板后的非正常 K 线数。 */
    private Integer abnormalKlineStateCount;
    /** 本次首板前 20 个交易日的非正常 K 线数。 */
    private Integer priorTwentyDayAbnormalKlineStateCount;
    /** 包含选股日在内的 3 日复权振幅，单位：%。 */
    private Double threeDayAmplitude;
    /** 10 日复权涨跌幅，单位：%。 */
    private Double tenDayChangeRate;
}
