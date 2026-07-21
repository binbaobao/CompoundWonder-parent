package com.compoundwonder.strategy.sell.five_to_six;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 5 板、今日 5 进 6、启动流通市值严格小于 119999 万元的卖出策略。
 *
 * <p>仅保留回测 145、147、146 中实际触发过的卖出场景。</p>
 */
@Slf4j
public final class FiveToSixSmallCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        double openIncrease = market.getOpenIncrease();
        double turnover = market.getTurnoverRate();
        double yesterdayTurnover = market.getYesterdayTurnover();
        double changePercent = market.getChangePercent();

        if (lbcs >= 5 && lbcs <= 8 && openIncrease >= 7.5
                && (yesterdayTurnover / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            // 回测样本：王力安防（605268），2026-03-11；深华发A（000020），2026-03-23。
            String remark = StrUtil.format(
                    "高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, openIncrease, turnover, yesterdayTurnover, changePercent,
                    market.getLimitUpBuyAmount());
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING,
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
