package com.compoundwonder.common.orderbook;

/**
 * 订单簿发出的交易动作出口。
 * 回测和实盘分别提供实现，核心订单簿不感知具体执行环境。
 */
public interface OrderExecutionGateway {

    /**
     * 执行带策略来源标识的订单意图。旧实现无需修改即可继续使用原有动作方法；
     * 需要区分多策略会话的实现可覆盖本方法保存来源标识。
     */
    default void execute(TradeOrderIntent intent) {
        switch (intent.action()) {
            case BUY -> buy(intent.date(), intent.symbolId(), intent.price(), intent.time());
            case SELL -> sell(intent.symbol(), intent.price(), intent.limitDownPrice());
            case QUICK_SELL -> quickSell(
                    intent.symbol(), intent.price(), intent.limitDownPrice());
            case CANCEL -> cancel(intent.symbol());
            case ENABLE_FIRST_LIMIT_UP_MODE -> enableFirstLimitUpTradingMode(intent.symbol());
        }
    }

    /**
     * 提交买入动作；回测实现记录模拟成交，实盘实现发送柜台委托。
     *
     * @param date 交易日期，格式为 {@code yyyy-MM-dd}
     * @param symbol 内部整数证券代码
     * @param price 委托价格，整数价格口径为元乘以 100
     * @param time 触发时间，格式为 {@code HHmmssSSS}
     */
    void buy(String date, int symbol, int price, int time);

    /**
     * 按普通卖出链路提交卖单。
     *
     * @param symbol 六位股票代码
     * @param price 首次委托价格，整数价格口径为元乘以 100
     * @param limitDownPrice 当日跌停价，作为持续降价卖出的下界
     */
    void sell(String symbol, int price, int limitDownPrice);

    /**
     * 按快速卖出链路提交卖单，未成交时由执行端立即撤单并继续卖出。
     *
     * @param symbol 六位股票代码
     * @param price 首次委托价格，整数价格口径为元乘以 100
     * @param limitDownPrice 当日跌停价，作为持续降价卖出的下界
     */
    void quickSell(String symbol, int price, int limitDownPrice);

    /**
     * 撤销指定股票当前由系统维护的活动委托。
     *
     * @param symbol 六位股票代码
     */
    void cancel(String symbol);

    /**
     * 在当前模式触发极速下跌保护时，通知执行端开启首板交易模式。
     *
     * @param symbol 六位股票代码
     */
    void enableFirstLimitUpTradingMode(String symbol);
}
