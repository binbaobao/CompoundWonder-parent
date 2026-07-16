package com.compoundwonder.core.processor.evaluator;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于涨停状态、封单变化和换手率的卖出条件。
 *
 * <p>条件按优先级顺序执行，首个命中的条件会生成规则记录并结束本轮评估。</p>
 */
@Slf4j
final class LimitUpSellEvaluator {

    private LimitUpSellEvaluator() {
    }

    static boolean evaluate(OrderBook orderBook, RuleRecord ruleRecord) {
        long marketValue = orderBook.getInitialMarketValue();
        double turnover = orderBook.getTurnoverRate();
        int lastPrice = orderBook.getLastPrice();
        int limitUpPrice = orderBook.getLimitUpPrice();
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
        int status = orderBook.getStatus();
        double changePercent = orderBook.getChangePercent();
        double twoDaysTurnover = orderBook.getTwoDaysTurnover();
        int oneWordLimitUp = orderBook.getOneWordLimitUp();
        long lastSealAmount = orderBook.getLastSealAmount();
        int time = orderBook.getTime();
        int lbcs = orderBook.getLbcs();
        double amplitude = orderBook.getAmplitude();
        double openIncrease = orderBook.getOpenIncrease();
        double limitUpBreakDepth = orderBook.getLimitUpBreakDepth();
        double yesterdayTurnover = orderBook.getYesterdayTurnover();
        double increase = orderBook.getIncrease();

        if (changePercent < -1 && marketValue < 130_000 && lbcs > 3
                && turnover < 15 && limitUpBuyAmount > 10_000
                && lastPrice == limitUpPrice && limitUpBuyAmount < lastSealAmount / 1.5) {
            String remark = StrUtil.format("小市值缩量一字板封单快速减弱；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return record(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_SMALL_CAP_SEAL_WEAKENING,
                    lastPrice, increase, remark);
        }

        if (changePercent < -3 && marketValue > 130_000
                && time > ConstantUtil.TIME_1130 && time < ConstantUtil.TIME_14563
                && turnover < 25 && twoDaysTurnover < 25) {
            String remark = StrUtil.format("前两天缩量板下午炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return record(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD,
                    lastPrice, increase, remark);
        }

        double maxTurnover = maxTurnover(marketValue);
        if (turnover > maxTurnover - 5 && isLimitUp(status)
                && lbcs <= 7 && time < ConstantUtil.TIME_14563) {
            if (turnover > maxTurnover + 5 && status > 20 && amplitude > 15) {
                String remark = StrUtil.format("换手过高且多次炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}",
                        lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, status);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK,
                        lastPrice, increase, remark);
            }
            if (turnover > maxTurnover && changePercent < -3 && lastSealAmount < 2_500) {
                String remark = StrUtil.format("高换手后封单减少炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，换手阈值 {}%",
                        lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, maxTurnover);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_SEAL_WEAKENING,
                        lastPrice, increase, remark);
            }
            if (turnover > maxTurnover && orderBook.getNextTradingDay() >= 2) {
                String remark = StrUtil.format("临近周末或假期高换手；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}",
                        lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, orderBook.getNextTradingDay());
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_TURNOVER,
                        lastPrice, increase, remark);
            }
            if (yesterdayTurnover > maxTurnover - 5) {
                String remark = StrUtil.format("连续两天换手过高；条件：昨日换手 {}%，今日换手 {}%",
                        yesterdayTurnover, turnover);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_CONSECUTIVE_HIGH_TURNOVER,
                        lastPrice, increase, remark);
            }
            if (time < ConstantUtil.TIME_1330 && changePercent < -3 && limitUpBuyAmount < 2_500) {
                String remark = StrUtil.format("早盘暴量换手且封单接近炸板；条件：换手率 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                        turnover, changePercent, limitUpBuyAmount);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_MORNING_HIGH_TURNOVER_WEAK_SEAL,
                        lastPrice, increase, remark);
            }
        }

        if (isLimitUp(status) && changePercent < -3 && limitUpBuyAmount < 2_500
                && lbcs <= 7 && turnover > 40 && twoDaysTurnover > 40) {
            String remark = StrUtil.format("前两天换手过高后今日再次爆量；条件：前两天换手 {}%，今日换手 {}%",
                    twoDaysTurnover, turnover);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_MULTI_DAY_HIGH_TURNOVER,
                    lastPrice, increase, remark);
        }

        if (openIncrease > 8 && amplitude < 3 && isLimitUp(status)
                && (oneWordLimitUp == 2 || (twoDaysTurnover < 30 && yesterdayTurnover < 35))
                && turnover < 25 && marketValue < 130_000
                && changePercent < -3 && lastSealAmount < 2_500 && lbcs < 7) {
            String remark = StrUtil.format("小市值连续一字板炸板；条件：连续 {} 个一字板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    oneWordLimitUp, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING,
                    lastPrice, increase, remark);
        }

        if (limitUpBreakDepth > 8 && isLimitUp(status) && changePercent < -2
                && lbcs < 7 && limitUpBuyAmount < 2_500) {
            String remark = StrUtil.format("炸板深度过深且封单继续减弱；条件：炸板深度 {}%，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    limitUpBreakDepth, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_DEEP_BREAK_WEAK_SEAL,
                    lastPrice, increase, remark);
        }

        if (openIncrease > 8 && amplitude < 4 && isLimitUp(status)
                && oneWordLimitUp == 3 && changePercent < -3
                && lastSealAmount < 2_500 && lbcs < 6) {
            String remark = StrUtil.format("小市值连续三个一字板炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_THREE_ONE_WORD_WEAKENING,
                    lastPrice, increase, remark);
        }

        if (lbcs >= 5 && lbcs <= 8 && openIncrease >= 7.5
                && (yesterdayTurnover / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            String remark = StrUtil.format("高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, openIncrease, turnover, yesterdayTurnover, changePercent, limitUpBuyAmount);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING,
                    lastPrice, increase, remark);
        }

        if (isLimitUp(status) && marketValue > 110_000 && amplitude > 17.5 && lbcs < 6) {
            String remark = StrUtil.format("小市值涨停中振幅过大；条件：振幅 {}%，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    amplitude, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_AMPLITUDE,
                    lastPrice, increase, remark);
        }

        if (lbcs >= 5 && lbcs < 7 && marketValue < 130_000) {
            double threeDaysTurnover = orderBook.getThreeDaysTurnover();
            if (threeDaysTurnover <= 16.6 && changePercent < -2 && lastSealAmount < 2_500) {
                String remark = StrUtil.format("高位连板缩量板炸板；条件：今日 {} 板，启动市值 {} 万，三日换手 {}%，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                        lbcs + 1, marketValue, threeDaysTurnover, limitUpBuyAmount, turnover, changePercent);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_LOW_TURNOVER,
                        lastPrice, increase, remark);
            }
        }

        if (isLimitUp(status) && orderBook.getNextTradingDay() >= 3 && (lbcs >= 6 || orderBook.getLastLimitUptime() < ConstantUtil.TIME_931) && turnover < 12) {
            String remark = StrUtil.format("高位连板遇到长假先落袋；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, orderBook.getNextTradingDay());
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_BOARD,
                    lastPrice, increase, remark);
        }

        int averageLimitUpHeight = orderBook.getAverageLimitUpHeight();
        if (isLimitUp(status) && (orderBook.getLastLimitUptime() < ConstantUtil.TIME_932 || amplitude < 3)
                && lbcs == averageLimitUpHeight && lastSealAmount < 2_500
                && changePercent <= -1.8) {
            String remark = StrUtil.format("达到近 15 日平均高度后秒板封单减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    averageLimitUpHeight, lbcs, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL,
                    lastPrice, increase, remark);
        }

        if (isLimitUp(status) && lbcs == averageLimitUpHeight
                && lastSealAmount > 2_000 && lastSealAmount < 5_500
                && changePercent <= -3.8) {
            String remark = StrUtil.format("达到近 15 日平均高度后封单继续减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    averageLimitUpHeight, lbcs, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_WEAK_SEAL,
                    lastPrice, increase, remark);
        }

//        if (isLimitUp(status) && lbcs == 2 && status >= 5
//                && lastSealAmount > 2_000 && lastSealAmount < 5_500
//                && changePercent <= -2.8) {
//            String remark = StrUtil.format("2进3 多次炸板骗炮；条件：昨日连板 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，涨停状态 {}",
//                    lbcs, marketValue, limitUpBuyAmount, turnover, changePercent, status);
//            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_TWO_TO_THREE_MULTI_BREAK,
//                    lastPrice, increase, remark);
//        }
        return false;
    }

    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    private static double maxTurnover(long marketValue) {
        if (marketValue < 80_000) {
            return 60;
        }
        if (marketValue < 105_000) {
            return 55;
        }
        if (marketValue < 140_000) {
            return 50;
        }
        return 45;
    }

    private static boolean record(OrderBook orderBook, RuleRecord ruleRecord, int ruleCode,
                                  int price, double increase, String remark) {
        ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, orderBook.getSymbol(),
                orderBook.getTime(), price, increase, remark);
        return true;
    }

    private static boolean recordAndLog(OrderBook orderBook, RuleRecord ruleRecord, int ruleCode,
                                        int price, double increase, String remark) {
        record(orderBook, ruleRecord, ruleCode, price, increase, remark);
        log.info(remark);
        return true;
    }
}
