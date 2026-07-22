package com.compoundwonder.strategy.sell;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通市值板位没有本场景直接样本时使用的跨板位安全兜底。
 *
 * <p>这里只复用 Run 32 已由其他普通市值板位真实样本确认、且条件本身允许跨板位
 * 运行的盘口规则。它防止未覆盖板位变成永久空策略，但不恢复没有样本依据的旧分支。</p>
 */
@Slf4j
public final class NormalCapCrossBoardFallbackSellRules {

    private NormalCapCrossBoardFallbackSellRules() {
    }

    /** 4 进 5 只启用不会提前截断中水渔业高位收益的平均高度兜底。 */
    public static boolean evaluateFourToFive(TradeMarketState market, TradeRuleRecord record) {
        return evaluateAverageHeightFastSeal(market, record);
    }

    /** 8 进 9 按原有高位规则优先级评估三个已确认兜底。 */
    public static boolean evaluateEightToNine(TradeMarketState market, TradeRuleRecord record) {
        return evaluateAfternoonShrinkingBoard(market, record)
                || evaluateHighBoardGapShrinking(market, record)
                || evaluateAverageHeightFastSeal(market, record);
    }

    /** 9 板以上不再满足规则 112 的既有上限，只评估规则 102 和 116。 */
    public static boolean evaluateHighBoard(TradeMarketState market, TradeRuleRecord record) {
        return evaluateAfternoonShrinkingBoard(market, record)
                || evaluateAverageHeightFastSeal(market, record);
    }

    private static boolean evaluateAfternoonShrinkingBoard(TradeMarketState market,
                                                            TradeRuleRecord record) {
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        double changePercent = market.getChangePercent();

        // 真实基准来源：苏豪弘业（600128），卖出 2025-01-13 14:17:42.980，规则 102。
        if (changePercent < -3
                && marketValue > 130_000
                && market.getTime() > ConstantUtil.TIME_1130
                && market.getTime() < ConstantUtil.TIME_14563
                && turnover < 25
                && market.getTwoDaysTurnover() < 25) {
            String remark = StrUtil.format(
                    "前两天缩量板下午炸板；条件：今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    market.getLbcs() + 1, marketValue, market.getLimitUpBuyAmount(),
                    turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD, remark);
        }
        return false;
    }

    private static boolean evaluateHighBoardGapShrinking(TradeMarketState market,
                                                          TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        double turnover = market.getTurnoverRate();
        double changePercent = market.getChangePercent();

        // 真实基准来源：津药药业（600488），卖出 2026-04-08 09:42:27.270，规则 112。
        if (lbcs >= 5
                && lbcs <= 8
                && market.getOpenIncrease() >= 7.5
                && (market.getYesterdayTurnover() / 2 > turnover || turnover < 10)
                && changePercent < -5) {
            String remark = StrUtil.format(
                    "高位连板大高开后缩量炸板；条件：连板 {} 板，开盘涨幅 {}%，当前换手 {}%，昨日换手 {}%，封单变化EMA {}%，涨停封单金额 {} 万",
                    lbcs, market.getOpenIncrease(), turnover, market.getYesterdayTurnover(),
                    changePercent, market.getLimitUpBuyAmount());
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING, remark);
        }
        return false;
    }

    private static boolean evaluateAverageHeightFastSeal(TradeMarketState market,
                                                          TradeRuleRecord record) {
        int lbcs = market.getLbcs();
        long marketValue = market.getInitialMarketValue();
        double turnover = market.getTurnoverRate();
        double changePercent = market.getChangePercent();

        // 真实基准来源：连云港（601008），卖出 2025-04-11 14:34:14.620，规则 116。
        if (isLimitUp(market.getStatus())
                && (market.getLastLimitUptime() < ConstantUtil.TIME_932 || market.getAmplitude() < 3)
                && lbcs == market.getAverageLimitUpHeight()
                && market.getLastSealAmount() < 2_500
                && turnover < 25
                && changePercent <= -1.8) {
            String remark = StrUtil.format(
                    "达到近 15 日平均高度后秒板封单减弱；条件：平均高度 {} 板，昨日连板 {} 板，今日 {} 板，启动市值 {} 万，涨停封单金额 {} 万，换手率 {}%，封单变化EMA {}%",
                    market.getAverageLimitUpHeight(), lbcs, lbcs + 1, marketValue,
                    market.getLimitUpBuyAmount(), turnover, changePercent);
            return match(market, record,
                    RuleConstant.SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL, remark);
        }
        return false;
    }

    private static boolean isLimitUp(int status) {
        return status % 2 == 1;
    }

    private static boolean match(TradeMarketState market, TradeRuleRecord record,
                                 int ruleCode, String remark) {
        record.fill(RuleConstant.TRADING_MODE_SELL, ruleCode, market.getSymbol(),
                market.getTime(), market.getLastPrice(), market.getIncrease(), remark);
        log.info(remark);
        return true;
    }
}
