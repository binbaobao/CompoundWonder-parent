package com.compoundwonder.core.engine;


import lombok.Data;

@Data
public class TickNode {

    // 数据区
    private int price;
    private int quantity;
    private int time;
    private byte direction; // 1: Buy, 2: Sell
    /**
     * 成交编号
     * 委托编号
     */
    public int orderId;

    /**
     * 重置节点状态，准备归还池子或重新借出
     */
    public void clear() {
        this.price = 0;
        this.quantity = 0;
        this.time = 0;
        this.direction = 0;
        this.orderId = 0;
    }

    /**
     * 深度拷贝 Disruptor 传来的数据
     */
    public void copyFrom(TickData data) {
        this.price = data.price;
        this.quantity = data.quantity;
        this.time = data.time;
        this.direction = data.direction;
        this.orderId = data.orderId;
    }

}
