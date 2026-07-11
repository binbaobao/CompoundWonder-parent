package com.compoundwonder.util;

import lombok.Data;

/**
 * 订单簿委托订单
 */
@Data
public class OrderBookOrderInfo {
    /**
     * 价格
     */
    private int limitPrice;

    /**
     * 数量
     */
    private int volume;
    /**
     * 系统报单编号
     */
    private int orderId;

    /**
     * 报单状态
     * 预埋 '0'
     * 未知 '1'
     * 交易所已接收 '2'
     * 部分成交 '3'
     * 全部成交 '4'
     * 部成部撤 '5'
     * 全部撤单 '6'
     * 交易所已拒绝 '7'
     */
    private char orderStatus;
    /**
     * 转变成 level 2 的时间戳
     */
    private int time;
    /**
     * 前面单量
     */
    private int orderVolume;
}
