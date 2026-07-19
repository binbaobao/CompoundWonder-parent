package com.compoundwonder.strategy;

/** 一种交易模式完整的买入、卖出和撤单规则入口。 */
public interface TradingStrategy {

    boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record);

    boolean evaluateSell(TradeMarketState market, TradeRuleRecord record);

    boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                     TradeRuleRecord record);

    boolean evaluateCancel(TradeMarketState market);

    boolean shouldEnableFirstBoardTradingMode(TradeMarketState market);

    boolean isContinuousBuyTimeAllowed(int time);

    int evaluateShanghaiAuctionBuy(int time, int price, int limitUpPrice,
                                   long totalBuyVolume, long totalSellVolume,
                                   long requiredBuyVolume, long limitUpBuyAmount);

    int evaluateShanghaiAuctionCancel(int price, int limitUpPrice,
                                      long totalBuyVolume, long totalSellVolume,
                                      long requiredBuyVolume);

    int evaluateShenzhenAuctionBuy(byte dataType, int price, int limitUpPrice,
                                  int orderQuantity, long limitUpBuyVolume,
                                  long totalSellVolume, long requiredBuyVolume,
                                  long limitUpBuyAmount, long circulation);

    int evaluateShenzhenAuctionCancel(long limitUpBuyVolume, long totalSellVolume,
                                     long requiredBuyVolume);

    int evaluateShenzhenSnapshotAuctionCancel(int price, int limitUpPrice);

    boolean evaluateClosingAuctionSell(int price, int limitUpPrice,
                                       long totalBuyVolume, long totalSellVolume);
}
