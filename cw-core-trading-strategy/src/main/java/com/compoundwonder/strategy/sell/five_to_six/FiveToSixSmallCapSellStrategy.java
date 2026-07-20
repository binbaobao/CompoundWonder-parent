package com.compoundwonder.strategy.sell.five_to_six;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 5 板、今日 5 进 6、启动流通市值严格小于 119999 万元的独立卖出策略。
 *
 * <p>本文件同时拥有盘口卖出与分钟均价卖出规则。当前内容由拆分前规则增量同步而来，
 * 后续调整该场景时只修改本文件；周末、节假日等全局规则由公共末级策略统一处理。</p>
 */
@Slf4j
public final class FiveToSixSmallCapSellStrategy implements BoardSellStrategy {

    /** 调用本场景独立的逐笔与盘口卖出规则。 */
    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        return OrderBookRules.evaluate(market, record);
    }

    /** 调用本场景独立的分钟价格与均价走势卖出规则。 */
    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return AveragePriceRules.evaluate(index, market, record);
    }

    /** 本场景独有的逐笔与盘口卖出规则，按书写顺序首个命中即结束。 */
    private static final class OrderBookRules {
        /**
         * 按既定优先级评估涨停盘口卖出规则；首个命中规则会填充记录并立即返回。
         *
         * @param orderBook 当前 Handler 私有订单簿的只读交易视图
         * @param ruleRecord 调用方预分配的规则记录
         * @return 命中任意涨停盘口卖出规则时返回 {@code true}
         */
        private static boolean evaluate(TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
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

    /** 本场景独有的分钟价格与均价走势卖出规则，按书写顺序首个命中即结束。 */
    private static final class AveragePriceRules {
        /**
         * 按既定优先级评估分钟走势卖出规则；首个命中规则会填充记录并立即返回。
         *
         * @param calculateIndex 当前分钟采样下标
         * @param orderBook 当前 Handler 私有订单簿的只读交易视图
         * @param ruleRecord 调用方预分配的规则记录
         * @return 命中任意分钟走势卖出规则时返回 {@code true}
         */
        private static boolean evaluate(int calculateIndex, TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
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
}
