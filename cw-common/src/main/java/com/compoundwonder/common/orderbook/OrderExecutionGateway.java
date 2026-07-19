package com.compoundwonder.common.orderbook;

/**
 * 订单簿发出的交易动作出口。
 * 回测和实盘分别提供实现，核心订单簿不感知具体执行环境。
 */
public interface OrderExecutionGateway {

    void buy(String date, int symbol, int price, int time);

    void sell(String symbol, int price, int limitDownPrice);

    void quickSell(String symbol, int price, int limitDownPrice);

    void cancel(String symbol);

    void enableFirstLimitUpTradingMode(String symbol);
}
