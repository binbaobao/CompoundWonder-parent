package com.compoundwonder.strategy.sell.two_to_three;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 2 板、今日 2 进 3、启动流通市值严格小于 119999 万元的卖出策略。
 *
 * <p>这里只保留基准任务 145、147、146 实际命中的规则；每条路径均标注真实股票与
 * 触发日期。</p>
 */
@Slf4j
public final class TwoToThreeSmallCapSellStrategy implements BoardSellStrategy {
    private static final double PROFIT_PROTECTION_THRESHOLD = 4.0D;

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int time = market.getTime();

        // 回测样本：新日股份（603787），2025-06-12。
        if (market.getTurnoverRate() > maxTurnover(time)
                && isLimitUp(market.getStatus())
                && time < ConstantUtil.TIME_14563
                && market.getEmaSealTrend() == -1) {
            String remark = StrUtil.format(
                    " 今日二进三放量炸板 条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%，炸板状态 {}",
                    market.getLbcs() + 1, market.getInitialMarketValue(),
                    market.getLimitUpBuyAmount(), market.getTurnoverRate(),
                    market.getChangePercent(), market.getStatus());
            return match(market, record, RuleConstant.SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK,
                    market.getLastPrice(), remark);
        }
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market,
                                        TradeRuleRecord record) {
        int currentPrice = market.getMinutePriceAt(index);
        int price3 = market.getMinutePriceAt(index - 3);
        int price2 = market.getMinutePriceAt(index - 2);
        int previousPrice = market.getMinutePriceAt(index - 1);

        // 优化样本：奇精机械（603677），2025-03-14；强势时观察，利润回落到 4% 即退出。
        if (price3 > price2 && price2 > previousPrice
                && market.getIncrease() <= PROFIT_PROTECTION_THRESHOLD) {
            String remark = StrUtil.format(
                    "2进3价格连续走弱；条件：连续 3 分钟价格下降，当前涨幅 {}%，利润保护阈值 {}%",
                    market.getIncrease(), PROFIT_PROTECTION_THRESHOLD);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }

        int averagePrice3 = market.getAveragePriceAt(index - 3);
        int averagePrice2 = market.getAveragePriceAt(index - 2);
        int previousAveragePrice = market.getAveragePriceAt(index - 1);

        // 回测样本：双枪科技（001211），2025-12-05；明显弱势仍快速卖出。
        if (averagePrice3 > averagePrice2 && averagePrice2 > previousAveragePrice
                && market.getIncrease() <= PROFIT_PROTECTION_THRESHOLD) {
            String remark = StrUtil.format(
                    "2进3均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%，利润保护阈值 {}%",
                    market.getIncrease(), PROFIT_PROTECTION_THRESHOLD);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }

        // 回测样本：庄园牧场（002910），2025-12-19。
        if (market.getIncrease() < -5 && market.getOpenIncrease() < -5) {
            String remark = StrUtil.format(
                    "2进3 开盘低，而且价格低 是十分弱势的体现，赶快卖掉，当前涨幅 {}%",
                    market.getIncrease());
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    currentPrice, remark);
        }
        return false;
    }

    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    private static double maxTurnover(int time) {
        if (time < ConstantUtil.TIME_1330) {
            return 30.0;
        }
        if (time > ConstantUtil.TIME_1330 && time < ConstantUtil.TIME_1430) {
            return 40.0;
        }
        return 45;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
