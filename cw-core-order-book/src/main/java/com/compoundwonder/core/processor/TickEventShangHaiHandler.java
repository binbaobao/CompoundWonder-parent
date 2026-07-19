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
public class TickEventShangHaiHandler implements EventHandler<TickData> {

    private static final int ORDER_BOOK_CAPACITY = 10_000;

    private int time;

    private final OrderExecutionGateway executionGateway;

    /** 按订单簿 tradeMode 以 switch 分发到三套独立交易规则。 */
    private final TradeDecisionService tradeDecisionService;

    private final TickNodePool tickNodePool = new TickNodePool(100000);

    private final RuleRecordBuffer ruleRecordBuffer = new RuleRecordBuffer(10);

    private final OrderBook[] orderBooks = new OrderBook[ORDER_BOOK_CAPACITY];

    public TickEventShangHaiHandler(OrderExecutionGateway executionGateway,
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

        if (order.dataType == 1) {
            byte type = order.type;
            //沪市 交易类型：2-限价，10-撤单
            if (type == 2) {
                if (addOrder(order, orderBook)) {
                    if (orderBook.buyMaxOrder.orderId == order.orderId) {
                        if (order.direction == 1) {
                            int quantity = orderBook.buyMaxOrder.getQuantity();
                            orderBook.buyMaxOrder.setQuantity(quantity + order.quantity);
                        }
                    } else {
                        orderBook.buyMaxOrder.clear();
                        if (order.direction == 1) {
                            orderBook.buyMaxOrder.copyFrom(order);
                        }
                    }
                }
            } else if (type == 10) {
                // 撤单
                tradeOrder(order, order.orderId, orderBook);
            }
            orderBook.updateLimitUpStatus(); //实时更新涨停状态
        } else if (order.dataType == 2) {
            // 成交额，成交量，最高价格，最低价格,最新价格
            orderBook.updatePrice(order.amount, order.quantity, order.price, order.time);
            tradeOrder(order, order.buyerOrderId, orderBook);
            tradeOrder(order, order.sellerOrderId, orderBook);
            orderBook.updateLimitUpStatus(); //实时更新涨停状态
            if (orderBook.buyMaxOrder.orderId == order.buyerOrderId) {
                orderBook.buyMaxOrder.setPrice(order.price);
                orderBook.buyMaxOrder.setQuantity(order.quantity + orderBook.buyMaxOrder.getQuantity());
            } else {
                orderBook.buyMaxOrder.clear();
                orderBook.buyMaxOrder.setQuantity(order.quantity);
                orderBook.buyMaxOrder.setPrice(order.price);
                orderBook.buyMaxOrder.setTime(order.time);
                orderBook.buyMaxOrder.setDirection((byte) 1);
                orderBook.buyMaxOrder.setOrderId(order.buyerOrderId);
            }
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
            // 上交所集合竞价期间用三秒一次的快照数据 || ConstantUtil.TIME_1457 <= order.time
            if (ConstantUtil.TIME_930 > order.time || (ConstantUtil.TIME_1457 <= order.time && ConstantUtil.TIME_1500 > order.time)) {
                if (order.time > ConstantUtil.TIME_920) {
                    orderBook.updateLowestPrice(order.price);
                }
                if (transStatus == 1) {
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    // 调用当前模式上海集合竞价买入规则。
                    if (tradeDecisionService.evaluateShanghaiAuctionBuy(
                            orderBook, order, time, ruleRecord)) {
                        executionGateway.buy(orderBook.getDate(), order.symbolId,
                                orderBook.getLimitUpPrice(), order.time);
                        orderBook.setTransactionStatus(2);
                        ruleRecordBuffer.commit();
                        transStatus = 2;
                    }
                }

                if (transStatus == -1
                        && ConstantUtil.TIME_1459 <= order.time
                        && ConstantUtil.TIME_1500 > order.time) {
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    // 调用上海尾盘集合竞价卖出规则。
                    if (tradeDecisionService.evaluateShanghaiClosingAuctionSell(
                            orderBook, order, time, ruleRecord)) {
                        executionGateway.sell(orderBook.getSymbol(),
                                orderBook.getLimitDownPrice(), orderBook.getLimitDownPrice());
                        orderBook.setTransactionStatus(-2);
                        ruleRecordBuffer.commit();
                        transStatus = -2;
                    }
                }

                // 上海竞价撤单观察窗口由 Handler 控制；具体撤单条件和原业务注释已迁入场景类。
                if (transStatus == 2
                        && order.time < ConstantUtil.TIME_920
                        && order.time > ConstantUtil.TIME_91530) {
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    // 调用当前模式上海集合竞价撤单规则；价格不匹配规则优先于封单减弱规则。
                    if (tradeDecisionService.evaluateShanghaiAuctionCancel(
                            orderBook, order, time, ruleRecord)) {
                        executionGateway.cancel(orderBook.getSymbol());
                        orderBook.setTransactionStatus(1);
                        ruleRecordBuffer.commit();
                        transStatus = 1;
                    }
                }
            } else if (order.time < ConstantUtil.TIME_1457) {
                // 连续竞价期间用3秒的tick 数据 成交额 /成交总量 = 均价
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
                    log.info("执行均价卖出策略 时间：{},成交额：{},成交量：{},均价：{},价格 {},涨幅:{} %", order.time / 1000, turnover, order.sellerOrderId, orderBook.avgPrice[calculateIndex], order.price, orderBook.getIncrease());
                    ruleRecordBuffer.commit();
                }
                // 09:31 前行情只更新订单簿；time 等于 L1 行情时间且买一为涨停价时，09:31 后才允许触发买入。
                if (transStatus == 1 && order.time == time && time >= ConstantUtil.TIME_931 && orderBook.getLimitUpPrice() == order.price) {
                    // 清空成交额成交量，与大单记录
                    orderBook.setTurnover(0);
                    orderBook.setVolume(0);
                    orderBook.buyMaxOrder.clear();
                    // 更新成交额成交量，价格
                    orderBook.updatePrice(turnover, order.sellerOrderId, order.price, order.time);
                    // 利用 l1 买一计算涨停封单金额，
                    long limitUpBuyAmount = order.buyerOrderId / 100L * orderBook.getLimitUpPrice() / 10000L;
                    orderBook.setLimitUpBuyAmount(limitUpBuyAmount);
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    // 进行买入信号判断
                    if (tradeDecisionService.evaluateBuy(orderBook, ruleRecord)) {
                        executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), time);
                        orderBook.setTransactionStatus(2);
                        ruleRecordBuffer.commit();
                        transStatus = 2;
                        log.info("l1 换手率达成买入");
                    }
                }
            }
        }

        // 上交所盘中策略统一从 09:31 开始，避开开盘初段行情延迟。
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
                log.info("打板股票代码 {} 触发单号 OrderId :{}，time:{}, 数据类型:({}) 封单变化：{},换手:{}", order.symbolId, order.orderId, order.time, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
            // 卖出监控中
            if (transStatus == -1 && tradeDecisionService.evaluateSell(orderBook, ruleRecordBuffer.nextRecord())) {
                executionGateway.quickSell(orderBook.getSymbol(), orderBook.getLastPrice(), orderBook.getLimitDownPrice());
                orderBook.setTransactionStatus(-2);
                log.info("卖出股票代码 {} 触发单号 OrderId :{}，time :{} ,数据类型:({}) 封单变化：{},换手:{}", order.symbolId, order.orderId, order.time, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
            // 买入未成交的状态下，随时准备撤单
//            if (transStatus == 2 && tradeDecisionService.evaluateCancel(orderBook)) {
//                transactionExecutorService.cancel(orderBook.getDate(), orderBook.getSymbol(), orderBook.getTime());
//                orderBook.setTransactionStatus(3);
//            }
        }
        order.time3 = System.nanoTime();
    }


    // 逐笔委托数据
    private boolean addOrder(TickData order, OrderBook orderBook) {
        TickNode tickNode = tickNodePool.borrowNode();
        tickNode.copyFrom(order);
        OrderBook.AddOrderResult result = orderBook.addOrder(tickNode);
        if (result == OrderBook.AddOrderResult.ADDED) {
            return true;
        }
        tickNodePool.release(tickNode);
        log.warn("上海委托已忽略 reason={}, symbolId={}, orderId={}, direction={}, price={}, quantity={}, time={}",
                result, order.symbolId, order.orderId, order.direction, order.price, order.quantity, order.time);
        return false;
    }


    private void tradeOrder(TickData order, int orderId, OrderBook orderBook) {
        TickNode completed = orderBook.applyTrade(orderId, order.quantity);
        if (completed != null) {
            tickNodePool.release(completed);
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
