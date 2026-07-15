package com.compoundwonder.core.processor.evaluator;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.TickNode;
import com.compoundwonder.util.CompactTimeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 连续竞价买入条件评估器。
 *
 * <p>低价股的大单主要看委托股数；中高价股同时看委托股数和委托金额。</p>
 */
@Slf4j
public final class ConditionEvaluatorBuy {

    private static final int RULE_LARGE_HIGH_PRICE_ORDER = 11;
    private static final int RULE_MEDIUM_HIGH_PRICE_ORDER = 12;
    private static final int RULE_LARGE_LOW_PRICE_ORDER = 13;
    private static final int RULE_NORMAL_LIMIT_UP_ORDER = 14;

    /** 价格单位为分，1000 表示 10 元。 */
    private static final int TEN_YUAN_PRICE_CENTS = 1_000;

    /** 委托数量单位为股。 */
    private static final int LARGE_HIGH_PRICE_QUANTITY_SHARES = 990_000;
    private static final int MEDIUM_HIGH_PRICE_QUANTITY_SHARES = 900_000;
    private static final int LARGE_LOW_PRICE_QUANTITY_SHARES = 888_800;

    /** 委托金额单位为元。 */
    private static final int LARGE_HIGH_PRICE_AMOUNT_YUAN = 9_000_000;
    private static final int MEDIUM_HIGH_PRICE_AMOUNT_YUAN = 7_000_000;

    /** 涨停封单金额和启动市值的单位均为万元。 */
    private static final long MAX_LIMIT_UP_BUY_AMOUNT_WAN = 8_000L;
    private static final long LARGE_ORDER_MAX_MARKET_VALUE_WAN = 198_000L;
    private static final long MEDIUM_ORDER_MAX_MARKET_VALUE_WAN = 150_000L;

    private static final double MIN_NORMAL_TURNOVER_RATE = 12.5;
    private static final double MAX_TURNOVER_RATE = 35.0;
    private static final double MAX_TURNOVER_DIVISOR = 3.3;
    private static final long HIGH_TRADING_AMOUNT_YUAN = 250_000_000L;
    private static final int RECENT_LIMIT_UP_WINDOW_MILLIS = 30_000;

    private ConditionEvaluatorBuy() {
    }

    /**
     * 根据当前订单簿判断是否触发买入规则。
     *
     * @param orderBook 当前股票订单簿
     * @param ruleRecord 预分配的规则记录，只有规则命中时才会填充
     * @return 命中买入规则返回 {@code true}
     */
    public static boolean evaluate(OrderBook orderBook, RuleRecord ruleRecord) {
        long marketValue = orderBook.getInitialMarketValue();
        double turnoverRate = orderBook.getTurnoverRate();
        int lastPrice = orderBook.getLastPrice();
        int limitUpPrice = orderBook.getLimitUpPrice();
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
        int time = orderBook.getTime();
        double sealChangePercent = orderBook.getChangePercent();

        if (limitUpBuyAmount > MAX_LIMIT_UP_BUY_AMOUNT_WAN) {
            return false;
        }
        if (!isTurnoverEligible(orderBook, turnoverRate)) {
            return false;
        }
        if (!isWithinLimitUpEntryWindow(time, orderBook.getLastLimitUptime())) {
            return false;
        }

        TickNode largestBuyOrder = orderBook.buyMaxOrder;
        if (largestBuyOrder.getPrice() != 0
                && largestBuyOrder.getPrice() == limitUpPrice
                && sealChangePercent >= 0) {
            int largeOrderRule = matchLargeOrderRule(
                    marketValue, limitUpPrice, largestBuyOrder.getQuantity());
            if (largeOrderRule != 0) {
                fillLargeOrderRecord(orderBook, ruleRecord, largeOrderRule);
                return true;
            }
        }

        if (!isNormalLimitUpOrderEligible(
                marketValue, limitUpBuyAmount, lastPrice, limitUpPrice, time)) {
            return false;
        }

        int lastLimitUpMillis = CompactTimeUtil.compactToMillis(orderBook.getLastLimitUptime());
        int updateMillis = CompactTimeUtil.compactToMillis(time);
        String remark = StrUtil.format(
                "正常排板：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，涨停下单时间差:{} ms",
                marketValue, limitUpBuyAmount, turnoverRate, sealChangePercent,
                updateMillis - lastLimitUpMillis);
        log.info(remark);
        ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, RULE_NORMAL_LIMIT_UP_ORDER,
                orderBook.getSymbol(), time, lastPrice, orderBook.getIncrease(), remark);
        return true;
    }

    /**
     * 判断当前换手是否达到历史最大换手或固定换手门槛。
     */
    private static boolean isTurnoverEligible(OrderBook orderBook, double turnoverRate) {
        if (turnoverRate > MAX_TURNOVER_RATE) {
            return false;
        }
        if (turnoverRate >= MIN_NORMAL_TURNOVER_RATE) {
            return true;
        }

        boolean supportsLowerTurnoverEntry = orderBook.getLbcs() == 1
                || orderBook.getAmplitude() > 7
                || orderBook.getLimitUpBreakDepth() > 3
                || orderBook.getTurnover() > HIGH_TRADING_AMOUNT_YUAN;
        if (!supportsLowerTurnoverEntry) {
            return false;
        }

        double historicalMaxTurnover = orderBook.getMaxVolume() * 100.0
                / orderBook.getCirculation();
        return turnoverRate >= historicalMaxTurnover / MAX_TURNOVER_DIVISOR;
    }

    /**
     * 保持原有语义：没有涨停时间时直接通过，否则只接受涨停后 30 秒内的信号。
     */
    private static boolean isWithinLimitUpEntryWindow(int time, int lastLimitUpTime) {
        int lastLimitUpMillis = CompactTimeUtil.compactToMillis(lastLimitUpTime);
        return lastLimitUpMillis == 0
                || CompactTimeUtil.compactToMillis(time) - lastLimitUpMillis
                < RECENT_LIMIT_UP_WINDOW_MILLIS;
    }

    /**
     * 按价格区间匹配大单规则。低价股看股数，中高价股看股数或金额。
     */
    private static int matchLargeOrderRule(long marketValue, int orderPrice, int orderQuantity) {
        if (orderPrice <= TEN_YUAN_PRICE_CENTS) {
            return orderQuantity >= LARGE_LOW_PRICE_QUANTITY_SHARES
                    ? RULE_LARGE_LOW_PRICE_ORDER
                    : 0;
        }

        int orderAmount = orderQuantity / 100 * orderPrice;
        if (marketValue < LARGE_ORDER_MAX_MARKET_VALUE_WAN
                && (orderQuantity >= LARGE_HIGH_PRICE_QUANTITY_SHARES
                || orderAmount > LARGE_HIGH_PRICE_AMOUNT_YUAN)) {
            return RULE_LARGE_HIGH_PRICE_ORDER;
        }
        if (marketValue < MEDIUM_ORDER_MAX_MARKET_VALUE_WAN
                && (orderQuantity >= MEDIUM_HIGH_PRICE_QUANTITY_SHARES
                || orderAmount > MEDIUM_HIGH_PRICE_AMOUNT_YUAN)) {
            return RULE_MEDIUM_HIGH_PRICE_ORDER;
        }
        return 0;
    }

    /**
     * 判断普通排板所需的市值分档封单金额。
     */
    private static boolean isNormalLimitUpOrderEligible(long marketValue,
                                                        long limitUpBuyAmount,
                                                        int lastPrice,
                                                        int limitUpPrice,
                                                        int time) {
        if (marketValue < 90_000) {
            return lastPrice == limitUpPrice;
        }
        if (marketValue < 140_000) {
            return limitUpBuyAmount > 500;
        }

        boolean afterTenThirty = time >= ConstantUtil.TIME_1030;
        if (marketValue < 155_000) {
            return limitUpBuyAmount > (afterTenThirty ? 500 : 1_000);
        }
        if (marketValue < 170_000) {
            return limitUpBuyAmount > (afterTenThirty ? 800 : 2_000);
        }
        if (marketValue < 185_000) {
            return limitUpBuyAmount > (afterTenThirty ? 1_000 : 3_000);
        }
        return limitUpBuyAmount > (afterTenThirty ? 2_000 : 4_500);
    }

    /**
     * 只在大单规则命中后生成说明并填充记录，未命中路径不创建字符串。
     */
    private static void fillLargeOrderRecord(OrderBook orderBook,
                                             RuleRecord ruleRecord,
                                             int ruleCode) {
        long marketValue = orderBook.getInitialMarketValue();
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
        double turnoverRate = orderBook.getTurnoverRate();
        double sealChangePercent = orderBook.getChangePercent();
        int orderQuantity = orderBook.buyMaxOrder.getQuantity();
        String remark;
        if (ruleCode == RULE_LARGE_HIGH_PRICE_ORDER) {
            remark = StrUtil.format(
                    "大单扫板：市值中大 - 金额与数量都要大 ，大单扫板跟随；条件：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，大单委托数量 {}",
                    marketValue, limitUpBuyAmount, turnoverRate, sealChangePercent, orderQuantity);
        } else if (ruleCode == RULE_MEDIUM_HIGH_PRICE_ORDER) {
            remark = StrUtil.format(
                    "大单扫板：市值中小 - 金额与数量也中等 价格中大单扫板跟随；条件：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，大单委托数量 {}",
                    marketValue, limitUpBuyAmount, turnoverRate, sealChangePercent, orderQuantity);
        } else {
            remark = StrUtil.format(
                    "大单扫板：低价股 - 要求必须是 8888 以上，大单扫板跟随；条件：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，大单委托数量 {}",
                    marketValue, limitUpBuyAmount, turnoverRate, sealChangePercent, orderQuantity);
        }

        log.info(remark);
        ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, ruleCode, orderBook.getSymbol(),
                orderBook.getTime(), orderBook.getLastPrice(), orderBook.getIncrease(), remark);
    }
}
