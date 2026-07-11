package com.compoundwonder.core.service;



import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.util.SymbolUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缓存服务
 */
@Slf4j
@Component
public class CacheService {


    /**
     * 股票订单簿
     */
    private final Int2ObjectOpenHashMap<OrderBook> orderBookMap = new Int2ObjectOpenHashMap<>();


    public final List<TickData> orderList = new ArrayList<>();

    /**
     * 添加盯盘任务
     *
     * @param code
     * @param orderBook
     */
    public void putOrderBook(int code, OrderBook orderBook) {
        orderBookMap.put(code, orderBook);
    }

    /**
     * 获取所有盯盘股票代码
     *
     * @return
     */
    public Set<String> getOrderBookCodes() {
        return orderBookMap.keySet().stream().map(SymbolUtil::intToSymbol).collect(Collectors.toSet());
    }

    public void printAllStockInfo() {
        log.info("打印股票信息----------------------------------------------------------------------------------------------------");
        ObjectCollection<OrderBook> values = orderBookMap.values();
        values.forEach(orderBook -> {
            log.info("-- {}({}) -- {}", orderBook.getSecurityName(), orderBook.getSymbol(), orderBook);
        });
        log.info("打印股票信息----------------------------------------------------------------------------------------------------");
    }

    /**
     * 根据股票代码获取订单簿
     *
     * @param code
     * @return
     */
    public OrderBook getOrderBook(int code) {
        return orderBookMap.get(code);
    }

    /**
     * 获取所有擒龙捉妖模式的监控股票
     *
     * @return
     */
    public List<String> getQinLongCodes() {
        List<String> qinLongCodes = new ArrayList<>();

        for (OrderBook value : orderBookMap.values()) {
            if (value.getLbcs() != 1 && value.getTransactionStatus() >= 0) {
                qinLongCodes.add(value.getSymbol());
            }
        }
        return qinLongCodes;
    }


    /**
     * 收盘清除缓存
     */
    public void clearCacheMap() {
        orderBookMap.clear();
    }
}
