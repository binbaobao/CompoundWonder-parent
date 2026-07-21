package com.compoundwonder.common.orderbook;

/**
 * 集合竞价策略读取的市场事件只读视图。
 *
 * <p>Disruptor 中已经预分配的 Tick 事件直接实现该接口，策略读取原对象，
 * 不复制快照、不创建临时 DTO，也不让策略模块依赖订单簿模块的具体事件类。</p>
 */
public interface AuctionMarketEvent {

    /** @return 数据类型：1 委托、2 成交、4 三秒行情快照 */
    byte getDataType();

    /** @return 逐笔委托方向：1 买、2 卖；快照和无方向事件为 0 */
    byte getDirection();

    /** @return 行情时间，紧凑格式 {@code HHmmssSSS} */
    int getTime();

    /** @return 当前委托或撮合价格，单位：分 */
    int getPrice();

    /** @return 当前委托或成交数量，单位：股 */
    int getQuantity();

    /** @return 当前交易所委托号或事件编号 */
    int getOrderId();

    /** @return 集合竞价快照买方总量，或逐笔事件中的买方委托号 */
    int getBuyerOrderId();

    /** @return 集合竞价快照卖方总量，或逐笔事件中的卖方委托号 */
    int getSellerOrderId();
}
