package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.orderbook.TradeStaticFacts;

import java.util.Set;

/**
 * 单只股票、单个交易日的预编译交易规则模板。
 *
 * <p>模板在订单簿会话初始化时按模式、昨日板高和启动市值解析一次。Handler
 * 只调用对应市场阶段的执行器，不再在每条行情上重复进行模式和卖出场景分发。</p>
 *
 * <p>六个执行器都是必填且应为无状态对象；不支持某个阶段时应通过
 * {@link #triggerTypes()} 取消订阅，而不是返回 {@code null}。规则收到的
 * {@link TradeMarketState} 必须是当前 {@code StrategyExecutionSession}，不能是共享市场会话。</p>
 */
public interface TradeExecutionTemplate {

    /** 高频三模式共用的稳定只读触发集合，避免 supports 在每条行情上创建临时 Set。 */
    Set<TradeTriggerType> HIGH_FREQUENCY_TRIGGERS = Set.of(
            TradeTriggerType.OPENING_AUCTION,
            TradeTriggerType.CONTINUOUS_TICK,
            TradeTriggerType.MINUTE_CLOSE,
            TradeTriggerType.CLOSING_AUCTION);

    TradeStaticFacts facts();

    /** 初始化时按静态事实预编译的板位、市值与允许执行时段；正式模板应保存并直接返回结果。 */
    default TradeExecutionProfile executionProfile() {
        return TradeExecutionProfile.from(facts());
    }

    /**
     * 当前模板需要接收的触发类型。以后低频模板可只声明日线或定时触发，避免逐笔分发。
     */
    default Set<TradeTriggerType> triggerTypes() {
        return HIGH_FREQUENCY_TRIGGERS;
    }

    default boolean supports(TradeTriggerType triggerType) {
        return triggerTypes().contains(triggerType);
    }

    /** 上海开盘集合竞价买入及挂单后的撤单。 */
    ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy();

    /** 深圳开盘集合竞价逐笔买入及逐笔/快照撤单。 */
    ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy();

    /** 连续竞价买入、时间门槛及模式控制。 */
    ContinuousBuyExecutor continuousBuy();

    /** 连续竞价盘口卖出。 */
    ContinuousSellExecutor continuousSell();

    /** 分钟快照驱动的均价卖出。 */
    AveragePriceSellExecutor averagePriceSell();

    /** 沪深尾盘集合竞价卖出。 */
    ClosingAuctionSellExecutor closingAuctionSell();

    interface ShanghaiOpeningAuctionBuyExecutor {
        boolean evaluateBuy(TradeMarketState market, AuctionMarketEvent event,
                            long previousBuyVolume, int recordTime,
                            TradeRuleRecord record);

        boolean evaluateCancel(TradeMarketState market, AuctionMarketEvent event,
                               int recordTime, TradeRuleRecord record);
    }

    interface ShenzhenOpeningAuctionBuyExecutor {
        boolean evaluateBuy(TradeMarketState market, AuctionMarketEvent event,
                            int recordTime, long limitUpBuyVolume,
                            long totalSellVolume, TradeRuleRecord record);

        boolean evaluateOrderBookCancel(TradeMarketState market,
                                        AuctionMarketEvent event,
                                        int recordTime,
                                        long limitUpBuyVolume,
                                        long totalSellVolume,
                                        TradeRuleRecord record);

        boolean evaluateSnapshotCancel(TradeMarketState market,
                                       AuctionMarketEvent event,
                                       int recordTime,
                                       long limitUpBuyVolume,
                                       long totalSellVolume,
                                       TradeRuleRecord record);
    }

    interface ContinuousBuyExecutor {
        boolean evaluate(TradeMarketState market, TradeRuleRecord record);

        boolean evaluateCancel(TradeMarketState market);

        boolean shouldEnableFirstBoardTradingMode(TradeMarketState market);

        boolean isTimeAllowed(TradeMarketState market, int time);
    }

    interface ContinuousSellExecutor {
        boolean evaluate(TradeMarketState market, TradeRuleRecord record);
    }

    interface AveragePriceSellExecutor {
        boolean evaluate(int calculateIndex, TradeMarketState market,
                         TradeRuleRecord record);
    }

    interface ClosingAuctionSellExecutor {
        boolean evaluateShanghai(TradeMarketState market, AuctionMarketEvent event,
                                 int recordTime, TradeRuleRecord record);

        boolean evaluateShenzhen(TradeMarketState market, AuctionMarketEvent event,
                                 int recordTime, TradeRuleRecord record);
    }
}
