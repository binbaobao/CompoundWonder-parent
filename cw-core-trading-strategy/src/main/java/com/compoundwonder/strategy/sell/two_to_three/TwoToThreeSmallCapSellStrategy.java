package com.compoundwonder.strategy.sell.two_to_three;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 2 板、今日 2 进 3、启动流通市值严格小于 119999 万元的独立卖出策略。
 *
 * <p>本文件同时拥有盘口卖出与分钟均价卖出规则。当前内容由拆分前规则增量同步而来，
 * 后续调整该场景时只修改本文件；周末、节假日等全局规则由公共末级策略统一处理。</p>
 */
@Slf4j
public final class TwoToThreeSmallCapSellStrategy implements BoardSellStrategy {

    /**
     * 调用本场景独立的逐笔与盘口卖出规则。
     */
    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return OrderBookRules.evaluate(market, record);
    }

    /**
     * 调用本场景独立的分钟价格与均价走势卖出规则。
     */
    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return AveragePriceRules.evaluate(index, market, record);
    }

    /**
     * 本场景独有的逐笔与盘口卖出规则，按书写顺序首个命中即结束。
     */
    private static final class OrderBookRules {
        /**
         * 按既定优先级评估涨停盘口卖出规则；首个命中规则会填充记录并立即返回。
         *
         * @param orderBook  当前 Handler 私有订单簿的只读交易视图
         * @param ruleRecord 调用方预分配的规则记录
         * @return 命中任意涨停盘口卖出规则时返回 {@code true}
         */
        private static boolean evaluate(TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
            // 本轮连板启动时的流通市值，单位：万元。
            long marketValue = orderBook.getInitialMarketValue();
            // 已经定格的分钟累计均价中的最低值，整数价格口径为元乘以 100。
            int minAveragePrice = orderBook.getMinAveragePrice();
            // 最低分钟累计均价相对昨收的涨跌幅，单位：%。
            double minAveragePriceIncrease = orderBook.getMinAveragePriceIncrease();
            // 当日截至当前时刻的累计换手率，单位：%。
            double turnover = orderBook.getTurnoverRate();
            // 最新成交价，单位：分。
            int lastPrice = orderBook.getLastPrice();
            // 当日涨停价，单位：分。
            int limitUpPrice = orderBook.getLimitUpPrice();
            // 当前涨停买单队列总金额，单位：万元；非涨停状态通常为 0。
            long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
            // 涨停/炸板累计状态：奇数表示封板中，偶数表示未封板；数值越大表示炸板回封次数越多。
            int status = orderBook.getStatus();
            // 涨停买单数量 EMA 的环比变化率，单位：%；负数表示封单减弱。
            double changePercent = orderBook.getChangePercent();
            // -1 持续减弱、0 方向未确认、1 持续增强
            int emaSealTrend = orderBook.getEmaSealTrend();
            // 昨日与前日换手率的算术平均值，单位：%。
            double twoDaysTurnover = orderBook.getTwoDaysTurnover();
            // 本轮连续涨停中一字板的数量。
            int oneWordLimitUp = orderBook.getOneWordLimitUp();
            // 上一次 EMA 评估时记录的涨停封单金额，单位：万元。
            long lastSealAmount = orderBook.getLastSealAmount();
            // 当前行情时间，紧凑格式 HHmmssSSS。
            int time = orderBook.getTime();
            // 昨日已经完成的连续涨停天数，今日板数展示时通常使用 lbcs + 1。
            int lbcs = orderBook.getLbcs();
            // 当日最高价与最低价相对昨收价形成的振幅，单位：%。
            double amplitude = orderBook.getAmplitude();
            // 今日开盘价相对昨收价的涨跌幅，单位：%。
            double openIncrease = orderBook.getOpenIncrease();
            // 涨停价到最近一次炸板最低价的价差占昨收价的比例，单位：%。
            double limitUpBreakDepth = orderBook.getLimitUpBreakDepth();
            // 上一交易日换手率，单位：%。
            double yesterdayTurnover = orderBook.getYesterdayTurnover();
            // 最新价相对昨收价的涨跌幅，单位：%。
            double increase = orderBook.getIncrease();

            if (changePercent < -1.5 && turnover < 15 && limitUpBuyAmount > 10_000 && lastPrice == limitUpPrice && limitUpBuyAmount < lastSealAmount / 1.5) {
                String remark = StrUtil.format("小市值缩量一字板封单快速减弱；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
                return record(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_SMALL_CAP_SEAL_WEAKENING, lastPrice, increase, remark);
            }

            if (changePercent < -3 && time > ConstantUtil.TIME_1130 && time < ConstantUtil.TIME_14563 && turnover < 25 && twoDaysTurnover < 25) {
                String remark = StrUtil.format("前两天缩量板下午炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
                return record(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD, lastPrice, increase, remark);
            }

            // 根据启动市值分档得到的基准最大换手率，单位：%。
            double maxTurnover = maxTurnover(time);
            if (turnover > maxTurnover && isLimitUp(status) && time < ConstantUtil.TIME_14563) {
                if (emaSealTrend == -1) { // 万控智造
                    String remark = StrUtil.format(" 今日二进三放量炸板 条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}", lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, status);
                    return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK, lastPrice, increase, remark);
                }

            }
            return false;
        }

        /**
         * 订单簿状态为奇数时表示当前处于涨停封板状态。
         */
        private static boolean isLimitUp(int status) {
            return status % 2 == 1;
        }

        /**
         *
         */
        private static double maxTurnover(int time) {
            if (time < ConstantUtil.TIME_1330) {
                return 30.0;
            }
            if (time > ConstantUtil.TIME_1330 && time < ConstantUtil.TIME_1430) {
                return 40.0;
            }
            return 45;
        }

        private static boolean record(TradeMarketState orderBook, TradeRuleRecord ruleRecord, int ruleCode,
                                      int price, double increase, String remark) {
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, orderBook.getSymbol(),
                    orderBook.getTime(), price, increase, remark);
            return true;
        }

        private static boolean recordAndLog(TradeMarketState orderBook, TradeRuleRecord ruleRecord, int ruleCode,
                                            int price, double increase, String remark) {
            record(orderBook, ruleRecord, ruleCode, price, increase, remark);
            log.info(remark);
            return true;
        }
    }

    /**
     * 本场景独有的分钟价格与均价走势卖出规则，按书写顺序首个命中即结束。
     */
    private static final class AveragePriceRules {
        /**
         * 按既定优先级评估分钟走势卖出规则；首个命中规则会填充记录并立即返回。
         *
         * @param calculateIndex 当前分钟采样下标
         * @param orderBook      当前 Handler 私有订单簿的只读交易视图
         * @param ruleRecord     调用方预分配的规则记录
         * @return 命中任意分钟走势卖出规则时返回 {@code true}
         */
        private static boolean evaluate(int calculateIndex, TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
            // 本轮连板启动时的流通市值，单位：万元。
            long marketValue = orderBook.getInitialMarketValue();
            // 已经定格的分钟累计均价中的最低值，整数价格口径为元乘以 100。
            int minAveragePrice = orderBook.getMinAveragePrice();
            // 最低分钟累计均价相对昨收的涨跌幅，单位：%。
            double minAveragePriceIncrease = orderBook.getMinAveragePriceIncrease();
            // 当日截至当前时刻的累计换手率，单位：%。
            double turnoverRate = orderBook.getTurnoverRate();
            // 当日最高价与最低价相对昨收价形成的振幅，单位：%。
            double amplitude = orderBook.getAmplitude();
            // 最新价相对昨收价的涨跌幅，单位：%。
            double increase = orderBook.getIncrease();
            // 当日最高成交价，单位：分。
            int highestPrice = orderBook.getHighestPrice();
            // 昨日收盘价，单位：分；本类涨跌幅和回撤统一以它为基准。
            int closePrice = orderBook.getClosePrice();
            // 今日开盘价相对昨收价的涨跌幅，单位：%。
            double openIncrease = orderBook.getOpenIncrease();
            // 当前行情时间，紧凑格式 HHmmssSSS，例如 09:31:00.000 为 93100000。
            int time = orderBook.getTime();
            // 昨日已经完成的连续涨停天数，例如 2 表示今天处于“2进3”。
            int lbcs = orderBook.getLbcs();
            // 当日截至当前时刻的累计成交额，单位：元。
            long turnover = orderBook.getTurnover();

            double maxHs = orderBook.getMaxHs();

            // 当前分钟最新价，单位：分。
            int currentPrice = orderBook.getMinutePriceAt(calculateIndex);
            // 根据启动市值分档得到的允许换手率上限，单位：%。
            double maxTurnover = maxTurnover(marketValue);

            // 三分钟前的分钟均价，单位：分。
            int averagePrice3 = orderBook.getAveragePriceAt(calculateIndex - 3);
            // 两分钟前的分钟均价，单位：分。
            int averagePrice2 = orderBook.getAveragePriceAt(calculateIndex - 2);
            // 上一分钟的分钟均价，单位：分。
            int previousAveragePrice = orderBook.getAveragePriceAt(calculateIndex - 1);

            // 三分钟前的分钟最新价，单位：分。
            int price3 = orderBook.getMinutePriceAt(calculateIndex - 3);
            // 两分钟前的分钟最新价，单位：分。
            int price2 = orderBook.getMinutePriceAt(calculateIndex - 2);
            // 上一分钟的分钟最新价，单位：分。
            int previousPrice = orderBook.getMinutePriceAt(calculateIndex - 1);

            if (price3 > price2 && price2 > previousPrice) {// 楚环科技 2025-12-23
                String remark = StrUtil.format("2进3价格连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%", increase);
                return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING, currentPrice, remark);
            }

            if (averagePrice3 > averagePrice2 && averagePrice2 > previousAveragePrice) {// 双枪科技 2025-12-05
                String remark = StrUtil.format("2进3均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%", increase);
                return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING, currentPrice, remark);
            }
            if (orderBook.getIncrease() <-5 && openIncrease < -5) { // 深华发A 2025-09-29
                String remark = StrUtil.format("2进3 开盘低，而且价格低 是十分弱势的体现，赶快卖掉，当前涨幅 {}%", increase);
                return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING, currentPrice, remark);
            }

            return false;
        }

        /**
         * 按启动市值（万元）返回均价卖出规则使用的换手率基准上限（%）。
         */
        private static double maxTurnover(long marketValue) {
            if (marketValue < 80_000) {
                return 55;
            }
            if (marketValue < 105_000) {
                return 50;
            }
            if (marketValue < 140_000) {
                return 45;
            }
            return 40;
        }

        private static boolean match(TradeMarketState orderBook, TradeRuleRecord ruleRecord, int ruleCode,
                                     int price, String remark) {
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, orderBook.getSymbol(), orderBook.getTime(), price, orderBook.getIncrease(), remark);
            log.info(remark);
            return true;
        }
    }
}
