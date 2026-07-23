package com.compoundwonder.strategy.smallcapfirstboard.trade;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.strategy.trade.FastLimitUpBuyPolicy;
import com.compoundwonder.util.CompactTimeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 连续竞价买入条件评估器。
 *
 * <p>低价股的大单主要看委托股数；中高价股同时看委托股数和委托金额。</p>
 */
@Slf4j
public final class ConditionEvaluatorBuy {

    /**
     * 中高价股的大市值分档大单扫板规则编号。
     */
    private static final int RULE_LARGE_HIGH_PRICE_ORDER = 11;
    /**
     * 中高价股的中市值分档大单扫板规则编号。
     */
    private static final int RULE_MEDIUM_HIGH_PRICE_ORDER = 12;
    /**
     * 10 元及以下低价股的大数量扫板规则编号。
     */
    private static final int RULE_LARGE_LOW_PRICE_ORDER = 13;
    /**
     * 未命中大单规则时，按封单金额分档判断的普通排板规则编号。
     */
    private static final int RULE_NORMAL_LIMIT_UP_ORDER = 14;

    /**
     * 价格单位为分，1000 表示 10 元。
     */
    private static final int TEN_YUAN_PRICE_CENTS = 1_000;

    /**
     * 大市值分档的最低大单委托数量，单位：股。
     */
    private static final int LARGE_HIGH_PRICE_QUANTITY_SHARES = 990_000;
    /**
     * 中市值分档的最低大单委托数量，单位：股。
     */
    private static final int MEDIUM_HIGH_PRICE_QUANTITY_SHARES = 900_000;
    /**
     * 10 元及以下低价股只看数量时的最低委托数量，单位：股。
     */
    private static final int LARGE_LOW_PRICE_QUANTITY_SHARES = 888_800;

    /**
     * 大市值分档的最低大单委托金额，单位：元。
     */
    private static final int LARGE_HIGH_PRICE_AMOUNT_YUAN = 9_000_000;
    /**
     * 中市值分档的最低大单委托金额，单位：元。
     */
    private static final int MEDIUM_HIGH_PRICE_AMOUNT_YUAN = 7_000_000;

    /**
     * 买入规则允许的最大涨停封单金额，单位：万元。
     */
    private static final long MAX_LIMIT_UP_BUY_AMOUNT_WAN = 8_000L;
    /**
     * 规则 11 允许的启动市值上限，单位：万元。
     */
    private static final long LARGE_ORDER_MAX_MARKET_VALUE_WAN = 198_000L;
    /**
     * 规则 12 允许的启动市值上限，单位：万元。
     */
    private static final long MEDIUM_ORDER_MAX_MARKET_VALUE_WAN = 150_000L;

    /**
     * 无需历史量能补充条件即可买入的最低当前换手率，单位：%。
     */
    private static final double MIN_NORMAL_TURNOVER_RATE = 14.5;
    /**
     * 所有买入规则允许的最大当前换手率，单位：%。
     */
    private static final double MAX_TURNOVER_RATE = 40.0;
    /**
     * 低换手买入门槛为“历史最大换手率 ÷ 3.3”。
     */
    private static final double MAX_TURNOVER_DIVISOR = 3;
    /**
     * 允许低换手买入的当日累计成交额补充条件，单位：元。
     */
    private static final long HIGH_TRADING_AMOUNT_YUAN = 250_000_000L;
    /**
     * 最近一次封板后允许产生买入信号的时间窗口，单位：毫秒。
     */
    private static final int RECENT_LIMIT_UP_WINDOW_MILLIS = 30_000;

    private ConditionEvaluatorBuy() {
    }

    /**
     * 根据当前订单簿判断是否触发买入规则。
     *
     * @param orderBook  当前股票订单簿
     * @param ruleRecord 预分配的规则记录，只有规则命中时才会填充
     * @return 命中买入规则返回 {@code true}
     */
    public static boolean evaluate(TradeMarketState orderBook, TradeRuleRecord ruleRecord) {
        // 本轮连板启动时的流通市值，单位：万元。
        long marketValue = orderBook.getInitialMarketValue();
        // 已经定格的分钟累计均价中的最低值，整数价格口径为元乘以 100。
        int minAveragePrice = orderBook.getMinAveragePrice();
        // 最低分钟累计均价相对昨收的涨跌幅，单位：%。
        double minAveragePriceIncrease = orderBook.getMinAveragePriceIncrease();
        // 当日截至当前时刻的累计换手率，单位：%。
        double turnoverRate = orderBook.getTurnoverRate();
        // 最新成交价，单位：分。
        int lastPrice = orderBook.getLastPrice();
        // 当日涨停价，单位：分。
        int limitUpPrice = orderBook.getLimitUpPrice();
        // 当前涨停买单队列的总金额，单位：万元；非涨停状态通常为 0。
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
        // 当前行情时间，紧凑格式 HHmmssSSS。
        int time = orderBook.getTime();
        // 涨停买单数量 EMA 的环比变化率，单位：%；负数表示封单减弱。
        double sealChangePercent = orderBook.getChangePercent();
        int lbcs = orderBook.getLbcs();
        // 如果涨停金额大于 8000w，说明行情数据可能出问题了，还有就是量化集中封涨停，排也排不到，排到就是坑
        if (limitUpBuyAmount > MAX_LIMIT_UP_BUY_AMOUNT_WAN) {
            return false;
        }
        if (lastPrice != limitUpPrice) {
            return false;
        }

        //判断当前换手是否达到历史最大换手或固定换手门槛。
        if (!isTurnoverEligible(orderBook, turnoverRate)) {
            return false;
        }
        if (!isWithinLimitUpEntryWindow(time, orderBook.getLastLimitUptime())) {
            return false;
        }
        // 开盘比较高，但是最低比-3还低，就是比较弱，就不打板
        if (orderBook.getOpenIncrease() > 0 && orderBook.getLowPriceIncrease() < 0 && orderBook.getOpenIncrease() - orderBook.getLowPriceIncrease() > 3) {
            return false;
        }
        // 如果是首板不允许最低点小于-1.5
        // 首板只是补充交易模式，不能接加速，只做换手充分，经历过程分歧的，不然特别容易炸板
        // 首板如果跌入深水也容易第二天走弱
        if (orderBook.getMinAveragePriceIncrease() < -3.5 ) {
            return false;
        }
        if (orderBook.getStatus() > 20) {
            return false;
        }
        // 10点钟不打回封
//        if (time <= ConstantUtil.TIME_1000 && orderBook.getStatus() > 3) {
//            return false;
//        }

        if (orderBook.getAmplitude() < 4.5 && time < ConstantUtil.TIME_935) {
            return false;
        }
        // 开的高不能封板太快,也不大
        if (orderBook.getOpenIncrease() > 3.5  && time < ConstantUtil.TIME_935) {
            return false;
        }
        if (orderBook.getOpenIncrease() > 6.5  && time < ConstantUtil.TIME_1000) {//  美能能源 2025-11-26
            return false;
        }

        if (FastLimitUpBuyPolicy.shouldReject(orderBook)) {
            return false;
        }

        // 当前仍留在订单簿中的最大单笔买委托，不代表历史已成交或已撤销委托。
        // 上海重新组合计算接到或者成交的委托，深圳直接就是本次接到的委托买。后续买入判断是否是大单
        if (orderBook.getLargestBuyOrderPrice() == limitUpPrice && sealChangePercent >= 0) {
            // 由启动市值、涨停价和最大买单股数组合匹配出的规则编号；0 表示未命中。
            int largeOrderRule = matchLargeOrderRule(marketValue, limitUpPrice, orderBook.getLargestBuyOrderQuantity());
            if (largeOrderRule != 0) {
                fillLargeOrderRecord(orderBook, ruleRecord, largeOrderRule);
                return true;
            }
        }


        // 最近一次封板时间转换后的当日毫秒值。
        int lastLimitUpMillis = CompactTimeUtil.compactToMillis(orderBook.getLastLimitUptime());
        // 当前行情时间转换后的当日毫秒值。
        int updateMillis = CompactTimeUtil.compactToMillis(time);
        String remark = StrUtil.format(
                "正常排板：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，涨停下单时间差:{} ms",
                marketValue, limitUpBuyAmount, turnoverRate, sealChangePercent,
                updateMillis - lastLimitUpMillis);
        log.info(remark + orderBook.getStatus());
        ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, RULE_NORMAL_LIMIT_UP_ORDER,
                orderBook.getSymbol(), time, lastPrice, orderBook.getIncrease(), remark);
        return true;
    }

    /**
     * 判断当前换手是否达到历史最大换手或固定换手门槛。
     */
    private static boolean isTurnoverEligible(TradeMarketState orderBook, double turnoverRate) {
        if (turnoverRate > MAX_TURNOVER_RATE) {
            return false;
        }
        if (turnoverRate >= MIN_NORMAL_TURNOVER_RATE  && turnoverRate >= orderBook.getMaxHs() / MAX_TURNOVER_DIVISOR) {
            return true;
        }
        return false;
    }

    /**
     * 保持原有语义：没有涨停时间时直接通过，否则只接受涨停后 30 秒内的信号。
     */
    private static boolean isWithinLimitUpEntryWindow(int time, int lastLimitUpTime) {
        // 紧凑时间转当日毫秒值；0 表示尚未记录过封板时间。
        int lastLimitUpMillis = CompactTimeUtil.compactToMillis(lastLimitUpTime);
        return lastLimitUpMillis == 0
                || CompactTimeUtil.compactToMillis(time) - lastLimitUpMillis
                < RECENT_LIMIT_UP_WINDOW_MILLIS;
    }

    /**
     * 按价格区间匹配大单规则。低价股看股数，中高价股看股数或金额。
     * 包内深圳集合竞价大单规则直接复用本方法，保证早盘竞价与盘中打板阈值一致。
     */
    static int matchLargeOrderRule(long marketValue, int orderPrice, int orderQuantity) {
        if (orderPrice <= TEN_YUAN_PRICE_CENTS) {
            return orderQuantity >= LARGE_LOW_PRICE_QUANTITY_SHARES
                    ? RULE_LARGE_LOW_PRICE_ORDER
                    : 0;
        }

        // 股数先除以 100，再乘以“分”为单位的价格，结果单位为元。
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
     * 只在大单规则命中后生成说明并填充记录，未命中路径不创建字符串。
     */
    private static void fillLargeOrderRecord(TradeMarketState orderBook,
                                             TradeRuleRecord ruleRecord,
                                             int ruleCode) {
        // 本轮连板启动时的流通市值，单位：万元。
        long marketValue = orderBook.getInitialMarketValue();
        // 当前涨停买单队列总金额，单位：万元。
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount();
        // 当日截至当前时刻的累计换手率，单位：%。
        double turnoverRate = orderBook.getTurnoverRate();
        // 涨停买单数量 EMA 的环比变化率，单位：%。
        double sealChangePercent = orderBook.getChangePercent();
        // 当前仍在订单簿中的最大单笔买委托数量，单位：股。
        int orderQuantity = orderBook.getLargestBuyOrderQuantity();
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
