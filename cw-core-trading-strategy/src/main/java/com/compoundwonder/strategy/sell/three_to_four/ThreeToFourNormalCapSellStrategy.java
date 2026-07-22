package com.compoundwonder.strategy.sell.three_to_four;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 3 板、今日 3 进 4、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>只保留最新普通首板基准 Run 32 实际触发的规则，每条规则均标注代表股票、
 * 代码、卖出日期和时间。</p>
 */
@Slf4j
public final class ThreeToFourNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        // 真实样本：摩恩电气（002451），卖出 2025-11-07 14:48:33.830，规则 102。
        if (market.getChangePercent() < -3
                && market.getInitialMarketValue() > 130_000
                && market.getTime() > ConstantUtil.TIME_1130
                && market.getTime() < ConstantUtil.TIME_14563
                && market.getTurnoverRate() < 25
                && market.getTwoDaysTurnover() < 25) {
            String remark = StrUtil.format(
                    "前两天缩量板下午炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    market.getLbcs() + 1, market.getInitialMarketValue(),
                    market.getLimitUpBuyAmount(), market.getTurnoverRate(),
                    market.getChangePercent());
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD,
                    market.getLastPrice(), remark);
        }
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market,
                                        TradeRuleRecord record) {
        double turnoverRate = market.getTurnoverRate();
        double amplitude = market.getAmplitude();
        double increase = market.getIncrease();
        int highestPrice = market.getHighestPrice();
        int closePrice = market.getClosePrice();
        double openIncrease = market.getOpenIncrease();
        int time = market.getTime();
        double maxHs = market.getMaxHs();
        int currentPrice = market.getMinutePriceAt(index);

        // 上午炸板且开盘涨幅不高时，沿用基准规则：暂不使用分钟均价卖出。
        if (market.getStatus() > 0 && time < ConstantUtil.TIME_1330 && openIncrease < 8) {
            return false;
        }

        int averagePrice3 = market.getAveragePriceAt(index - 3);
        int averagePrice2 = market.getAveragePriceAt(index - 2);
        int previousAveragePrice = market.getAveragePriceAt(index - 1);
        int currentAveragePrice = market.getAveragePriceAt(index);
        int price2 = market.getMinutePriceAt(index - 2);
        int previousPrice = market.getMinutePriceAt(index - 1);

        double highestIncrease = (highestPrice - closePrice) * 100.0 / closePrice;
        double peakToCurrentDrawdown = (highestPrice - currentPrice) * 100.0 / closePrice;

        // 虚拟卖出样本：世荣兆业（002016），卖出 2025-02-12 09:34:59.930，规则 204。
        if (index >= 5 && index <= 30
                && highestIncrease >= 9.5
                && increase < 5.5
                && turnoverRate < maxHs * 0.6
                && previousPrice < previousAveragePrice
                && currentAveragePrice <= previousAveragePrice) {
            String remark = StrUtil.format(
                    "冲高接近涨停后被均线压制；条件：高点回落 {}%，当前涨幅 {}%，均线继续下行",
                    peakToCurrentDrawdown, increase);
            return match(market, record, RuleConstant.SELL_AVERAGE_NEAR_LIMIT_UP_PRESSURE,
                    currentPrice, remark);
        }

        int patternStartIndex = Math.max(0, index - 15);
        int secondPeakIndex = -1;
        for (int i = patternStartIndex + 1; i < index; i++) {
            if (market.getMinutePriceAt(i) > 0
                    && (secondPeakIndex < 0
                    || market.getMinutePriceAt(i) > market.getMinutePriceAt(secondPeakIndex))) {
                secondPeakIndex = i;
            }
        }
        if (market.getOpenPrice() > 0 && secondPeakIndex > patternStartIndex) {
            int pullbackLowIndex = -1;
            for (int i = patternStartIndex; i < secondPeakIndex; i++) {
                if (market.getMinutePriceAt(i) > 0
                        && (pullbackLowIndex < 0
                        || market.getMinutePriceAt(i) < market.getMinutePriceAt(pullbackLowIndex))) {
                    pullbackLowIndex = i;
                }
            }
            if (pullbackLowIndex >= 0) {
                int firstPeakPrice = market.getOpenPrice();
                int pullbackLowPrice = market.getMinutePriceAt(pullbackLowIndex);
                int secondPeakPrice = market.getMinutePriceAt(secondPeakIndex);
                double firstPeakIncrease = (firstPeakPrice - closePrice) * 100.0 / closePrice;
                double secondPeakIncrease = (secondPeakPrice - closePrice) * 100.0 / closePrice;
                double firstPullback = (firstPeakPrice - pullbackLowPrice) * 100.0 / closePrice;
                double secondDrawdown = (secondPeakPrice - currentPrice) * 100.0 / closePrice;

                // 真实样本：立方制药（003020），卖出 2026-07-14 09:36:47.940，规则 205。
                if ((highestPrice != market.getLimitUpPrice() || time > ConstantUtil.TIME_1330)
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
                    return match(market, record, RuleConstant.SELL_AVERAGE_PEAK_DRAWDOWN,
                            currentPrice, remark);
                }
            }
        }

        // 真实样本：万向德农（600371），卖出 2025-04-10 09:47:20.750，规则 206。
        if (price2 > previousPrice && previousPrice > currentPrice
                && increase > 0 && increase < 2.5 && amplitude > 9) {
            double lowIncrease = (market.getLowPrice() - closePrice) * 100.0 / closePrice;
            if (lowIncrease < -7) {
                String remark = StrUtil.format(
                        "大振幅后涨幅偏弱且走势连续下降；条件：振幅 {}%，当前涨幅 {}%",
                        amplitude, increase);
                return match(market, record,
                        RuleConstant.SELL_AVERAGE_LARGE_AMPLITUDE_WEAKENING,
                        currentPrice, remark);
            }
        }

        boolean crossesBelowAverage = previousPrice > previousAveragePrice
                && currentPrice < currentAveragePrice
                && averagePrice3 >= averagePrice2
                && averagePrice2 >= previousAveragePrice
                && averagePrice3 > previousAveragePrice;

        // 虚拟卖出样本：银座股份（600858），卖出 2025-01-10 10:07:14.770，规则 207。
        if (crossesBelowAverage && increase < 3.5 && amplitude > 10) {
            String remark = StrUtil.format(
                    "跌破均线后振幅过大且涨幅偏弱；条件：振幅 {}%，当前涨幅 {}%",
                    amplitude, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_BREAK_WITH_LARGE_AMPLITUDE,
                    currentPrice, remark);
        }
        return false;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
