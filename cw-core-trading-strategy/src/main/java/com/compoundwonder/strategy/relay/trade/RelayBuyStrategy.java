package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.BuyStrategy;

/**
 * 连板接力模式买入规则聚合入口。
 *
 * <p>只负责分发上海集合竞价、深圳集合竞价、连续竞价打板和买入撤单。
 * 持仓卖出由独立的板高/市值分发器处理。</p>
 */
public final class RelayBuyStrategy implements BuyStrategy {

    @Override
    public boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record) {
        // 调用连板接力买入规则。
        return ContinuousLimitUpBuyEvaluator.evaluate(market, record);
    }

    @Override
    public boolean evaluateCancel(TradeMarketState market) {
        // 调用连板接力撤单规则。
        return ConditionEvaluatorCancel.evaluate(market);
    }

    @Override
    public boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
        // 调用连板接力盘中模式切换规则。
        return TradingControlEvaluator.shouldEnableFirstBoardTradingMode(market);
    }

    @Override
    public boolean isContinuousBuyTimeAllowed(int time) {
        // 调用连板接力连续竞价买入时段规则。
        return TradingControlEvaluator.isContinuousBuyTimeAllowed(time);
    }

    @Override
    public int evaluateShanghaiAuctionBuy(int time, int price, int limitUpPrice,
                                          long totalBuyVolume, long totalSellVolume,
                                          long requiredBuyVolume, long limitUpBuyAmount) {
        // 调用连板接力上海集合竞价买入规则。
        return ShanghaiAuctionBuyEvaluator.evaluateBuy(time, price, limitUpPrice,
                totalBuyVolume, totalSellVolume, requiredBuyVolume, limitUpBuyAmount);
    }

    @Override
    public int evaluateShanghaiAuctionCancel(int price, int limitUpPrice,
                                             long totalBuyVolume, long totalSellVolume,
                                             long requiredBuyVolume) {
        // 调用连板接力上海集合竞价撤单规则。
        return ShanghaiAuctionBuyEvaluator.evaluateCancel(price, limitUpPrice,
                totalBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenAuctionBuy(byte dataType, int price, int limitUpPrice,
                                         int orderQuantity, long limitUpBuyVolume,
                                         long totalSellVolume, long requiredBuyVolume,
                                         long limitUpBuyAmount, long circulation) {
        // 调用连板接力深圳集合竞价买入规则。
        return ShenzhenAuctionBuyEvaluator.evaluateBuy(dataType, price, limitUpPrice,
                orderQuantity, limitUpBuyVolume, totalSellVolume, requiredBuyVolume,
                limitUpBuyAmount, circulation);
    }

    @Override
    public int evaluateShenzhenAuctionCancel(long limitUpBuyVolume, long totalSellVolume,
                                            long requiredBuyVolume) {
        // 调用连板接力深圳集合竞价撤单规则。
        return ShenzhenAuctionBuyEvaluator.evaluateCancel(
                limitUpBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenSnapshotAuctionCancel(int price, int limitUpPrice) {
        // 调用连板接力深圳快照集合竞价撤单规则。
        return ShenzhenAuctionBuyEvaluator.evaluateSnapshotCancel(price, limitUpPrice);
    }

}

