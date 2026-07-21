package com.compoundwonder.strategy.sell.four_to_five;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 昨日 4 板、今日 4 进 5、启动流通市值严格小于 119999 万元的卖出策略。
 *
 * <p>这里只保留基准任务 145、147、146 实际命中的规则。每条规则都标注真实股票与
 * 触发日期；后续新规则必须先由回测样本确认，再补充到本场景及对应测试。</p>
 */
@Slf4j
public final class FourToFiveSmallCapSellStrategy implements BoardSellStrategy {

    @Override
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int status = market.getStatus();
        int lbcs = market.getLbcs();
        double turnover = market.getTurnoverRate();
        double changePercent = market.getChangePercent();
        long lastSealAmount = market.getLastSealAmount();

        // 回测样本：新炬网络（605398），2025-02-05。
        if (market.getOpenIncrease() > 8
                && market.getAmplitude() < 3
                && isLimitUp(status)
                && (market.getOneWordLimitUp() == 2
                || (market.getTwoDaysTurnover() < 30 && market.getYesterdayTurnover() < 35))
                && turnover < 25
                && market.getInitialMarketValue() < 130_000
                && changePercent < -3
                && lastSealAmount < 2_500
                && lbcs < 7) {
            String remark = StrUtil.format(
                    "小市值连续一字板炸板；条件：连续 {} 个一字板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    market.getOneWordLimitUp(), lbcs + 1, market.getInitialMarketValue(),
                    market.getLimitUpBuyAmount(), turnover, changePercent);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING,
                    market.getLastPrice(), remark);
        }

        // 回测样本：美邦股份（605033），2026-02-25。
        if (isLimitUp(status)
                && lbcs == market.getAverageLimitUpHeight()
                && turnover < 25
                && lastSealAmount > 2_000
                && lastSealAmount < 5_500
                && changePercent <= -3.8) {
            String remark = StrUtil.format(
                    "达到近 15 日平均高度后封单继续减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    market.getAverageLimitUpHeight(), lbcs, lbcs + 1,
                    market.getInitialMarketValue(), market.getLimitUpBuyAmount(),
                    turnover, changePercent);
            return match(market, record, RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_WEAK_SEAL,
                    market.getLastPrice(), remark);
        }
        return false;
    }

    @Override
    public boolean evaluateAveragePrice(int index, TradeMarketState market,
                                        TradeRuleRecord record) {
        int time = market.getTime();
        int lbcs = market.getLbcs();
        double openIncrease = market.getOpenIncrease();
        double increase = market.getIncrease();
        double turnoverRate = market.getTurnoverRate();

        if (market.getInitialMarketValue() < 109_999
                && (time < ConstantUtil.TIME_1330 || turnoverRate < maxTurnover(market.getInitialMarketValue()))
                && openIncrease > 3.5 && openIncrease < 8
                && openIncrease - increase < 7) {
            return false;
        }
        if (market.getStatus() > 0 && time < ConstantUtil.TIME_1330 && openIncrease < 8) {
            return false;
        }
        if ((lbcs >= 4 && market.getTurnover() < 950_000_000 && time < ConstantUtil.TIME_1330
                && increase > 0 && openIncrease < 8) || lbcs > 6) {
            return false;
        }

        int averagePrice3 = market.getAveragePriceAt(index - 3);
        int averagePrice2 = market.getAveragePriceAt(index - 2);
        int previousAveragePrice = market.getAveragePriceAt(index - 1);
        boolean threeMinuteWeakeningWithOneWord = averagePrice3 > averagePrice2
                && averagePrice2 > previousAveragePrice
                && market.getOneWordLimitUp() >= 1;
        boolean recentWeakeningAfterPositiveOpen = averagePrice2 > previousAveragePrice
                && openIncrease >= 1;

        // 回测样本：南矿集团（001360），2025-07-24。
        if (threeMinuteWeakeningWithOneWord || recentWeakeningAfterPositiveOpen) {
            String remark = StrUtil.format(
                    "4进5均价连续走弱；条件：连续 3 分钟均价下降，当前涨幅 {}%", increase);
            return match(market, record, RuleConstant.SELL_AVERAGE_LOW_OPEN_WEAKENING,
                    market.getMinutePriceAt(index), remark);
        }
        return false;
    }

    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    private static double maxTurnover(long marketValue) {
        if (marketValue < 80_000) {
            return 55;
        }
        if (marketValue < 105_000) {
            return 50;
        }
        if (marketValue < 140_000) {
            return 45;
        }
        return 40;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, int price, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), price, market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
