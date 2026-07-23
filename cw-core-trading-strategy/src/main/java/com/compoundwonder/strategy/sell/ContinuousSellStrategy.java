package com.compoundwonder.strategy.sell;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.util.CompactTimeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 连续竞价统一卖出策略。
 *
 * <p>盘口和分钟均价卖出都从本类进入，规则按固定优先级逐条判断。昨日板数、启动
 * 市值等只能作为具体规则的条件，不能先用于选择另一套卖出执行器。上海和深圳的
 * 开盘集合竞价、收盘集合竞价仍由各自的独立执行器处理。</p>
 */
@Slf4j
public final class ContinuousSellStrategy {

    private static final int RESEAL_CONFIRMATION_MILLIS = 60_000;
    private static final double MIN_CONFIRMED_BREAK_DEPTH_PERCENT = 4D;
    private static final double TWO_TO_THREE_PROFIT_PROTECTION = 5D;

    /**
     * 按统一优先级判断连续竞价盘口卖出规则。
     */
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        int time = market.getTime();
        int status = market.getStatus();
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        double changePercent = market.getChangePercent();
        long limitUpBuyAmount = market.getLimitUpBuyAmount();
        long lastSealAmount = market.getLastSealAmount();
        double yesterdayTurnover = market.getYesterdayTurnover();
        double twoDaysTurnover = market.getTwoDaysTurnover();
        double amplitude = market.getAmplitude();

        boolean highTurnoverBreak = lbcs >= 2 && lbcs <= 3 && (turnover > 50
                || (lbcs == 2
                && turnover > twoToThreeTurnoverThreshold(time)
                && market.getEmaSealTrend() == -1));
        if (highTurnoverBreak && status > 0 && time < ConstantUtil.TIME_14563) {
            String remark = StrUtil.format(
                    "连续竞价高换手炸板；条件：昨日连板 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}",
                    lbcs, marketValue, limitUpBuyAmount, turnover, changePercent, status);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK,
                    market.getLastPrice(), remark);
        }

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

        double maxTurnover = maxOrderBookTurnover(marketValue);
        if (turnover > maxTurnover
                && isLimitUp(status)
                && lbcs >= 5 && lbcs <= 7
                && time < ConstantUtil.TIME_14563
                && changePercent < -3
                && lastSealAmount < 2_500) {
            String remark = StrUtil.format(
                    "高换手后封单减少炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，换手阈值 {}%",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover,
                    changePercent, maxTurnover);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_SEAL_WEAKENING,
                    market.getLastPrice(), remark);
        }

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

        if (turnover > maxTurnover - 5
                && isLimitUp(status)
                && lbcs >= 5 && lbcs <= 7
                && time < ConstantUtil.TIME_1330
                && changePercent < -3
                && limitUpBuyAmount < 2_500) {
            String remark = StrUtil.format(
                    "早盘暴量换手且封单接近炸板；条件：换手率 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    turnover, changePercent, limitUpBuyAmount);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_MORNING_HIGH_TURNOVER_WEAK_SEAL,
                    market.getLastPrice(), remark);
        }

        if (matchesOneWordWeakeningProfile(market)) {
            int todayBoard = lbcs + 1;
            int averageHeight = market.getAverageLimitUpHeight();
            int heightGap = todayBoard - averageHeight;
            boolean belowAverageHeight = averageHeight > 0 && heightGap < 0;
            boolean weakeningWhileSealed = amplitude < 3
                    && isLimitUp(status)
                    && changePercent < -3
                    && lastSealAmount < 2_500;
            if (weakeningWhileSealed && !belowAverageHeight) {
                String remark = StrUtil.format(
                        "小市值连续一字板炸板；条件：连续 {} 个一字板，今日 {} 板，近 15 日平均高度 {} 板，高度差 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                        market.getOneWordLimitUp(), todayBoard, averageHeight, heightGap,
                        marketValue, limitUpBuyAmount, turnover, changePercent);
                return match(market, record,
                        RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING,
                        market.getLastPrice(), remark);
            }
            if (belowAverageHeight
                    && matchesPreBreakWeakening(market)
                    && isBreakConfirmed(market)) {
                int breakDurationSeconds =
                        (CompactTimeUtil.compactToMillis(time)
                                - CompactTimeUtil.compactToMillis(
                                market.getLastLimitUpBreakTime())) / 1_000;
                String remark = StrUtil.format(
                        "低于近 15 日平均高度炸板确认未回封；条件：连续 {} 个一字板，今日 {} 板，近 15 日平均高度 {} 板，高度差 {} 板，炸板持续 {} 秒，涨停回落 {}%，启动市值 {} 万，换手率 {}%，炸板前封单金额 {} 万，炸板前封单变化EMA {}%",
                        market.getOneWordLimitUp(), todayBoard, averageHeight, heightGap,
                        breakDurationSeconds, market.getLimitUpBreakDepth(), marketValue,
                        turnover, market.getLastSealedAmount(),
                        market.getLastSealedChangePercent());
                return match(market, record,
                        RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING,
                        market.getLastPrice(), remark);
            }
        }

        if (lbcs >= 5 && lbcs <= 8
                && market.getOpenIncrease() >= 7.5
                && (yesterdayTurnover / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            String remark = StrUtil.format(
                    "高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, market.getOpenIncrease(), turnover, yesterdayTurnover,
                    changePercent, limitUpBuyAmount);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING,
                    market.getLastPrice(), remark);
        }

        if (isLimitUp(status)
                && marketValue > 110_000
                && amplitude > 17.5
                && lbcs < 6) {
            String remark = StrUtil.format(
                    "涨停中振幅过大；条件：振幅 {}%，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    amplitude, lbcs + 1, marketValue, limitUpBuyAmount,
                    turnover, changePercent);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_AMPLITUDE,
                    market.getLastPrice(), remark);
        }

        if (lbcs >= 5 && lbcs < 7
                && marketValue < 130_000
                && market.getThreeDaysTurnover() <= 16.6
                && changePercent < -2
                && lastSealAmount < 2_500) {
            int todayBoard = lbcs + 1;
            int marketHeight = market.getAverageLimitUpHeight();
            String remark = StrUtil.format(
                    "高位连板缩量板炸板；条件：今日 {} 板，近 15 日平均高度 {} 板，高度差 {} 板，启动市值 {} 万，三日换手 {}%，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    todayBoard, marketHeight, todayBoard - marketHeight,
                    marketValue, market.getThreeDaysTurnover(), limitUpBuyAmount,
                    turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_LOW_TURNOVER,
                    market.getLastPrice(), remark);
        }

        int averageHeight = market.getAverageLimitUpHeight();
        if (isLimitUp(status)
                && (market.getLastLimitUptime() < ConstantUtil.TIME_932 || amplitude < 3)
                && lbcs == averageHeight
                && lastSealAmount < 2_500
                && turnover < 25
                && changePercent <= -1.8) {
            String remark = StrUtil.format(
                    "达到近 15 日平均高度后秒板封单减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    averageHeight, lbcs, lbcs + 1, marketValue,
                    limitUpBuyAmount, turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL,
                    market.getLastPrice(), remark);
        }

        if (isLimitUp(status)
                && lbcs == averageHeight
                && turnover < 25
                && lastSealAmount > 2_000
                && lastSealAmount < 5_500
                && changePercent <= -3.8) {
            String remark = StrUtil.format(
                    "达到近 15 日平均高度后封单继续减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    averageHeight, lbcs, lbcs + 1, marketValue,
                    limitUpBuyAmount, turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_WEAK_SEAL,
                    market.getLastPrice(), remark);
        }

        if (isLimitUp(status)
                && lbcs >= 5 && lbcs <= 7
                && time < ConstantUtil.TIME_14563
                && turnover > maxTurnover
                && market.getNextTradingDay() >= 2) {
            String remark = StrUtil.format(
                    "临近周末或假期高换手；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover,
                    changePercent, market.getNextTradingDay());
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_TURNOVER,
                    market.getLastPrice(), remark);
        }

        if (isLimitUp(status)
                && market.getEmaSealTrend() == -1
                && market.getNextTradingDay() >= 3
                && (lbcs >= 6 || market.getLastLimitUptime() < ConstantUtil.TIME_931)
                && turnover < 12) {
            String remark = StrUtil.format(
                    "高位连板遇到长假先落袋；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover,
                    changePercent, market.getNextTradingDay());
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_BOARD,
                    market.getLastPrice(), remark);
        }
        return false;
    }

    /**
     * 按统一优先级判断连续竞价分钟均价卖出规则。
     */
    public boolean evaluateAveragePrice(int index, TradeMarketState market,
                                        TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        if (isOneWordResealObservationActive(market)) {
            return false;
        }

        int currentPrice = market.getMinutePriceAt(index);
        int price3 = market.getMinutePriceAt(index - 3);
        int price2 = market.getMinutePriceAt(index - 2);
        int previousPrice = market.getMinutePriceAt(index - 1);
        int averagePrice3 = market.getAveragePriceAt(index - 3);
        int averagePrice2 = market.getAveragePriceAt(index - 2);
        int previousAveragePrice = market.getAveragePriceAt(index - 1);
        int currentAveragePrice = market.getAveragePriceAt(index);
        double increase = market.getIncrease();
        boolean smallCap = market.getInitialMarketValue() < 119_999;

        if (smallCap && lbcs == 2
                && price3 > price2 && price2 > previousPrice
                && increase <= TWO_TO_THREE_PROFIT_PROTECTION) {
            String remark = StrUtil.format(
                    "2进3价格连续走弱；条件：连续 3 分钟价格下降，当前涨幅 {}%，利润保护阈值 {}%",
                    increase, TWO_TO_THREE_PROFIT_PROTECTION);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }
        if (smallCap && lbcs == 2
                && averagePrice3 > averagePrice2
                && averagePrice2 > previousAveragePrice
                && increase <= TWO_TO_THREE_PROFIT_PROTECTION) {
            String remark = StrUtil.format(
                    "2进3均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%，利润保护阈值 {}%",
                    increase, TWO_TO_THREE_PROFIT_PROTECTION);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }
        if (smallCap && lbcs == 2
                && increase < -5 && market.getOpenIncrease() < -5) {
            String remark = StrUtil.format(
                    "2进3低开后继续弱势；条件：当前涨幅 {}%，开盘涨幅 {}%",
                    increase, market.getOpenIncrease());
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }

        if (lbcs == 4) {
            return smallCap && evaluateFourToFiveAverage(index, market, record,
                    averagePrice3, averagePrice2, previousAveragePrice);
        }
        if (smallCap || lbcs < 2 || lbcs > 3) {
            return false;
        }

        double turnoverRate = market.getTurnoverRate();
        double amplitude = market.getAmplitude();
        int highestPrice = market.getHighestPrice();
        int closePrice = market.getClosePrice();
        int time = market.getTime();
        double openIncrease = market.getOpenIncrease();
        if (market.getStatus() > 0 && time < ConstantUtil.TIME_1330 && openIncrease < 8) {
            return false;
        }

        double openDropPercentage = openIncrease - increase;
        double previousPriceIncrease = (previousPrice - closePrice) * 100.0 / closePrice;
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

        if (lbcs == 2 && increase <= -3 && index >= 12) {
            double movingAverageIncrease =
                    (previousAveragePrice - closePrice) * 100.0 / closePrice;
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

        if (index >= 5 && index <= 30
                && highestIncrease >= 9.5
                && increase < 5.5
                && turnoverRate < market.getMaxHs() * 0.6
                && previousPrice < previousAveragePrice
                && currentAveragePrice <= previousAveragePrice) {
            String remark = StrUtil.format(
                    "冲高接近涨停后被均线压制；条件：高点回落 {}%，当前涨幅 {}%，均线继续下行",
                    peakToCurrentDrawdown, increase);
            return match(market, record, RuleConstant.SELL_AVERAGE_NEAR_LIMIT_UP_PRESSURE,
                    currentPrice, remark);
        }

        if (lbcs == 3 && evaluatePeakDrawdown(index, market, record,
                currentPrice, currentAveragePrice, closePrice, highestPrice)) {
            return true;
        }

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
        if (crossesBelowAverage && increase < 3.5 && amplitude > 10) {
            String remark = StrUtil.format(
                    "跌破均线后振幅过大且涨幅偏弱；条件：振幅 {}%，当前涨幅 {}%",
                    amplitude, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_BREAK_WITH_LARGE_AMPLITUDE,
                    currentPrice, remark);
        }
        if (lbcs == 2 && crossesBelowAverage
                && increase <= 4 && peakToCurrentDrawdown >= 5) {
            String remark = StrUtil.format(
                    "跌破均线后高点回落过大；条件：高点回落 {}%，当前涨幅 {}%",
                    peakToCurrentDrawdown, increase);
            return match(market, record,
                    RuleConstant.SELL_AVERAGE_BREAK_WITH_PEAK_DRAWDOWN,
                    currentPrice, remark);
        }
        return false;
    }

    private static boolean evaluatePeakDrawdown(
            int index, TradeMarketState market, TradeRuleRecord record,
            int currentPrice, int currentAveragePrice, int closePrice, int highestPrice) {
        int patternStartIndex = Math.max(0, index - 15);
        int secondPeakIndex = -1;
        for (int i = patternStartIndex + 1; i < index; i++) {
            if (market.getMinutePriceAt(i) > 0
                    && (secondPeakIndex < 0
                    || market.getMinutePriceAt(i)
                    > market.getMinutePriceAt(secondPeakIndex))) {
                secondPeakIndex = i;
            }
        }
        if (market.getOpenPrice() <= 0 || secondPeakIndex <= patternStartIndex) {
            return false;
        }
        int pullbackLowIndex = -1;
        for (int i = patternStartIndex; i < secondPeakIndex; i++) {
            if (market.getMinutePriceAt(i) > 0
                    && (pullbackLowIndex < 0
                    || market.getMinutePriceAt(i)
                    < market.getMinutePriceAt(pullbackLowIndex))) {
                pullbackLowIndex = i;
            }
        }
        if (pullbackLowIndex < 0) {
            return false;
        }
        int firstPeakPrice = market.getOpenPrice();
        int pullbackLowPrice = market.getMinutePriceAt(pullbackLowIndex);
        int secondPeakPrice = market.getMinutePriceAt(secondPeakIndex);
        double firstPeakIncrease = (firstPeakPrice - closePrice) * 100.0 / closePrice;
        double secondPeakIncrease = (secondPeakPrice - closePrice) * 100.0 / closePrice;
        double firstPullback = (firstPeakPrice - pullbackLowPrice) * 100.0 / closePrice;
        double secondDrawdown = (secondPeakPrice - currentPrice) * 100.0 / closePrice;
        if ((highestPrice != market.getLimitUpPrice()
                || market.getTime() > ConstantUtil.TIME_1330)
                && firstPeakIncrease >= 5
                && firstPullback >= 2.5
                && secondPeakPrice >= firstPeakPrice
                && secondPeakIncrease >= 8
                && secondDrawdown >= 3.5
                && market.getIncrease() <= 5.5
                && currentAveragePrice > 0
                && currentPrice < currentAveragePrice) {
            String remark = StrUtil.format(
                    "高开回落后二次冲高失败；条件：首次回落 {}%，二次高点涨幅 {}%，二次回落 {}%，当前涨幅 {}%",
                    firstPullback, secondPeakIncrease, secondDrawdown, market.getIncrease());
            return match(market, record, RuleConstant.SELL_AVERAGE_PEAK_DRAWDOWN,
                    currentPrice, remark);
        }
        return false;
    }

    private static boolean evaluateFourToFiveAverage(
            int index, TradeMarketState market, TradeRuleRecord record,
            int averagePrice3, int averagePrice2, int previousAveragePrice) {
        int currentPrice = market.getMinutePriceAt(index);
        int limitUpPrice = market.getLimitUpPrice();
        if (limitUpPrice > 0 && currentPrice >= limitUpPrice) {
            return false;
        }
        int time = market.getTime();
        double openIncrease = market.getOpenIncrease();
        double increase = market.getIncrease();
        double turnoverRate = market.getTurnoverRate();
        if (market.getInitialMarketValue() < 109_999
                && (time < ConstantUtil.TIME_1330
                || turnoverRate < maxAverageTurnover(market.getInitialMarketValue()))
                && openIncrease > 3.5 && openIncrease < 8
                && openIncrease - increase < 7) {
            return false;
        }
        if (market.getStatus() > 0 && time < ConstantUtil.TIME_1330 && openIncrease < 8) {
            return false;
        }
        if (market.getTurnover() < 950_000_000
                && time < ConstantUtil.TIME_1330
                && increase > 0 && openIncrease < 8) {
            return false;
        }
        boolean threeMinuteWeakeningWithOneWord =
                averagePrice3 > averagePrice2
                        && averagePrice2 > previousAveragePrice
                        && market.getOneWordLimitUp() >= 1;
        boolean recentWeakeningAfterPositiveOpen =
                averagePrice2 > previousAveragePrice && openIncrease >= 1;
        if (threeMinuteWeakeningWithOneWord || recentWeakeningAfterPositiveOpen) {
            String remark = StrUtil.format(
                    "4进5均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%",
                    increase);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }
        return false;
    }

    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    private static boolean isBreakConfirmed(TradeMarketState market) {
        int lastBreakTime = market.getLastLimitUpBreakTime();
        return market.getStatus() > 0
                && market.getStatus() % 2 == 0
                && lastBreakTime > 0
                && market.getLimitUpBreakDepth() >= MIN_CONFIRMED_BREAK_DEPTH_PERCENT
                && CompactTimeUtil.compactToMillis(market.getTime())
                - CompactTimeUtil.compactToMillis(lastBreakTime)
                >= RESEAL_CONFIRMATION_MILLIS;
    }

    private static boolean matchesPreBreakWeakening(TradeMarketState market) {
        return market.getLastSealedAmplitude() < 3
                && market.getLastSealedChangePercent() < -3
                && market.getLastSealedAmount() > 0
                && market.getLastSealedAmount() < 2_500;
    }

    private static boolean matchesOneWordWeakeningProfile(TradeMarketState market) {
        return market.getOpenIncrease() > 8
                && (market.getOneWordLimitUp() == 2
                || (market.getTwoDaysTurnover() < 30
                && market.getYesterdayTurnover() < 35))
                && market.getTurnoverRate() < 25
                && market.getInitialMarketValue() < 130_000
                && market.getLbcs() < 7;
    }

    private static boolean isOneWordResealObservationActive(TradeMarketState market) {
        int averageHeight = market.getAverageLimitUpHeight();
        return averageHeight > 0
                && market.getLbcs() + 1 < averageHeight
                && market.getStatus() > 0
                && market.getStatus() % 2 == 0
                && matchesOneWordWeakeningProfile(market)
                && matchesPreBreakWeakening(market);
    }

    private static double twoToThreeTurnoverThreshold(int time) {
        if (time < ConstantUtil.TIME_1330) return 30D;
        if (time > ConstantUtil.TIME_1330 && time < ConstantUtil.TIME_1430) return 40D;
        return 45D;
    }

    private static double maxOrderBookTurnover(long marketValue) {
        if (marketValue < 80_000) return 60D;
        if (marketValue < 105_000) return 55D;
        if (marketValue < 140_000) return 50D;
        return 45D;
    }

    private static double maxAverageTurnover(long marketValue) {
        if (marketValue < 80_000) return 55D;
        if (marketValue < 105_000) return 50D;
        if (marketValue < 140_000) return 45D;
        return 40D;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
