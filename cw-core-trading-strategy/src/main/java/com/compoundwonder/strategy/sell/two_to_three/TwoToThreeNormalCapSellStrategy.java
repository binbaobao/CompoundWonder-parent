package com.compoundwonder.strategy.sell.two_to_three;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 2 板、今日 2 进 3、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>只保留最新普通首板基准 Run 32 实际触发的规则，每条规则均标注代表股票、
 * 代码、卖出日期和时间；真实未成交但完成虚拟卖出的样本也纳入保留依据。</p>
 */
@Slf4j
public final class TwoToThreeNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        int time = market.getTime();
        int lbcs = market.getLbcs();
        int status = market.getStatus();
        double changePercent = market.getChangePercent();
        long limitUpBuyAmount = market.getLimitUpBuyAmount();
        long lastSealAmount = market.getLastSealAmount();
        double twoDaysTurnover = market.getTwoDaysTurnover();
        double yesterdayTurnover = market.getYesterdayTurnover();

        // 真实样本：苏豪弘业（600128），卖出 2025-01-13 14:17:42.980，规则 102。
        if (changePercent < -3
                && marketValue > 130_000
                && time > ConstantUtil.TIME_1130
                && time < ConstantUtil.TIME_14563
                && turnover < 25
                && twoDaysTurnover < 25) {
            String remark = StrUtil.format(
                    "前两天缩量板下午炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD,
                    market.getLastPrice(), remark);
        }

        double maxTurnover = maxTurnover(marketValue);
        // 虚拟卖出样本：佛慈制药（002644），卖出 2025-07-30 14:18:15.880，规则 106。
        if (turnover > maxTurnover - 5
                && isLimitUp(status)
                && lbcs <= 7
                && time < ConstantUtil.TIME_14563
                && yesterdayTurnover > maxTurnover - 5) {
            String remark = StrUtil.format(
                    "连续两天换手过高；条件：昨日换手 {}%，今日换手 {}%",
                    yesterdayTurnover, turnover);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_CONSECUTIVE_HIGH_TURNOVER,
                    market.getLastPrice(), remark);
        }

        // 真实样本：宁水集团（603700），卖出 2025-03-05 10:51:38.250，规则 109。
        if (market.getOpenIncrease() > 8
                && market.getAmplitude() < 3
                && isLimitUp(status)
                && (market.getOneWordLimitUp() == 2
                || (twoDaysTurnover < 30 && yesterdayTurnover < 35))
                && turnover < 25
                && marketValue < 130_000
                && changePercent < -3
                && lastSealAmount < 2_500
                && lbcs < 7) {
            String remark = StrUtil.format(
                    "小市值连续一字板炸板；条件：连续 {} 个一字板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    market.getOneWordLimitUp(), lbcs + 1, marketValue,
                    limitUpBuyAmount, turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING,
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
        int lbcs = market.getLbcs();
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
        int price3 = market.getMinutePriceAt(index - 3);
        int price2 = market.getMinutePriceAt(index - 2);
        int previousPrice = market.getMinutePriceAt(index - 1);

        double openDropPercentage = openIncrease - increase;
        double previousPriceIncrease = (previousPrice - closePrice) * 100.0 / closePrice;

        // 真实样本：兴通股份（603209），卖出 2025-02-20 09:37:00.910，规则 201。
        if ((lbcs == 2 || market.getYesterdayTurnover() > 45)
                && openDropPercentage >= 4.5
                && previousPriceIncrease < -3
                && averagePrice3 > averagePrice2
                && averagePrice2 > previousAveragePrice
                && price3 > price2
                && price2 > previousPrice) {
            String remark = StrUtil.format(
                    "2进3或昨日高换手后均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%",
                    increase);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }

        // 真实样本：晶华新材（603683），卖出 2025-01-10 10:01:00.000，规则 202。
        if (lbcs == 2 && increase <= -3 && index >= 12) {
            double movingAverageIncrease = (previousAveragePrice - closePrice) * 100.0 / closePrice;
            if (movingAverageIncrease <= -3
                    && averagePrice3 >= averagePrice2
                    && averagePrice2 >= previousAveragePrice
                    && averagePrice3 > previousAveragePrice) {
                String remark = StrUtil.format(
                        "2进3 开盘后均线与走势同步走弱；条件：均线涨幅 {}%，当前涨幅 {}%",
                        movingAverageIncrease, increase);
                return match(market, record,
                        RuleConstant.SELL_AVERAGE_TWO_TO_THREE_WEAKENING,
                        currentPrice, remark);
            }
        }

        double highestIncrease = (highestPrice - closePrice) * 100.0 / closePrice;
        double peakToCurrentDrawdown = (highestPrice - currentPrice) * 100.0 / closePrice;

        // 真实样本：君禾股份（603617），卖出 2025-01-03 09:32:59.970，规则 203。
        if (lbcs == 2
                && highestIncrease >= 8
                && previousPrice < previousAveragePrice
                && price2 >= averagePrice2) {
            String remark = StrUtil.format(
                    "2进3 冲高后跌破均线；条件：高点回落 {}%，当前涨幅 {}%，均线连续走弱",
                    peakToCurrentDrawdown, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_TWO_TO_THREE_BREAK_AVERAGE,
                    currentPrice, remark);
        }

        // 真实样本：亚星化学（600319），卖出 2025-11-20 09:35:00.990，规则 204。
        if (index >= 5 && index <= 30
                && highestIncrease >= 9.5
                && increase < 5.5
                && turnoverRate < maxHs * 0.6
                && previousPrice < previousAveragePrice
                && currentAveragePrice <= previousAveragePrice) {
            String remark = StrUtil.format(
                    "冲高接近涨停后被均线压制；条件：高点回落 {}%，当前涨幅 {}%，均线继续下行",
                    peakToCurrentDrawdown, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_NEAR_LIMIT_UP_PRESSURE,
                    currentPrice, remark);
        }

        // 虚拟卖出样本：宁波联合（600051），卖出 2025-04-28 14:32:59.350，规则 206。
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
        if (!crossesBelowAverage) {
            return false;
        }

        // 虚拟卖出样本：东望时代（600052），卖出 2026-04-24 09:35:20.950，规则 207。
        if (increase < 3.5 && amplitude > 10) {
            String remark = StrUtil.format(
                    "跌破均线后振幅过大且涨幅偏弱；条件：振幅 {}%，当前涨幅 {}%",
                    amplitude, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_BREAK_WITH_LARGE_AMPLITUDE,
                    currentPrice, remark);
        }

        // 虚拟卖出样本：华纬科技（001380），卖出 2025-08-08 09:48:50.720，规则 209。
        if (increase <= 4 && peakToCurrentDrawdown >= 5) {
            String remark = StrUtil.format(
                    "跌破均线后高点回落过大；条件：高点回落 {}%，当前涨幅 {}%",
                    peakToCurrentDrawdown, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_BREAK_WITH_PEAK_DRAWDOWN,
                    currentPrice, remark);
        }
        return false;
    }

    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    private static double maxTurnover(long marketValue) {
        if (marketValue < 80_000) return 60;
        if (marketValue < 105_000) return 55;
        if (marketValue < 140_000) return 50;
        return 45;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
