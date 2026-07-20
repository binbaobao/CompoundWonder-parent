package com.compoundwonder.strategy.firstboard.trade;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通首板模式的深圳早盘集合竞价买入与撤单规则。
 *
 * <p>深圳集合竞价使用逐笔委托、成交和撤单重建完整订单簿，买入有两条彼此独立的
 * 路径：盘口封单绝对强度，或者本次成功入簿的涨停价买方向大单。逐笔事件更新订单簿
 * 后同时承担买入与绝对强度撤单判断；当前数据源约九秒一张的快照不触发买入，但也
 * 能触发价格和绝对强度撤单，作为 Level2 行情延迟时的兜底。</p>
 *
 * <p>两条买入路径都要求启动流通市值严格小于 20 亿元。封单绝对强度的买入与
 * 撤单必须调用同一个判断方法，避免条件漂移后在同一盘口反复买入、撤单。</p>
 */
@Slf4j
final class ShenzhenAuctionBuyEvaluator {

    /** 启动流通市值单位为万元，200000 表示 20 亿元；边界采用严格小于。 */
    private static final int MAX_START_MARKET_VALUE_EXCLUSIVE = 200_000;
    /** 深圳集合竞价单笔大单买入沿用原规则编号 6。 */
    private static final int RULE_LARGE_ORDER = 6;
    /** 深圳集合竞价封单绝对强度买入沿用原规则编号 7。 */
    private static final int RULE_ABSOLUTE_STRENGTH = 7;
    /** 快照竞价价格离开涨停价时使用撤单规则编号 1。 */
    private static final int RULE_PRICE_CANCEL = 1;
    /** 快照价格仍为涨停价、但绝对强度不足时使用撤单规则编号 2。 */
    private static final int RULE_STRENGTH_CANCEL = 2;

    private ShenzhenAuctionBuyEvaluator() {
    }

    /**
     * 判断深圳集合竞价买入。
     *
     * <p>绝对强度是订单簿状态规则，任一有效逐笔事件更新订单簿后都可命中；大单是
     * 单笔事件规则，只有 Handler 已确认本次委托成功入簿、方向为买且有效价格等于
     * 涨停价时，{@code acceptedLimitUpBuyOrder} 才能为 {@code true}。重复委托已经由
     * 订单簿插入结果自然排除，这里不再重复查询订单号。</p>
     *
     * @param limitUpBuyVolume 当前涨停价买队列剩余总量，单位为股
     * @param totalSellVolume 当前所有价格档位卖队列剩余总量，单位为股
     * @return 命中大单或封单绝对强度，并完成规则记录填充时返回 {@code true}
     */
    static boolean evaluateBuy(TradeMarketState market, AuctionMarketEvent event,
                               int recordTime, boolean acceptedLimitUpBuyOrder,
                               long limitUpBuyVolume, long totalSellVolume,
                               TradeRuleRecord record) {
        if (event.getTime() >= ConstantUtil.TIME_925
                || market.getInitialMarketValue() >= MAX_START_MARKET_VALUE_EXCLUSIVE) {
            return false;
        }

        int limitUpPrice = market.getLimitUpPrice();
        long requiredBuyVolume = calculateRequiredBuyVolume(market);
        boolean absoluteStrength = hasAbsoluteStrength(
                limitUpBuyVolume, totalSellVolume, requiredBuyVolume);
        int continuousLargeOrderRule = acceptedLimitUpBuyOrder
                ? ConditionEvaluatorBuy.matchLargeOrderRule(
                        market.getInitialMarketValue(), limitUpPrice, event.getQuantity())
                : 0;

        if (continuousLargeOrderRule == 0 && !absoluteStrength) {
            return false;
        }

        int ruleCode;
        String remark;
        if (continuousLargeOrderRule != 0) {
            ruleCode = RULE_LARGE_ORDER;
            long orderAmountWan = event.getQuantity() / 100L * limitUpPrice / 10_000L;
            remark = StrUtil.format(
                    "买入 - 深圳早盘竞价涨停大单，股票代码:{}，时间:{}，订单号:{}，委托量:{}，委托金额:{}W，复用连续竞价大单档位规则:{}",
                    market.getSymbol(), event.getTime(), event.getOrderId(),
                    event.getQuantity(), orderAmountWan, continuousLargeOrderRule);
        } else {
            ruleCode = RULE_ABSOLUTE_STRENGTH;
            long estimatedMatchedVolume = totalSellVolume;
            long estimatedRemainingBuyVolume = limitUpBuyVolume - estimatedMatchedVolume;
            long limitUpBuyAmountWan = limitUpBuyVolume / 100L * limitUpPrice / 10_000L;
            remark = StrUtil.format(
                    "买入 - 深圳早盘竞价封单绝对强度，股票代码:{}，时间:{}，涨停买量:{}，最低要求:{}，全价位卖量:{}，预计撮合卖量:{}，预计剩余封单:{}，卖量占买量:{}%，涨停买金额:{}W",
                    market.getSymbol(), event.getTime(), limitUpBuyVolume,
                    requiredBuyVolume, totalSellVolume, estimatedMatchedVolume,
                    estimatedRemainingBuyVolume,
                    totalSellVolume * 100.0 / limitUpBuyVolume,
                    limitUpBuyAmountWan);
        }

        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_BUY, ruleCode, market.getSymbol(),
                recordTime, limitUpPrice,
                increase(limitUpPrice, market.getClosePrice()), remark);
        return true;
    }

    /**
     * 逐笔委托、成交或撤单更新订单簿后判断是否撤销已经挂出的竞价买单。
     *
     * <p>逐笔事件可以比约九秒快照更早暴露封单转弱，因此两者都是撤单触发源。
     * 逐笔事件没有可靠的竞价快照价格，只判断与绝对强度买入完全相同的盘口公式。</p>
     */
    static boolean evaluateOrderBookCancel(TradeMarketState market,
                                           AuctionMarketEvent event,
                                           int recordTime,
                                           long limitUpBuyVolume,
                                           long totalSellVolume,
                                           TradeRuleRecord record) {
        return evaluateAbsoluteStrengthCancel(
                market, event, recordTime, limitUpBuyVolume,
                totalSellVolume, "逐笔订单簿", record);
    }

    /**
     * 使用深圳集合竞价快照判断是否撤单。
     *
     * <p>快照只提供撤单触发时点和竞价价格；涨停买量及全价位卖量来自逐笔数据重建
     * 的订单簿。价格离开涨停价优先记规则 1；价格仍为涨停价但与买入完全相同的
     * 封单绝对强度不再成立时记规则 2。</p>
     */
    static boolean evaluateSnapshotCancel(TradeMarketState market,
                                          AuctionMarketEvent event,
                                          int recordTime,
                                          long limitUpBuyVolume,
                                          long totalSellVolume,
                                          TradeRuleRecord record) {
        int limitUpPrice = market.getLimitUpPrice();
        if (event.getPrice() != limitUpPrice) {
            String remark = StrUtil.format(
                    "撤单 - 深圳早盘竞价价格离开涨停，股票代码:{}，时间:{}，竞价价格:{}，涨停价:{}",
                    market.getSymbol(), event.getTime(), event.getPrice(), limitUpPrice);
            log.info(remark);
            record.fill(RuleConstant.TRADING_MODE_CANCEL, RULE_PRICE_CANCEL,
                    market.getSymbol(), recordTime, event.getPrice(),
                    increase(event.getPrice(), market.getClosePrice()), remark);
            return true;
        }

        return evaluateAbsoluteStrengthCancel(
                market, event, recordTime, limitUpBuyVolume,
                totalSellVolume, "快照", record);
    }

    /**
     * 两种撤单触发源共用的绝对强度撤单实现，避免快照与逐笔路径复制不同边界。
     */
    private static boolean evaluateAbsoluteStrengthCancel(
            TradeMarketState market, AuctionMarketEvent event, int recordTime,
            long limitUpBuyVolume, long totalSellVolume, String triggerSource,
            TradeRuleRecord record) {
        long requiredBuyVolume = calculateRequiredBuyVolume(market);
        if (hasAbsoluteStrength(
                limitUpBuyVolume, totalSellVolume, requiredBuyVolume)) {
            return false;
        }

        String remark = StrUtil.format(
                "撤单 - 深圳早盘竞价封单绝对强度不足，触发源:{}，股票代码:{}，时间:{}，涨停买量:{}，最低要求:{}，全价位卖量:{}，要求涨停买量大于全部卖量且卖量严格小于买量40%",
                triggerSource, market.getSymbol(), event.getTime(),
                limitUpBuyVolume, requiredBuyVolume, totalSellVolume);
        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_CANCEL, RULE_STRENGTH_CANCEL,
                market.getSymbol(), recordTime, event.getPrice(),
                increase(event.getPrice(), market.getClosePrice()), remark);
        return true;
    }

    /** 涨停买量最低要求取流通股本 5% 与近 200 根 K 线最大成交量 20% 中的较小值。 */
    private static long calculateRequiredBuyVolume(TradeMarketState market) {
        return Math.min(market.getCirculation() / 20, market.getMaxVolume() / 5);
    }

    /**
     * 买入和撤单共用的封单绝对强度公式，所有边界均为严格比较。
     *
     * <p>涨停买量大于全部卖量时，集合竞价预计撮合量等于全部卖量；这里比较的是
     * 撮合前涨停买量，预计剩余封单只用于日志，不再次参与最低强度判断。</p>
     */
    private static boolean hasAbsoluteStrength(long limitUpBuyVolume,
                                               long totalSellVolume,
                                               long requiredBuyVolume) {
        return limitUpBuyVolume > totalSellVolume
                && limitUpBuyVolume > requiredBuyVolume
                && totalSellVolume * 100L < limitUpBuyVolume * 40L;
    }

    private static double increase(int price, int closePrice) {
        return (price - closePrice) * 100.0 / closePrice;
    }
}
