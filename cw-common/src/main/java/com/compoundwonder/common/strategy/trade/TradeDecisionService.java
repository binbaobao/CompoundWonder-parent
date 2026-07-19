package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/** 订单簿调用交易策略的统一接口，具体实现由 app 组装根注入。 */
public interface TradeDecisionService {

    boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record);

    boolean evaluateSell(TradeMarketState market, TradeRuleRecord record);

    boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                     TradeRuleRecord record);

    boolean evaluateCancel(TradeMarketState market);

    boolean shouldEnableFirstBoardTradingMode(TradeMarketState market);

    boolean isContinuousBuyTimeAllowed(TradeMarketState market, int time);

    int evaluateShanghaiAuctionBuy(TradeMarketState market, int time, int price,
                                   int limitUpPrice, long totalBuyVolume,
                                   long totalSellVolume, long requiredBuyVolume,
                                   long limitUpBuyAmount);

    int evaluateShanghaiAuctionCancel(TradeMarketState market, int price,
                                      int limitUpPrice, long totalBuyVolume,
                                      long totalSellVolume, long requiredBuyVolume);

    int evaluateShenzhenAuctionBuy(TradeMarketState market, byte dataType,
                                  int price, int limitUpPrice, int orderQuantity,
                                  long limitUpBuyVolume, long totalSellVolume,
                                  long requiredBuyVolume, long limitUpBuyAmount,
                                  long circulation);

    int evaluateShenzhenAuctionCancel(TradeMarketState market,
                                     long limitUpBuyVolume, long totalSellVolume,
                                     long requiredBuyVolume);

    int evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                             int price, int limitUpPrice);

    boolean evaluateClosingAuctionSell(TradeMarketState market, int price,
                                       int limitUpPrice, long totalBuyVolume,
                                       long totalSellVolume);
}
