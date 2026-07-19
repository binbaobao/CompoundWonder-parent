package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.strategy.TradeMarketState;
import com.compoundwonder.strategy.TradeRuleRecord;
import com.compoundwonder.strategy.TradingStrategy;

/**
 * 连板接力模式独立交易规则。
 *
 * <p>当前版本从原统一交易规则完整复制，后续只在本包内按该模式逐步调整买入、卖出和撤单。</p>
 */
public final class RelayTradingStrategy implements TradingStrategy {

    @Override
    public boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record) {
        // 调用连板接力买入规则。
        return ConditionEvaluatorBuy.evaluate(market, record);
    }

    @Override
    public boolean evaluateSell(TradeMarketState market, TradeRuleRecord record) {
        // 调用连板接力盘口卖出规则。
        return ConditionEvaluatorSell.evaluate(market, record);
    }

    @Override
    public boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                            TradeRuleRecord record) {
        // 调用连板接力分钟均价卖出规则。
        return ConditionEvaluatorSell.averagePriceSellStrategy(calculateIndex, market, record);
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
        return AuctionEvaluator.evaluateShanghaiBuy(time, price, limitUpPrice,
                totalBuyVolume, totalSellVolume, requiredBuyVolume, limitUpBuyAmount);
    }

    @Override
    public int evaluateShanghaiAuctionCancel(int price, int limitUpPrice,
                                             long totalBuyVolume, long totalSellVolume,
                                             long requiredBuyVolume) {
        // 调用连板接力上海集合竞价撤单规则。
        return AuctionEvaluator.evaluateShanghaiCancel(price, limitUpPrice,
                totalBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenAuctionBuy(byte dataType, int price, int limitUpPrice,
                                         int orderQuantity, long limitUpBuyVolume,
                                         long totalSellVolume, long requiredBuyVolume,
                                         long limitUpBuyAmount, long circulation) {
        // 调用连板接力深圳集合竞价买入规则。
        return AuctionEvaluator.evaluateShenzhenBuy(dataType, price, limitUpPrice,
                orderQuantity, limitUpBuyVolume, totalSellVolume, requiredBuyVolume,
                limitUpBuyAmount, circulation);
    }

    @Override
    public int evaluateShenzhenAuctionCancel(long limitUpBuyVolume, long totalSellVolume,
                                            long requiredBuyVolume) {
        // 调用连板接力深圳集合竞价撤单规则。
        return AuctionEvaluator.evaluateShenzhenCancel(
                limitUpBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenSnapshotAuctionCancel(int price, int limitUpPrice) {
        // 调用连板接力深圳快照集合竞价撤单规则。
        return AuctionEvaluator.evaluateShenzhenSnapshotCancel(price, limitUpPrice);
    }

    @Override
    public boolean evaluateClosingAuctionSell(int price, int limitUpPrice,
                                              long totalBuyVolume, long totalSellVolume) {
        // 调用连板接力尾盘集合竞价卖出规则。
        return AuctionEvaluator.evaluateClosingSell(
                price, limitUpPrice, totalBuyVolume, totalSellVolume);
    }

}
