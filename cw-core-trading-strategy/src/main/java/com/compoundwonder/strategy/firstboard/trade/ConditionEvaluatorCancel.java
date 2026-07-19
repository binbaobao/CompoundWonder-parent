package com.compoundwonder.strategy.firstboard.trade;


import com.compoundwonder.strategy.TradeMarketState;

/**
 * 买入挂单撤单条件评估器。
 *
 * <p>当前 Handler 调用仍处于注释状态，本类字段口径保留用于恢复撤单规则时核对。</p>
 */
public class ConditionEvaluatorCancel {


    /**
     * 主评估方法
     *
     * @param orderBook 当前股票盘口快照
     * @return 命中撤单条件时返回 {@code true}
     */
    public static boolean evaluate(TradeMarketState orderBook) {

        // 本轮连板启动时的流通市值，单位：万元。
        long mv = orderBook.getInitialMarketValue();
        // 当日截至当前时刻的累计换手率，单位：%。
        double turnover = orderBook.getTurnoverRate();
        // 当日截至当前时刻的累计成交量，单位：股。
        long volume = orderBook.getVolume();
        // 初始化订单簿时传入的历史最大成交量，单位：股。
        long maxVolume = orderBook.getMaxVolume();
        // 最新成交价，单位：分。
        int lastPrice = orderBook.getLastPrice();
        // 当日涨停价，单位：分。
        int limitUpPrice = orderBook.getLimitUpPrice();
        // 当前涨停买单队列总金额，单位：万元；不是委托股数。
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
        // 涨停/炸板累计状态：奇数表示封板中，偶数表示未封板。
        int status = orderBook.getStatus();
        // 涨停买单数量 EMA 的环比变化率，单位：%；负数表示封单减弱。
        double changePercent = orderBook.getChangePercent();
        // 昨日与前日换手率的算术平均值，单位：%。
        double twoDaysTurnover = orderBook.getTwoDaysTurnover();
        // 最近一次计算得到的涨停买单数量 EMA，单位：股。
        double lastEmaVolume = orderBook.getLastEmaVolume();
        // 当前行情时间，紧凑格式 HHmmssSSS。
        int time = orderBook.getTime();

        // --- 区间参数定义 ---
        // 预留的组合封单金额，单位应为万元；当前规则尚未使用。
        long limitUpAmount;
        // 预留的组合变化率，单位应为%；当前规则尚未使用。
        double change;
        // && twoDaysTurnover < 25 && changePercent <-2

        // --- 市值区间判断 ---
        // 小市值缩量一字板，三班组，产业链
        if (changePercent < -5 && limitUpBuyAmount < 1500) {
            return true;
        }
        return false;
    }

}

