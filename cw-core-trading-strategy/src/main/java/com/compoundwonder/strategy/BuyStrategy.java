package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;

/**
 * 单一买入模式的交易判断契约。
 *
 * <p>{@code tradeMode} 只用于买入：1 连板、2 普通首板、3 小市值首板。
 * 持仓卖出由昨日板高和启动流通市值动态分发，不属于本接口。</p>
 */
public interface BuyStrategy {
    /** 连续竞价打板买入；命中时填充 {@code record} 并返回 {@code true}。 */
    boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record);

    /** 判断盘中尚未成交的买入挂单是否需要撤销。 */
    boolean evaluateCancel(TradeMarketState market);

    /** 判断连板标的走弱后是否允许启用首板补充交易状态。 */
    boolean shouldEnableFirstBoardTradingMode(TradeMarketState market);

    /**
     * @param time 紧凑时间 {@code HHmmssSSS}
     * @return 当前模式是否仍允许产生连续竞价买入信号
     */
    boolean isContinuousBuyTimeAllowed(int time);

    /**
     * 上海早盘集合竞价买入。价格单位为分，数量单位为股，金额单位为万元。
     *
     * @return 命中规则并完成记录填充时返回 {@code true}
     */
    boolean evaluateShanghaiAuctionBuy(TradeMarketState market, AuctionMarketEvent event,
                                       long previousBuyVolume, int recordTime,
                                       TradeRuleRecord record);

    /** 上海早盘集合竞价撤单；命中规则并完成记录填充时返回 {@code true}。 */
    boolean evaluateShanghaiAuctionCancel(TradeMarketState market, AuctionMarketEvent event,
                                          int recordTime, TradeRuleRecord record);

    /**
     * 深圳逐笔委托驱动的早盘集合竞价买入。价格单位为分，数量和流通股本单位为股，
     * 金额单位为万元；命中规则并完成记录填充时返回 {@code true}。
     */
    boolean evaluateShenzhenAuctionBuy(TradeMarketState market, AuctionMarketEvent event,
                                      int recordTime, long limitUpBuyVolume,
                                      long totalSellVolume, TradeRuleRecord record);

    /** 深圳逐笔委托驱动的早盘集合竞价撤单；命中时返回 {@code true}。 */
    boolean evaluateShenzhenAuctionCancel(TradeMarketState market, AuctionMarketEvent event,
                                         int recordTime, long limitUpBuyVolume,
                                         long totalSellVolume, TradeRuleRecord record);

    /** 深圳快照撮合价驱动的补充撤单判断；命中时返回 {@code true}。 */
    boolean evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                                  AuctionMarketEvent event,
                                                  int recordTime, TradeRuleRecord record);
}
