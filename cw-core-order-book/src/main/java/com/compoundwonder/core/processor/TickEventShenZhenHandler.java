package com.compoundwonder.core.processor;


import cn.hutool.core.util.StrUtil;

import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.core.engine.TickNodePool;
import com.compoundwonder.core.processor.evaluator.ConditionEvaluatorBuy;
import com.compoundwonder.core.processor.evaluator.ConditionEvaluatorSell;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.core.service.OrderExecutionGateway;
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

    private final TickNodePool tickNodePool = new TickNodePool(100000);

    private final RuleRecordBuffer ruleRecordBuffer = new RuleRecordBuffer(10);

    private final OrderBook[] orderBooks = new OrderBook[ORDER_BOOK_CAPACITY];

    public TickEventShenZhenHandler(OrderExecutionGateway executionGateway) {
        this.executionGateway = executionGateway;
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
            return;
        } else if (order.dataType == 4) {
            //深圳早盘竞价卖出 TODO 急跌。低开，连续三天加速 然后低开卖出
            // 集合竞价期间 买卖撤单操作 ConstantUtil.TIME_1457
            double increase = (order.price - orderBook.getClosePrice()) * 100.0 / orderBook.getClosePrice();
            if (ConstantUtil.TIME_930 > order.time || (ConstantUtil.TIME_1457 <= order.time && ConstantUtil.TIME_1500 > order.time)) {
                orderBook.setLastPrice(order.price);
                RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                if (transStatus == 2 && order.time < ConstantUtil.TIME_920 && order.time > ConstantUtil.TIME_916) {
                    //集合竞价期间价格不等于涨停价直接撤单,然后把状态设置为 1 ，继续等待买入状态
                    if (order.price != orderBook.getLimitUpPrice()) {
                        executionGateway.cancel(orderBook.getSymbol());
                        orderBook.setTransactionStatus(1);
                        String remark = StrUtil.format("早盘竞价 {}，股票代码:{} 竞价 {} 不等于涨停价{}直接撤单", order.time, order.symbolId, order.price, orderBook.getLimitUpPrice());
                        transStatus = 1;
                        log.info(remark);
                        ruleRecord.fill(RuleConstant.TRADING_MODE_CANCEL, 1, orderBook.getSymbol(), time,order.price, increase, remark);
                        ruleRecordBuffer.commit();

                    }
                }
                // 盯盘卖出状态下。尾盘竞价盯盘，最后几秒判断是否是涨停, 深圳要用 level 2 数据判断是不是涨停
                if (transStatus == -1 && ConstantUtil.TIME_1459 <= order.time && ConstantUtil.TIME_1500 > order.time) {
                    // 如果竞价价格比涨停价格低 或者竞价买小于竞价卖
                    if (order.price < orderBook.getLimitUpPrice() || order.buyerOrderId < order.sellerOrderId) {
                        executionGateway.sell(orderBook.getSymbol(), orderBook.getLimitDownPrice(), orderBook.getLimitDownPrice());
                        String remark = StrUtil.format("尾盘{}竞价 ： 如果竞价价格{}比涨停价格低{} 或者竞价买{}小于竞价卖{}, 股票代码 {} 以跌停价格{}卖出", order.time, order.price, orderBook.getLimitUpPrice(), order.buyerOrderId, order.sellerOrderId, orderBook.getSymbol(), orderBook.getLimitDownPrice());
                        log.info(remark);
                        ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 1, orderBook.getSymbol(), time, order.price,increase, remark);
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
                if (transStatus == -1 && calculateIndex >= 5 && ConditionEvaluatorSell.averagePriceSellStrategy(calculateIndex, orderBook, ruleRecordBuffer.nextRecord())) {
                    executionGateway.quickSell(orderBook.getSymbol(), order.price, orderBook.getLimitDownPrice());
                    orderBook.setTransactionStatus(-2);
                    transStatus = 0;
                    log.info("时间：{},成交额：{},成交量：{},均价：{},价格 {}", order.time, turnover, order.sellerOrderId, orderBook.avgPrice[calculateIndex], order.price);
                    ruleRecordBuffer.commit();
                }
            }
        }

        // 深交所 在 9：25 之前并且是准备买入或下单买入准备撤单状态，观察集合竞价
        // 集合竞价下单只适合小市值股票
        if ((order.dataType == 1 || order.dataType == 2) && order.time <= ConstantUtil.TIME_925 && transStatus > 0) {
            RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
            double increase = (order.price - orderBook.getClosePrice()) * 100.0 / orderBook.getClosePrice();
            // 涨停价格
            int limitUpPrice = orderBook.getLimitUpPrice();
            // 涨停总买手数
            long limitUpBuyVolume = orderBook.getBuyQuantity(limitUpPrice);
            int totalSellVolume = orderBook.getTotalSellVolume();
            // 总涨停买 金额 单位 W
            long limitUpBuyAmount = limitUpBuyVolume / 100L * limitUpPrice / 10000L;
            // 流通股
            long circulation = orderBook.getCirculation();
            // 流通值的 5% 或者是最大换手的 20%，谁小用谁 , 20/5=4,15/5=3,12/5=2.4
            int buyVolume = Math.toIntExact(Math.min(circulation / 20, orderBook.getMaxVolume() / 5));
            // 涨停总买手，大于 全部总卖手，全部总卖全部以涨停价格成交，总卖小于 2500w ，如果遇到 委托买单 大于 200w，则跟单 orderBook.getInitialMarketValue() < 120_000 || orderBook.getLbcs() > 1 || transStatus == 2) && transStatus != 0
            if (transStatus == 1 && totalSellVolume * 100.0 / limitUpBuyVolume <= 40) {

                // 买入方向的委托单，委托价格是涨停价格，委托金额大于 200w，
                if (order.dataType == 1 && limitUpBuyAmount > 1500 && order.price == limitUpPrice
                        && (order.quantity > 900000 || order.quantity / 100L * limitUpPrice / 10000L > 600) && limitUpBuyVolume * 100.0 / circulation > 2) {
                    executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), orderBook.getTime());
                    orderBook.setTransactionStatus(2);
                    String remark = StrUtil.format("买入 - 深圳早盘竞价，大单买 股票代码 {} 时间 :{} 触发单号 OrderId :{} ,买单数:{}，买单金额 {} W", order.symbolId, order.time, order.orderId, order.quantity, order.price / 100L * order.quantity / 10000);
                    log.info(remark);
                    ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 6, orderBook.getSymbol(), time,order.price, increase, remark);
                    ruleRecordBuffer.commit();
                    transStatus = 2;
                }
                // 如果上面没有修改交易状态，就去判断 总涨停买如果大于最大换手 20% 则下单买入, 总卖单不能太多，不能占涨停总买的 35%
                if (transStatus == 1 && limitUpBuyVolume > buyVolume) {
                    executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), orderBook.getTime());
                    orderBook.setTransactionStatus(2);
                    String remark = StrUtil.format("买入 - 深圳早盘竞价  股票代码 {} 时间 :{} 触发单号 OrderId :{},总买超过占最大换手 {} % ,占流通股:{} % ,涨停总买:{}", order.symbolId, order.time, order.orderId, limitUpBuyVolume * 100.0 / orderBook.getMaxVolume(), order.buyerOrderId * 100.0 / circulation, limitUpBuyVolume);
                    log.info(remark);
                    ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 7, orderBook.getSymbol(), time, order.price,increase, remark);
                    ruleRecordBuffer.commit();
                    transStatus = 2;
                }
            }
            // 如果是下单状态，在 9:19:58:500 之后判断是否撤单。总涨停买如果小于 4.5% 则撤单
            if (transStatus == 2 && order.time >= ConstantUtil.TIME_91952 && order.time < ConstantUtil.TIME_920) {
                // 总卖大于涨停买，涨停买小于 流通的 4.5% 或者涨停卖大于 3500 w，或者 涨停买减去全部卖小于 2.5%
                //// 封单如果大于流通市值的 40% 可能是三班组，也撤单  TODO ,只要是节点票或者1进2 就不怕三班组
                if (limitUpBuyVolume <= buyVolume || totalSellVolume * 100.0 / limitUpBuyVolume > 40) {
                    executionGateway.cancel(orderBook.getSymbol());
                    orderBook.setTransactionStatus(1);
                    String remark = StrUtil.format("撤单 - 深圳早盘竞价 早盘撤单,股票代码 {} 时间 :{} 总涨停买占最大换手 {} % ,占流通股:{} %   触发单号 OrderId :{}，数据类型:({}) 封单变化：{},涨停总买:{}", order.symbolId, order.time, limitUpBuyVolume * 100.0 / orderBook.getMaxVolume(), limitUpBuyVolume * 100.0 / circulation, order.orderId, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), limitUpBuyVolume);
                    log.info(remark);
                    ruleRecord.fill(RuleConstant.TRADING_MODE_CANCEL, 2, orderBook.getSymbol(), time, order.price,increase, remark);
                    ruleRecordBuffer.commit();
                    transStatus = 1;
                }
            }
        }
        // 在连续竞价期间才能交易,判断是否可以交易
        if (transStatus != 0 && order.time >= ConstantUtil.TIME_931 && order.time >= time && order.time < ConstantUtil.TIME_1457) {
            // 以开盘价为基准 跌幅 >= 5 如果是擒龙捉妖就去打开其他的
            if (transStatus == 1 && order.time < ConstantUtil.TIME_939 && (orderBook.getOpenIncrease() - orderBook.getIncrease() >= 5) && orderBook.getIncrease() <= -1) {
                if (orderBook.getLbcs() > 1) {
                    executionGateway.enableFirstLimitUpTradingMode(orderBook.getSymbol());
                }
            }
            // 可交易状态 涨停价成交才能
            if (transStatus == 1 && ConditionEvaluatorBuy.evaluate(orderBook, ruleRecordBuffer.nextRecord())) {
                executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), orderBook.getTime());
                orderBook.setTransactionStatus(2);
                log.info("打板,股票代码 {} 触发单号 OrderId :{}，数据类型:({}) 封单变化：{},封单金额:{},换手:{}%", order.symbolId, order.orderId, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getLimitUpBuyAmount(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
            // 卖出监控中
            if (transStatus == -1 && ConditionEvaluatorSell.evaluate(orderBook, ruleRecordBuffer.nextRecord())) {
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
        int limitDownPrice = orderBook.getLimitDownPrice();
        int limitUpPrice = orderBook.getLimitUpPrice();
        if (order.price < limitDownPrice || order.price > limitUpPrice) {
            order.price = orderBook.getLastPrice();
        }
        TickNode tickNode = tickNodePool.borrowNode();
        tickNode.copyFrom(order);
        if (orderBook.addOrder(tickNode)) {
            return true;
        }
        tickNodePool.release(tickNode);
        log.warn("深圳重复委托已忽略 symbolId={}, orderId={}, price={}, quantity={}, time={}",
                order.symbolId, order.orderId, order.price, order.quantity, order.time);
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
