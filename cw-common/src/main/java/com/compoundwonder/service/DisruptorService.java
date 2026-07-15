package com.compoundwonder.service;


/**
 * disruptor 推送数据源内容接口
 */
public interface DisruptorService {


    void publishNGTSTickData(int symbolInt, int time, byte dataType, int orderId, int amount, byte direction, int price, int volume, byte bizType, int buyNo, int sellNo);


    /**
     * 推送 逐笔委托订单数据
     */
    void publishTickOrderData(int symbolInt, int time, int orderId, int price, int quantity, byte direction, byte type);

    /**
     * 推送委托信息
     *
     * @param symbolInt
     * @param time
     * @param price
     * @param quantity
     * @param direction
     * @param type
     */
    void pushOrderInfo(int symbolInt, int time, int price, int quantity, byte direction, byte type);

    /**
     * 推送逐笔成交订单数据
     */
    void publishTickTradeData(int symbolInt, int time, int orderId, int price, int quantity, int amount, byte direction, byte type, int buyerOrderId, int sellerOrderId);

    void publishLogData(int[] tick);

    /**
     * 整理 个人交易信息
     *
     * @param symbol 代码
     * @param type   交易信息：1、买入下单 2、买入成交 3、卖出成交，4，买入撤单(现在只有集合竞价有撤单)
     *               <br>1、买入下单，买入，自己这个已经关了，修改成盯撤单状态，然后关闭其他的下单
     *               <br>2、买入成交，买入，自己关了，其他的也关了
     *               <br>3、卖出成交，卖出，自己的关了，其他的打开（1,2板；如果持有的是龙头股，3板第二天就是中位）
     *               <br>4、买入撤单，买入，自己的打开，别的也打开,如果是擒龙捉妖就只打开擒龙捉妖
     *               <br>5、如果擒龙捉妖到指定时间还没有买入，就把所有的盯盘都打开
     *               <br>6、撤单后继续卖出
     *               <br>7、卖出下单修改自己的状态，基本只有在手动卖出时有用
     *               <br>下单，卖出，把自己的先关了，其他都是关闭状态，其他的暂时不动，也不用发
     */
    void publishTransInfoData(String symbol, int type);

    /**
     * 推送单个消息到盯盘
     *
     * @param symbol
     * @param type
     */
    void publishTransInfoDataOne(String symbol, byte type);

    /**
     * 推送 3 秒行情快照信息
     * 集合竞价 买入（只上海） 卖出
     *
     * @param symbol     代码
     * @param time       快照时间
     * @param sellVolume 竞价涨停总卖
     * @param buyVolume  竞价涨停总买
     */
    void publishSnapshotData(int symbol, int time, int price, long amount, long sellVolume, long buyVolume);

}
