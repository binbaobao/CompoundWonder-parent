package com.compoundwonder.core.processor.evaluator;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.core.constant.ConstantUtil;
import com.compoundwonder.core.constant.RuleConstant;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.OrderBook;
import lombok.extern.slf4j.Slf4j;

/**
 * 卖出条件控制
 */
@Slf4j
public class ConditionEvaluatorSell {
    /**
     * 主评估方法
     *
     * @param orderBook 当前股票盘口快照
     * @return bitmask 标志位
     * <p>
     * * 1.对晋级失败或者跌停股票坚决执行核按钮操作，寻求新的机会
     * * 2.对买入的股票要有信心，有利润的低位票要格局，高位要必须格局
     * * 3.盘中炸板,(1)刚买入的票开盘拉升摸板封单较少且封板力度较弱，炸板先卖出
     * * 4.盘中炸板,(2)链子票连续加速任何时间炸板都要卖出
     * * 5.盘中炸板,(3)合力票连续缩量加速下午炸板卖出
     * * 6.盘中炸板,(4)对于尾盘没有涨停，收盘竞价卖出
     * * 7.换手过高,(1)换手率超过 50% 炸板一定卖出
     * * 8.换手过高,(2)节假日前换手率超过 50% 也要卖出
     * * 8.竞价封单超过 1.5 亿，突然减少炸板先卖出
     */
    public static boolean evaluate(OrderBook orderBook, RuleRecord ruleRecord) {

        // 提前取出字段，避免重复访问 getter
        long mv = orderBook.getInitialMarketValue();      // 市值 (万元)
        double turnover = orderBook.getTurnoverRate();    // 换手率 (%)
        long volume = orderBook.getVolume();              // 当前成交量
        long maxVolume = orderBook.getMaxVolume();        // 200日最大成交量
        int lastPrice = orderBook.getLastPrice();         // 最新价
        int lowPrice = orderBook.getLowPrice();         // 最低价
        int limitUpPrice = orderBook.getLimitUpPrice();   // 涨停价
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount(); // 涨停封单金额
        int status = orderBook.getStatus();               // 状态标志（奇数代表涨停中）
        double changePercent = orderBook.getChangePercent();// 封单趋势变化
        double twoDaysTurnover = orderBook.getTwoDaysTurnover();// 两日换手
        int oneWordLimitUp = orderBook.getOneWordLimitUp();// 连续一字板数量
        double lastSealAmount = orderBook.getLastSealAmount(); // 封单金额 趋势平均
        int time = orderBook.getTime();
        int lastLimitUptime = orderBook.getLastLimitUptime();// 涨停时间
        int lbcs = orderBook.getLbcs();
        double amplitude = orderBook.getAmplitude();//振幅
        double openIncrease = orderBook.getOpenIncrease();
        double limitUpBreakDepth = orderBook.getLimitUpBreakDepth();// 炸板深度
//        long circulation = orderBook.getCirculation();
//        double maxHs = maxVolume * 100.0 / circulation;
        double yesterdayTurnover = orderBook.getYesterdayTurnover();
        double increase = orderBook.getIncrease();
        int averageLimitUpHeight = orderBook.getAverageLimitUpHeight();// 近 15天 平均高度
        // --- 市值区间判断 ---
        // 小市值缩量一字板，三班组，产业链
        if (changePercent < -1 && mv < 130_000 && lbcs > 3 && turnover < 15 && limitUpBuyAmount > 10000 && orderBook.getLastPrice() == limitUpPrice && limitUpBuyAmount < lastSealAmount / 1.5) {
            String remark = StrUtil.format("小市值缩量一字板封单快速减弱；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            return true;
        }
        // 市值大于 13亿， 前两天缩量版， 下午炸板要卖出
        if (changePercent < -3 && mv > 130_000 && time > ConstantUtil.TIME_1130 && time < ConstantUtil.TIME_14563 && turnover < 25 && twoDaysTurnover < 25) {
            String remark = StrUtil.format("前两天缩量板下午炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            return true;
        }

        double maxTurnover;
        // --- 市值区间判断 ---
        if (mv < 80_000) {
            maxTurnover = 60;
        } else if (mv < 105_000) {
            maxTurnover = 55;
        } else if (mv < 140_000) {
            maxTurnover = 50;
        } else {
            maxTurnover = 45;
        }

        // 当换手超过 50% 的时候开始处理逻辑
        if (turnover > maxTurnover - 5 && status % 2 == 1 && lbcs <= 7 && time < ConstantUtil.TIME_14563) {
            // 换手超过 65 都要卖出
            if (turnover > maxTurnover + 5 && (status > 20 && amplitude > 15)) {
                String remark = StrUtil.format("换手过高且多次炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent, status);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
            // 换手超过 50 % ，封单减少炸板就卖出
            if (turnover > maxTurnover && changePercent < -3 && lastSealAmount < 2500) {
                String remark = StrUtil.format("高换手后封单减少炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，换手阈值 {}%", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent, maxTurnover);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
            // 周末或者假期高换手即使封板也卖出
            // 大于 6板的票，遇到放假，换手过
            if (turnover > maxTurnover && orderBook.getNextTradingDay() >= 2) {
                String remark = StrUtil.format("临近周末或假期高换手；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent, orderBook.getNextTradingDay());
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
            // 连续两天换手超过 45 %,前两天换手大，经历分歧转一致又分歧的情况
            if (yesterdayTurnover > maxTurnover - 5) {
                String remark = StrUtil.format("连续两天换手过高；条件：昨日换手 {}%，今日换手 {}%", yesterdayTurnover, turnover);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }

            if (time < ConstantUtil.TIME_1330 && changePercent < -3 && limitUpBuyAmount < 2500) {
                String remark = StrUtil.format("早盘暴量换手且封单接近炸板；条件：换手率 {}%，封单变化EMA {}%，涨停封单金额 {} 万", turnover, changePercent, limitUpBuyAmount);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
        }
        // 前两天换手大，经历分歧转一致又分歧的情况 森林包装
        if (status % 2 == 1 && changePercent < -3 && limitUpBuyAmount < 2500 && lbcs <= 7 && turnover > 40 && twoDaysTurnover > 40) {
            String remark = StrUtil.format("前两天换手过高后今日再次爆量；条件：前两天换手 {}%，今日换手 {}%", twoDaysTurnover, turnover);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // TODO 连续一字板股票
        // 如果连续两个一字板，还是小市值 && turnover > 40
        if (openIncrease > 8 && amplitude < 3 && status % 2 == 1 && (oneWordLimitUp == 2 || (twoDaysTurnover < 30 && yesterdayTurnover < 35)) && turnover < 25 && mv < 130_000 && changePercent < -3 && lastSealAmount < 2500 && lbcs < 7) {
            String remark = StrUtil.format("小市值连续一字板炸板；条件：连续 {} 个一字板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", oneWordLimitUp, lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 炸板深度大于 8，就是炸板到 2个点，然后涨停又想炸板的时候就卖出
        if (limitUpBreakDepth > 8 && status % 2 == 1 && changePercent < -2 && lbcs < 7 && limitUpBuyAmount < 2500) {
            String remark = StrUtil.format("炸板深度过深且封单继续减弱；条件：炸板深度 {}%，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", limitUpBreakDepth, lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }

        if (openIncrease > 8 && amplitude < 4 && status % 2 == 1 && oneWordLimitUp == 3 && changePercent < -3 && lastSealAmount < 2500 && lbcs < 6) {
            String remark = StrUtil.format("小市值连续三个一字板炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 如果连板位置高了，早盘时间大高开且炸板 克来机电的 6板
        if (lbcs >= 5 && lbcs <= 8 && openIncrease >= 7.5 && ((yesterdayTurnover / 2) > turnover || turnover < 10) && changePercent < -5) {
            String remark = StrUtil.format("高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万", lbcs, openIncrease, turnover, yesterdayTurnover, changePercent, limitUpBuyAmount);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 7板以下，涨停还是小市值,如果振幅太大就卖出
        if (status % 2 == 1 && mv > 110_000 && amplitude > 17.5 && lbcs < 6) {
            String remark = StrUtil.format("小市值涨停中振幅过大；条件：振幅 {}%，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", amplitude, lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 连板次大于6板，启动市值小于 13亿，缩量板炸板随时卖出
        if (lbcs >= 5 && lbcs < 7 && mv < 130_000) {
            double threeDaysTurnover = orderBook.getThreeDaysTurnover();
            if (threeDaysTurnover <= 16.6 && changePercent < -2 && lastSealAmount < 2500) {
                String remark = StrUtil.format("高位连板缩量板炸板；条件：今日 {} 板，启动市值 {} 万，三日换手 {}%，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", lbcs + 1, mv, threeDaysTurnover, limitUpBuyAmount, turnover, changePercent);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
        }
        // 克来 5 ，蓝丰 6 兴业 6
        if (status % 2 == 1 && orderBook.getLbcs() >= 6 && orderBook.getNextTradingDay() >= 3) {
            String remark = StrUtil.format("高位连板遇到长假先落袋；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}", lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent, orderBook.getNextTradingDay());
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 基于近 15日 平均高度 设置一个格局点，如果快速涨停秒板，换手较少炸板就先卖出
        if (status == 1 && lastLimitUptime < ConstantUtil.TIME_932 && lbcs == averageLimitUpHeight && lastSealAmount < 9500 && changePercent <= -1.8) {
            String remark = StrUtil.format("达到近 15 日平均高度后秒板封单减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", averageLimitUpHeight, lbcs, lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 基于近 15日 平均高度 设置一个格局点，如果快速涨停秒板，换手较少炸板就先卖出
        if (status % 2 == 1 && lbcs == averageLimitUpHeight && lastSealAmount > 2000 && lastSealAmount < 5500 && changePercent <= -3.8) {
            String remark = StrUtil.format("达到近 15 日平均高度后封单继续减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%", averageLimitUpHeight, lbcs, lbcs + 1, mv, limitUpBuyAmount, turnover, changePercent);
            log.info(remark);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            return true;
        }
        // 2进 3 骗炮 涨停还老老炸板就卖出
        if (status % 2 == 1 && lbcs == 2 && status >= 5 && lastSealAmount > 2000 && lastSealAmount < 5500 && changePercent <= -2.8) {
            String remark = StrUtil.format("2进3 多次炸板骗炮；条件：昨日连板 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，涨停状态 {}", lbcs, mv, limitUpBuyAmount, turnover, changePercent, status);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        return false;
    }

    /**
     * 均价卖出策略
     *
     * @param calculateIndex
     * @param orderBook
     * @return
     */
    public static boolean averagePriceSellStrategy(int calculateIndex, OrderBook orderBook, RuleRecord ruleRecord) {
        long mv = orderBook.getInitialMarketValue();      // 市值 (万元)
        double turnoverRate = orderBook.getTurnoverRate();    // 换手率 (%)
        double amplitude = orderBook.getAmplitude();      //振幅 (%)
        long maxVolume = orderBook.getMaxVolume();        // 当日最大成交量
        double increase = orderBook.getIncrease();
        int highestPrice = orderBook.getHighestPrice();
        int limitUpPrice = orderBook.getLimitUpPrice();
        int closePrice = orderBook.getClosePrice();
        double openIncrease = orderBook.getOpenIncrease();
        double yesterdayTurnover = orderBook.getYesterdayTurnover();
        int time = orderBook.getTime();
        long circulation = orderBook.getCirculation();
        double maxHs = maxVolume * 100.0 / circulation / 1.2;
        int lbcs = orderBook.getLbcs();
        long turnover = orderBook.getTurnover();

        int lastPrice = orderBook.price[calculateIndex];

        double maxTurnover;
        // --- 市值区间判断 ---
        if (mv < 80_000) {
            maxTurnover = 55;
        } else if (mv < 105_000) {
            maxTurnover = 50;
        } else if (mv < 140_000) {
            maxTurnover = 45;
        } else {
            maxTurnover = 40;
        }
        // 启动市值如果小于 13亿，就认定为三班组操作，早盘不使用均线策略
        if (mv < 109_999 && (ConstantUtil.TIME_1330 > time || turnoverRate < maxTurnover) && (openIncrease > 3.5 && openIncrease < 8 && (openIncrease - increase) < 7)) {
            return false;
        }
        // 如果炸板 在上午就不先启用均线策略
        if (orderBook.getStatus() > 0 && ConstantUtil.TIME_1330 > time && openIncrease < 8) {
            return false;
        }
        if ((lbcs >= 4 && turnover < 950_000_000 && time < ConstantUtil.TIME_1330 && increase > 0) && openIncrease < 8 || lbcs > 6) {
            return false;
        }
        // 开盘相对最低价格跌幅
        double openDropPercentage = openIncrease - increase;
        int i3 = orderBook.avgPrice[calculateIndex - 3];
        int i2 = orderBook.avgPrice[calculateIndex - 2];
        int i1 = orderBook.avgPrice[calculateIndex - 1];

        int p3 = orderBook.price[calculateIndex - 3];
        int p2 = orderBook.price[calculateIndex - 2];
        int p1 = orderBook.price[calculateIndex - 1];

        // 今天是2进3或者如果昨天换手太高，这两种情况很开能会低开行情，如果跌幅较大，时判断如果均线连续 3分钟 下降卖出止损切换标的
        if ((lbcs == 2 || yesterdayTurnover > 45) && openDropPercentage >= 4.5 && p1 < -3) {
            if (i3 > i2 && i2 > i1 && p3 > p2 && p2 > p1) {
                String remark = StrUtil.format("2进3或昨日高换手后均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%", increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
        }

        // 今天是2进3 ,超过 15分钟 涨幅还小于 4.5% ,均线还呈下降趋势就卖出
        if (lbcs == 2 && increase <= -3 && calculateIndex >= 12) {
            // 均线涨幅
            double movingAverageIncrease = (i1 - closePrice) * 100.0 / closePrice;
            if (movingAverageIncrease <= -3 && i3 >= i2 && i2 >= i1 && i3 > i1) {
                String remark = StrUtil.format("2进3 开盘后均线与走势同步走弱；条件：均线涨幅 {}%，当前涨幅 {}%", movingAverageIncrease, increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
        }


        double highestDrawdown = (highestPrice - closePrice) * 100.0 / closePrice;
        double peakToCurrentDrawdown = (highestPrice - lastPrice) * 100.0 / closePrice;
        // 今天是2进3 ,超过 15分钟 涨幅还小于 4.5% ,均线还呈下降趋势就卖出
        if (lbcs == 2 && highestDrawdown >= 8 && p1 < i1 && p2 >= i2) {
            String remark = StrUtil.format("2进3 冲高后跌破均线；条件：高点回落 {}%，当前涨幅 {}%，均线连续走弱", peakToCurrentDrawdown, increase);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }

        // 均线买出逻辑：冲高到涨停附近，被砸落下
        if (calculateIndex >= 5 && calculateIndex <= 30 && highestDrawdown >= 9.5 && increase < 5.5 && orderBook.price[calculateIndex - 1] < orderBook.avgPrice[calculateIndex - 1]
                && orderBook.avgPrice[calculateIndex] <= orderBook.avgPrice[calculateIndex - 1]) {
            String remark = StrUtil.format("冲高接近涨停后被均线压制；条件：高点回落 {}%，当前涨幅 {}%，均线继续下行", highestDrawdown, increase);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }


        if ((highestPrice != limitUpPrice || ConstantUtil.TIME_1330 < time) && p1 < -3
                && peakToCurrentDrawdown > 2 * lbcs && openDropPercentage >= 2 * lbcs
                && orderBook.price[calculateIndex - 1] < orderBook.avgPrice[calculateIndex - 1]
                && orderBook.avgPrice[calculateIndex] < orderBook.avgPrice[calculateIndex - 1]) {
            String remark = StrUtil.format("冲高回落后均线继续下压；条件：高点回落 {}%，当前涨幅 {}%",
                    peakToCurrentDrawdown, increase);
            ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
            log.info(remark);
            return true;
        }
        // 如果均线拐头向下了， 如果涨幅小于 1.5 并且是振幅大于10，就是说最低到了-7.5 以下了
        if (orderBook.price[calculateIndex - 2] > orderBook.price[calculateIndex - 1] && orderBook.price[calculateIndex - 1] > orderBook.price[calculateIndex] && increase > 0 && increase < 2.5 && amplitude > 9) {
            double v = (orderBook.getLowPrice() - orderBook.getClosePrice()) * 100.0 / orderBook.getClosePrice();
            // 计算最低价位置，如果最低价小于-7，且达成上面条件就快点卖出
            if (v < -7) {
                String remark = StrUtil.format("大振幅后涨幅偏弱且走势连续下降；条件：振幅 {}%，当前涨幅 {}%", amplitude, increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
        }
        // 经历开盘 5 分钟，如果发生价格由上至下穿过均线，开启均线卖出逻辑判断
        if (orderBook.price[calculateIndex - 1] > orderBook.avgPrice[calculateIndex - 1]
                && orderBook.price[calculateIndex] < orderBook.avgPrice[calculateIndex]
                && i3 >= i2 && i2 >= i1 && i3 > i1) {
            if (increase < 3.5 && amplitude > 10) {
                String remark = StrUtil.format("跌破均线后振幅过大且涨幅偏弱；条件：振幅 {}%，当前涨幅 {}%", amplitude, increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
            if (increase < 5.5 && amplitude > 15) {
                String remark = StrUtil.format("跌破均线后振幅超过 15% 且涨幅不足；条件：振幅 {}%，当前涨幅 {}%", amplitude, increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }

            if (increase <= 4 && peakToCurrentDrawdown >= 5) {
                String remark = StrUtil.format("跌破均线后高点回落过大；条件：高点回落 {}%，当前涨幅 {}%", peakToCurrentDrawdown, increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
            if ((time > ConstantUtil.TIME_1330 || turnoverRate > maxTurnover) && increase < 7) {
                String remark = StrUtil.format("跌破均线后高换手或尾盘涨幅不足；条件：换手率 {}%，当前涨幅 {}%", turnoverRate, increase);
                ruleRecord.fill(RuleConstant.TRADING_MODE_SELL, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                log.info(remark);
                return true;
            }
        }
        return false;
    }
}
