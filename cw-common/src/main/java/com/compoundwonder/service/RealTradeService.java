package com.compoundwonder.service;

/**
 * 实盘交易接口
 */
public interface RealTradeService {

    /**
     * 初始化交易柜台连接、登录回调和私有交易回报订阅，并保持连接到收盘后释放。
     *
     * @throws InterruptedException 初始化或保持连接期间线程被中断时抛出
     */
    void traderApiInit() throws InterruptedException;

    /**
     * 查询柜台返回的当日证券信息，用于开盘前修正订单簿昨收、涨停价和跌停价。
     *
     * @param symbol 六位股票代码
     */
    void queryOrderBookInfo(String symbol);

    /**
     * 异步查询股东账户、资金账户、当日报单和持仓，并由柜台回调刷新交易缓存。
     */
    void updateTraderInfo();

    /**
     * 清理实盘交易实现维护的临时缓存，为重新登录或下一个交易日做准备。
     */
    void clearCache();

    /**
     * 按资金和主板单笔数量限制拆分后提交买单。
     *
     * @param date 交易日期，格式为 {@code yyyy-MM-dd}
     * @param symbol 内部整数证券代码
     * @param price 委托价格，整数价格口径为元乘以 100
     * @param time 规则触发时间，格式为 {@code HHmmssSSS}
     */
    void buy(String date, int symbol, int price, int time);

    /**
     * 提交普通卖出任务，执行端可在未成交后撤单并继续降低委托价格。
     *
     * @param symbol 六位股票代码
     * @param price 首次委托价格，整数价格口径为元乘以 100
     * @param limitDownPrice 当日跌停价，作为持续降价卖出的下界
     */
    void sell(String symbol, int price, int limitDownPrice);

    /**
     * 提交快速卖出任务，使用更积极的撤单和重新委托节奏。
     *
     * @param symbol 六位股票代码
     * @param price 首次委托价格，整数价格口径为元乘以 100
     * @param limitDownPrice 当日跌停价，作为持续降价卖出的下界
     */
    void quickSell(String symbol, int price, int limitDownPrice);

    /**
     * 撤销指定股票当前由系统跟踪的活动委托。
     *
     * @param symbol 六位股票代码
     */
    void cancel(String symbol);

    /**
     * 擒龙捉妖模式极速下跌的时候，及时发送消息开启首板模式
     *
     * @param stackCode 触发保护逻辑的六位股票代码
     */
    void enableFirstLimitUpTradingMode(String stackCode);
}
