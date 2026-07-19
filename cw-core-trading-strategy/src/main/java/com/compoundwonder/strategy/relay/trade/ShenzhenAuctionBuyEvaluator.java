package com.compoundwonder.strategy.relay.trade;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * 连板模式的深圳早盘集合竞价场景。
 *
 * <p>使用逐笔委托识别隔夜买入，并同时处理逐笔与快照撤单。场景类负责
 * 原 Handler 中的派生参数计算、规则判断、备注和规则记录。</p>
 */
@Slf4j
final class ShenzhenAuctionBuyEvaluator {

    private ShenzhenAuctionBuyEvaluator() {
    }

    /**
     * 涨停总买手大于全部总卖手时，全部总卖以涨停价格成交；总卖小于 2500 万，
     * 如果遇到大额委托买单则跟单。原规则 6 的大单买优先于规则 7 的总买量买入。
     *
     * <p>原注释中的候选边界一并保留：集合竞价下单只适合小市值股票。
     * 当前由选股模式负责候选范围，本方法负责逐笔竞价信号。</p>
     */
    static boolean evaluateBuy(TradeMarketState market, AuctionMarketEvent event,
                               int recordTime, long limitUpBuyVolume,
                               long totalSellVolume, TradeRuleRecord record) {
        long circulation = market.getCirculation();
        int limitUpPrice = market.getLimitUpPrice();
        // 总涨停买 金额 单位 W
        long limitUpBuyAmount = limitUpBuyVolume / 100L * limitUpPrice / 10_000L;
        long requiredBuyVolume = calculateRequiredBuyVolume(market, event);

        if (totalSellVolume * 100.0 / limitUpBuyVolume > 40) {
            return false;
        }
        boolean largeOrder = event.getDataType() == 1 && limitUpBuyAmount > 1_500
                && event.getPrice() == limitUpPrice
                && (event.getQuantity() > 900_000
                || event.getQuantity() / 100L * limitUpPrice / 10_000L > 600)
                && limitUpBuyVolume * 100.0 / circulation > 2;

        int ruleCode;
        String remark;
        if (largeOrder) {
            ruleCode = 6;
            remark = StrUtil.format(
                    "买入 - 深圳早盘竞价，大单买 股票代码 {} 时间 :{} 触发单号 OrderId :{} ,买单数:{}，买单金额 {} W",
                    market.getSymbol(), event.getTime(), event.getOrderId(), event.getQuantity(),
                    event.getPrice() / 100L * event.getQuantity() / 10_000L);
        } else if (limitUpBuyVolume > requiredBuyVolume) {
            ruleCode = 7;
            remark = StrUtil.format(
                    "买入 - 深圳早盘竞价  股票代码 {} 时间 :{} 触发单号 OrderId :{},总买超过占最大换手 {} % ,占流通股:{} % ,涨停总买:{}",
                    market.getSymbol(), event.getTime(), event.getOrderId(),
                    limitUpBuyVolume * 100.0 / market.getMaxVolume(),
                    event.getBuyerOrderId() * 100.0 / circulation, limitUpBuyVolume);
        } else {
            return false;
        }

        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_BUY, ruleCode, market.getSymbol(),
                recordTime, event.getPrice(), increase(event.getPrice(), market.getClosePrice()), remark);
        return true;
    }

    /**
     * 如果是下单状态，在 9:19:58:500 之后判断是否撤单。总卖大于涨停买、
     * 涨停买小于流通的 4.5%、涨停卖过大或净买量不足时沿用原规则 2 撤单。
     *
     * <p>原注释还记录了“封单如果大于流通市值的 40% 可能是三班组，也撤单”的
     * 待办设想；原代码没有启用该条件，本次迁移同样不新增该业务判断。
     * 实际观察时段由 Handler 的 {@code TIME_91952} 至 {@code TIME_920} 控制。</p>
     */
    static boolean evaluateCancel(TradeMarketState market, AuctionMarketEvent event,
                                  int recordTime, long limitUpBuyVolume,
                                  long totalSellVolume, TradeRuleRecord record) {
        long requiredBuyVolume = calculateRequiredBuyVolume(market, event);
        if (limitUpBuyVolume > requiredBuyVolume
                && totalSellVolume * 100.0 / limitUpBuyVolume <= 40) {
            return false;
        }

        String remark = StrUtil.format(
                "撤单 - 深圳早盘竞价 早盘撤单,股票代码 {} 时间 :{} 总涨停买占最大换手 {} % ,占流通股:{} %   触发单号 OrderId :{}，数据类型:({}) 封单变化：{},涨停总买:{}",
                market.getSymbol(), event.getTime(),
                limitUpBuyVolume * 100.0 / market.getMaxVolume(),
                limitUpBuyVolume * 100.0 / market.getCirculation(), event.getOrderId(),
                event.getDataType() == 1 ? "委托" : "成交", market.getChangePercent(),
                limitUpBuyVolume);
        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_CANCEL, 2, market.getSymbol(),
                recordTime, event.getPrice(), increase(event.getPrice(), market.getClosePrice()), remark);
        return true;
    }

    /** 深圳三秒快照竞价价离开涨停价时，沿用原价格撤单规则 1。 */
    static boolean evaluateSnapshotCancel(TradeMarketState market, AuctionMarketEvent event,
                                          int recordTime, TradeRuleRecord record) {
        if (event.getPrice() == market.getLimitUpPrice()) {
            return false;
        }
        String remark = StrUtil.format(
                "早盘竞价 {}，股票代码:{} 竞价 {} 不等于涨停价{}直接撤单",
                event.getTime(), market.getSymbol(), event.getPrice(), market.getLimitUpPrice());
        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_CANCEL, 1, market.getSymbol(),
                recordTime, event.getPrice(), increase(event.getPrice(), market.getClosePrice()), remark);
        return true;
    }

    /**
     * 流通值的 5% 或者是最大换手的 20%，谁小用谁，20/5=4，15/5=3，12/5=2.4。
     * 较大启动市值严格沿用原 Handler，在流通值 5% 的基础上加当前事件的
     * {@code sellerOrderId}；逐笔事件中该字段不等同于订单簿累计卖量，迁移时不能替换。
     */
    private static long calculateRequiredBuyVolume(TradeMarketState market,
                                                   AuctionMarketEvent event) {
        return market.getInitialMarketValue() < 120_000
                ? Math.min(market.getCirculation() / 20, market.getMaxVolume() / 5)
                : market.getCirculation() / 20 + event.getSellerOrderId();
    }

    private static double increase(int price, int closePrice) {
        return (price - closePrice) * 100.0 / closePrice;
    }
}
