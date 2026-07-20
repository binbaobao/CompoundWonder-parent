package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;

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
     * @param event 上海三秒集合竞价快照
     * @param previousBuyVolume 该股票上一张上海集合竞价快照的买量，单位为股；首张快照为 -1
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param record 调用方预分配的规则记录
     * @return 命中买入规则并完成记录填充时返回 {@code true}
     */
    boolean evaluateShanghaiAuctionBuy(TradeMarketState market, AuctionMarketEvent event,
                                       long previousBuyVolume, int recordTime,
                                       TradeRuleRecord record);

    /**
     * 判断上海集合竞价买单撤单规则。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param event 上海三秒集合竞价快照
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param record 调用方预分配的规则记录
     * @return 命中撤单规则并完成记录填充时返回 {@code true}
     */
    boolean evaluateShanghaiAuctionCancel(TradeMarketState market, AuctionMarketEvent event,
                                          int recordTime, TradeRuleRecord record);

    /**
     * 判断深圳集合竞价买入规则；逐笔事件先更新订单簿，再使用更新后的完整盘口判断。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param event 深圳逐笔委托或成交事件
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param acceptedLimitUpBuyOrder 本次事件是否为成功进入订单簿的买方向涨停价新增委托；
     *                                该标志只控制单笔大单规则，不限制盘口绝对强度规则
     * @param limitUpBuyVolume 当前涨停价买队列总量，单位为股
     * @param totalSellVolume 当前订单簿所有价位的剩余卖单总量，单位为股
     * @param record 调用方预分配的规则记录
     * @return 命中买入规则并完成记录填充时返回 {@code true}
     */
    boolean evaluateShenzhenAuctionBuy(TradeMarketState market, AuctionMarketEvent event,
                                      int recordTime, boolean acceptedLimitUpBuyOrder,
                                      long limitUpBuyVolume,
                                      long totalSellVolume, TradeRuleRecord record);

    /**
     * 逐笔事件更新深圳集合竞价订单簿后，判断已经挂出的买单是否需要撤销。
     *
     * <p>逐笔事件不提供快照竞价价格，因此只判断封单绝对强度；该强度公式必须与
     * 深圳集合竞价绝对强度买入及快照强度撤单完全相同。</p>
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param event 已经完成订单簿更新的深圳逐笔事件
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param limitUpBuyVolume 当前涨停价买队列剩余总量，单位为股
     * @param totalSellVolume 当前订单簿所有价位剩余卖单总量，单位为股
     * @param record 调用方预分配的规则记录
     * @return 封单绝对强度不再成立并完成撤单记录填充时返回 {@code true}
     */
    boolean evaluateShenzhenAuctionCancel(TradeMarketState market,
                                          AuctionMarketEvent event,
                                          int recordTime,
                                          long limitUpBuyVolume,
                                          long totalSellVolume,
                                          TradeRuleRecord record);

    /**
     * 使用深圳当前数据源约九秒一次的集合竞价快照判断买单是否需要撤销。
     * 快照只提供撤单触发时点和竞价价格，绝对强度仍取逐笔数据重建的订单簿状态。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param event 深圳集合竞价快照
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param limitUpBuyVolume 快照到达时订单簿涨停价买队列总量，单位为股
     * @param totalSellVolume 快照到达时订单簿所有价位剩余卖单总量，单位为股
     * @param record 调用方预分配的规则记录
     * @return 命中撤单规则并完成记录填充时返回 {@code true}
     */
    boolean evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                                  AuctionMarketEvent event,
                                                  int recordTime,
                                                  long limitUpBuyVolume,
                                                  long totalSellVolume,
                                                  TradeRuleRecord record);

    /**
     * 判断收盘集合竞价是否应快速卖出；该场景不依赖买入模式和板高分发。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param event 上海三秒收盘集合竞价快照
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param record 调用方预分配的规则记录
     * @return 命中卖出规则并完成记录填充时返回 {@code true}
     */
    boolean evaluateShanghaiClosingAuctionSell(TradeMarketState market,
                                               AuctionMarketEvent event,
                                               int recordTime, TradeRuleRecord record);

    /**
     * 判断深圳收盘集合竞价是否应快速卖出，并在命中时填充原有规则记录。
     *
     * @param market Handler 私有订单簿提供的只读市场状态
     * @param event 深圳三秒行情快照
     * @param recordTime Handler 当前市场时间，格式为 {@code HHmmssSSS}
     * @param record 调用方预分配的规则记录
     * @return 命中收盘集合竞价卖出规则时返回 {@code true}
     */
    boolean evaluateShenzhenClosingAuctionSell(TradeMarketState market,
                                               AuctionMarketEvent event,
                                               int recordTime, TradeRuleRecord record);
}
