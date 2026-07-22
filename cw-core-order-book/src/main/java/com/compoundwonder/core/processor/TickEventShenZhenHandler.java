package com.compoundwonder.core.processor;


import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.core.engine.TickNodePool;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.OrderBookSession;
import com.compoundwonder.core.engine.StrategyExecutionSession;
import com.compoundwonder.core.engine.TradeExecutionState;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.strategy.trade.TradeTriggerType;
import com.compoundwonder.util.CompactTimeUtil;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;


/**
 * 处理模拟撮合或盘口逻辑
 * <p>
 * 处理下单撤单逻辑
 */
@Slf4j
public class TickEventShenZhenHandler implements EventHandler<TickData> {

    private static final int ORDER_BOOK_CAPACITY = 10_000;

    private int time;

    private final UnifiedTradeExecutor tradeExecutor;

    private final TickNodePool tickNodePool = new TickNodePool(100000);

    private final RuleRecordBuffer ruleRecordBuffer = new RuleRecordBuffer(10);

    private final OrderBookSession[] sessions = new OrderBookSession[ORDER_BOOK_CAPACITY];

    public TickEventShenZhenHandler(OrderExecutionGateway executionGateway) {
        this.tradeExecutor = new UnifiedTradeExecutor(executionGateway);
    }

    public void registerSession(int symbolId, OrderBookSession session) {
        sessions[symbolId % ORDER_BOOK_CAPACITY] = session;
    }

    public void reset() {
        for (OrderBookSession session : sessions) {
            if (session != null) {
                session.orderBook().clearOrders(tickNodePool::release);
            }
        }
        Arrays.fill(sessions, null);
        ruleRecordBuffer.clear();
        time = 0;
    }

    @Override
    public void onEvent(TickData order, long sequence, boolean endOfBatch) {
        OrderBookSession marketSession = sessions[order.symbolId % ORDER_BOOK_CAPACITY];
        if (marketSession == null) return;
        OrderBook orderBook = marketSession.orderBook();
        List<StrategyExecutionSession> strategySessions = marketSession.strategySessions();
        order.time2 = System.nanoTime();
        this.updateTime(order.dataType, order.time);
        // 撤单只能处理事件到达前已经存在的挂单，防止本次大单刚触发买入后又被
        // 同一个逐笔事件立即撤销；从下一条逐笔或下一张快照开始正常观察强度。
        boolean[] pendingAuctionBuyBeforeEvent = new boolean[strategySessions.size()];
        for (int index = 0; index < strategySessions.size(); index++) {
            pendingAuctionBuyBeforeEvent[index] = strategySessions.get(index)
                    .executionState().isBuyOrderPending();
        }
        orderBook.buyMaxOrder.clear();
        if (order.dataType == 1) {
            //逐笔委托数据
            boolean added = addOrder(order, marketSession);
            if (added && order.direction == 1) {
                orderBook.buyMaxOrder.copyFrom(order);
            }
            orderBook.updateLimitUpStatus(marketSession.spec()); //实时更新涨停状态
        } else if (order.dataType == 2) {
            // 深市 交易类型：0-成交，1-撤单
            if (order.type == 0) {
                // 成交额，成交量，最高价格，最低价格,最新价格
                orderBook.updatePrice(order.amount, order.quantity, order.price, order.time, marketSession.spec());
                // 处理成交数据
                tradeOrder(order, order.buyerOrderId, orderBook);
                tradeOrder(order, order.sellerOrderId, orderBook);
            } else {
                cancelOrder(order, orderBook);
            }
            orderBook.updateLimitUpStatus(marketSession.spec()); //实时更新涨停状态
        } else if (order.dataType == 3) {
            processControlEvent(marketSession, order);
            order.time3 = System.nanoTime();
            return;
        } else if (order.dataType == 5) {
            // 券商下单受理回报只用于推进当前市场时间，不修改订单簿，也不重复触发策略。
            order.time3 = System.nanoTime();
            return;
        } else if (order.dataType == 4) {
            // 原注释：深圳早盘竞价卖出 TODO 急跌、低开、连续三天加速后低开卖出。
            // 当前业务已经明确不执行常规早盘低开卖出，这里只保留竞价撤单和尾盘卖出执行编排。
            // 集合竞价期间 买卖撤单操作 ConstantUtil.TIME_1457
            if (ConstantUtil.TIME_930 > order.time || (ConstantUtil.TIME_1457 <= order.time && ConstantUtil.TIME_1500 > order.time)) {
                if (order.time > ConstantUtil.TIME_920) {
                    orderBook.updateLowestPrice(order.price, marketSession.spec());
                }
                orderBook.setLastPrice(order.price);
                processSnapshotAuctionStrategies(marketSession, order);
            } else if (order.time < ConstantUtil.TIME_1457) {
                // 连续竞价期间用 3秒的tick 数据修复成交额成交量数据
                long turnover;
                if (order.type == 0) {
                    turnover = order.orderId;
                } else {
                    turnover = order.orderId * 100L;
                }
                if (order.price == 0) {
                    order.price = marketSession.getLimitDownPrice();
                }
                // 计算均价
                int calculateIndex = CompactTimeUtil.calculateIndex(order.time);
                if (order.sellerOrderId != 0) {
                    int currentAveragePrice = (int) (turnover * 100L / order.sellerOrderId);
                    orderBook.updateMinuteAveragePrice(calculateIndex, currentAveragePrice, order.time, marketSession.spec());
                }
                // 如果这一分钟的价格是 0 就打印一次信息，这就是每分钟一次
                int l1v = order.sellerOrderId / 1000000;
                int l2v = (int) (orderBook.getVolume() / 1000000);
                if (orderBook.price[calculateIndex] == 0 && calculateIndex >= 3 && (l1v - l2v) > l2v) {
                    log.info("信息对照 -- {}({}):时间:{},L1(价格:{},成交量:{} W手) || L2(价格:{}, 成交量:{} W手)", marketSession.getSecurityName(), marketSession.getSymbol(), time / 100000, order.price, order.sellerOrderId / 1000000, orderBook.getLastPrice(), orderBook.getVolume() / 1000000);
                    orderBook.setTurnover(0);
                    orderBook.setVolume(0);
                    // 更新成交额成交量，价格
                    orderBook.updatePrice(turnover, order.sellerOrderId, order.price, order.time, marketSession.spec());
                }
                orderBook.price[calculateIndex] = order.price;
                processAveragePriceSell(marketSession, calculateIndex, order, turnover);
            }
            // 可交易状态下，10点还没有涨停的首板就是弱了，直接关闭打板任务
            disableWeakFirstBoardSessions(marketSession);
        }

        // 深交所 09:25 集中撮合前使用逐笔事件观察集合竞价。
        // 逐笔事件同时负责买入与撤单：绝对强度读取更新后的全量订单簿；单笔大单
        // 还必须是本次成功入簿的买方向涨停价新增委托。快照是 Level2 行情延迟时
        // 的补充撤单触发源，不能替代逐笔撤单。
        if ((order.dataType == 1 || order.dataType == 2) && order.time < ConstantUtil.TIME_925) {
            int limitUpPrice = marketSession.getLimitUpPrice();
            // 涨停价买队列剩余总量，单位为股。
            long limitUpBuyVolume = orderBook.getBuyQuantity(limitUpPrice);
            // 所有价格档位仍留在订单簿中的卖单剩余总量，单位为股。
            long totalSellVolume = orderBook.getTotalSellVolume();

            processOpeningAuctionStrategies(strategySessions, pendingAuctionBuyBeforeEvent,
                    order, limitUpBuyVolume, totalSellVolume);
        }
        // 深交所盘中策略统一从 09:31 开始，避开开盘初段行情延迟。
        if (order.time >= time && order.time < ConstantUtil.TIME_1457) {
            processContinuousStrategies(marketSession, order);
        }
        order.time3 = System.nanoTime();
    }

    private void processControlEvent(OrderBookSession marketSession, TickData order) {
        tradeExecutor.processControl(marketSession, order);
    }

    private void processSnapshotAuctionStrategies(OrderBookSession marketSession, TickData order) {
        OrderBook orderBook = marketSession.orderBook();
        for (StrategyExecutionSession session : marketSession.strategySessions()) {
            TradeTriggerType triggerType = order.time < ConstantUtil.TIME_930
                    ? TradeTriggerType.OPENING_AUCTION : TradeTriggerType.CLOSING_AUCTION;
            if (!session.template().supports(triggerType)) continue;
            TradeExecutionState state = session.executionState();
            if (state.isBuyOrderPending()
                    && order.time < ConstantUtil.TIME_920
                    && order.time > ConstantUtil.TIME_91530) {
                long limitUpBuyVolume = orderBook.getBuyQuantity(session.getLimitUpPrice());
                long totalSellVolume = orderBook.getTotalSellVolume();
                RuleRecord record = ruleRecordBuffer.nextRecord(session);
                if (session.template().shenzhenOpeningAuctionBuy().evaluateSnapshotCancel(
                        session, order, time, limitUpBuyVolume, totalSellVolume, record)) {
                    tradeExecutor.submitCancel(session);
                    state.beginBuyMonitoring();
                    ruleRecordBuffer.commit();
                }
            }
            if (state.isSellMonitoring()
                    && ConstantUtil.TIME_1459 <= order.time
                    && ConstantUtil.TIME_1500 > order.time) {
                RuleRecord record = ruleRecordBuffer.nextRecord(session);
                if (session.template().closingAuctionSell().evaluateShenzhen(
                        session, order, time, record)) {
                    tradeExecutor.submitSell(session, session.getLimitDownPrice());
                    state.beginSellOrder();
                    ruleRecordBuffer.commit();
                }
            }
        }
    }

    private void processAveragePriceSell(OrderBookSession marketSession, int calculateIndex,
                                         TickData order, long turnover) {
        tradeExecutor.processAveragePriceSell(
                marketSession, calculateIndex, order, ruleRecordBuffer);
    }

    private void disableWeakFirstBoardSessions(OrderBookSession marketSession) {
        tradeExecutor.disableWeakFirstBoardSessions(marketSession, time);
    }

    private void processOpeningAuctionStrategies(
            List<StrategyExecutionSession> sessions,
            boolean[] pendingBeforeEvent,
            TickData order, long limitUpBuyVolume, long totalSellVolume) {
        for (int index = 0; index < sessions.size(); index++) {
            StrategyExecutionSession session = sessions.get(index);
            if (!session.template().supports(TradeTriggerType.OPENING_AUCTION)) continue;
            TradeExecutionState state = session.executionState();
            if (state.isBuyMonitoring()) {
                RuleRecord buyRecord = ruleRecordBuffer.nextRecord(session);
                if (session.template().shenzhenOpeningAuctionBuy().evaluateBuy(
                        session, order, time, limitUpBuyVolume, totalSellVolume, buyRecord)) {
                    tradeExecutor.submitBuy(session, order.symbolId, session.orderBook().getTime());
                    state.beginBuyOrder();
                    ruleRecordBuffer.commit();
                }
            }
            if (pendingBeforeEvent[index]
                    && order.time > ConstantUtil.TIME_91957
                    && order.time < ConstantUtil.TIME_920) {
                RuleRecord cancelRecord = ruleRecordBuffer.nextRecord(session);
                if (session.template().shenzhenOpeningAuctionBuy().evaluateOrderBookCancel(
                        session, order, time, limitUpBuyVolume, totalSellVolume, cancelRecord)) {
                    tradeExecutor.submitCancel(session);
                    state.beginBuyMonitoring();
                    ruleRecordBuffer.commit();
                }
            }
        }
    }

    private void processContinuousStrategies(OrderBookSession marketSession, TickData order) {
        tradeExecutor.processContinuous(marketSession, order, ruleRecordBuffer);
    }

    // 逐笔委托数据
    private boolean addOrder(TickData order, OrderBookSession session) {
        OrderBook orderBook = session.orderBook();
        // 深圳市价委托可能以 0 或涨跌停区间外的价格推送，统一按到达时订单簿最新价入队。
        if (order.price < session.getLimitDownPrice()
                || order.price > session.getLimitUpPrice()) {
            order.price = orderBook.getLastPrice();
        }
        TickNode tickNode = tickNodePool.borrowNode();
        tickNode.copyFrom(order);
        OrderBook.AddOrderResult result = orderBook.addOrder(tickNode);
        if (result == OrderBook.AddOrderResult.ADDED) {
            return true;
        }
        tickNodePool.release(tickNode);
        log.warn("深圳委托已忽略 reason={}, symbolId={}, orderId={}, direction={}, price={}, quantity={}, time={}",
                result, order.symbolId, order.orderId, order.direction, order.price, order.quantity, order.time);
        return false;
    }

    /**
     * 逐笔成交数据
     *
     * @param trade
     * @param orderId
     * @param orderBook
     */
    private void tradeOrder(TickData trade, int orderId, OrderBook orderBook) {
        TickNode completed = orderBook.applyTrade(orderId, trade.quantity);
        if (completed != null) {
            tickNodePool.release(completed);
        }
    }

    // 撤单
    private void cancelOrder(TickData trade, OrderBook orderBook) {
        //交易方向 1-买方成交，2-卖方成交
        int orderId = trade.buyerOrderId == 0 ? trade.sellerOrderId : trade.buyerOrderId;
        TickNode cancelled = orderBook.cancelOrder(orderId);
        if (cancelled != null) {
            tickNodePool.release(cancelled);
        }
    }

    /**
     * 更新行情时间
     *
     * @param dataType
     * @param time
     */
    private void updateTime(byte dataType, int time) {
        if (dataType == 4) {
            this.time = time;
        } else if (time > this.time) {
            this.time = time;
        }
    }

    /**
     *
     * @return
     */
    public RuleRecordBuffer getRuleRecords() {

        return ruleRecordBuffer;
    }
}
