package com.compoundwonder.core.processor.evaluator;


import com.compoundwonder.core.engine.OrderBook;

/**
 * 卖出条件控制
 */
public class ConditionEvaluatorCancel {


    /**
     * 主评估方法
     *
     * @param orderBook 当前股票盘口快照
     * @return bitmask 标志位
     */
    public static boolean evaluate(OrderBook orderBook) {

        // 提前取出字段，避免重复访问 getter
        long mv = orderBook.getInitialMarketValue();      // 市值 (万元)
        double turnover = orderBook.getTurnoverRate();    // 换手率 (%)
        long volume = orderBook.getVolume();              // 当前成交量
        long maxVolume = orderBook.getMaxVolume();        // 200日最大成交量
        int lastPrice = orderBook.getLastPrice();         // 最新价
        int limitUpPrice = orderBook.getLimitUpPrice();   // 涨停价
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount(); // 涨停封单量
        int status = orderBook.getStatus();               // 状态标志（奇数代表涨停中）
        double changePercent = orderBook.getChangePercent();// 封单趋势变化
        double twoDaysTurnover = orderBook.getTwoDaysTurnover();// 两日换手
        double lastEmaVolume = orderBook.getLastEmaVolume(); // 封单 趋势平均
        int time = orderBook.getTime();

        // --- 区间参数定义 ---
        // 封单金额
        long limitUpAmount;
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
