package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplateFactory;
import com.compoundwonder.common.strategy.trade.TradeExecutionProfile;
import com.compoundwonder.strategy.firstboard.trade.FirstBoardBuyStrategy;
import com.compoundwonder.strategy.relay.trade.RelayBuyStrategy;
import com.compoundwonder.strategy.sell.ShanghaiClosingAuctionSellEvaluator;
import com.compoundwonder.strategy.sell.SellStrategyDispatcher;
import com.compoundwonder.strategy.sell.ShenzhenClosingAuctionSellEvaluator;
import com.compoundwonder.strategy.smallcapfirstboard.trade.SmallCapFirstBoardBuyStrategy;

/**
 * 默认交易模板编译器；买入模式只在 {@link #compile} 中解析一次。
 *
 * <p>三个买入策略和统一卖出规则目录均为无状态常驻对象，可被不同股票会话安全复用；
 * 每次编译只创建绑定静态事实及执行时段的轻量模板。</p>
 */
public final class DefaultTradeExecutionTemplateFactory implements TradeExecutionTemplateFactory {

    private final BuyStrategy relayBuy = new RelayBuyStrategy();
    private final BuyStrategy firstBoardBuy = new FirstBoardBuyStrategy();
    private final BuyStrategy smallCapFirstBoardBuy = new SmallCapFirstBoardBuyStrategy();
    private final SellStrategyDispatcher sellCatalog = new SellStrategyDispatcher();

    @Override
    public TradeExecutionTemplate compile(TradeStaticFacts facts) {
        if (facts == null) {
            throw new IllegalArgumentException("静态交易事实不能为空");
        }
        BuyStrategy buy = switch (facts.tradeMode()) {
            case 1 -> relayBuy;
            case 2 -> firstBoardBuy;
            case 3 -> smallCapFirstBoardBuy;
            default -> throw new IllegalStateException("不支持的交易模式: " + facts.tradeMode());
        };
        return compileTemplate(facts, buy, sellCatalog);
    }

    private record CompiledTradeExecutionTemplate(
            TradeStaticFacts facts,
            TradeExecutionProfile executionProfile,
            ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy,
            ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy,
            ContinuousBuyExecutor continuousBuy,
            ContinuousSellExecutor continuousSell,
            AveragePriceSellExecutor averagePriceSell,
            ClosingAuctionSellExecutor closingAuctionSell)
            implements TradeExecutionTemplate {

    }

    private static TradeExecutionTemplate compileTemplate(
            TradeStaticFacts facts, BuyStrategy buy, SellStrategyDispatcher sellRules) {
        // Profile 只负责编译跨交易所约束；沪深竞价市值门槛仍由各自 evaluator 判断。
        TradeExecutionProfile profile = TradeExecutionProfile.from(facts);
        return new CompiledTradeExecutionTemplate(
                facts, profile,
                shanghaiOpeningExecutor(buy, profile),
                shenzhenOpeningExecutor(buy, profile),
                continuousBuyExecutor(buy, profile),
                sellRules::evaluateOrderBook,
                sellRules::evaluateAveragePrice,
                closingAuctionSellExecutor());
    }

    private static TradeExecutionTemplate.ShanghaiOpeningAuctionBuyExecutor
    shanghaiOpeningExecutor(BuyStrategy buy, TradeExecutionProfile profile) {
        return new TradeExecutionTemplate.ShanghaiOpeningAuctionBuyExecutor() {
            @Override
            public boolean evaluateBuy(TradeMarketState market,
                                       AuctionMarketEvent event,
                                       long previousBuyVolume, int recordTime,
                                       TradeRuleRecord record) {
                return profile.openingAuctionBuyAllowed()
                        && buy.evaluateShanghaiAuctionBuy(
                        market, event, previousBuyVolume, recordTime, record);
            }

            @Override
            public boolean evaluateCancel(TradeMarketState market,
                                          AuctionMarketEvent event,
                                          int recordTime,
                                          TradeRuleRecord record) {
                return buy.evaluateShanghaiAuctionCancel(market, event, recordTime, record);
            }
        };
    }

    private static TradeExecutionTemplate.ShenzhenOpeningAuctionBuyExecutor
    shenzhenOpeningExecutor(BuyStrategy buy, TradeExecutionProfile profile) {
        return new TradeExecutionTemplate.ShenzhenOpeningAuctionBuyExecutor() {
            @Override
            public boolean evaluateBuy(TradeMarketState market,
                                       AuctionMarketEvent event,
                                       int recordTime, long limitUpBuyVolume,
                                       long totalSellVolume,
                                       TradeRuleRecord record) {
                return profile.openingAuctionBuyAllowed()
                        && buy.evaluateShenzhenAuctionBuy(market, event, recordTime,
                        limitUpBuyVolume, totalSellVolume, record);
            }

            @Override
            public boolean evaluateOrderBookCancel(
                    TradeMarketState market,
                    AuctionMarketEvent event,
                    int recordTime, long limitUpBuyVolume, long totalSellVolume,
                    TradeRuleRecord record) {
                return buy.evaluateShenzhenAuctionCancel(market, event, recordTime,
                        limitUpBuyVolume, totalSellVolume, record);
            }

            @Override
            public boolean evaluateSnapshotCancel(
                    TradeMarketState market,
                    AuctionMarketEvent event,
                    int recordTime, long limitUpBuyVolume, long totalSellVolume,
                    TradeRuleRecord record) {
                return buy.evaluateShenzhenSnapshotAuctionCancel(market, event, recordTime,
                        limitUpBuyVolume, totalSellVolume, record);
            }
        };
    }

    private static TradeExecutionTemplate.ContinuousBuyExecutor
    continuousBuyExecutor(BuyStrategy buy, TradeExecutionProfile profile) {
        return new TradeExecutionTemplate.ContinuousBuyExecutor() {
            @Override
            public boolean evaluate(TradeMarketState market,
                                    TradeRuleRecord record) {
                return buy.evaluateBuy(market, record);
            }

            @Override
            public boolean evaluateCancel(TradeMarketState market) {
                return buy.evaluateCancel(market);
            }

            @Override
            public boolean shouldEnableFirstBoardTradingMode(
                    TradeMarketState market) {
                return buy.shouldEnableFirstBoardTradingMode(market);
            }

            @Override
            public boolean isTimeAllowed(
                    TradeMarketState market, int time) {
                return time >= profile.earliestContinuousBuyTime()
                        && buy.isContinuousBuyTimeAllowed(time);
            }
        };
    }

    private static TradeExecutionTemplate.ClosingAuctionSellExecutor closingAuctionSellExecutor() {
        return new TradeExecutionTemplate.ClosingAuctionSellExecutor() {
            @Override
            public boolean evaluateShanghai(
                    TradeMarketState market,
                    AuctionMarketEvent event,
                    int recordTime, TradeRuleRecord record) {
                return ShanghaiClosingAuctionSellEvaluator.evaluate(
                        market, event, recordTime, record);
            }

            @Override
            public boolean evaluateShenzhen(
                    TradeMarketState market,
                    AuctionMarketEvent event,
                    int recordTime, TradeRuleRecord record) {
                return ShenzhenClosingAuctionSellEvaluator.evaluate(
                        market, event, recordTime, record);
            }
        };
    }
}
