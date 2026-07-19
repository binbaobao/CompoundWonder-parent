package com.compoundwonder.strategy.firstboard.trade;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.constant.RuleConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通首板模式的上海早盘集合竞价场景。
 *
 * <p>只处理 09:25 前的隔夜买单和配套撤单。场景类负责原 Handler 中的
 * 派生参数计算、规则判断、备注和规则记录；Handler 只负责交易状态与执行动作。</p>
 */
@Slf4j
final class ShanghaiAuctionBuyEvaluator {

    private ShanghaiAuctionBuyEvaluator() {
    }

    /**
     * 上海集合竞价买入规则。
     *
     * <p>原注释：集合竞价下单只适合小市值股票。当前由选股模式先限定候选范围，
     * 本场景继续沿用原 Handler 的竞价封单判断。</p>
     *
     * @return 命中原规则 2 并完成规则记录填充时返回 {@code true}
     */
    static boolean evaluateBuy(TradeMarketState market, AuctionMarketEvent event,
                               int recordTime, TradeRuleRecord record) {
        int eventTime = event.getTime();
        int price = event.getPrice();
        int limitUpPrice = market.getLimitUpPrice();
        long totalBuyVolume = event.getBuyerOrderId();
        long totalSellVolume = event.getSellerOrderId();
        // 总涨停买 金额 单位 W
        long limitUpBuyAmount = totalBuyVolume / 100L * limitUpPrice / 10_000L;
        long requiredBuyVolume = calculateRequiredBuyVolume(market, totalSellVolume);

        if (eventTime > ConstantUtil.TIME_925 || price != limitUpPrice
                || totalBuyVolume <= requiredBuyVolume / 3) {
            return false;
        }
        boolean sellPressureEligible = totalSellVolume * 100.0 / totalBuyVolume <= 40
                || limitUpBuyAmount > 15_000;
        if (!sellPressureEligible || totalBuyVolume <= requiredBuyVolume) {
            return false;
        }

        double increase = increase(price, market.getClosePrice());
        String remark = StrUtil.format(
                "买入 - 上午早盘竞价 {}，涨停总买占最大成交 {} % 股票代码 {},涨停总买量 {} 手,占流通股:{} %，涨停总买:{} W,",
                eventTime, totalBuyVolume * 100.0 / market.getMaxVolume(), market.getSymbol(),
                totalBuyVolume, totalBuyVolume * 100.0 / market.getCirculation(), limitUpBuyAmount);
        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_BUY, 2, market.getSymbol(),
                recordTime, limitUpPrice, increase, remark);
        return true;
    }

    /**
     * 上海集合竞价撤单规则。竞价价离开涨停价为规则 1，封单不足为规则 2。
     *
     * <p>原注释：如果是下单状态，在 9:19:56:500 之后判断是否撤单，
     * 总涨停买如果小于 4.5% 则撤单。当前观察时段仍由 Handler 的时间常量控制；
     * 实际撤单阈值以本方法保留的最低买量和卖压判断为准。</p>
     */
    static boolean evaluateCancel(TradeMarketState market, AuctionMarketEvent event,
                                  int recordTime, TradeRuleRecord record) {
        int price = event.getPrice();
        int limitUpPrice = market.getLimitUpPrice();
        long totalBuyVolume = event.getBuyerOrderId();
        long totalSellVolume = event.getSellerOrderId();
        long requiredBuyVolume = calculateRequiredBuyVolume(market, totalSellVolume);

        int ruleCode;
        String remark;
        if (price != limitUpPrice) {
            ruleCode = 1;
            remark = StrUtil.format(
                    "撤单 - 早盘竞价 {}，股票代码:{} 竞价 {} 不等于涨停价 {} 直接撤单",
                    event.getTime(), market.getSymbol(), price, limitUpPrice);
        } else if (totalBuyVolume <= requiredBuyVolume
                || totalSellVolume * 100.0 / totalBuyVolume > 40) {
            ruleCode = 2;
            remark = StrUtil.format(
                    "撤单 - 早盘竞价 {}，股票代码:{} 竞价 {} 买单占最大换手 {} % ,占流通股:{} %",
                    event.getTime(), market.getSymbol(), price,
                    totalBuyVolume * 100.0 / market.getMaxVolume(),
                    totalBuyVolume * 100.0 / market.getCirculation());
        } else {
            return false;
        }

        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_CANCEL, ruleCode, market.getSymbol(),
                recordTime, price, increase(price, market.getClosePrice()), remark);
        return true;
    }

    /**
     * 流通值的 5% 或者是最大换手的 20%，谁小用谁，20/5=4，15/5=3，12/5=2.4。
     * 较大启动市值沿用原逻辑，在流通值 5% 的基础上加当前竞价卖量。
     */
    private static long calculateRequiredBuyVolume(TradeMarketState market, long totalSellVolume) {
        return market.getInitialMarketValue() < 120_000
                ? Math.min(market.getCirculation() / 20, market.getMaxVolume() / 5)
                : market.getCirculation() / 20 + totalSellVolume;
    }

    private static double increase(int price, int closePrice) {
        return (price - closePrice) * 100.0 / closePrice;
    }
}
