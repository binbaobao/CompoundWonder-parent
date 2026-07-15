package com.compoundwonder.core.processor.evaluator;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
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

    static boolean evaluate(int calculateIndex, OrderBook orderBook, RuleRecord ruleRecord) {
        long marketValue = orderBook.getInitialMarketValue();
        double turnoverRate = orderBook.getTurnoverRate();
        double amplitude = orderBook.getAmplitude();
        double increase = orderBook.getIncrease();
        int highestPrice = orderBook.getHighestPrice();
        int closePrice = orderBook.getClosePrice();
        double openIncrease = orderBook.getOpenIncrease();
        int time = orderBook.getTime();
        int lbcs = orderBook.getLbcs();
        long turnover = orderBook.getTurnover();

        int currentPrice = orderBook.price[calculateIndex];
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

        int averagePrice3 = orderBook.avgPrice[calculateIndex - 3];
        int averagePrice2 = orderBook.avgPrice[calculateIndex - 2];
        int previousAveragePrice = orderBook.avgPrice[calculateIndex - 1];
        int currentAveragePrice = orderBook.avgPrice[calculateIndex];
        int price3 = orderBook.price[calculateIndex - 3];
        int price2 = orderBook.price[calculateIndex - 2];
        int previousPrice = orderBook.price[calculateIndex - 1];

        double openDropPercentage = openIncrease - increase;
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

        double highestIncrease = (highestPrice - closePrice) * 100.0 / closePrice;
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
                && increase < 5.5
                && previousPrice < previousAveragePrice
                && currentAveragePrice <= previousAveragePrice) {
            String remark = StrUtil.format("冲高接近涨停后被均线压制；条件：高点回落 {}%，当前涨幅 {}%，均线继续下行",
                    peakToCurrentDrawdown, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_NEAR_LIMIT_UP_PRESSURE,
                    currentPrice, remark);
        }

        if ((highestPrice != orderBook.getLimitUpPrice() || time > ConstantUtil.TIME_1330)
                && previousPriceIncrease < -3
                && peakToCurrentDrawdown > 2 * lbcs
                && openDropPercentage >= 2 * lbcs
                && previousPrice < previousAveragePrice
                && currentAveragePrice < previousAveragePrice) {
            String remark = StrUtil.format("冲高回落后均线继续下压；条件：高点回落 {}%，当前涨幅 {}%",
                    peakToCurrentDrawdown, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_PEAK_DRAWDOWN,
                    currentPrice, remark);
        }

        if (price2 > previousPrice && previousPrice > currentPrice
                && increase > 0 && increase < 2.5 && amplitude > 9) {
            double lowIncrease = (orderBook.getLowPrice() - closePrice) * 100.0 / closePrice;
            if (lowIncrease < -7) {
                String remark = StrUtil.format("大振幅后涨幅偏弱且走势连续下降；条件：振幅 {}%，当前涨幅 {}%",
                        amplitude, increase);
                return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_LARGE_AMPLITUDE_WEAKENING,
                        currentPrice, remark);
            }
        }

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
        if ((time > ConstantUtil.TIME_1330 || turnoverRate > maxTurnover) && increase < 7) {
            String remark = StrUtil.format("跌破均线后高换手或尾盘涨幅不足；条件：换手率 {}%，当前涨幅 {}%",
                    turnoverRate, increase);
            return match(orderBook, ruleRecord, RuleConstant.SELL_AVERAGE_BREAK_LATE_OR_HIGH_TURNOVER,
                    currentPrice, remark);
        }
        return false;
    }

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

    private static boolean match(OrderBook orderBook, RuleRecord ruleRecord, int ruleCode,
                                 int price, String remark) {
        ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, orderBook.getSymbol(),
                orderBook.getTime(), price, orderBook.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
