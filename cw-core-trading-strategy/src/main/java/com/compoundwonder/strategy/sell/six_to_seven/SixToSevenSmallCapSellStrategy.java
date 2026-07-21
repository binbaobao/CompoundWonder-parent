package com.compoundwonder.strategy.sell.six_to_seven;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 6 板、今日 6 进 7、启动流通市值严格小于 119999 万元的卖出策略。
 *
 * <p>仅保留回测 145、147、146 中实际触发过的卖出场景。</p>
 */
@Slf4j
public final class SixToSevenSmallCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        int todayBoard = lbcs + 1;
        int averageLimitUpHeight = market.getAverageLimitUpHeight();
        int heightGap = todayBoard - averageLimitUpHeight;
        double openIncrease = market.getOpenIncrease();
        double turnover = market.getTurnoverRate();
        double yesterdayTurnover = market.getYesterdayTurnover();
        double changePercent = market.getChangePercent();

        if (lbcs >= 5 && lbcs <= 8 && openIncrease >= 7.5
                && (yesterdayTurnover / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            // 回测样本：蓝丰生化（002513），2025-09-30。
            String remark = StrUtil.format(
                    "高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, openIncrease, turnover, yesterdayTurnover, changePercent,
                    market.getLimitUpBuyAmount());
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING,
                    market.getLastPrice(), remark);
        }

        if (lbcs >= 5 && lbcs < 7 && market.getInitialMarketValue() < 130_000
                && market.getThreeDaysTurnover() <= 16.6
                && changePercent < -2 && market.getLastSealAmount() < 2_500) {
            // 回测样本：国芳集团（601086），2025-04-14；兴业股份（603928），2025-01-27。
            String remark = StrUtil.format(
                    "高位连板缩量板炸板；条件：今日 {} 板，近 15 日平均高度 {} 板，高度差 {} 板，启动市值 {} 万，三日换手 {}%，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    todayBoard, averageLimitUpHeight, heightGap, market.getInitialMarketValue(),
                    market.getThreeDaysTurnover(), market.getLimitUpBuyAmount(), turnover,
                    changePercent);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_LOW_TURNOVER,
                    market.getLastPrice(), remark);
        }
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record, int ruleCode,
                                 int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
