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
import java.util.List;


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

    private final TickNodePool tickNodePool = new TickNodePool(100000);

    private final RuleRecordBuffer ruleRecordBuffer = new RuleRecordBuffer(10);

    private final OrderBook[] orderBooks = new OrderBook[ORDER_BOOK_CAPACITY];

    public TickEventShangHaiHandler(OrderExecutionGateway executionGateway) {
        this.executionGateway = executionGateway;
    }

    public void registerOrderBook(int symbolId, OrderBook orderBook) {
        orderBooks[symbolId % ORDER_BOOK_CAPACITY] = orderBook;
    }

    public void reset() {
        for (OrderBook orderBook : orderBooks) {
            if (orderBook != null) {
                orderBook.getIdIndex().values().forEach(tickNodePool::release);
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
                addOrder(order, orderBook);
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
            return;
        } else if (order.dataType == 4) {
            // 集合竞价下单只适合小市值股票
            // 上交所集合竞价期间用三秒一次的快照数据 || ConstantUtil.TIME_1457 <= order.time
            if (ConstantUtil.TIME_930 >= order.time || (ConstantUtil.TIME_1457 <= order.time && ConstantUtil.TIME_1500 > order.time)) {
                // 涨停价格 && transStatus >= 1 && transStatus <= 2
                int limitUpPrice = orderBook.getLimitUpPrice();
                // 总涨停买 金额 单位 W
                long limitUpBuyAmount = order.buyerOrderId / 100L * limitUpPrice / 10000L;

                long circulation = orderBook.getCirculation();
                TickData snapshot = orderBook.getSnapshot();
                // 将这一次的快照放入
                orderBook.setSnapshot(order);
                if (snapshot == null) {
                    snapshot = new TickData();
                    snapshot.quantity = 0;
                }
                double increase = (order.price - orderBook.getClosePrice()) * 100.0 / orderBook.getClosePrice();
                // 流通值的 5% 或者是最大换手的 20%，谁小用谁 , 20/5=4,15/5=3,12/5=2.4
                int buyVolume = Math.toIntExact(Math.min(circulation / 20, orderBook.getMaxVolume() / 5));
                if (transStatus == 1 && order.price == limitUpPrice && order.buyerOrderId > buyVolume / 3) {
                    if (ConstantUtil.TIME_925 >= order.time && (order.sellerOrderId * 100.0 / order.buyerOrderId <= 40 || limitUpBuyAmount > 15_000)) {
                        RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                        // 买入方向的委托单，委托价格是涨停价格，这次快照比上次快照如果涨停买单多大于总卖，并且总买大于流通的 2 %
                        // 如果上面没有修改交易状态，就去判断 总涨停买如果大于 4.5% 则下单买入
                        if (transStatus == 1 && order.buyerOrderId > buyVolume) {
                            executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), order.time);
                            orderBook.setTransactionStatus(2);
                            String remark = StrUtil.format("买入 - 上午早盘竞价 {}，涨停总买占最大成交 {} % 股票代码 {},涨停总买量 {} 手,占流通股:{} %，涨停总买:{} W,", order.time, order.buyerOrderId * 100.0 / orderBook.getMaxVolume(), order.symbolId, order.buyerOrderId, order.buyerOrderId * 100.0 / circulation, limitUpBuyAmount);
                            log.info(remark);
                            ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 2, orderBook.getSymbol(), time, limitUpPrice, increase, remark);
                            ruleRecordBuffer.commit();
                            transStatus = 2;
                        }
                    }
                }
                // 盯盘卖出状态下。尾盘竞价盯盘，最后几秒判断是否是涨停 上海 后期要设置一个 145958 的定时任务去检查订单簿的状态
                if (transStatus == -1 && ConstantUtil.TIME_1459 <= order.time && ConstantUtil.TIME_1500 > order.time) {
                    // 如果竞价价格比涨停价格低 或者竞价买小于竞价卖
                    if (order.price < orderBook.getLimitUpPrice() || order.buyerOrderId < order.sellerOrderId) {
                        executionGateway.sell(orderBook.getSymbol(), orderBook.getLimitDownPrice(), orderBook.getLimitDownPrice());
                        String remark = StrUtil.format("卖出 - 尾盘 {} 竞价 ： 如果竞价价格 {} 比涨停价格低 {} 或者竞价买 {} 小于竞价卖 {}, 股票代码 {} 以跌停价格 {} 卖出", order.time, order.price, orderBook.getLimitUpPrice(), order.buyerOrderId, order.sellerOrderId, orderBook.getSymbol(), orderBook.getLimitDownPrice());
                        log.info(remark);
                        orderBook.setTransactionStatus(1);
                        transStatus = 0;
                        RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                        ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 1, orderBook.getSymbol(), time, order.price, increase, remark);
                        ruleRecordBuffer.commit();
                    }
                }

                // 如果是下单状态，在 9:19:56:500 之后判断是否撤单。总涨停买如果小于 4.5% 则撤单
                if (transStatus == 2 && order.time < ConstantUtil.TIME_920 && order.time > ConstantUtil.TIME_916) {
                    RuleRecord ruleRecord = ruleRecordBuffer.nextRecord();
                    //集合竞价期间价格不等于涨停价直接撤单,然后把状态设置为 1 ，继续等待买入状态
                    //集合竞价期间价格不等于涨停价直接撤单,然后把状态设置为 1 ，继续等待买入状态
                    if (order.price != orderBook.getLimitUpPrice()) {
                        executionGateway.cancel(orderBook.getSymbol());
                        orderBook.setTransactionStatus(1);
                        String remark = StrUtil.format("撤单 - 早盘竞价 {}，股票代码:{} 竞价 {} 不等于涨停价 {} 直接撤单", order.time, order.symbolId, order.price, orderBook.getLimitUpPrice());
                        log.info(remark);
                        transStatus = 1;
                        ruleRecord.fill(RuleConstant.TRADING_MODE_CANCEL, 1, orderBook.getSymbol(), time, order.price, increase, remark);
                        ruleRecordBuffer.commit();
                    }
                    if (transStatus == 2 && (order.buyerOrderId <= buyVolume || order.sellerOrderId * 100.0 / order.buyerOrderId > 40)) {
                        executionGateway.cancel(orderBook.getSymbol());
                        orderBook.setTransactionStatus(1);
                        String remark = StrUtil.format("撤单 - 早盘竞价 {}，股票代码:{} 竞价 {} 买单占最大换手 {} % ,占流通股:{} %", order.time, order.symbolId, order.price, order.buyerOrderId * 100.0 / orderBook.getMaxVolume(), order.buyerOrderId * 100.0 / circulation);
                        log.info(remark);
                        transStatus = 1;
                        ruleRecord.fill(RuleConstant.TRADING_MODE_CANCEL, 2, orderBook.getSymbol(), time, order.price, increase, remark);
                        ruleRecordBuffer.commit();
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
                if (transStatus == -1 && calculateIndex >= 5 && ConditionEvaluatorSell.averagePriceSellStrategy(calculateIndex, orderBook, ruleRecordBuffer.nextRecord())) {
                    executionGateway.quickSell(orderBook.getSymbol(), order.price, orderBook.getLimitDownPrice());
                    orderBook.setTransactionStatus(-2);
                    transStatus = 0;
                    log.info("执行均价卖出策略 时间：{},成交额：{},成交量：{},均价：{},价格 {},涨幅:{} %", order.time / 1000, turnover, order.sellerOrderId, orderBook.avgPrice[calculateIndex], order.price, orderBook.getIncrease());
                    ruleRecordBuffer.commit();
                }
                //如果是 time 等于 l1 行情的时间，说明现在是l1更快，并且买一是涨停价说明已经涨停
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
                    if (ConditionEvaluatorBuy.evaluate(orderBook, ruleRecord)) {
                        executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), time);
                        orderBook.setTransactionStatus(2);
                        ruleRecordBuffer.commit();
                        transStatus = 2;
                        log.info("l1 换手率达成买入");
                    }
                }
            }
        }

        // 上交所，只有 在连续竞价期间才能交易,判断是否可以交易 CompactTimeUtil.millisToCompact(order.time)
        if (transStatus != 0 && order.time >= ConstantUtil.TIME_931 && order.time >= time && order.time < ConstantUtil.TIME_1457) {
            //一个票换手超过50% 或者 以开盘价为基准 跌幅 >= 5 如果是擒龙捉妖就去打开其他的
            if (transStatus == 1 && order.time < ConstantUtil.TIME_939 && (orderBook.getOpenIncrease() - orderBook.getIncrease() >= 5) && orderBook.getIncrease() <= -1) {
                if (orderBook.getLbcs() > 1) {
                    executionGateway.enableFirstLimitUpTradingMode(orderBook.getSymbol());
                }
            }
            // 可交易状态 涨停价成交才能
            if (transStatus == 1 && ConditionEvaluatorBuy.evaluate(orderBook, ruleRecordBuffer.nextRecord())) {
                executionGateway.buy(orderBook.getDate(), order.symbolId, orderBook.getLimitUpPrice(), orderBook.getTime());
                orderBook.setTransactionStatus(2);
                log.info("打板股票代码 {} 触发单号 OrderId :{}，time:{}, 数据类型:({}) 封单变化：{},换手:{}", order.symbolId, order.orderId, order.time, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
            // 卖出监控中
            if (transStatus == -1 && ConditionEvaluatorSell.evaluate(orderBook, ruleRecordBuffer.nextRecord())) {
                executionGateway.quickSell(orderBook.getSymbol(), orderBook.getLastPrice(), orderBook.getLimitDownPrice());
                orderBook.setTransactionStatus(-2);
                log.info("卖出股票代码 {} 触发单号 OrderId :{}，time :{} ,数据类型:({}) 封单变化：{},换手:{}", order.symbolId, order.orderId, order.time, order.dataType == 1 ? "委托" : "成交", orderBook.getChangePercent(), orderBook.getTurnoverRate());
                ruleRecordBuffer.commit();
            }
            // 买入未成交的状态下，随时准备撤单
//            if (transStatus == 2 && ConditionEvaluatorCancel.evaluate(orderBook)) {
//                transactionExecutorService.cancel(orderBook.getDate(), orderBook.getSymbol(), orderBook.getTime());
//                orderBook.setTransactionStatus(3);
//            }
        }
        order.time3 = System.nanoTime();
    }


    // 逐笔委托数据
    private void addOrder(TickData order, OrderBook orderBook) {
        int limitDownPrice = orderBook.getLimitDownPrice();
        int limitUpPrice = orderBook.getLimitUpPrice();
        if (order.price < limitDownPrice || order.price > limitUpPrice) {
            order.price = orderBook.getLastPrice();
            log.info("委托价格异常:{}", order);
        }
        TickNode tickNode = tickNodePool.borrowNode();
        tickNode.copyFrom(order);
        int priceIndex = order.price - orderBook.getLimitDownPrice();
        orderBook.getIdIndex().put(order.orderId, tickNode);
        orderBook.getPriceIndex().get(order.price).add(tickNode);
        if (order.direction == 1) {
            orderBook.getPriceBuySum()[priceIndex] += order.quantity;
            orderBook.totalBuyVolume += order.quantity;
        } else {
            orderBook.getPriceSellerSum()[priceIndex] += order.quantity;
            orderBook.totalSellVolume += order.quantity;
        }
    }


    private void tradeOrder(TickData order, int orderId, OrderBook orderBook) {
        TickNode person = orderBook.getIdIndex().get(orderId);
        if (person == null) {
            return;
        }
        removeOrderBook(person, order.quantity, orderBook);
        // 全部成交
        if (person.getQuantity() <= order.quantity) {
            TickNode person1 = orderBook.getIdIndex().remove(orderId);
            if (person1 != null) {
                List<TickNode> ageGroup = orderBook.getPriceIndex().get(person1.getPrice());
                if (ageGroup != null) {
                    ageGroup.remove(person1);
                }
                tickNodePool.release(person1);
            }
        } else {
            List<TickNode> ageGroup = orderBook.getPriceIndex().get(person.getPrice());
            if (ageGroup != null) {
                person.setQuantity(person.getQuantity() - order.quantity);
            }
        }
    }

    /**
     * 删除订单簿统计数据
     *
     * @param orderData
     */
    private void removeOrderBook(TickNode orderData, int quantity, OrderBook orderBook) {
        int priceIndex = orderData.getPrice() - orderBook.getLimitDownPrice();
        if (orderData.getDirection() == 1) {
            orderBook.getPriceBuySum()[priceIndex] -= quantity;
            orderBook.totalBuyVolume -= quantity;
        } else {
            orderBook.getPriceSellerSum()[priceIndex] -= quantity;
            orderBook.totalSellVolume -= quantity;
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
