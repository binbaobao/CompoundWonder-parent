package com.compoundwonder.service;

/**
 * 实盘交易接口
 */
public interface RealTradeService {

    /**
     * 实盘初始化
     * @throws InterruptedException
     */
    void traderApiInit() throws InterruptedException;

    /**
     * 查询股票信息，重新设置涨停跌停价格
     *
     * @param symbol
     */
    void queryOrderBookInfo(String symbol);

    void updateTraderInfo();

    void clearCache();

    void buy(String date, int symbol, int price, int time);

    void sell(String symbol, int price, int limitDownPrice);

    void quickSell(String symbol, int price, int limitDownPrice);

    void cancel(String symbol);

    /**
     * 擒龙捉妖模式极速下跌的时候，及时发送消息开启首板模式
     */
    void enableFirstLimitUpTradingMode(String stackCode);
}
