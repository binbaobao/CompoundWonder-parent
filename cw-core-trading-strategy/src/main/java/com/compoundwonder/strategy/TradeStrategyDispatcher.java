package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.strategy.firstboard.trade.FirstBoardTradingStrategy;
import com.compoundwonder.strategy.relay.trade.RelayTradingStrategy;
import com.compoundwonder.strategy.smallcapfirstboard.trade.SmallCapFirstBoardTradingStrategy;

/**
 * 高频交易规则分发器。
 *
 * <p>订单簿已经保存稳定的 {@code tradeMode}，因此热路径只做一次 {@code switch}，
 * 不使用 Map、反射、Spring 查找或每笔行情创建对象。</p>
 */
public final class TradeStrategyDispatcher implements TradeDecisionService {

    private final TradingStrategy relayStrategy = new RelayTradingStrategy();
    private final TradingStrategy firstBoardStrategy = new FirstBoardTradingStrategy();
    private final TradingStrategy smallCapFirstBoardStrategy = new SmallCapFirstBoardTradingStrategy();

    @Override
    public boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record) {
        // 调用当前订单簿交易模式对应的买入方法。
        return strategy(market.getTradeMode()).evaluateBuy(market, record);
    }

    @Override
    public boolean evaluateSell(TradeMarketState market, TradeRuleRecord record) {
        // 调用当前订单簿交易模式对应的盘口卖出方法。
        return strategy(market.getTradeMode()).evaluateSell(market, record);
    }

    @Override
    public boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                            TradeRuleRecord record) {
        // 调用当前订单簿交易模式对应的分钟均价卖出方法。
        return strategy(market.getTradeMode())
                .evaluateAveragePriceSell(calculateIndex, market, record);
    }

    @Override
    public boolean evaluateCancel(TradeMarketState market) {
        // 调用当前订单簿交易模式对应的撤单方法。
        return strategy(market.getTradeMode()).evaluateCancel(market);
    }

    @Override
    public boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
        return strategy(market.getTradeMode()).shouldEnableFirstBoardTradingMode(market);
    }

    @Override
    public boolean isContinuousBuyTimeAllowed(TradeMarketState market, int time) {
        return strategy(market.getTradeMode()).isContinuousBuyTimeAllowed(time);
    }

    @Override
    public int evaluateShanghaiAuctionBuy(TradeMarketState market, int time, int price,
                                          int limitUpPrice, long totalBuyVolume,
                                          long totalSellVolume, long requiredBuyVolume,
                                          long limitUpBuyAmount) {
        return strategy(market.getTradeMode()).evaluateShanghaiAuctionBuy(
                time, price, limitUpPrice, totalBuyVolume, totalSellVolume,
                requiredBuyVolume, limitUpBuyAmount);
    }

    @Override
    public int evaluateShanghaiAuctionCancel(TradeMarketState market, int price,
                                             int limitUpPrice, long totalBuyVolume,
                                             long totalSellVolume, long requiredBuyVolume) {
        return strategy(market.getTradeMode()).evaluateShanghaiAuctionCancel(
                price, limitUpPrice, totalBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenAuctionBuy(TradeMarketState market, byte dataType,
                                         int price, int limitUpPrice, int orderQuantity,
                                         long limitUpBuyVolume, long totalSellVolume,
                                         long requiredBuyVolume, long limitUpBuyAmount,
                                         long circulation) {
        return strategy(market.getTradeMode()).evaluateShenzhenAuctionBuy(
                dataType, price, limitUpPrice, orderQuantity, limitUpBuyVolume,
                totalSellVolume, requiredBuyVolume, limitUpBuyAmount, circulation);
    }

    @Override
    public int evaluateShenzhenAuctionCancel(TradeMarketState market,
                                            long limitUpBuyVolume, long totalSellVolume,
                                            long requiredBuyVolume) {
        return strategy(market.getTradeMode()).evaluateShenzhenAuctionCancel(
                limitUpBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                                     int price, int limitUpPrice) {
        return strategy(market.getTradeMode())
                .evaluateShenzhenSnapshotAuctionCancel(price, limitUpPrice);
    }

    @Override
    public boolean evaluateClosingAuctionSell(TradeMarketState market, int price,
                                              int limitUpPrice, long totalBuyVolume,
                                              long totalSellVolume) {
        return strategy(market.getTradeMode()).evaluateClosingAuctionSell(
                price, limitUpPrice, totalBuyVolume, totalSellVolume);
    }

    private TradingStrategy strategy(int tradeMode) {
        return switch (tradeMode) {
            case 1 -> relayStrategy;
            case 2 -> firstBoardStrategy;
            case 3 -> smallCapFirstBoardStrategy;
            default -> throw new IllegalStateException("订单簿未设置有效交易模式: " + tradeMode);
        };
    }
}
