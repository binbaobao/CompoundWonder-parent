package com.compoundwonder.strategy.sell.seven_to_eight;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 7 板、今日 7 进 8、启动流通市值大于等于 119999 万元的卖出策略。
 *
 * <p>最新普通首板基准 Run 32 只确认一个盘口卖出场景。</p>
 */
@Slf4j
public final class SevenToEightNormalCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        double turnover = market.getTurnoverRate();
        double yesterdayTurnover = market.getYesterdayTurnover();
        double changePercent = market.getChangePercent();

        // 真实样本：津药药业（600488），卖出 2026-04-08 09:42:27.270，规则 112。
        if (lbcs >= 5 && lbcs <= 8
                && market.getOpenIncrease() >= 7.5
                && (yesterdayTurnover / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            String remark = StrUtil.format(
                    "高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, market.getOpenIncrease(), turnover, yesterdayTurnover,
                    changePercent, market.getLimitUpBuyAmount());
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING, remark);
        }
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        return false;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), market.getLastPrice(), market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
