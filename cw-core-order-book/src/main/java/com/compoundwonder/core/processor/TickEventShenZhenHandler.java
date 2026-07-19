package com.compoundwonder.core.processor;


import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.core.engine.TickNodePool;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.util.CompactTimeUtil;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/**
 * 处理模拟撮合或盘口逻辑
 * <p>
 * 处理下单撤单逻辑
 */
@Slf4j
public class TickEventShenZhenHandler implements EventHandler<TickData> {

    private static final int ORDER_BOOK_CAPACITY = 10_000;

    private int time;

    private final OrderExecutionGateway executionGateway;

    /** 按订单簿 tradeMode 以 switch 分发到三套独立交易规则。 */
    private final TradeDecisionService tradeDecisionService;

    private final TickNodePool tickNodePool = new TickNodePool(100000);

    private final RuleRecordBuffer ruleRecordBuffer = new RuleRecordBuffer(10);

    private final OrderBook[] orderBooks = new OrderBook[ORDER_BOOK_CAPACITY];

    public TickEventShenZhenHandler(OrderExecutionGateway executionGateway,
                                    TradeDecisionService tradeDecisionService) {
        this.executionGateway = executionGateway;
        this.tradeDecisionService = tradeDecisionService;
    }

    public void registerOrderBook(int symbolId, OrderBook orderBook) {
        orderBooks[symbolId % ORDER_BOOK_CAPACITY] = orderBook;
    }

    public void reset() {
        for (OrderBook orderBook : orderBooks) {
            if (orderBook != null) {
                orderBook.clearOrders(tickNodePool::release);
            }
        }
        Arrays.fill(orderBooks, null);
        ruleRecordBuffer.clear();
        time = 0;
    }

    @Override
    public void onEvent(TickData order, long sequence, boolean endOfBatch) {
        OrderBook orderBook = orderBooks[order.symbolId % ORDER_BOOK_CAPACITY];
        if (orderBook == null) return;
        order.time2 = System.nanoTime();
        this.updateTime(order.dataType, order.time);
        // 0 任务暂时不执行 或 任务已经执行(已经买入或者已经卖出)， 1 待买入，2 买入待撤单  -1待卖出 -2 卖出待撤单
        int transStatus = orderBook.getTransactionStatus();
        orderBook.buyMaxOrder.clear();
        if (order.dataType == 1) {
            //逐笔委托数据
            boolean added = addOrder(order, orderBook);
            if (added && order.direction == 1) {
                orderBook.buyMaxOrder.copyFrom(order);
            }
            orderBook.updateLimitUpStatus(); //实时更新涨停状态
        } else if (order.dataType == 2) {
            // 深市 交易类型：0-成交，1-撤单
            if (order.type == 0) {
                // 成交额，成交量，最高价格，最低价格,最新价格
                orderBook.updatePrice(order.amount, order.quantity, order.price, order.time);
                // 处理成交数据
                tradeOrder(order, order.buyerOrderId, orderBook);
                tradeOrder(order, order.sellerOrderId, orderBook);
            } else {
                cancelOrder(order, orderBook);
            }
            orderBook.updateLimitUpStatus(); //实时更新涨停状态
        } else if (order.dataType == 3) {
            if (transStatus >= 0 && transStatus != order.type) {
                // 处理推送 个人交易信息
                orderBook.setTransactionStatus(order.type);
                log.info("修改股票 {} 监控状态，由 {} -> {}", order.symbolId, transStatus, order.type);
            } else if (transStatus < 0) {
                if (order.type == 0) {
                    orderBook.setTransactionStatus(order.type);
                    log.info("修改卖出股票 {} 监控状态，由 {} -> {}", order.symbolId, transStatus, order.type);
                } else {
                    executionGateway.quickSell(orderBook.getSymbol(), orderBook.getLastPrice(), orderBook.getLimitDownPrice());
                }
            }
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
                    orderBook.updateLowestPrice(order.price);
                }
                orderBook.setLastPrice(order.price);
                if (transStatus == 2 && order.time < ConstantUtil.TIME_920 && order.time > ConstantUtil.TIME_91530) {
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    // 调用当前模式深圳快照集合竞价撤单规则。
                    if (tradeDecisionService.evaluateShenzhenSnapshotAuctionCancel(
                            orderBook, order, time, ruleRecord)) {
                        executionGateway.cancel(orderBook.getSymbol());
                        orderBook.setTransactionStatus(1);
                        transStatus = 1;
                        ruleRecordBuffer.commit();
                    }
                }
                if (transStatus == -1 && ConstantUtil.TIME_1459 <= order.time && ConstantUtil.TIME_1500 > order.time) {
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    // 调用深圳尾盘集合竞价卖出规则。
                    if (tradeDecisionService.evaluateShenzhenClosingAuctionSell(
                            orderBook, order, time, ruleRecord)) {
                        executionGateway.sell(orderBook.getSymbol(), orderBook.getLimitDownPrice(), orderBook.getLimitDownPrice());
                        orderBook.setTransactionStatus(-2);
                        transStatus = -2;
                        ruleRecordBuffer.commit();
                    }
                }
            } else if (order.time < ConstantUtil.TIME_1457) {
                // 连续竞价期间用 3秒的tick 数据修复成交额成交量数据
                long turnover;
                if (order.type == 0) {
                    turnover = order.orderId;
                } else {
                    turnover = order.orderId * 100L;
                }
                if (order.price == 0) {
                    order.price = orderBook.getLimitDownPrice();
                }
                // 计算均价
                int calculateIndex = CompactTimeUtil.calculateIndex(order.time);
                if (order.sellerOrderId != 0) {
                    orderBook.avgPrice[calculateIndex] = (int) (turnover * 100L / order.sellerOrderId);
                }
                // 如果这一分钟的价格是 0 就打印一次信息，这就是每分钟一次
                int l1v = order.sellerOrderId / 1000000;
                int l2v = (int) (orderBook.getVolume() / 1000000);
                if (orderBook.price[calculateIndex] == 0 && calculateIndex >= 3 && (l1v - l2v) > l2v) {
                    log.info("信息对照 -- {}({}):时间:{},L1(价格:{},成交量:{} W手) || L2(价格:{}, 成交量:{} W手)", orderBook.getSecurityName(), orderBook.getSymbol(), time / 100000, order.price, order.sellerOrderId / 1000000, orderBook.getLastPrice(), orderBook.getVolume() / 1000000);
                    orderBook.setTurnover(0);
                    orderBook.setVolume(0);
                    // 更新成交额成交量，价格
                    orderBook.updatePrice(turnover, order.sellerOrderId, order.price, order.time);
                }
                orderBook.price[calculateIndex] = order.price;
                // 执行均价卖出策略
                if (transStatus == -1 && calculateIndex >= 5 && tradeDecisionService.evaluateAveragePriceSell(calculateIndex, orderBook, ruleRecordBuffer.nextRecord())) {
                    executionGateway.quickSell(orderBook.getSymbol(), order.price, orderBook.getLimitDownPrice());
                    orderBook.setTransactionStatus(-2);
                    transStatus = 0;
                    log.info("时间：{},成交额：{},成交量：{},均价：{},价格 {}", order.time, turnover, order.sellerOrderId, orderBook.avgPrice[calculateIndex], order.price);
                    ruleRecordBuffer.commit();
                }
            }
        }

        // 深交所在 09:25 之前，并且处于准备买入或买入待撤单状态时，观察集合竞价。
        if ((order.dataType == 1 || order.dataType == 2)
                && order.time <= ConstantUtil.TIME_925 && transStatus > 0) {
            int limitUpPrice = orderBook.getLimitUpPrice();
            // 涨停总买手数
            long limitUpBuyVolume = orderBook.getBuyQuantity(limitUpPrice);
            long totalSellVolume = orderBook.getTotalSellVolume();

            // 调用当前模式深圳集合竞价买入规则。
            if (transStatus == 1) {
                RuleRecord buyRuleRecord = ruleRecordBuffer.nextRecord();
                if (tradeDecisionService.evaluateShenzhenAuctionBuy(
                        orderBook, order, time, limitUpBuyVolume,
                        totalSellVolume, buyRuleRecord)) {
                    executionGateway.buy(orderBook.getDate(), order.symbolId,
                            orderBook.getLimitUpPrice(), orderBook.getTime());
                    orderBook.setTransactionStatus(2);
                    ruleRecordBuffer.commit();
                    transStatus = 2;
                }
            }

            // 深圳竞价撤单观察窗口由 Handler 控制；具体撤单条件和原业务注释已迁入场景类。
            if (transStatus == 2
                    && order.time >= ConstantUtil.TIME_91952
                    && order.time < ConstantUtil.TIME_920) {
                // 调用当前模式深圳集合竞价撤单规则。
                RuleRecord cancelRuleRecord = ruleRecordBuffer.nextRecord();
                if (tradeDecisionService.evaluateShenzhenAuctionCancel(
                        orderBook, order, time, limitUpBuyVolume,
                        totalSellVolume, cancelRuleRecord)) {
                    executionGateway.cancel(orderBook.getSymbol());
                    orderBook.setTransactionStatus(1);
                    ruleRecordBuffer.commit();
                    transStatus = 1;
                }
            }
        }
        // 深交所盘中策略统一从 09:31 开始，避开开盘初段行情延迟。
        if (transStatus != 0 && order.time >= ConstantUtil.TIME_931 && order.time >= time && order.time < ConstantUtil.TIME_1457) {
            // 调用当前模式的盘中交易模式切换规则。
            if (transStatus == 1
                    && tradeDecisionService.shouldEnableFirstBoardTradingMode(orderBook)) {
                executionGateway.enableFirstLimitUpTradingMode(orderBook.getSymbol());
            }
            // 调用当前模式连续竞价买入时段与买入规则。
            if (transStatus == 1
                    && tradeDecisionService.isContinuousBuyTimeAllowed(orderBook, order.time)
                    && tradeDecisionService.evaluateBuy(orderBook, ruleRecordBuffer.nextRecord())) {
                executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), orderBook.getTime());
                orderBook.setTransactionStatus(2);
                log.info("打板,股票代码 {} 触发单号 OrderId :{}，数据类型:({}) 封单变化：{},封单金额:{},换手:{}%", order.symbolId, order.orderId, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getLimitUpBuyAmount(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
            // 卖出监控中
            if (transStatus == -1 && tradeDecisionService.evaluateSell(orderBook, ruleRecordBuffer.nextRecord())) {
                executionGateway.quickSell(orderBook.getSymbol(), orderBook.getLastPrice(), orderBook.getLimitDownPrice());
                orderBook.setTransactionStatus(-2);
                log.info("卖出,股票代码 {} 触发单号 OrderId :{}，数据类型:({}) 封单变化：{},封单金额:{},换手:{}%", order.symbolId, order.orderId, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getLimitUpBuyAmount(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
        }
        order.time3 = System.nanoTime();
    }

    // 逐笔委托数据
    private boolean addOrder(TickData order, OrderBook orderBook) {
        // 深圳市价委托可能以 0 或涨跌停区间外的价格推送，统一按到达时订单簿最新价入队。
        if (order.price < orderBook.getLimitDownPrice()
                || order.price > orderBook.getLimitUpPrice()) {
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
