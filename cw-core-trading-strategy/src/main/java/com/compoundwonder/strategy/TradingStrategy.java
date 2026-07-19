package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/**
 * 一种交易模式完整的交易判断契约。
 *
 * <p>实现类只负责判断规则并填充调用方传入的规则记录，不查询数据库、不修改订单簿，
 * 也不直接向券商发送委托。价格统一使用“分”，时间统一使用 {@code HHmmssSSS}
 * 紧凑整数，成交量和委托量统一使用“股”。</p>
 */
public interface TradingStrategy {

    /**
     * 根据连续竞价盘口判断是否产生买入规则。
     *
     * @param market 当前 Handler 私有订单簿的只读交易视图
     * @param record 调用方预分配的规则记录，只有命中规则时才填充
     * @return 命中买入规则时返回 {@code true}
     */
    boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record);

    /**
     * 根据涨停状态、封单变化和换手等盘口指标判断是否卖出。
     *
     * @param market 当前 Handler 私有订单簿的只读交易视图
     * @param record 调用方预分配的规则记录，只有命中规则时才填充
     * @return 命中盘口卖出规则时返回 {@code true}
     */
    boolean evaluateSell(TradeMarketState market, TradeRuleRecord record);

    /**
     * 根据分钟价格和均价走势判断是否卖出。
     *
     * @param calculateIndex 当前分钟采样下标
     * @param market 当前 Handler 私有订单簿的只读交易视图
     * @param record 调用方预分配的规则记录，只有命中规则时才填充
     * @return 命中分钟走势卖出规则时返回 {@code true}
     */
    boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                     TradeRuleRecord record);

    /**
     * 判断当前买入挂单是否需要撤销。
     *
     * @param market 当前 Handler 私有订单簿的只读交易视图
     * @return 命中撤单规则时返回 {@code true}
     */
    boolean evaluateCancel(TradeMarketState market);

    /**
     * 判断连板标的盘中走弱后是否允许切换到首板交易逻辑。
     *
     * @param market 当前 Handler 私有订单簿的只读交易视图
     * @return 满足模式切换条件时返回 {@code true}
     */
    boolean shouldEnableFirstBoardTradingMode(TradeMarketState market);

    /**
     * 判断当前时间是否仍处于连续竞价买入时段。
     *
     * @param time 紧凑时间 {@code HHmmssSSS}
     * @return 仍允许产生连续竞价买入信号时返回 {@code true}
     */
    boolean isContinuousBuyTimeAllowed(int time);

    /**
     * 评估上海集合竞价买入规则。
     *
     * @param time 紧凑时间 {@code HHmmssSSS}
     * @param price 当前撮合价格，单位：分
     * @param limitUpPrice 当日涨停价，单位：分
     * @param totalBuyVolume 集合竞价买方总量，单位：股
     * @param totalSellVolume 集合竞价卖方总量，单位：股
     * @param requiredBuyVolume 策略要求的最低买方量，单位：股
     * @param limitUpBuyAmount 涨停买单金额，单位：万元
     * @return {@code 0} 表示不触发，否则返回集合竞价买入规则编号
     */
    int evaluateShanghaiAuctionBuy(int time, int price, int limitUpPrice,
                                   long totalBuyVolume, long totalSellVolume,
                                   long requiredBuyVolume, long limitUpBuyAmount);

    /**
     * 评估上海集合竞价买入挂单的撤单规则。
     *
     * @param price 当前撮合价格，单位：分
     * @param limitUpPrice 当日涨停价，单位：分
     * @param totalBuyVolume 集合竞价买方总量，单位：股
     * @param totalSellVolume 集合竞价卖方总量，单位：股
     * @param requiredBuyVolume 策略要求的最低买方量，单位：股
     * @return {@code 0} 表示不撤单，否则返回集合竞价撤单规则编号
     */
    int evaluateShanghaiAuctionCancel(int price, int limitUpPrice,
                                      long totalBuyVolume, long totalSellVolume,
                                      long requiredBuyVolume);

    /**
     * 评估深圳逐笔委托驱动的集合竞价买入规则。
     *
     * @param dataType 深圳原始委托类型，{@code 1} 表示限价委托
     * @param price 委托价格，单位：分
     * @param limitUpPrice 当日涨停价，单位：分
     * @param orderQuantity 当前委托数量，单位：股
     * @param limitUpBuyVolume 当前涨停买单量，单位：股
     * @param totalSellVolume 当前卖方总量，单位：股
     * @param requiredBuyVolume 策略要求的最低买方量，单位：股
     * @param limitUpBuyAmount 当前涨停买单金额，单位：万元
     * @param circulation 当时流通股本，单位：股
     * @return {@code 0} 表示不触发，否则返回集合竞价买入规则编号
     */
    int evaluateShenzhenAuctionBuy(byte dataType, int price, int limitUpPrice,
                                  int orderQuantity, long limitUpBuyVolume,
                                  long totalSellVolume, long requiredBuyVolume,
                                  long limitUpBuyAmount, long circulation);

    /**
     * 评估深圳逐笔行情驱动的集合竞价撤单规则。
     *
     * @param limitUpBuyVolume 当前涨停买单量，单位：股
     * @param totalSellVolume 当前卖方总量，单位：股
     * @param requiredBuyVolume 策略要求的最低买方量，单位：股
     * @return {@code 0} 表示不撤单，否则返回集合竞价撤单规则编号
     */
    int evaluateShenzhenAuctionCancel(long limitUpBuyVolume, long totalSellVolume,
                                     long requiredBuyVolume);

    /**
     * 使用深圳快照撮合价格补充判断集合竞价挂单是否撤销。
     *
     * @param price 当前撮合价格，单位：分
     * @param limitUpPrice 当日涨停价，单位：分
     * @return {@code 0} 表示不撤单，否则返回集合竞价撤单规则编号
     */
    int evaluateShenzhenSnapshotAuctionCancel(int price, int limitUpPrice);

    /**
     * 判断收盘集合竞价期间是否需要卖出。
     *
     * @param price 当前撮合价格，单位：分
     * @param limitUpPrice 当日涨停价，单位：分
     * @param totalBuyVolume 集合竞价买方总量，单位：股
     * @param totalSellVolume 集合竞价卖方总量，单位：股
     * @return 撮合价低于涨停价或卖方总量占优时返回 {@code true}
     */
    boolean evaluateClosingAuctionSell(int price, int limitUpPrice,
                                       long totalBuyVolume, long totalSellVolume);
}
