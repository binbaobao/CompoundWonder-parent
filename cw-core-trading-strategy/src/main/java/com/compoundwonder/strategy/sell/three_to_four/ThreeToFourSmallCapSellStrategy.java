package com.compoundwonder.strategy.sell.three_to_four;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 3 板、今日 3 进 4、启动流通市值严格小于 119999 万元的卖出策略。
 *
 * <p>仅保留回测 145、147、146 中实际触发过的卖出场景。</p>
 */
@Slf4j
public final class ThreeToFourSmallCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        int lastPrice = market.getLastPrice();
        long limitUpBuyAmount = market.getLimitUpBuyAmount();
        int status = market.getStatus();
        double changePercent = market.getChangePercent();
        double twoDaysTurnover = market.getTwoDaysTurnover();
        int oneWordLimitUp = market.getOneWordLimitUp();
        long lastSealAmount = market.getLastSealAmount();
        int time = market.getTime();
        int lbcs = market.getLbcs();
        double amplitude = market.getAmplitude();
        double openIncrease = market.getOpenIncrease();
        double yesterdayTurnover = market.getYesterdayTurnover();
        double increase = market.getIncrease();

        if (turnover > 50 && isLimitUp(status) && time < ConstantUtil.TIME_14563) {
            // 回测样本：南京港（002040），2025-05-16。
            String remark = StrUtil.format(
                    " 今日二进三放量炸板 条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}",
                    lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent, status);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK,
                    lastPrice, remark);
        }

        if (openIncrease > 8 && amplitude < 3 && isLimitUp(status)
                && (oneWordLimitUp == 2 || (twoDaysTurnover < 30 && yesterdayTurnover < 35))
                && turnover < 25 && marketValue < 130_000
                && changePercent < -3 && lastSealAmount < 2_500 && lbcs < 7) {
            // 回测样本：德创环保（603177），2025-01-17。
            String remark = StrUtil.format(
                    "小市值连续一字板炸板；条件：连续 {} 个一字板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    oneWordLimitUp, lbcs + 1, marketValue, limitUpBuyAmount, turnover, changePercent);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING,
                    lastPrice, remark);
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

    private static boolean match(TradeMarketState market, TradeRuleRecord record, int ruleCode,
                                 int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
