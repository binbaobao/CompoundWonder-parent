package com.compoundwonder.core.engine;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.common.strategy.trade.TradeExecutionTemplate;

/**
 * 旧引擎单元测试的兼容模板。
 *
 * <p>正式回测由策略模块在会话初始化时编译模板，不会创建本适配器。</p>
 */
final class LegacyTradeExecutionTemplate implements TradeExecutionTemplate {
    private final TradeStaticFacts facts;
    private final ShanghaiOpeningAuctionBuyExecutor shanghai;
    private final ShenzhenOpeningAuctionBuyExecutor shenzhen;
    private final ContinuousBuyExecutor continuousBuy;
    private final ContinuousSellExecutor continuousSell;
    private final AveragePriceSellExecutor averageSell;
    private final ClosingAuctionSellExecutor closingSell;

    LegacyTradeExecutionTemplate(TradeStaticFacts facts, TradeDecisionService decisions) {
        this.facts = facts;
        this.shanghai = new ShanghaiOpeningAuctionBuyExecutor() {
            @Override
            public boolean evaluateBuy(com.compoundwonder.common.orderbook.TradeMarketState market,
                                       com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                                       long previousBuyVolume, int recordTime,
                                       com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShanghaiAuctionBuy(
                        market, event, previousBuyVolume, recordTime, record);
            }

            @Override
            public boolean evaluateCancel(com.compoundwonder.common.orderbook.TradeMarketState market,
                                          com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                                          int recordTime,
                                          com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShanghaiAuctionCancel(market, event, recordTime, record);
            }
        };
        this.shenzhen = new ShenzhenOpeningAuctionBuyExecutor() {
            @Override
            public boolean evaluateBuy(com.compoundwonder.common.orderbook.TradeMarketState market,
                                       com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                                       int recordTime, long limitUpBuyVolume,
                                       long totalSellVolume,
                                       com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShenzhenAuctionBuy(market, event, recordTime,
                        limitUpBuyVolume, totalSellVolume, record);
            }

            @Override
            public boolean evaluateOrderBookCancel(
                    com.compoundwonder.common.orderbook.TradeMarketState market,
                    com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                    int recordTime, long limitUpBuyVolume, long totalSellVolume,
                    com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShenzhenAuctionCancel(market, event, recordTime,
                        limitUpBuyVolume, totalSellVolume, record);
            }

            @Override
            public boolean evaluateSnapshotCancel(
                    com.compoundwonder.common.orderbook.TradeMarketState market,
                    com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                    int recordTime, long limitUpBuyVolume, long totalSellVolume,
                    com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShenzhenSnapshotAuctionCancel(market, event, recordTime,
                        limitUpBuyVolume, totalSellVolume, record);
            }
        };
        this.continuousBuy = new ContinuousBuyExecutor() {
            @Override
            public boolean evaluate(com.compoundwonder.common.orderbook.TradeMarketState market,
                                    com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateBuy(market, record);
            }

            @Override
            public boolean evaluateCancel(com.compoundwonder.common.orderbook.TradeMarketState market) {
                return decisions.evaluateCancel(market);
            }

            @Override
            public boolean shouldEnableFirstBoardTradingMode(
                    com.compoundwonder.common.orderbook.TradeMarketState market) {
                return decisions.shouldEnableFirstBoardTradingMode(market);
            }

            @Override
            public boolean isTimeAllowed(
                    com.compoundwonder.common.orderbook.TradeMarketState market, int time) {
                return decisions.isContinuousBuyTimeAllowed(market, time);
            }
        };
        this.continuousSell = decisions::evaluateSell;
        this.averageSell = decisions::evaluateAveragePriceSell;
        this.closingSell = new ClosingAuctionSellExecutor() {
            @Override
            public boolean evaluateShanghai(
                    com.compoundwonder.common.orderbook.TradeMarketState market,
                    com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                    int recordTime, com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShanghaiClosingAuctionSell(
                        market, event, recordTime, record);
            }

            @Override
            public boolean evaluateShenzhen(
                    com.compoundwonder.common.orderbook.TradeMarketState market,
                    com.compoundwonder.common.orderbook.AuctionMarketEvent event,
                    int recordTime, com.compoundwonder.common.orderbook.TradeRuleRecord record) {
                return decisions.evaluateShenzhenClosingAuctionSell(
                        market, event, recordTime, record);
            }
        };
    }

    @Override public TradeStaticFacts facts() { return facts; }
    @Override public ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy() { return shanghai; }
    @Override public ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy() { return shenzhen; }
    @Override public ContinuousBuyExecutor continuousBuy() { return continuousBuy; }
    @Override public ContinuousSellExecutor continuousSell() { return continuousSell; }
    @Override public AveragePriceSellExecutor averagePriceSell() { return averageSell; }
    @Override public ClosingAuctionSellExecutor closingAuctionSell() { return closingSell; }
}
