package com.compoundwonder.strategy.smallcapfirstboard.trade;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于分钟价格和均价走势的卖出条件。
 *
 * <p>条件按优先级顺序执行，首个命中的条件会生成规则记录并结束本轮评估。</p>
 */
@Slf4j
final class AveragePriceSellEvaluator {

    private AveragePriceSellEvaluator() {
    }

    static boolean evaluate(int calculateIndex, TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
        // 本轮连板启动时的流通市值，单位：万元。
        long marketValue = orderBook.getInitialMarketValue();
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

        // 小市值三班组早盘暂不使用均线策略。
        if (marketValue < 109_999
                && (time < ConstantUtil.TIME_1330 || turnoverRate < maxTurnover)
                && openIncrease > 3.5 && openIncrease < 8
                && openIncrease - increase < 7) {
            return false;
        }
        // 上午炸板且开盘涨幅不高时，暂不使用均线策略。
        if (orderBook.getStatus() > 0 && time < ConstantUtil.TIME_1330 && openIncrease < 8) {
            return false;
        }
        if ((lbcs >= 4 && turnover < 950_000_000 && time < ConstantUtil.TIME_1330
                && increase > 0 && openIncrease < 8) || lbcs > 6) {
            return false;
        }

        // 三分钟前的分钟均价，单位：分。
        int averagePrice3 = orderBook.getAveragePriceAt(calculateIndex - 3);
        // 两分钟前的分钟均价，单位：分。
        int averagePrice2 = orderBook.getAveragePriceAt(calculateIndex - 2);
        // 上一分钟的分钟均价，单位：分。
        int previousAveragePrice = orderBook.getAveragePriceAt(calculateIndex - 1);
        // 当前分钟均价，单位：分。
        int currentAveragePrice = orderBook.getAveragePriceAt(calculateIndex);
        // 三分钟前的分钟最新价，单位：分。
        int price3 = orderBook.getMinutePriceAt(calculateIndex - 3);
        // 两分钟前的分钟最新价，单位：分。
        int price2 = orderBook.getMinutePriceAt(calculateIndex - 2);
        // 上一分钟的分钟最新价，单位：分。
        int previousPrice = orderBook.getMinutePriceAt(calculateIndex - 1);

        // 开盘涨幅减当前涨幅，表示从开盘位置回落了多少个百分点；正数表示回落。
        double openDropPercentage = openIncrease - increase;
        // 上一分钟价格相对昨收价的涨跌幅，单位：%。
        double previousPriceIncrease = (previousPrice - closePrice) * 100.0 / closePrice;

        if ((lbcs == 2 || orderBook.getYesterdayTurnover() > 45)
                && openDropPercentage >= 4.5
                && previousPriceIncrease < -3
                && averagePrice3 > averagePrice2
                && averagePrice2 > previousAveragePrice
                && price3 > price2
                && price2 > previousPrice) {
            String remark = StrUtil.format("2进3或昨日高换手后均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%", increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }

        if (lbcs == 2 && increase <= -3 && calculateIndex >= 12) {
            // 上一分钟均价相对昨收价的涨跌幅，单位：%。
            double movingAverageIncrease = (previousAveragePrice - closePrice) * 100.0 / closePrice;
            if (movingAverageIncrease <= -3
                    && averagePrice3 >= averagePrice2
                    && averagePrice2 >= previousAveragePrice
                    && averagePrice3 > previousAveragePrice) {
                String remark = StrUtil.format("2进3 开盘后均线与走势同步走弱；条件：均线涨幅 {}%，当前涨幅 {}%",
                        movingAverageIncrease, increase);
                return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_TWO_TO_THREE_WEAKENING,
                        currentPrice, remark);
            }
        }

        // 当日最高价相对昨收价的涨幅，单位：%。
        double highestIncrease = (highestPrice - closePrice) * 100.0 / closePrice;
        // 最高价到当前价的回落幅度，分母仍为昨收价，表示回落了多少个百分点。
        double peakToCurrentDrawdown = (highestPrice - currentPrice) * 100.0 / closePrice;

        if (lbcs == 2 && highestIncrease >= 8
                && previousPrice < previousAveragePrice
                && price2 >= averagePrice2) {
            String remark = StrUtil.format("2进3 冲高后跌破均线；条件：高点回落 {}%，当前涨幅 {}%，均线连续走弱",
                    peakToCurrentDrawdown, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_TWO_TO_THREE_BREAK_AVERAGE,
                    currentPrice, remark);
        }

        if (calculateIndex >= 5 && calculateIndex <= 30
                && highestIncrease >= 9.5
                && increase < 5.5 && turnoverRate < maxHs * 0.6
                && previousPrice < previousAveragePrice
                && currentAveragePrice <= previousAveragePrice) {
            String remark = StrUtil.format("冲高接近涨停后被均线压制；条件：高点回落 {}%，当前涨幅 {}%，均线继续下行",
                    peakToCurrentDrawdown, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_NEAR_LIMIT_UP_PRESSURE,
                    currentPrice, remark);
        }

        // 最近 15 个分钟采样的起点，窗口内只识别本轮“回落—再冲高”结构。
        int patternStartIndex = Math.max(0, calculateIndex - 15);
        // 当前分钟之前的最高采样位置，作为开盘回落后的二次冲高高点。
        int secondPeakIndex = -1;
        for (int i = patternStartIndex + 1; i < calculateIndex; i++) {
            if (orderBook.getMinutePriceAt(i) > 0
                    && (secondPeakIndex < 0 || orderBook.getMinutePriceAt(i) > orderBook.getMinutePriceAt(secondPeakIndex))) {
                secondPeakIndex = i;
            }
        }
        if (orderBook.getOpenPrice() > 0 && secondPeakIndex > patternStartIndex) {
            // 开盘价与第二高点之间的最低采样位置，作为首次回落低点。
            int pullbackLowIndex = -1;
            for (int i = patternStartIndex; i < secondPeakIndex; i++) {
                if (orderBook.getMinutePriceAt(i) > 0
                        && (pullbackLowIndex < 0 || orderBook.getMinutePriceAt(i) < orderBook.getMinutePriceAt(pullbackLowIndex))) {
                    pullbackLowIndex = i;
                }
            }
            if (pullbackLowIndex >= 0) {
                // 第一高点固定使用集合竞价形成的开盘价，不能使用 09:30 分钟结束价。
                int firstPeakPrice = orderBook.getOpenPrice();
                // 首次回落最低价，单位：分。
                int pullbackLowPrice = orderBook.getMinutePriceAt(pullbackLowIndex);
                // 二次冲高价格，单位：分。
                int secondPeakPrice = orderBook.getMinutePriceAt(secondPeakIndex);
                // 第一高点相对昨收价的涨幅，单位：%。
                double firstPeakIncrease = (firstPeakPrice - closePrice) * 100.0 / closePrice;
                // 二次高点相对昨收价的涨幅，单位：%。
                double secondPeakIncrease = (secondPeakPrice - closePrice) * 100.0 / closePrice;
                // 第一高点到首次低点的回落幅度，分母为昨收价，单位：%。
                double firstPullback = (firstPeakPrice - pullbackLowPrice) * 100.0 / closePrice;
                // 二次高点到当前价的回落幅度，分母为昨收价，单位：%。
                double secondDrawdown = (secondPeakPrice - currentPrice) * 100.0 / closePrice;
                if ((highestPrice != orderBook.getLimitUpPrice() || time > ConstantUtil.TIME_1330)
                        && firstPeakIncrease >= 5
                        && firstPullback >= 2.5
                        && secondPeakPrice >= firstPeakPrice
                        && secondPeakIncrease >= 8
                        && secondDrawdown >= 3.5
                        && increase <= 5.5
                        && currentAveragePrice > 0
                        && currentPrice < currentAveragePrice) {
                    String remark = StrUtil.format(
                            "高开回落后二次冲高失败；条件：首次回落 {}%，二次高点涨幅 {}%，二次回落 {}%，当前涨幅 {}%",
                            firstPullback, secondPeakIncrease, secondDrawdown, increase);
                    return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_PEAK_DRAWDOWN,
                            currentPrice, remark);
                }
            }
        }

        if (price2 > previousPrice && previousPrice > currentPrice
                && increase > 0 && increase < 2.5 && amplitude > 9) {
            // 当日最低价相对昨收价的涨跌幅，单位：%。
            double lowIncrease = (orderBook.getLowPrice() - closePrice) * 100.0 / closePrice;
            if (lowIncrease < -7) {
                String remark = StrUtil.format("大振幅后涨幅偏弱且走势连续下降；条件：振幅 {}%，当前涨幅 {}%",
                        amplitude, increase);
                return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_LARGE_AMPLITUDE_WEAKENING,
                        currentPrice, remark);
            }
        }

        // 价格由均线上方跌到均线下方，同时最近三分钟均价整体不抬升。
        boolean crossesBelowAverage = previousPrice > previousAveragePrice
                && currentPrice < currentAveragePrice
                && averagePrice3 >= averagePrice2
                && averagePrice2 >= previousAveragePrice
                && averagePrice3 > previousAveragePrice;
        if (!crossesBelowAverage) {
            return false;
        }

        if (increase < 3.5 && amplitude > 10) {
            String remark = StrUtil.format("跌破均线后振幅过大且涨幅偏弱；条件：振幅 {}%，当前涨幅 {}%",
                    amplitude, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_BREAK_WITH_LARGE_AMPLITUDE,
                    currentPrice, remark);
        }
        if (increase < 5.5 && amplitude > 15) {
            String remark = StrUtil.format("跌破均线后振幅超过 15% 且涨幅不足；条件：振幅 {}%，当前涨幅 {}%",
                    amplitude, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_BREAK_WITH_EXTREME_AMPLITUDE,
                    currentPrice, remark);
        }
        if (increase <= 4 && peakToCurrentDrawdown >= 5) {
            String remark = StrUtil.format("跌破均线后高点回落过大；条件：高点回落 {}%，当前涨幅 {}%",
                    peakToCurrentDrawdown, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_BREAK_WITH_PEAK_DRAWDOWN,
                    currentPrice, remark);
        }
        if ((time > ConstantUtil.TIME_1330 || turnoverRate > maxTurnover) && increase < 7 && turnoverRate < maxHs * 0.6) {
            String remark = StrUtil.format("跌破均线后高换手或尾盘涨幅不足；条件：换手率 {}%，当前涨幅 {}%",
                    turnoverRate, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_BREAK_LATE_OR_HIGH_TURNOVER,
                    currentPrice, remark);
        }
        return false;
    }

    /** 按启动市值（万元）返回均价卖出规则使用的换手率基准上限（%）。 */
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
        ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, orderBook.getSymbol(),
                orderBook.getTime(), price, orderBook.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}

