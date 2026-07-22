package com.compoundwonder.strategy.sell.six_to_seven;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 6 板、今日 6 进 7、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 只确认一个盘口卖出场景。</p>
 */
@Slf4j
public final class SixToSevenNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        double maxTurnover = maxTurnover(marketValue);

        // 真实样本：易明医药（002826），卖出 2025-06-12 11:16:28.840，规则 104。
        if (turnover > maxTurnover - 5
                && isLimitUp(market.getStatus())
                && market.getLbcs() <= 7
                && market.getTime() < ConstantUtil.TIME_14563
                && turnover > maxTurnover
                && market.getChangePercent() < -3
                && market.getLastSealAmount() < 2_500
                && market.getLbcs() >= 5) {
            String remark = StrUtil.format(
                    "高换手后封单减少炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，换手阈值 {}%",
                    market.getLbcs() + 1, marketValue, market.getLimitUpBuyAmount(),
                    turnover, market.getChangePercent(), maxTurnover);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_SEAL_WEAKENING, remark);
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
