package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/** 订单簿调用交易策略的统一接口，具体实现由 app 组装根注入。 */
public interface TradeDecisionService {

    /**
     * 按订单簿的稳定交易模式判断连续竞价买入规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param record 命中规则后写入的预分配记录对象
     * @return 命中任一买入规则并已填充记录时返回 {@code true}
     */
    boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record);

    /**
     * 按昨日板高和启动流通市值，判断盘口、涨停封单和炸板相关的盘中卖出规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param record 命中规则后写入的预分配记录对象
     * @return 命中任一卖出规则并已填充记录时返回 {@code true}
     */
    boolean evaluateSell(TradeMarketState market, TradeRuleRecord record);

    /**
     * 按昨日板高和启动流通市值，判断基于分钟最新价和分钟均价序列的卖出规则。
     *
     * @param calculateIndex 当前有效分钟槽位下标
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param record 命中规则后写入的预分配记录对象
     * @return 命中分钟走势卖出规则并已填充记录时返回 {@code true}
     */
    boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                     TradeRuleRecord record);

    /**
     * 判断当前买入挂单是否需要撤销。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @return 需要向执行端发送撤单动作时返回 {@code true}
     */
    boolean evaluateCancel(TradeMarketState market);

    /**
     * 判断当前模式是否触发切换到首板交易模式的保护条件。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @return 需要通知执行端开启首板模式时返回 {@code true}
     */
    boolean shouldEnableFirstBoardTradingMode(TradeMarketState market);

    /**
     * 判断当前模式在给定时间是否允许执行连续竞价买入规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param time 行情时间，格式为 {@code HHmmssSSS}
     * @return 允许进入连续竞价买入判断时返回 {@code true}
     */
    boolean isContinuousBuyTimeAllowed(TradeMarketState market, int time);

    /**
     * 判断上海集合竞价买入规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param time 行情时间，格式为 {@code HHmmssSSS}
     * @param price 当前竞价价格，整数价格口径为元乘以 100
     * @param limitUpPrice 当日涨停价，整数价格口径为元乘以 100
     * @param totalBuyVolume 集合竞价累计买量，单位为股
     * @param totalSellVolume 集合竞价累计卖量，单位为股
     * @param requiredBuyVolume 策略根据历史成交量计算的最低买量，单位为股
     * @param limitUpBuyAmount 涨停买单金额，单位为万元
     * @return {@code 0} 表示不触发；非零值为当前模式内的买入规则编号
     */
    int evaluateShanghaiAuctionBuy(TradeMarketState market, int time, int price,
                                   int limitUpPrice, long totalBuyVolume,
                                   long totalSellVolume, long requiredBuyVolume,
                                   long limitUpBuyAmount);

    /**
     * 判断上海集合竞价买单撤单规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param price 当前竞价价格，整数价格口径为元乘以 100
     * @param limitUpPrice 当日涨停价，整数价格口径为元乘以 100
     * @param totalBuyVolume 集合竞价累计买量，单位为股
     * @param totalSellVolume 集合竞价累计卖量，单位为股
     * @param requiredBuyVolume 策略要求的最低买量，单位为股
     * @return {@code 0} 表示不撤单；非零值为当前模式内的撤单规则编号
     */
    int evaluateShanghaiAuctionCancel(TradeMarketState market, int price,
                                      int limitUpPrice, long totalBuyVolume,
                                      long totalSellVolume, long requiredBuyVolume);

    /**
     * 判断深圳集合竞价买入规则；可同时处理逐笔委托与逐笔成交事件。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param dataType 订单簿事件类型，1 为委托、2 为成交
     * @param price 当前事件价格，整数价格口径为元乘以 100
     * @param limitUpPrice 当日涨停价，整数价格口径为元乘以 100
     * @param orderQuantity 当前事件委托或成交数量，单位为股
     * @param limitUpBuyVolume 当前涨停价买队列总量，单位为股
     * @param totalSellVolume 集合竞价累计卖量，单位为股
     * @param requiredBuyVolume 策略要求的最低买量，单位为股
     * @param limitUpBuyAmount 涨停买单金额，单位为万元
     * @param circulation 当日可交易流通股本，单位为股
     * @return {@code 0} 表示不触发；非零值为当前模式内的买入规则编号
     */
    int evaluateShenzhenAuctionBuy(TradeMarketState market, byte dataType,
                                  int price, int limitUpPrice, int orderQuantity,
                                  long limitUpBuyVolume, long totalSellVolume,
                                  long requiredBuyVolume, long limitUpBuyAmount,
                                  long circulation);

    /**
     * 判断深圳逐笔行情形成的集合竞价撤单规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param limitUpBuyVolume 当前涨停价买队列总量，单位为股
     * @param totalSellVolume 集合竞价累计卖量，单位为股
     * @param requiredBuyVolume 策略要求的最低买量，单位为股
     * @return {@code 0} 表示不撤单；非零值为当前模式内的撤单规则编号
     */
    int evaluateShenzhenAuctionCancel(TradeMarketState market,
                                     long limitUpBuyVolume, long totalSellVolume,
                                     long requiredBuyVolume);

    /**
     * 使用深圳三秒快照的竞价价格补充判断买单是否需要撤销。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param price 当前竞价价格，整数价格口径为元乘以 100
     * @param limitUpPrice 当日涨停价，整数价格口径为元乘以 100
     * @return {@code 0} 表示不撤单；非零值为当前模式内的撤单规则编号
     */
    int evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                             int price, int limitUpPrice);

    /**
     * 判断收盘集合竞价是否应快速卖出；该场景不依赖买入模式和板高分发。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param price 收盘竞价价格，整数价格口径为元乘以 100
     * @param limitUpPrice 当日涨停价，整数价格口径为元乘以 100
     * @param totalBuyVolume 收盘竞价累计买量，单位为股
     * @param totalSellVolume 收盘竞价累计卖量，单位为股
     * @return 竞价未封涨停或卖压超过买量时返回 {@code true}
     */
    boolean evaluateClosingAuctionSell(TradeMarketState market, int price,
                                       int limitUpPrice, long totalBuyVolume,
                                       long totalSellVolume);
}
