package com.compoundwonder.common.orderbook;

/**
 * 交易策略读取的订单簿只读视图。
 *
 * <p>订单簿直接实现该接口，热路径不复制快照，也不创建临时 DTO。</p>
 */
public interface TradeMarketState {

    int getTradeMode();
    String getSymbol();
    int getStatus();
    int getLbcs();
    int getTime();
    int getClosePrice();
    int getLastPrice();
    int getLowPrice();
    double getLowPriceIncrease();
    int getHighestPrice();
    int getLimitUpPrice();
    int getOpenPrice();
    double getOpenIncrease();
    double getIncrease();
    double getAmplitude();
    double getLimitUpBreakDepth();
    double getTurnoverRate();
    long getTurnover();
    long getVolume();
    long getMaxVolume();
    double getMaxHs();
    long getCirculation();
    int getInitialMarketValue();
    double getThreeDaysTurnover();
    double getTwoDaysTurnover();
    double getYesterdayTurnover();
    int getOneWordLimitUp();
    int getAverageLimitUpHeight();
    int getNextTradingDay();
    long getLimitUpBuyAmount();
    int getLastLimitUptime();
    double getLastEmaVolume();
    double getChangePercent();
    long getLastSealAmount();
    int getMinutePriceAt(int index);
    int getAveragePriceAt(int index);
    int getLargestBuyOrderPrice();
    int getLargestBuyOrderQuantity();
}
