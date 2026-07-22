package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplateFactory;
import com.compoundwonder.strategy.firstboard.trade.FirstBoardBuyStrategy;
import com.compoundwonder.strategy.relay.trade.RelayBuyStrategy;
import com.compoundwonder.strategy.sell.BoardSellStrategy;
import com.compoundwonder.strategy.sell.ShanghaiClosingAuctionSellEvaluator;
import com.compoundwonder.strategy.sell.SellStrategyDispatcher;
import com.compoundwonder.strategy.sell.ShenzhenClosingAuctionSellEvaluator;
import com.compoundwonder.strategy.sell.common.CommonSellStrategy;
import com.compoundwonder.strategy.smallcapfirstboard.trade.SmallCapFirstBoardBuyStrategy;

/** 默认交易模板编译器；模式与卖出场景只在 {@link #compile} 中解析一次。 */
public final class DefaultTradeExecutionTemplateFactory implements TradeExecutionTemplateFactory {

    private final BuyStrategy relayBuy = new RelayBuyStrategy();
    private final BuyStrategy firstBoardBuy = new FirstBoardBuyStrategy();
    private final BuyStrategy smallCapFirstBoardBuy = new SmallCapFirstBoardBuyStrategy();
    private final SellStrategyDispatcher sellCatalog = new SellStrategyDispatcher();
    private final BoardSellStrategy commonSell = new CommonSellStrategy();

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
        BoardSellStrategy sceneSell = sellCatalog.resolveStrategy(
                facts.lbcs(), facts.initialMarketValue());
        return new CompiledTradeExecutionTemplate(facts, buy, sceneSell, commonSell);
    }

    private record CompiledTradeExecutionTemplate(
            TradeStaticFacts facts,
            ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy,
            ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy,
            ContinuousBuyExecutor continuousBuy,
            ContinuousSellExecutor continuousSell,
            AveragePriceSellExecutor averagePriceSell,
            ClosingAuctionSellExecutor closingAuctionSell)
            implements TradeExecutionTemplate {

        private CompiledTradeExecutionTemplate(TradeStaticFacts facts,
                                               BuyStrategy buy,
                                               BoardSellStrategy sceneSell,
                                               BoardSellStrategy commonSell) {
            this(facts,
                    shanghaiOpeningExecutor(buy),
                    shenzhenOpeningExecutor(buy),
                    continuousBuyExecutor(buy),
                    continuousSellExecutor(sceneSell, commonSell),
                    averagePriceSellExecutor(sceneSell, commonSell),
                    closingAuctionSellExecutor());
        }
    }

    private static TradeExecutionTemplate.ShanghaiOpeningAuctionBuyExecutor
    shanghaiOpeningExecutor(BuyStrategy buy) {
        return new TradeExecutionTemplate.ShanghaiOpeningAuctionBuyExecutor() {
            @Override
            public boolean evaluateBuy(TradeMarketState market,
                                       AuctionMarketEvent event,
                                       long previousBuyVolume, int recordTime,
                                       TradeRuleRecord record) {
                return buy.evaluateShanghaiAuctionBuy(
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
    shenzhenOpeningExecutor(BuyStrategy buy) {
        return new TradeExecutionTemplate.ShenzhenOpeningAuctionBuyExecutor() {
            @Override
            public boolean evaluateBuy(TradeMarketState market,
                                       AuctionMarketEvent event,
                                       int recordTime, long limitUpBuyVolume,
                                       long totalSellVolume,
                                       TradeRuleRecord record) {
                return buy.evaluateShenzhenAuctionBuy(market, event, recordTime,
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
    continuousBuyExecutor(BuyStrategy buy) {
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
                return buy.isContinuousBuyTimeAllowed(time);
            }
        };
    }

    private static TradeExecutionTemplate.ContinuousSellExecutor continuousSellExecutor(
            BoardSellStrategy scene, BoardSellStrategy common) {
        return (market, record) -> scene != null
                && (scene.evaluateOrderBook(market, record)
                || common.evaluateOrderBook(market, record));
    }

    private static TradeExecutionTemplate.AveragePriceSellExecutor averagePriceSellExecutor(
            BoardSellStrategy scene, BoardSellStrategy common) {
        return (index, market, record) -> scene != null
                && market.getStatus() % 2 == 0
                && (scene.evaluateAveragePrice(index, market, record)
                || common.evaluateAveragePrice(index, market, record));
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
