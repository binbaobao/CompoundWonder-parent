package com.compoundwonder.common.orderbook;

/**
 * 策略执行器输出的带来源标识订单意图。
 *
 * <p>策略只说明要执行的动作；回测和实盘网关分别决定如何记录、撮合或发往柜台。
 * 买入使用全部价格与时间字段；卖出使用 {@code price/limitDownPrice}；撤单和模式切换
 * 未使用的数值字段固定为 0、日期固定为 {@code null}，网关必须按动作解释字段。</p>
 */
public record TradeOrderIntent(
        Action action,
        String strategySessionId,
        String strategyId,
        String date,
        int symbolId,
        String symbol,
        int price,
        int limitDownPrice,
        int time) {

    public TradeOrderIntent {
        if (action == null) throw new IllegalArgumentException("订单动作不能为空");
        if (strategySessionId == null || strategySessionId.isBlank()) {
            throw new IllegalArgumentException("strategySessionId 不能为空");
        }
        if (strategyId == null || strategyId.isBlank()) {
            throw new IllegalArgumentException("strategyId 不能为空");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol 不能为空");
        }
    }

    public static TradeOrderIntent buy(String sessionId, String strategyId, String date,
                                       int symbolId, String symbol, int price, int time) {
        return new TradeOrderIntent(Action.BUY, sessionId, strategyId, date,
                symbolId, symbol, price, 0, time);
    }

    public static TradeOrderIntent sell(String sessionId, String strategyId, String symbol,
                                        int price, int limitDownPrice) {
        return new TradeOrderIntent(Action.SELL, sessionId, strategyId, null,
                0, symbol, price, limitDownPrice, 0);
    }

    public static TradeOrderIntent quickSell(String sessionId, String strategyId, String symbol,
                                             int price, int limitDownPrice) {
        return new TradeOrderIntent(Action.QUICK_SELL, sessionId, strategyId, null,
                0, symbol, price, limitDownPrice, 0);
    }

    public static TradeOrderIntent cancel(String sessionId, String strategyId, String symbol) {
        return new TradeOrderIntent(Action.CANCEL, sessionId, strategyId, null,
                0, symbol, 0, 0, 0);
    }

    public static TradeOrderIntent enableFirstBoardMode(
            String sessionId, String strategyId, String symbol) {
        return new TradeOrderIntent(Action.ENABLE_FIRST_LIMIT_UP_MODE,
                sessionId, strategyId, null, 0, symbol, 0, 0, 0);
    }

    /** 核心执行器允许输出的有限动作集合；不包含成交或柜台受理状态。 */
    public enum Action {
        BUY,
        SELL,
        QUICK_SELL,
        CANCEL,
        ENABLE_FIRST_LIMIT_UP_MODE
    }
}
