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

    /**
     * 开盘前使用交易柜台返回的当日证券信息修正订单簿参考价格。
     *
     * <p>该操作会重建订单簿价格索引，必须在第一条行情进入订单簿前完成，
     * 禁止在盘中行情处理期间调用。</p>
     *
     * @param securityID 证券代码
     * @param closePrice 昨收价
     * @param limitUpPrice 当日涨停价
     * @param limitDownPrice 当日跌停价
     * @param securityName 证券名称
     */
    void updatePreOpenPriceLimits(String securityID, double closePrice, double limitUpPrice,
                                  double limitDownPrice, String securityName);
}
