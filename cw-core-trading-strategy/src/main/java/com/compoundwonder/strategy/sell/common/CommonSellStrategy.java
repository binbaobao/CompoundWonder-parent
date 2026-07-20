package com.compoundwonder.strategy.sell.common;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 所有板高与市值场景共用的末级卖出规则。
 *
 * <p>场景专属策略没有命中时才执行本类。这里只放不应随板高、市值分支复制的
 * 全局规则，例如周末、节假日风险控制；后续新增公共规则时仍需保持首个命中即返回。</p>
 */
@Slf4j
public final class CommonSellStrategy implements BoardSellStrategy {

    /**
     * 评估公共盘口卖出规则。
     *
     * @param market 当前 Handler 私有订单簿的只读交易视图
     * @param record 调用方预分配的规则记录
     * @return 命中公共卖出规则时返回 {@code true}
     */
    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        int status = market.getStatus();
        int lbcs = market.getLbcs();
        int time = market.getTime();
        int nextTradingDay = market.getNextTradingDay();
        int lastPrice = market.getLastPrice();
        double increase = market.getIncrease();

        // 临近周末或假期时，高位连板已经超过市值档换手阈值，优先落袋。
        double maxTurnover = maxTurnover(marketValue);
        if (isLimitUp(status)
                && lbcs >= 5 && lbcs <= 7
                && time < ConstantUtil.TIME_14563
                && turnover > maxTurnover
                && nextTradingDay >= 2) {
            String remark = StrUtil.format(
                    "临近周末或假期高换手；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}",
                    lbcs + 1, marketValue, market.getLimitUpBuyAmount(), turnover,
                    market.getChangePercent(), nextTradingDay);
            return recordAndLog(market, record, RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_TURNOVER,
                    lastPrice, increase, remark);
        }

        // 三天及以上长假前，高位连板或早盘秒板且换手不足时先锁定利润。
        if (isLimitUp(status)
                && nextTradingDay >= 3
                && (lbcs >= 6 || market.getLastLimitUptime() < ConstantUtil.TIME_931)
                && turnover < 12) {
            String remark = StrUtil.format(
                    "高位连板遇到长假先落袋；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，下个交易日间隔 {}",
                    lbcs + 1, marketValue, market.getLimitUpBuyAmount(), turnover,
                    market.getChangePercent(), nextTradingDay);
            return recordAndLog(market, record, RuleConstant.SELL_LIMIT_UP_HOLIDAY_HIGH_BOARD,
                    lastPrice, increase, remark);
        }
        return false;
    }

    /** 当前没有独立于板高与市值场景的公共分钟均价规则。 */
    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }

    /** 订单簿状态为奇数时表示当前处于涨停封板状态。 */
    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    /** 按启动市值（万元）返回公共假期规则使用的换手率基准上限（%）。 */
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

    private static boolean recordAndLog(TradeMarketState market, TradeRuleRecord record, int ruleCode,
                                        int price, double increase, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, increase, remark);
        log.info(remark);
        return true;
    }
}
