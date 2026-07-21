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

    /** 启动流通市值必须严格小于 20 亿元；字段单位为万元。 */
    private static final int MAX_START_MARKET_VALUE_EXCLUSIVE = 200_000;
    /** 规则 2 的启动流通市值必须严格小于 16 亿元；字段单位为万元。 */
    private static final int RULE_TWO_MAX_START_MARKET_VALUE_EXCLUSIVE = 160_000;
    private static final int RULE_ABSOLUTE_STRENGTH = 2;
    private static final int RULE_SNAPSHOT_GROWTH = 3;

    private ShanghaiAuctionBuyEvaluator() {
    }

    /**
     * 上海集合竞价买入规则。
     *
     * <p>启动市值小于 16 亿元时，规则 2 判断当前封单绝对强度；启动市值小于
     * 20 亿元时，规则 3 判断相邻快照封单突然增长。两条规则同时命中时优先记录
     * 规则 2。启动市值达到 16 亿元且原始绝对强度成立时，不能降级为规则 3。</p>
     *
     * @return 命中规则 2 或规则 3 并完成规则记录填充时返回 {@code true}
     */
    static boolean evaluateBuy(TradeMarketState market, AuctionMarketEvent event,
                               long previousBuyVolume, int recordTime,
                               TradeRuleRecord record) {
        int eventTime = event.getTime();
        int price = event.getPrice();
        int limitUpPrice = market.getLimitUpPrice();
        long currentBuyVolume = event.getBuyerOrderId();
        long matchedSellVolume = event.getSellerOrderId();

        if (eventTime > ConstantUtil.TIME_925
                || market.getInitialMarketValue() >= MAX_START_MARKET_VALUE_EXCLUSIVE
                || price != limitUpPrice) {
            return false;
        }

        long requiredBuyVolume = calculateRequiredBuyVolume(market);
        boolean absoluteStrength = hasAbsoluteStrength(
                currentBuyVolume, matchedSellVolume, requiredBuyVolume);
        if (absoluteStrength && market.getInitialMarketValue()
                >= RULE_TWO_MAX_START_MARKET_VALUE_EXCLUSIVE) {
            return false;
        }
        boolean snapshotGrowth = hasSnapshotGrowth(
                currentBuyVolume, previousBuyVolume, market.getCirculation());
        if (!absoluteStrength && !snapshotGrowth) {
            return false;
        }

        int ruleCode = absoluteStrength ? RULE_ABSOLUTE_STRENGTH : RULE_SNAPSHOT_GROWTH;
        long limitUpBuyAmount = currentBuyVolume / 100L * limitUpPrice / 10_000L;
        double increase = increase(price, market.getClosePrice());
        String remark = absoluteStrength
                ? absoluteStrengthRemark(market, eventTime, currentBuyVolume,
                matchedSellVolume, requiredBuyVolume, limitUpBuyAmount)
                : snapshotGrowthRemark(market, eventTime, previousBuyVolume,
                currentBuyVolume, limitUpBuyAmount);
        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_BUY, ruleCode, market.getSymbol(),
                recordTime, limitUpPrice, increase, remark);
        return true;
    }

    /**
     * 上海集合竞价撤单规则。竞价价离开涨停价为规则 1；价格仍在涨停价，但规则 2
     * 的封单绝对强度不能继续达成为规则 2。通过快照增长规则买入后也使用同一标准续单。
     */
    static boolean evaluateCancel(TradeMarketState market, AuctionMarketEvent event,
                                  int recordTime, TradeRuleRecord record) {
        int price = event.getPrice();
        int limitUpPrice = market.getLimitUpPrice();
        long currentBuyVolume = event.getBuyerOrderId();
        long matchedSellVolume = event.getSellerOrderId();
        long requiredBuyVolume = calculateRequiredBuyVolume(market);

        int ruleCode;
        String remark;
        if (price != limitUpPrice) {
            ruleCode = 1;
            remark = StrUtil.format(
                    "撤单 - 早盘竞价 {}，股票代码:{} 竞价 {} 不等于涨停价 {} 直接撤单",
                    event.getTime(), market.getSymbol(), price, limitUpPrice);
        } else if (!hasAbsoluteStrength(
                currentBuyVolume, matchedSellVolume, requiredBuyVolume)) {
            ruleCode = 2;
            remark = StrUtil.format(
                    "撤单 - 上海早盘竞价 {}，股票代码:{} 封单绝对强度不足，涨停买量:{}，最低要求:{}，已撮合卖量:{}，要求卖量严格小于买量40%",
                    event.getTime(), market.getSymbol(), currentBuyVolume,
                    requiredBuyVolume, matchedSellVolume);
        } else {
            return false;
        }

        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_CANCEL, ruleCode, market.getSymbol(),
                recordTime, price, increase(price, market.getClosePrice()), remark);
        return true;
    }

    /**
     * 涨停买量最低要求取“流通股本 5%”与“历史最大成交量 20%”中的较小值。
     */
    private static long calculateRequiredBuyVolume(TradeMarketState market) {
        return Math.min(market.getCirculation() / 20, market.getMaxVolume() / 5);
    }

    /** 买量必须严格超过最低要求，已撮合卖量必须严格小于买量的 40%。 */
    private static boolean hasAbsoluteStrength(long currentBuyVolume,
                                               long matchedSellVolume,
                                               long requiredBuyVolume) {
        return currentBuyVolume > requiredBuyVolume
                && matchedSellVolume * 100L < currentBuyVolume * 40L;
    }

    /** -1 表示首张快照；从 0 增长也属于有效比较，增幅需严格超过流通股本 1.5%，且总买量超过 3%。 */
    private static boolean hasSnapshotGrowth(long currentBuyVolume,
                                             long previousBuyVolume,
                                             long circulation) {
        return previousBuyVolume >= 0
                && currentBuyVolume > previousBuyVolume
                && (currentBuyVolume - previousBuyVolume) * 1_000L > circulation * 15L
                && currentBuyVolume * 100L > circulation * 3L;
    }

    private static String absoluteStrengthRemark(TradeMarketState market, int eventTime,
                                                 long currentBuyVolume, long matchedSellVolume,
                                                 long requiredBuyVolume, long limitUpBuyAmount) {
        return StrUtil.format(
                "买入 - 上海早盘竞价封单绝对强度，时间:{}，股票代码:{}，涨停买量:{}，最低要求:{}，已撮合卖量:{}，卖量占买量:{}%，涨停买金额:{}W",
                eventTime, market.getSymbol(), currentBuyVolume, requiredBuyVolume,
                matchedSellVolume, matchedSellVolume * 100.0 / currentBuyVolume,
                limitUpBuyAmount);
    }

    private static String snapshotGrowthRemark(TradeMarketState market, int eventTime,
                                               long previousBuyVolume, long currentBuyVolume,
                                               long limitUpBuyAmount) {
        long growthVolume = currentBuyVolume - previousBuyVolume;
        return StrUtil.format(
                "买入 - 上海早盘竞价封单增长，时间:{}，股票代码:{}，上次买量:{}，本次买量:{}，增加:{}，增量占流通股:{}%，本次买量占流通股:{}%，涨停买金额:{}W",
                eventTime, market.getSymbol(), previousBuyVolume, currentBuyVolume,
                growthVolume, growthVolume * 100.0 / market.getCirculation(),
                currentBuyVolume * 100.0 / market.getCirculation(), limitUpBuyAmount);
    }

    private static double increase(int price, int closePrice) {
        return (price - closePrice) * 100.0 / closePrice;
    }
}
