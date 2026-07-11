package com.compoundwonder.core.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;

import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.RuleRecordBuffer;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.core.processor.DisruptorManager;
import com.compoundwonder.core.processor.TickEventShangHaiHandler;
import com.compoundwonder.core.processor.TickEventShenZhenHandler;
import com.compoundwonder.constant.MarketEnum;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.dto.TstpOrderDto;
import com.compoundwonder.util.HighPrecisionClock;
import com.compoundwonder.util.SymbolUtil;
import com.lmax.disruptor.dsl.Disruptor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实盘交易服务
 */
@Slf4j
@Component("liveTradeService")
public class LiveTradeService {

    @Autowired
    private TraderApi traderApi;

    @Autowired
    private XmdTcpDataApi xmdTcpDataApi;

    @Autowired
    private Lev2DataApi lev2DataApi;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private DisruptorManager disruptorManager;

    @Autowired
    private TradeCacheService tradeCacheService;

    @Autowired
    private StockTransactionCalendarService stockTransactionCalendarService;

    @Autowired
    private StockWatchingTaskDao stockWatchingTaskDao;

    private final Map<Integer, Disruptor<TickData>> disruptorMap;

    private TickEventShangHaiHandler tickEventShangHaiHandler ;
    private TickEventShenZhenHandler tickEventShenZhenHandler ;


    public LiveTradeService(Map<Integer, Disruptor<TickData>> disruptorMap) {
        this.disruptorMap = disruptorMap;
    }

    /**
     * 实盘交易主要的方法
     *
     * @throws InterruptedException
     */
    @Scheduled(cron = "0 12 09 * * MON-FRI")
    public void openMarketTask() throws InterruptedException {
        Boolean tradingDay = stockTransactionCalendarService.isTodayTradingDay();
        if (!tradingDay) {
            return;
        }
        log.info("实盘交易主 方法");
        // 清空缓存
        cacheService.clearCacheMap();
        tradeCacheService.clearCache();
        cacheService.orderList.clear();
        //手动校准/同步基准点
        HighPrecisionClock.sync();
        // 异步线程池技术，登录交易系统，查询账户信息，查询持仓数据，查询保单数据
        traderApi.traderApiInit();
        Thread.sleep(2000);
        String date = LocalDate.now().toString();
        // 查询上个交易日推荐的股票
        String previousOne = stockTransactionCalendarService.findPreviousOne(date);
        QueryWrapper<StockWatchingTaskEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("task_date", previousOne);
        wrapper.le("transaction_mode", 2);
        List<StockWatchingTaskEntity> stockWatchingTaskEntities = stockWatchingTaskDao.selectList(wrapper);

        // 把所有推荐的股票添加到盯盘任务，状态都设置为 0 ，关闭状态，剩下的状态都等交易系统交互更新
        for (StockWatchingTaskEntity taskEntity : stockWatchingTaskEntities) {
            log.info("打板任务：{}", taskEntity.toString());
            OrderBook orderBook = new OrderBook(taskEntity.getCode(), taskEntity.getCirculationVolume(), taskEntity.getPrice(), taskEntity.getMaxVolume());
            orderBook.setLbcs(taskEntity.getLbc());
            // 启动流通市值 流通股本 * 昨天 收盘价格 /1.1 ^ 连板次数 = 启动时的流通市值
            long floatMarketCap = Math.round(taskEntity.getCirculationVolume() * taskEntity.getPrice() / Math.pow(1.1, taskEntity.getLbc()) * 100.0) / 1000000;
            // 流通市值 单位 万
            orderBook.setInitialMarketValue((int) floatMarketCap);
            // 最大换手数据
            double maxHs = (double) orderBook.getMaxVolume() / orderBook.getCirculation() * 100.0;
            orderBook.setMaxHs(maxHs);
            orderBook.setDate(date);
            log.info("初始化信信息：{}", orderBook);
            int symbolToInt = SymbolUtil.fastSymbolToInt(taskEntity.getCode());
            cacheService.putOrderBook(symbolToInt, orderBook);
        }
        // 初始化持仓数据盯盘，同步盯盘状态
        traderApi.initStockAccountInfo(date);
        log.info("添加股票盯盘结束，盯盘股代码： {}", cacheService.getOrderBookCodes());
        // 获取所有盯盘，如果没有买入，也没有卖出盯盘，直接结束任务
        if (cacheService.getOrderBookCodes().isEmpty()) {
            log.info("获取所有盯盘，如果没有买入，也没有卖出盯盘，直接结束任务");
            return;
        }
        log.info("添加订阅逐笔、tick 数据订阅----------------");
        // 添加行情数据（逐笔 + 3秒tick）订阅
        xmdTcpDataApi.xmdApiInit();
        lev2DataApi.levelApiInit();
        // 查询交易系统的涨跌停价
        for (String orderBookCode : cacheService.getOrderBookCodes()) {
            traderApi.queryOrderBookInfo(orderBookCode);
            Thread.sleep(1000);
        }
    }

    /**
     * 9:55检查擒龙捉妖模式有没有成交，如果这个时间还没有成交就把首板模式去打开
     */
    @Scheduled(cron = "0 39 09 * * MON-FRI")
    public void updateStatus() {
        Boolean tradingDay = stockTransactionCalendarService.isTodayTradingDay();
        if (!tradingDay) {
            return;
        }
        log.info("9:39检查擒龙捉妖模式有没有成交，如果这个时间还没有成交就把首板模式去打开");
        cacheService.printAllStockInfo();
//        traderApi.updateTraderInfo();
        // 判断有没有擒龙捉妖的标的
        if (!cacheService.getQinLongCodes().isEmpty()) {
            for (String qinLongCode : cacheService.getQinLongCodes()) {
                int symbolToInt = SymbolUtil.fastSymbolToInt(qinLongCode);
                OrderBook orderBook = cacheService.getOrderBook(symbolToInt);
                log.info(" {} 状态：{}", qinLongCode, orderBook.getTransactionStatus());
                // 如果有擒龙捉妖的票已经下单了就终止方法
                List<TstpOrderDto> orderDtos = tradeCacheService.findOrderBySecurityID(qinLongCode);
                try {
                    log.info("盘中检查 下单:{},{},{},{}",orderDtos,ArrayUtil.isNotEmpty(orderDtos),orderDtos.size(),orderDtos.isEmpty());
                } catch (Exception e) {
                    log.error("盘中检查:{}",e.getMessage());
                }
                if (orderDtos != null && !orderDtos.isEmpty()) {
                    return;
                }
            }
            //如果所有擒龙捉妖的票都没有买入，就把所有的票都打开
            disruptorManager.publishTransInfoData("", 5);
            log.info("盘中检查，发送消息就把所有的票都打开");
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        cacheService.printAllStockInfo();
    }

    /**
     * 开盘后检查擒龙捉妖 模式股票涨幅
     */
    @Scheduled(cron = "0 27 09 * * MON-FRI")
    public void openUpdateStatus() {
        Boolean tradingDay = stockTransactionCalendarService.isTodayTradingDay();
        if (!tradingDay) {
            return;
        }
        log.info("9:27检查擒龙捉妖模式涨幅，如果都小于两个点就把首板模式打开");
        cacheService.printAllStockInfo();
        // 判断有没有擒龙捉妖的标的
        if (!cacheService.getQinLongCodes().isEmpty()) {
            for (String qinLongCode : cacheService.getQinLongCodes()) {
                int symbolToInt = SymbolUtil.fastSymbolToInt(qinLongCode);
                OrderBook orderBook = cacheService.getOrderBook(symbolToInt);
                log.info(" {} 涨幅：{}", qinLongCode, orderBook.getIncrease());
                if (orderBook.getIncrease() > 2 && orderBook.getLimitUpBuyAmount() < 15_000) {
                    cacheService.printAllStockInfo();
                    return;
                }
                // 隔夜单 竞价结束后 9:27 执行观察任务，如果排队金额大于 2.5亿的隔夜单就撤掉
                List<TstpOrderDto> entrustmentRecords = tradeCacheService.findEntrustmentRecords(orderBook.getSymbol());
                if (ArrayUtil.isNotEmpty(entrustmentRecords) && !entrustmentRecords.isEmpty()) {
                    TstpOrderDto tstpOrderDto = entrustmentRecords.get(entrustmentRecords.size() - 1);
                    if (ObjectUtil.isNotEmpty(tstpOrderDto)) {
                        // 挂单价格
                        int price = (int) (tstpOrderDto.getLimitPrice() * 100 + 0.5);
                        //00:24:48,01:24:48
                        LocalTime parse = LocalTime.parse(tstpOrderDto.getInsertTime());
                        long acceptTimeStamp = 0;
                        if (orderBook.getMarket().equals(MarketEnum.SH)) {
                            // 915000417532 -> 91500040
                            acceptTimeStamp = tstpOrderDto.getAcceptTimeStamp() / 100000L * 10;
                        }else {
                            //20260623091500000 -> 91500040
                            acceptTimeStamp = tstpOrderDto.getAcceptTimeStamp() - Integer.parseInt(tstpOrderDto.getTradingDay()) * 1_000_000_000L;
                        }
                        List<TickNode> tickNodes = orderBook.getPriceIndex().get(price);
                        // 排队金额
                        long queuedOrderAmount = 0;
                        for (TickNode tickNode : tickNodes) {
                            // 数量相等，交易所时间也相对就跳出排队金额计算
                            if (tickNode.getQuantity() == tstpOrderDto.getVolumeTotalOriginal() && acceptTimeStamp == tickNode.getTime()) {
                                continue;
                            }
                            queuedOrderAmount += tickNode.getQuantity() / 100L * tickNode.getPrice();
                        }
                        if (parse.getHour() < 9 && queuedOrderAmount / 10_000 >= 25_000) {
                            traderApi.cancel(orderBook.getSymbol());
                            //就把所有的票都打开
                            log.info("隔夜单 竞价结束后 9:27 执行观察任务，如果排队{}金额大于 2.5亿的隔夜单就撤掉", queuedOrderAmount);
                            break;
                        }
                    }
                }
            }
            //就把所有的票都打开
            disruptorManager.publishTransInfoData("", 5);
            log.info("开盘检查，发送消息就把所有的票都打开");
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        cacheService.printAllStockInfo();
    }

    /**
     * 收盘工作
     */
    @Scheduled(cron = "0 03 15 * * MON-FRI")
    public void closeMarketTask() {
        Boolean tradingDay = stockTransactionCalendarService.isTodayTradingDay();
        if (!tradingDay) {
            return;
        }
        cacheService.printAllStockInfo();
        // 清空盯盘数据缓存
        log.debug("完成盯盘任务，清空盯盘数据缓存");
        cacheService.clearCacheMap();
        tradeCacheService.clearCache();
        for (TickData order : cacheService.orderList) {
            log.info("alllevel2data：{}", order.toString());
        }
        cacheService.orderList.clear();
        log.debug("完成盯盘任务 结束盯盘任务");
    }

    /**
     * 每个交易日开始打印一行日志，把前一天日志在凌晨就打包完成
     */
    @Scheduled(cron = "0 01 00 * * MON-FRI")
    public void printlnLog() {
        Boolean tradingDay = stockTransactionCalendarService.isTodayTradingDay();
        if (!tradingDay) {
            return;
        }
        log.debug("每个交易日开始打印一行日志，把前一天日志在凌晨就打包完成");
    }

    /**
     * 启动就执行 Disruptor
     */
    @PostConstruct
    public void startDisruptor() {
        tickEventShangHaiHandler = new TickEventShangHaiHandler(cacheService, traderApi);
        tickEventShenZhenHandler = new TickEventShenZhenHandler(cacheService, traderApi);
        disruptorMap.get(0).handleEventsWith(tickEventShangHaiHandler);
        disruptorMap.get(1).handleEventsWith(tickEventShenZhenHandler);
        disruptorMap.get(0).start();
        disruptorMap.get(1).start();
        log.info("启动 disruptor");
    }


    public List<RuleRecordDTO> queryRuleExecutionRecords() {
        List<RuleRecordDTO> list = new ArrayList<>();
        RuleRecordBuffer ruleRecords = tickEventShangHaiHandler.getRuleRecords();

        if (ruleRecords.size() > 0){
            RuleRecord[] records = ruleRecords.records();
            for (int i = 0; i < ruleRecords.size(); i++) {
                list.add(records[i].toDTO());
            }
            ruleRecords.clear();
        }

        ruleRecords = tickEventShenZhenHandler.getRuleRecords();
        if (ruleRecords.size() > 0){
            RuleRecord[] records = ruleRecords.records();
            for (int i = 0; i < ruleRecords.size(); i++) {
                list.add(records[i].toDTO());
            }
            ruleRecords.clear();
        }
        return list;
    }
}
