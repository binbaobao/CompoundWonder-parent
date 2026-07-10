package com.compoundwonder.service;


import cn.hutool.core.util.StrUtil;

import com.compoundwonder.core.util.SymbolUtil;
import com.compoundwonder.core.util.ThreadSafeIdGenerator;
import com.compoundwonder.core.util.TradeCalculator;
import com.compoundwonder.dto.TradingAccountDto;
import com.compoundwonder.dto.TstpOrderDto;
import com.compoundwonder.dto.TstpPositionDto;
import com.compoundwonder.dto.UserLoginInfoDto;
import com.compoundwonder.spi.TraderSpi;
import com.tora.traderapi.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class TraderApi {

    static {
        String property = System.getProperties().getProperty("os.name");
        if (property.contains("Linux")) {
            System.load("/home/usergcb/os/libjavatraderapi.so");
        }
    }

    private final ReentrantLock globalLock = new ReentrantLock();

    @Autowired
    private CacheService cacheService;

    @Autowired
    private EmotionCycleInfoDao emotionCycleInfoDao;

    @Autowired
    private DisruptorManager disruptorManager;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private StockWatchingTaskDao stockWatchingTaskDao;

    @Autowired
    private TradeCacheService tradeCacheService;

    @Autowired
    private StockDailyService stockDailyService;

    @Autowired
    private StockTransactionCalendarService stockTransactionCalendarService;

    private CTORATstpTraderApi ctoraTstpTraderApi;

    private TraderSpi traderSpi;

    @Async("orderDataExecutor")
    public void traderApiInit() throws InterruptedException {
        log.info("开始初始化 交易接口");

        ctoraTstpTraderApi = CTORATstpTraderApi.CreateTstpTraderApi();
        traderSpi = new TraderSpi(ctoraTstpTraderApi, tradeCacheService, disruptorManager, this);

        ctoraTstpTraderApi.RegisterSpi(traderSpi);

        ctoraTstpTraderApi.RegisterFront("tcp://10.225.29.226:6500");
        ctoraTstpTraderApi.SubscribePrivateTopic(TORA_TE_RESUME_TYPE.TORA_TERT_RESTART);
        ctoraTstpTraderApi.SubscribePublicTopic(TORA_TE_RESUME_TYPE.TORA_TERT_RESTART);

        ctoraTstpTraderApi.Init();

        // 暂停等待 2 秒钟。
        Thread.sleep(2000);

        while (true) {
            LocalTime now = LocalTime.now();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();

            // 检查是否到达目标结束时间
            int targetHour = 15;
            int targetMinute = 2;
            if (currentHour == targetHour && currentMinute >= targetMinute) {
                break;
            }
            // 每分钟检查一次
            Thread.sleep(60000);
        }
        // 销毁
        ctoraTstpTraderApi.Release();
        traderSpi = null;
        log.info("调度器已停止，交易接口已销毁，结束时间: {}", LocalTime.now());
    }

    /**
     * 提前挂隔夜单功能
     */
    @Scheduled(cron = "0 00 01 * * MON-FRI")
    public void preOpenOrderFeature() throws InterruptedException {
        Boolean tradingDay = stockTransactionCalendarService.isTodayTradingDay();
        if (!tradingDay) {
            return;
        }
        log.info("提前挂隔夜单功能，开始初始化 交易接口");

        ctoraTstpTraderApi = CTORATstpTraderApi.CreateTstpTraderApi();
        traderSpi = new TraderSpi(ctoraTstpTraderApi, tradeCacheService, disruptorManager, this);

        ctoraTstpTraderApi.RegisterSpi(traderSpi);

        ctoraTstpTraderApi.RegisterFront("tcp://10.225.29.226:6500");
        ctoraTstpTraderApi.SubscribePrivateTopic(TORA_TE_RESUME_TYPE.TORA_TERT_RESTART);
        ctoraTstpTraderApi.SubscribePublicTopic(TORA_TE_RESUME_TYPE.TORA_TERT_RESTART);

        ctoraTstpTraderApi.Init();

        // 暂停等待 5 秒钟。
        Thread.sleep(5000);

        // 提前挂隔夜单
        preliminaryNocturnalOperation(LocalDate.now().toString());
        Thread.sleep(5000);
        // 销毁
        ctoraTstpTraderApi.Release();
        traderSpi = null;
        log.info("调度器已停止，交易接口已销毁，结束时间: {}", LocalTime.now());
    }

    /**
     * 更新全部的交易信息，账户、持仓、报单
     */
    public void updateTraderInfo() {
        log.info("更新全部的交易信息，账户、持仓、报单");
        // 查询股东账户
        ctoraTstpTraderApi.ReqQryShareholderAccount(new CTORATstpQryShareholderAccountField(), ThreadSafeIdGenerator.generateId());
        // 查询资金账户，获取账户资金
        ctoraTstpTraderApi.ReqQryTradingAccount(new CTORATstpQryTradingAccountField(), ThreadSafeIdGenerator.generateId());
        // 查询报单记录，隔夜单需要撤单
        ctoraTstpTraderApi.ReqQryOrder(new CTORATstpQryOrderField(), ThreadSafeIdGenerator.generateId());
        // 查询持仓记录，后续要进行买卖
        ctoraTstpTraderApi.ReqQryPosition(new CTORATstpQryPositionField(), ThreadSafeIdGenerator.generateId());
    }

    /**
     * 每个交易日夜间提前操作
     * 隔夜单。买卖操作
     */
    public void preliminaryNocturnalOperation(String date) {

        // 判断是否挂了隔夜单，如果遇到紧急情况可能挂隔夜单，已经有隔夜单就不继续执行
        TstpOrderDto nocturnalRecords = tradeCacheService.findNocturnalRecords();
        if (nocturnalRecords != null || LocalTime.now().isAfter(LocalTime.of(9, 15))) {
            log.info("不用挂单直接退出！！！");
            return;
        }

        String previousOneDate = stockTransactionCalendarService.findPreviousOne(date);
        // 判断现在是否持仓中，持仓与空仓两套解决方案
        if (tradeCacheService.getPositionCodes().isEmpty()) {
            StockWatchingTaskEntity stockWatchingTaskEntity = stockWatchingTaskDao.queryPreOpenOrdersStocks(previousOneDate);
            if (stockWatchingTaskEntity != null) {
                // 隔夜单买入
                int price = (int) Math.round(stockWatchingTaskEntity.getPrice() * 110);
                log.info("提前挂单买入 -> 股票： {} , 挂单价格{} ", stockWatchingTaskEntity.getCode(), price);
                this.buy(date, SymbolUtil.fastSymbolToInt(stockWatchingTaskEntity.getCode()), price, LocalTime.now().toSecondOfDay() * 1000);
            }
        } else {
            // 如果是持仓状态就判断要不要挂隔夜卖单
            for (String positionCode : tradeCacheService.getPositionCodes()) {
                List<StockDailyEntity> recentStockDaily = stockDailyService.findRecentStockDaily(positionCode, 1, date);

                StockDailyEntity stockDaily = recentStockDaily.get(0);
                // 如果是非涨停状态，或者是在持仓并且有推荐但是持仓是没有推荐的切超过3板的票，这是中位票，也执行卖出
                // || (!qinLongCodes.isEmpty() && !qinLongCodes.contains(positionCode) && stockDaily.getConsecutiveLimitUpDays() == 3)
                // 或者持有的非节点
                if ((stockDaily.getKlineState() > 5 || stockDaily.getKlineState() < 1)) {
                    // 根据收盘价格计算明天跌停价，隔夜跌停价挂单
                    int price = (int) Math.round(stockDaily.getClosePrice() * 90);
                    // 隔夜单卖出
                    this.sell(positionCode, price, price);
                    log.info("隔夜单卖出 -> 卖出炸板持仓：{} , 挂单价格{} ", positionCode, price);
                }
            }
        }
    }

    /**
     * 初始化股票账户信息
     * 账户信息，持仓信息，隔夜单信息
     */
    public void initStockAccountInfo(String date) {
        // 判断现在是否持仓中，持仓与空仓两套解决方案
        if (tradeCacheService.getPositionCodes().isEmpty()) {
            log.info("初始化股票账户信息，无持仓");
            // 如果是空仓状态，判断是否有已经挂了隔夜买单，竞价期间盯撤单
            TstpOrderDto nocturnalRecords = tradeCacheService.findNocturnalRecords();

            // 如果有一个隔夜单，并且是买入状态的隔夜单，就把盯盘任务修改成 2，买入待撤单状态
            if (nocturnalRecords != null && nocturnalRecords.getOrderStatus() == '0') {
                log.info("初始化股票账户信息，无持仓 有隔夜买单，将这个股票设置为盯撤单情况：{}", nocturnalRecords);
                // 设置已经挂单的状态为盯买入撤单状态
                disruptorManager.publishTransInfoDataOne(nocturnalRecords.getSecurityID(), (byte) 2);
            } else if (nocturnalRecords == null) {
                // 如果是没有隔夜单，没有挂成功的情况下
                log.info("初始化股票账户信息，无持仓 无隔夜单，将所有盯盘打开 ");
                disruptorManager.publishTransInfoData(date, 4);
            }
        } else {
            // 如果有持仓判断是有已经挂隔夜卖单
            // 如果没有挂隔夜卖单，说明是涨停状态、连续竞价持仓待卖出状态 （-1）
            // 初始化盯盘买入的任务都都是关闭的（0），如果挂了卖单等竞价结束资金会更新，盯盘任务也会更新到买入状态
            TstpOrderDto nocturnalRecords = tradeCacheService.findNocturnalRecords();
            if (nocturnalRecords == null) {
                // 查询上个交易日推荐的股票
                String previousOne = stockTransactionCalendarService.findPreviousOne(date);
                LocalDate parse = LocalDate.parse(date);
                // 查询最近 半个月的平均高，制定格局点。
                int averageLimitUpHeight = emotionCycleInfoDao.queryRecentAverageLimitUpHeight(parse.plusDays(-15), parse);
                for (String positionCode : tradeCacheService.getPositionCodes()) {
                    // 近 200交易日最大换手k线数据
                    StockDailyEntity maxVolumeStockDaily = stockDailyDao.fandMaxVolume(positionCode, LocalDate.parse(previousOne));

                    List<StockDailyEntity> recentStockDaily = stockDailyService.findRecentStockDaily(positionCode, 3, previousOne);
                    // 大前天。前天。昨天 日 k 数据
                    StockDailyEntity stockDaily0 = recentStockDaily.get(2);
                    StockDailyEntity stockDaily1 = recentStockDaily.get(1);
                    StockDailyEntity stockDaily = recentStockDaily.get(0);

                    OrderBook orderBook = new OrderBook(positionCode, maxVolumeStockDaily.getFloatShares(), stockDaily.getClosePrice(), maxVolumeStockDaily.getVolume());
                    orderBook.setTransactionStatus(-1);
                    // 上个交易日收盘流通市值
                    Double floatMarketCap = stockDaily.getFloatMarketCap();
                    Integer consecutiveLimitUpDays = stockDaily.getConsecutiveLimitUpDays();
                    // 流通市值
                    double pow = Math.pow(1.1, consecutiveLimitUpDays);
                    orderBook.setInitialMarketValue((int) (Math.round(floatMarketCap / pow * 100.0) / 100));
                    orderBook.setLbcs(consecutiveLimitUpDays);
                    long circulation = orderBook.getCirculation();

                    // 三日换手率
                    double threeDaysTurnover = (stockDaily1.getTurnoverRate() + stockDaily.getTurnoverRate() + stockDaily0.getTurnoverRate()) / 3;
                    orderBook.setThreeDaysTurnover(threeDaysTurnover);
                    orderBook.setAverageLimitUpHeight(averageLimitUpHeight);
                    // 两日换手率
                    double twoDaysTurnover = (stockDaily1.getTurnoverRate() + stockDaily.getTurnoverRate()) / 2;
                    orderBook.setTwoDaysTurnover(twoDaysTurnover);
                    // 昨日换手
                    orderBook.setYesterdayTurnover(stockDaily.getTurnoverRate());
                    // 判断前两天是不是一字板，只有前一天是一字板，才累加后一天
                    Integer klineState = stockDaily.getKlineState();
                    Integer klineState1 = stockDaily1.getKlineState();
                    Integer klineState0 = stockDaily0.getKlineState();
                    int oneWordLimitUp = 0;
                    if (klineState == 3) {
                        oneWordLimitUp = 1;
                        if (klineState1 == 3) {
                            oneWordLimitUp = 2;
                            if (klineState0 == 3) {
                                oneWordLimitUp = 3;
                            }
                        }
                    }
                    orderBook.setOneWordLimitUp(oneWordLimitUp);

                    double maxHs = (double) orderBook.getMaxVolume() / circulation * 100.0;
                    orderBook.setMaxHs(maxHs);
                    // 计算本交易日到下一个交易日的 自然日, 如果碰到长假做一些避险操作
                    String nextTradingDay = stockTransactionCalendarService.findNextTradingDay(date);
                    LocalDate nextTradingDayDate = LocalDate.parse(nextTradingDay);
                    LocalDate nowDate = LocalDate.parse(date);
                    long days = ChronoUnit.DAYS.between(nowDate, nextTradingDayDate) - 1;
                    orderBook.setNextTradingDay((int) days);
                    orderBook.setDate(date);
                    int symbolToInt = SymbolUtil.fastSymbolToInt(positionCode);
                    log.info("盯盘卖出：{}", orderBook);
                    cacheService.putOrderBook(symbolToInt, orderBook);
                }
            }
        }
    }

    /**
     * 擒龙捉妖模式极速下跌的时候，及时发送消息开启首板模式
     */
    public void enableFirstLimitUpTradingMode(String stackCode) {
        disruptorManager.publishTransInfoData(stackCode, (byte) 8);
    }

    /**
     * 查询股票信息，重新设置涨停跌停价格
     *
     * @param symbol
     */
    public void queryOrderBookInfo(String symbol) {
        // 查询合约信息
        CTORATstpQrySecurityField qry_security_field = new CTORATstpQrySecurityField();

        qry_security_field.setExchangeID(this.exchange(symbol));
        qry_security_field.setSecurityID(symbol);

        int ret = ctoraTstpTraderApi.ReqQrySecurity(qry_security_field, ThreadSafeIdGenerator.generateId());
        if (ret != 0) {
            log.info("查询股票信息，:{} 错误码：{}", symbol, ret);
        }
    }

    /**
     * 更新订单簿信息
     *
     * @param pSecurity
     */
    public void updateOrderBookInfo(CTORATstpSecurityField pSecurity) {
        int symbolInt = SymbolUtil.fastSymbolToInt(pSecurity.getSecurityID());
        OrderBook orderBook = cacheService.getOrderBook(symbolInt);
        log.info("更新 {}({}) 订单簿信息: 昨收盘价:{},今涨停价:{}，今跌停价:{},总股本:{}, 流通股本:{}", pSecurity.getSecurityName(), pSecurity.getSecurityID(), pSecurity.getPreClosePrice(), pSecurity.getUpperLimitPrice(), pSecurity.getLowerLimitPrice(), pSecurity.getTotalEquity(), pSecurity.getCirculationEquity());
        orderBook.updateOrderBookInfo(pSecurity.getPreClosePrice(), pSecurity.getUpperLimitPrice(), pSecurity.getLowerLimitPrice(), pSecurity.getSecurityName());
    }


    @Async("orderAsyncExecutor")
    public void buy(String date, int symbol, int price, int time) {
        boolean locked = false;
        try {
            // 带超时锁，防止意外长时间阻塞
            locked = globalLock.tryLock(200, TimeUnit.MILLISECONDS);
            if (locked) {
                TradingAccountDto stockAccountInfo = tradeCacheService.getAccountInfo();
                TradeCalculator.OrderResultContainer orderRequests = TradeCalculator.calculateBuyOrders(symbol, stockAccountInfo.getUsefulMoney(), price);

                for (int index = 0; index < orderRequests.getSize(); index++) {
                    TradeCalculator.OrderRequest order = orderRequests.getOrder(index);
                    // 请求报单
                    CTORATstpInputOrderField input_order_field = new CTORATstpInputOrderField();
                    input_order_field.setExchangeID(this.exchange(order.symbol));
                    input_order_field.setSecurityID(order.symbol);
                    input_order_field.setShareholderID(tradeCacheService.getAccountInfo().getStringStringMap().get(exchange(order.symbol)));
                    // 买卖方向
                    input_order_field.setDirection(traderapi.getTORA_TSTP_D_Buy());
                    input_order_field.setVolumeTotalOriginal(order.quantity);
                    input_order_field.setLimitPrice(order.price);
                    // 报单价格条件///限价
                    input_order_field.setOrderPriceType(traderapi.getTORA_TSTP_OPT_LimitPrice());
                    //有效期类型 当日有效
                    input_order_field.setTimeCondition(traderapi.getTORA_TSTP_TC_GFD());
                    // ///成交量类型 ///任何数量
                    input_order_field.setVolumeCondition(traderapi.getTORA_TSTP_VC_AV());
                    input_order_field.setOrderRef(ThreadSafeIdGenerator.generateId());
                    int ret = ctoraTstpTraderApi.ReqOrderInsert(input_order_field, ThreadSafeIdGenerator.generateId());
                    if (ret == 0) {

                        tradeCacheService.ddOrder(input_order_field.getOrderRef(), order.symbol);
                        // 调用 交易服务
                        log.info(" {} 日 {} 买入：{} , 挂单价格 {} 买入数量：{} ,交易结果", date, (time), order.symbol, price, order.quantity);
                        // 发送交易下单信息
                        disruptorManager.publishTransInfoData(order.symbol, 1);
                    }
                }
            }
        } catch (IllegalArgumentException le) {
            log.error("下单数量出错:{}", le.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) globalLock.unlock();
        }
    }

    @Async("orderAsyncExecutor")
    public void sell(String symbol, int price, int limitDownPrice) {
        boolean locked = false;
        try {
            // 带超时锁，防止意外长时间阻塞
            locked = globalLock.tryLock(200, TimeUnit.MILLISECONDS);
            if (locked) {
                // 获取持仓数量
                TstpPositionDto positionRecords = tradeCacheService.getPositionRecords(symbol);

                if (positionRecords == null || positionRecords.getHistoryPos() == 0) return;

                int symbolToInt = SymbolUtil.fastSymbolToInt(symbol);

                TradeCalculator.OrderResultContainer results = TradeCalculator.calculateSellOrders(symbolToInt, positionRecords.getHistoryPos(), price, limitDownPrice);

                // 注意：一定要根据 getSize() 遍历，不要直接遍历 allOrders
                for (int i = 0; i < results.getSize(); i++) {
                    TradeCalculator.OrderRequest req = results.getOrder(i);
                    char exchangeID = positionRecords.getExchangeID();
                    CTORATstpInputOrderField input_order_field = new CTORATstpInputOrderField();
                    input_order_field.setExchangeID(positionRecords.getExchangeID());//A00032773
                    input_order_field.setSecurityID(symbol);//InvestorID
                    input_order_field.setShareholderID(tradeCacheService.getAccountInfo().getStringStringMap().get(exchangeID));
                    input_order_field.setDirection(traderapi.getTORA_TSTP_D_Sell());
                    input_order_field.setVolumeTotalOriginal(req.quantity);
                    input_order_field.setLimitPrice(req.price);
                    input_order_field.setOrderPriceType(traderapi.getTORA_TSTP_OPT_LimitPrice());
                    input_order_field.setTimeCondition(traderapi.getTORA_TSTP_TC_GFD());//集合竞价有效
                    input_order_field.setVolumeCondition(traderapi.getTORA_TSTP_VC_AV());
                    input_order_field.setOrderRef(ThreadSafeIdGenerator.generateId());
                    int ret = ctoraTstpTraderApi.ReqOrderInsert(input_order_field, ThreadSafeIdGenerator.generateId());

                    if (ret != 0) {
                        log.error("卖出失败 股票：{} 卖出数量：{}", symbol, req.quantity);
                    } else {
                        tradeCacheService.ddOrder(input_order_field.getOrderRef(), symbol);
                        // 调用 交易服务
                        log.info("卖出 股票代码：{} , 挂单价格 {} 卖出数量：{} ", symbol, price, req.quantity);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) globalLock.unlock();
        }
    }

    @Async("orderAsyncExecutor")
    public void quickSell(String symbol, int price, int limitDownPrice) {
        boolean locked = false;
        try {
            // 带超时锁，防止意外长时间阻塞
            locked = globalLock.tryLock(200, TimeUnit.MILLISECONDS);
            if (locked) {
                // 获取持仓数量
                TstpPositionDto positionRecords = tradeCacheService.getPositionRecords(symbol);

                if (positionRecords == null || positionRecords.getHistoryPos() == 0) return;

                int symbolToInt = SymbolUtil.fastSymbolToInt(symbol);

                TradeCalculator.OrderResultContainer results = TradeCalculator.calculateSellOrders(symbolToInt, positionRecords.getHistoryPos(), price, limitDownPrice);

                // 注意：一定要根据 getSize() 遍历，不要直接遍历 allOrders
                for (int i = 0; i < results.getSize(); i++) {
                    TradeCalculator.OrderRequest req = results.getOrder(i);
                    char exchangeID = positionRecords.getExchangeID();
                    CTORATstpInputOrderField input_order_field = new CTORATstpInputOrderField();
                    input_order_field.setExchangeID(positionRecords.getExchangeID());//A00032773
                    input_order_field.setSecurityID(symbol);//InvestorID
                    input_order_field.setShareholderID(tradeCacheService.getAccountInfo().getStringStringMap().get(exchangeID));
                    input_order_field.setDirection(traderapi.getTORA_TSTP_D_Sell());
                    input_order_field.setVolumeTotalOriginal(req.quantity);
                    input_order_field.setLimitPrice(req.price);
                    input_order_field.setOrderPriceType(traderapi.getTORA_TSTP_OPT_FiveLevelPrice());
                    input_order_field.setTimeCondition(traderapi.getTORA_TSTP_TC_IOC());//集合竞价有效
                    input_order_field.setVolumeCondition(traderapi.getTORA_TSTP_VC_AV());
                    input_order_field.setOrderRef(ThreadSafeIdGenerator.generateId());
                    int ret = ctoraTstpTraderApi.ReqOrderInsert(input_order_field, ThreadSafeIdGenerator.generateId());

                    if (ret != 0) {
                        log.error("卖出失败 股票：{} 卖出数量：{}", symbol, req.quantity);
                    } else {
                        tradeCacheService.ddOrder(input_order_field.getOrderRef(), symbol);
                        // 调用 交易服务
                        log.info("卖出 股票代码：{} , 挂单价格 {} 卖出数量：{} ", symbol, req.price, req.quantity);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) globalLock.unlock();
        }
    }

    public void cancel(String symbol) {
        List<TstpOrderDto> orderDtos = tradeCacheService.findEntrustmentRecords(symbol);
        // 获取未成交单数据,设置撤单警戒线 TODO
//        long unsoldOrderCount = tradeCacheService.getUnsoldOrderCount();

        for (TstpOrderDto orderDto : orderDtos) {
            String orderSysID = orderDto.getOrderSysID();
            CTORATstpInputOrderActionField orderActionField = new CTORATstpInputOrderActionField();
            orderActionField.setActionFlag(traderapi.getTORA_TSTP_AF_Delete());
            orderActionField.setExchangeID(orderDto.getExchangeID());

            if (StrUtil.isNotBlank(orderSysID)) {
                orderActionField.setOrderSysID(orderSysID);
            } else {
                UserLoginInfoDto userLoginInfoDto = tradeCacheService.getUserLoginInfoDto();
                int sessionID = userLoginInfoDto.getSessionID();
                int frontID = userLoginInfoDto.getFrontID();
                orderActionField.setFrontID(frontID);
                orderActionField.setSessionID(sessionID);
                orderActionField.setOrderRef(orderDto.getOrderRef());
            }

            int ret = ctoraTstpTraderApi.ReqOrderAction(orderActionField, ThreadSafeIdGenerator.generateId());
            if (ret != 0) {
                log.error("撤单失败 股票：{} 订单号：{} OrderRef：{}", symbol, orderSysID, orderDto.getOrderRef());
            } else {
                log.info("撤单成功 股票：{} 订单号：{} OrderRef：{}", symbol, orderSysID, orderDto.getOrderRef());
            }
        }
    }


    private static long lastOrderTime = 0;
    private static int ordersInSecond = 0;
    // 单日委托上限
    private static int orderNumLimit = 500;
    // 单日委托数量
    private static int orderNum = 0;

    private boolean checkOrderRateLimit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOrderTime > 1000) {
            // 超过1秒，重置计数器
            ordersInSecond = 0;
            lastOrderTime = currentTime;
        }
        if (orderNum >= orderNumLimit) {
            log.info("单日累计最高申报笔数{}", orderNumLimit);
            return false;
        }
        // 交易系统要求每秒不超过20笔，本系统设置 18
        if (ordersInSecond >= 18) {
            log.info("报单频率超限: 每秒超过20笔");
            return false;
        }
        ordersInSecond++;
        orderNum++;
        return true;
    }

    private Character exchange(String code) {
        String str = code.substring(0, 2);
        return str.equals("60") || str.equals("68") ? '1' : '2';
    }
}
