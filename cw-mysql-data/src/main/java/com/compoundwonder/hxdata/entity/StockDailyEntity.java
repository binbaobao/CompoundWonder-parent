package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票日K线数据表
 * <p>
 * TODO 1.全量数据都要落库，包括停牌
 * TODO 2.每天要检查是否有漏掉的数据
 * TODO 3.根据涨停价判断是否是已经 st，日 k 线数据保留 st 状态，反过来判断股票信息是否更新及时，发出消息
 *
 * @author chaobin
 * @since 1.0.0 2025-12-17
 */
@Data
@TableName("stock_daily")
public class StockDailyEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 股票代码
     */
    private String stockCode;
    /**
     * 当日股票名称
     */
    private String stockName;
    /**
     * 当日是否ST
     */
    private Boolean isSt;
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    /**
     * 开盘价
     */
    private Double openPrice;
    /**
     * 最高价
     */
    private Double highPrice;
    /**
     * 最低价
     */
    private Double lowPrice;
    /**
     * 收盘价
     */
    private Double closePrice;
    /**
     * 前收盘价
     */
    private Double prevClose;
    /**
     * 复权昨收盘价
     */
    private Double adjustPreClosePrice;
    /**
     * 复权开盘价
     */
    private Double adjustOpenPrice;
    /**
     * 复权最高价
     */
    private Double adjustHighPrice;
    /**
     * 复权最低价
     */
    private Double adjustLowPrice;
    /**
     * 复权收盘价
     */
    private Double adjustClosePrice;
    /**
     * 复权因子
     */
    private Double adjustFactor;

    /**
     * 成交量(股)
     */
    private Long volume;
    /**
     * 成交额(万元)
     */
    private Double turnover;
    /**
     * 换手率(%)
     */
    private Double turnoverRate;
    /**
     * 涨跌幅(%)
     */
    private Double changeRate;
    /**
     * 振幅(%)
     */
    private Double amplitude;
    /**
     * 流通市值(万元)
     */
    private Double floatMarketCap;
    /**
     * 流通股本(股)
     */
    private Long floatShares;
    /**
     * K线状态(-13到13)
     * 13 地天板涨停板炸板
     * 12 开盘涨停板炸板
     * 11 普通涨停炸板
     * 5  天地天涨停
     * 4  地天涨停
     * 3  一字涨停
     * 2  T字涨停
     * 1  实体涨停
     * 0  普通行情
     * -1 实体跌停
     * -2 倒T字跌停
     * -3 一字跌停
     * -4 天地跌停
     * -5 地天地跌停
     * -11 跌停炸板
     * -12 开盘跌停板炸板
     */
    private Integer klineState;
    /**
     * 连续涨停天数 or 昨天涨停今日断板位置
     * 如果昨天是 涨停的 1 板，今天断板就是 -1，
     * 如果昨天是 涨停的 3 板，今天断板就是 -3，
     * 如果昨天是 涨停的 8 板，今天断板就是 -8，
     */
    private Integer consecutiveLimitUpDays;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
