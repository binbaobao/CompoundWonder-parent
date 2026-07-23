package com.compoundwonder.strategy.smallcapfirstboard.trade;

import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.BuyStrategy;

/**
 * 小市值首板模式买入规则聚合入口。
 *
 * <p>只负责上海集合竞价、深圳集合竞价、连续竞价打板和买入撤单。
 * 持仓卖出由统一连续竞价卖出策略处理。</p>
 */
public final class SmallCapFirstBoardBuyStrategy implements BuyStrategy {

    @Override
    public boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record) {
        // 调用小市值首板买入规则。
        return ContinuousLimitUpBuyEvaluator.evaluate(market, record);
    }

    @Override
    public boolean evaluateCancel(TradeMarketState market) {
        // 调用小市值首板撤单规则。
        return ConditionEvaluatorCancel.evaluate(market);
    }

    @Override
    public boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
        // 调用小市值首板盘中模式切换规则。
        return TradingControlEvaluator.shouldEnableFirstBoardTradingMode(market);
    }

    @Override
    public boolean isContinuousBuyTimeAllowed(int time) {
        // 调用小市值首板连续竞价买入时段规则。
        return TradingControlEvaluator.isContinuousBuyTimeAllowed(time);
    }

    @Override
    public boolean evaluateShanghaiAuctionBuy(TradeMarketState market, AuctionMarketEvent event,
                                              long previousBuyVolume, int recordTime,
                                              TradeRuleRecord record) {
        // 调用当前模式上海集合竞价买入规则。
        return ShanghaiAuctionBuyEvaluator.evaluateBuy(
                market, event, previousBuyVolume, recordTime, record);
    }

    @Override
    public boolean evaluateShanghaiAuctionCancel(TradeMarketState market, AuctionMarketEvent event,
                                                 int recordTime, TradeRuleRecord record) {
        // 调用当前模式上海集合竞价撤单规则。
        return ShanghaiAuctionBuyEvaluator.evaluateCancel(market, event, recordTime, record);
    }

    @Override
    public boolean evaluateShenzhenAuctionBuy(TradeMarketState market, AuctionMarketEvent event,
                                              int recordTime,
                                              long limitUpBuyVolume,
                                              long totalSellVolume, TradeRuleRecord record) {
        // 调用当前模式深圳集合竞价买入规则。
        return ShenzhenAuctionBuyEvaluator.evaluateBuy(
                market, event, recordTime, limitUpBuyVolume, totalSellVolume, record);
    }

    @Override
    public boolean evaluateShenzhenAuctionCancel(TradeMarketState market,
                                                 AuctionMarketEvent event,
                                                 int recordTime,
                                                 long limitUpBuyVolume,
                                                 long totalSellVolume,
                                                 TradeRuleRecord record) {
        // 调用当前模式深圳逐笔订单簿集合竞价撤单规则。
        return ShenzhenAuctionBuyEvaluator.evaluateOrderBookCancel(
                market, event, recordTime, limitUpBuyVolume,
                totalSellVolume, record);
    }

    @Override
    public boolean evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                                         AuctionMarketEvent event,
                                                         int recordTime,
                                                         long limitUpBuyVolume,
                                                         long totalSellVolume,
                                                         TradeRuleRecord record) {
        // 调用当前模式深圳快照集合竞价撤单规则。
        return ShenzhenAuctionBuyEvaluator.evaluateSnapshotCancel(
                market, event, recordTime, limitUpBuyVolume,
                totalSellVolume, record);
    }
}
