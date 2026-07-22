package com.compoundwonder.strategy.sell.five_to_six;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 5 板、今日 5 进 6、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>只保留最新普通首板基准 Run 32 实际触发的规则，每条规则均标注代表股票、
 * 代码、卖出日期和时间。</p>
 */
@Slf4j
public final class FiveToSixNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        long marketValue = market.getInitialMarketValue();
        int lbcs = market.getLbcs();
        int time = market.getTime();
        int status = market.getStatus();
        double turnover = market.getTurnoverRate();
        double changePercent = market.getChangePercent();
        long limitUpBuyAmount = market.getLimitUpBuyAmount();
        long lastSealAmount = market.getLastSealAmount();
        double openIncrease = market.getOpenIncrease();
        double yesterdayTurnover = market.getYesterdayTurnover();
        double amplitude = market.getAmplitude();

        double maxTurnover = maxTurnover(marketValue);

        // 真实样本：金奥博（002917），卖出 2025-01-16 11:14:58.210，规则 107。
        if (turnover > maxTurnover - 5
                && isLimitUp(status)
                && lbcs <= 7
                && time < ConstantUtil.TIME_1330
                && changePercent < -3
                && limitUpBuyAmount < 2_500
                && lbcs >= 5) {
            String remark = StrUtil.format(
                    "早盘暴量换手且封单接近炸板；条件：换手率 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    turnover, changePercent, limitUpBuyAmount);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_MORNING_HIGH_TURNOVER_WEAK_SEAL, remark);
        }

        // 虚拟卖出样本：三木集团（000632），卖出 2025-11-14 09:30:00.000，规则 112。
        if (lbcs >= 5 && lbcs <= 8 && openIncrease >= 7.5
                && (yesterdayTurnover / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            String remark = StrUtil.format(
                    "高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, openIncrease, turnover, yesterdayTurnover, changePercent,
                    limitUpBuyAmount);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING, remark);
        }

        // 真实样本：中水渔业（000798），卖出 2025-11-21 09:30:59.830，规则 113。
        if (isLimitUp(status) && marketValue > 110_000 && amplitude > 17.5 && lbcs < 6) {
            String remark = StrUtil.format(
                    "小市值涨停中振幅过大；条件：振幅 {}%，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    amplitude, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_AMPLITUDE, remark);
        }

        int averageLimitUpHeight = market.getAverageLimitUpHeight();
        // 真实样本：连云港（601008），卖出 2025-04-11 14:34:14.620，规则 116。
        if (isLimitUp(status)
                && (market.getLastLimitUptime() < ConstantUtil.TIME_932 || amplitude < 3)
                && lbcs == averageLimitUpHeight
                && lastSealAmount < 2_500
                && turnover < 25
                && changePercent <= -1.8) {
            String remark = StrUtil.format(
                    "达到近 15 日平均高度后秒板封单减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    averageLimitUpHeight, lbcs, lbcs + 1, marketValue,
                    limitUpBuyAmount, turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL, remark);
        }
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
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
                                 int ruleCode, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), market.getLastPrice(), market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
