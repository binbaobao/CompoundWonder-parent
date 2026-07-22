package com.compoundwonder.core.service;

import com.compoundwonder.core.engine.OrderBookSession;

import java.util.Set;

/**
 * 订单簿注册表，供引擎初始化流程和行情 Handler 共享同一实例。
 *
 * <p>{@link #put(int, OrderBookSession)} 和 {@link #clear()} 仅供引擎注册、重置等生命周期操作使用。
 * {@link #get(int)} 返回 Handler 正在维护的订单簿会话引用，普通调用方只能读取，
 * 禁止通过该引用修改订单簿；行情运行期间 Handler 是订单簿的唯一写入者。</p>
 */
public interface OrderBookRepository {

    /**
     * 在行情进入前注册订单簿。
     */
    void put(int symbolId, OrderBookSession session);

    /**
     * 获取共享订单簿引用。调用方不得修改返回对象。
     */
    OrderBookSession get(int symbolId);

    Set<String> getSymbols();

    /**
     * 在引擎重置或关闭时清空注册关系。
     */
    void clear();
}
