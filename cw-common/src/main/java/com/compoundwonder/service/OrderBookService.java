package com.compoundwonder.service;

import java.util.Set;

/**
 * 订单簿接口
 */
public interface OrderBookService {
    /**
     * 获取当日所有盯盘股票，盯盘初始化或者断开重连
     * @return
     */
    Set<String> getOrderBookCodes();
}
