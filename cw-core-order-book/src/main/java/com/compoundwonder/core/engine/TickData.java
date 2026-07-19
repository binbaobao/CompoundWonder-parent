package com.compoundwonder.core.engine;


import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.util.HighPrecisionClock;

/**
 * 逐笔数据
 * 逐笔委托或逐笔成交数据
 */
public class TickData implements AuctionMarketEvent {
    // --- Padding 1: 隔离对象头 ---
    protected long p1, p2, p3;
    // --- 4字节区 (8个int, 总共32字节, 完美对齐) ---
    /**
     * 证券代码
     */
    public int symbolId;
    /**
     * 成交时间
     * 委托时间
     */
    public int time;
    /**
     * 成交编号
     * 委托编号
     */
    public int orderId;
    /**
     * 成交单价
     * 委托单价
     * 除 100
     */
    public int price;
    /**
     * 成交数量
     * 委托数量
     */
    public int quantity;
    /**
     * 成交金额
     */
    public int amount;
    /**
     * 买方委托编号
     * 沪市 主动成交无委托记录，剩余转委托；被动成交都有委托记录，成交价一定等于委托价；集合竞价都有委托记录，成交价不一定等于委托价
     * 深市 主动被动都有委托记录；对手价扫单 1-5 档，未成转限价、转撤单；排队价被动成交
     */
    public int buyerOrderId;
    /**
     * 卖方委托编号
     * 沪市 主动成交无委托记录，剩余转委托；被动成交都有委托记录，成交价一定等于委托价；集合竞价都有委托记录，成交价不一定等于委托价
     * 深市 主动被动都有委托记录；对手价扫单 1-5 档，未成转限价、转撤单；本方优先只有被动
     */
    public int sellerOrderId;
    // --- 1字节区 (3个byte, 紧随其后) ---
    /**
     * 数据类型 ：1:委托 , 2:成交 , 3: 交易信息修改股票交易状态 4:集合竞价 5:下单信息
     */
    public byte dataType;
    /**
     * 成交：交易方向 1-买方成交，2-卖方成交
     * 委托：交易方向：1-买入，2-卖出
     */
    public byte direction;
    /**
     * 成交：
     * 沪市 交易类型：全部为 0
     * 深市 交易类型：0-成交，1-撤单
     * 委托：
     * 沪市 交易类型：2-限价，10-撤单
     * 深市 交易类型：1-对手价，2-限价，3-排队价
     */
    public byte type;

    public long time1;
    public long time2;
    public long time3;

    // --- Padding 2: 隔离下一个对象 ---
    // 因为前面数据区(32 + 3 = 35字节) + 对象头(12/16字节)
    // 我们需要补齐到 64 字节的倍数
    protected long p8, p9, p10;

    @Override
    public byte getDataType() {
        return dataType;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public int getOrderId() {
        return orderId;
    }

    @Override
    public int getBuyerOrderId() {
        return buyerOrderId;
    }

    @Override
    public int getSellerOrderId() {
        return sellerOrderId;
    }

    @Override
    public String toString() {
        return symbolId + "," + dataType + "," + time + "," + orderId + "," + price + "," + quantity + "," + amount + "," + direction + "," + type + "," + buyerOrderId + "," + sellerOrderId + " ,接收时间：" + HighPrecisionClock.format(time1) + " ,待处理时间：" + HighPrecisionClock.format(time2) + " ,处理完成时间：" + HighPrecisionClock.format(time3);
        //1600791,1,135530810,14066599,802,946200,0,2,2,0,14066599 ,接收时间：13:55:29.430577207 ,待处理时间：13:55:29.430578102 ,处理完成时间：13:55:29.458211973
    }

}
