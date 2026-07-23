package com.compoundwonder.core.service;



import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.MarketSessionSpec;
import com.compoundwonder.core.engine.TradeExecutionState;
import com.compoundwonder.core.engine.StrategyExecutionSession;
import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.service.OrderBookService;
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
public class CacheService implements OrderBookRepository, OrderBookService {

    /**
     * 股票订单簿
     */
    private final Int2ObjectOpenHashMap<OrderBookSession> orderBookMap = new Int2ObjectOpenHashMap<>();

    public final List<TickData> orderList = new ArrayList<>();

    /**
     * 添加盯盘任务
     *
     * @param code 内部整数证券编号
     * @param orderBook 旧调用链已经初始化的单策略订单簿
     */
    public void putOrderBook(int code, OrderBook orderBook) {
        put(code, orderBook);
    }

    /**
     * 旧缓存测试兼容入口；行情引擎正式链路必须注册完整会话。
     * 这里生成的模板执行器均为空，禁止把该入口注册的订单簿送入行情 Handler。
     */
    @Deprecated
    public void put(int symbolId, OrderBook orderBook) {
        TradeStaticFacts facts = new TradeStaticFacts(
                orderBook.getTradeMode(), orderBook.getLbcs(), orderBook.getMaxVolume(),
                orderBook.getMaxHs(), orderBook.getInitialMarketValue(),
                orderBook.getThreeDaysTurnover(), orderBook.getTwoDaysTurnover(),
                orderBook.getYesterdayTurnover(), orderBook.getOneWordLimitUp(),
                orderBook.getAverageLimitUpHeight(), orderBook.getNextTradingDay(), 0, 0);
        TradeExecutionTemplate template = new TradeExecutionTemplate() {
            @Override public TradeStaticFacts facts() { return facts; }
            @Override public ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy() { return null; }
            @Override public ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy() { return null; }
            @Override public ContinuousBuyExecutor continuousBuy() { return null; }
            @Override public ContinuousSellExecutor continuousSell() { return null; }
            @Override public AveragePriceSellExecutor averagePriceSell() { return null; }
            @Override public ClosingAuctionSellExecutor closingAuctionSell() { return null; }
        };
        MarketSessionSpec spec = new MarketSessionSpec(
                orderBook.getSymbol(), orderBook.getSecurityName(), orderBook.getMarket(),
                orderBook.getDate(), orderBook.getCirculation(), orderBook.getClosePrice(),
                orderBook.getLimitUpPrice(), orderBook.getLimitDownPrice());
        put(symbolId, new OrderBookSession(spec, facts, orderBook, template,
                new TradeExecutionState(orderBook.getTransactionStatus())));
    }

    @Override
    public void put(int symbolId, OrderBookSession session) {
        orderBookMap.put(symbolId, session);
    }

    /**
     * 获取所有盯盘股票代码
     *
     * @return 当前已注册的六位股票代码集合
     */
    public Set<String> getOrderBookCodes() {
        return getSymbols();
    }

    @Override
    public Set<String> getSymbols() {
        return orderBookMap.keySet().stream().map(SymbolUtil::intToSymbol).collect(Collectors.toSet());
    }

    public void printAllStockInfo() {
        log.info("打印股票信息----------------------------------------------------------------------------------------------------");
        ObjectCollection<OrderBookSession> values = orderBookMap.values();
        values.forEach(session -> {
            log.info("-- {}({}) -- {}", session.getSecurityName(), session.getSymbol(), session.orderBook());
        });
        log.info("打印股票信息----------------------------------------------------------------------------------------------------");
    }

    /**
     * 根据股票代码获取订单簿
     *
     * @param code 内部整数证券编号
     * @return 对应共享盘口；未注册时返回 {@code null}
     */
    public OrderBook getOrderBook(int code) {
        OrderBookSession session = get(code);
        return session == null ? null : session.orderBook();
    }

    @Override
    public OrderBookSession get(int symbolId) {
        return orderBookMap.get(symbolId);
    }

    @Override
    public void updatePreOpenPriceLimits(String securityID, double closePrice, double limitUpPrice,
                                         double limitDownPrice, String securityName) {
        OrderBookSession session = get(SymbolUtil.fastSymbolToInt(securityID));
        if (session != null) {
            // MarketSessionSpec 供规则读取，OrderBook 自身的价位数组供热路径索引；两者必须同步。
            session.spec().updatePreOpenPriceLimits(closePrice, limitUpPrice, limitDownPrice, securityName);
            session.orderBook().updatePreOpenPriceLimits(
                    closePrice, limitUpPrice, limitDownPrice, securityName);
        }
    }

    /**
     * 获取所有擒龙捉妖模式的监控股票
     *
     * @return 至少存在一个非首板、未进入卖出状态策略会话的股票代码
     */
    public List<String> getQinLongCodes() {
        List<String> qinLongCodes = new ArrayList<>();

        for (OrderBookSession marketSession : orderBookMap.values()) {
            // 同股多模式共享盘口，是否属于擒龙监控必须检查全部独立策略，不能读取主会话代替。
            boolean monitored = marketSession.strategySessions().stream()
                    .anyMatch(CacheService::isQinLongMonitoringSession);
            if (monitored) {
                qinLongCodes.add(marketSession.getSymbol());
            }
        }
        return qinLongCodes;
    }

    private static boolean isQinLongMonitoringSession(StrategyExecutionSession session) {
        return session.getLbcs() != 1
                && session.executionState().transactionStatus() >= 0;
    }


    /**
     * 收盘清除缓存
     */
    public void clearCacheMap() {
        clear();
    }

    @Override
    public void clear() {
        orderBookMap.clear();
    }
}
