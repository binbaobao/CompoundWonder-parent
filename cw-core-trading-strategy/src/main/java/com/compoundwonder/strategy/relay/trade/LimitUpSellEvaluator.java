package com.compoundwonder.strategy.relay.trade;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.TradeMarketState;
import com.compoundwonder.strategy.TradeRuleRecord;
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

    static boolean evaluate(TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
        // 本轮连板启动时的流通市值，单位：万元。
        long marketValue = orderBook.getInitialMarketValue();
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

        // 根据启动市值分档得到的基准最大换手率，单位：%。
        double maxTurnover = maxTurnover(marketValue);
        if (turnover > maxTurnover - 5 && isLimitUp(status)
                && lbcs <= 7 && time < ConstantUtil.TIME_14563) {
            if (turnover > maxTurnover + 5 && status > 20 && amplitude > 15) {
                String remark = StrUtil.format("换手过高且多次炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}",
                        lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, status);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK,
                        lastPrice, increase, remark);
            }
            if (turnover > maxTurnover && changePercent < -3 && lastSealAmount < 2_500 && lbcs >= 5) {
                String remark = StrUtil.format("高换手后封单减少炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，换手阈值 {}%",
                        lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, maxTurnover);
                return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_SEAL_WEAKENING,
                        lastPrice, increase, remark);
            }
            if (turnover > maxTurnover && orderBook.getNextTradingDay() >= 2 && lbcs >= 5) {
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
            if (time < ConstantUtil.TIME_1330 && changePercent < -3 && limitUpBuyAmount < 2_500 && lbcs >= 5) {
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

        if (limitUpBreakDepth > 8 && isLimitUp(status) && changePercent < -2 && turnover < 30
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
            // 昨日起最近三个交易日换手率的算术平均值，单位：%。
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

        // 近 15 个交易日涨停股票的平均连板高度，单位：板。,换手小的卖出，换手大的就不用着急
        int averageLimitUpHeight = orderBook.getAverageLimitUpHeight();
        if (isLimitUp(status) && (orderBook.getLastLimitUptime() < ConstantUtil.TIME_932 || amplitude < 3)
                && lbcs == averageLimitUpHeight && lastSealAmount < 2_500 && turnover < 25
                && changePercent <= -1.8) {
            String remark = StrUtil.format("达到近 15 日平均高度后秒板封单减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    averageLimitUpHeight, lbcs, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return recordAndLog(orderBook, ruleRecord, RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL,
                    lastPrice, increase, remark);
        }

        if (isLimitUp(status) && lbcs == averageLimitUpHeight && turnover < 25
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

    /** 订单簿状态为奇数时表示当前处于涨停封板状态。 */
    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    /** 按启动市值（万元）返回卖出规则使用的换手率基准上限（%）。 */
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

