package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.strategy.firstboard.trade.FirstBoardBuyStrategy;
import com.compoundwonder.strategy.relay.trade.RelayBuyStrategy;
import com.compoundwonder.strategy.sell.ClosingAuctionSellEvaluator;
import com.compoundwonder.strategy.sell.SellStrategyDispatcher;
import com.compoundwonder.strategy.smallcapfirstboard.trade.SmallCapFirstBoardBuyStrategy;

/**
 * 高频交易规则分发器。
 *
 * <p>买入按订单簿稳定的 {@code tradeMode} 分发，卖出按昨日板高 {@code lbcs}
 * 分发并在场景内按启动流通市值分档。热路径只使用直接 {@code switch}，
 * 不使用 Map、反射、Spring 查找或每笔行情创建对象。</p>
 */
public final class TradeStrategyDispatcher implements TradeDecisionService {

    private final BuyStrategy relayStrategy = new RelayBuyStrategy();
    private final BuyStrategy firstBoardStrategy = new FirstBoardBuyStrategy();
    private final BuyStrategy smallCapFirstBoardStrategy = new SmallCapFirstBoardBuyStrategy();
    private final SellStrategyDispatcher sellStrategyDispatcher = new SellStrategyDispatcher();

    @Override
    public boolean evaluateBuy(TradeMarketState market, TradeRuleRecord record) {
        // 调用当前订单簿交易模式对应的买入方法。
        return buyStrategy(market.getTradeMode()).evaluateBuy(market, record);
    }

    @Override
    public boolean evaluateSell(TradeMarketState market, TradeRuleRecord record) {
        // 卖出不沿用买入模式，按昨日板高和启动流通市值进入持仓卖出场景。
        return sellStrategyDispatcher.evaluateOrderBook(market, record);
    }

    @Override
    public boolean evaluateAveragePriceSell(int calculateIndex, TradeMarketState market,
                                            TradeRuleRecord record) {
        // 卖出不沿用买入模式，按昨日板高和启动流通市值进入持仓卖出场景。
        return sellStrategyDispatcher.evaluateAveragePrice(calculateIndex, market, record);
    }

    @Override
    public boolean evaluateCancel(TradeMarketState market) {
        // 调用当前订单簿交易模式对应的撤单方法。
        return buyStrategy(market.getTradeMode()).evaluateCancel(market);
    }

    @Override
    public boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
        return buyStrategy(market.getTradeMode()).shouldEnableFirstBoardTradingMode(market);
    }

    @Override
    public boolean isContinuousBuyTimeAllowed(TradeMarketState market, int time) {
        return buyStrategy(market.getTradeMode()).isContinuousBuyTimeAllowed(time);
    }

    @Override
    public int evaluateShanghaiAuctionBuy(TradeMarketState market, int time, int price,
                                          int limitUpPrice, long totalBuyVolume,
                                          long totalSellVolume, long requiredBuyVolume,
                                          long limitUpBuyAmount) {
        return buyStrategy(market.getTradeMode()).evaluateShanghaiAuctionBuy(
                time, price, limitUpPrice, totalBuyVolume, totalSellVolume,
                requiredBuyVolume, limitUpBuyAmount);
    }

    @Override
    public int evaluateShanghaiAuctionCancel(TradeMarketState market, int price,
                                             int limitUpPrice, long totalBuyVolume,
                                             long totalSellVolume, long requiredBuyVolume) {
        return buyStrategy(market.getTradeMode()).evaluateShanghaiAuctionCancel(
                price, limitUpPrice, totalBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenAuctionBuy(TradeMarketState market, byte dataType,
                                         int price, int limitUpPrice, int orderQuantity,
                                         long limitUpBuyVolume, long totalSellVolume,
                                         long requiredBuyVolume, long limitUpBuyAmount,
                                         long circulation) {
        return buyStrategy(market.getTradeMode()).evaluateShenzhenAuctionBuy(
                dataType, price, limitUpPrice, orderQuantity, limitUpBuyVolume,
                totalSellVolume, requiredBuyVolume, limitUpBuyAmount, circulation);
    }

    @Override
    public int evaluateShenzhenAuctionCancel(TradeMarketState market,
                                            long limitUpBuyVolume, long totalSellVolume,
                                            long requiredBuyVolume) {
        return buyStrategy(market.getTradeMode()).evaluateShenzhenAuctionCancel(
                limitUpBuyVolume, totalSellVolume, requiredBuyVolume);
    }

    @Override
    public int evaluateShenzhenSnapshotAuctionCancel(TradeMarketState market,
                                                     int price, int limitUpPrice) {
        return buyStrategy(market.getTradeMode())
                .evaluateShenzhenSnapshotAuctionCancel(price, limitUpPrice);
    }

    @Override
    public boolean evaluateClosingAuctionSell(TradeMarketState market, int price,
                                              int limitUpPrice, long totalBuyVolume,
                                              long totalSellVolume) {
        return ClosingAuctionSellEvaluator.evaluate(
                price, limitUpPrice, totalBuyVolume, totalSellVolume);
    }

    /**
     * 将订单簿固化的交易模式映射到常驻策略实例。
     *
     * <p>未知模式直接失败，避免高频链路在错误配置下悄悄套用其他模式规则。</p>
     *
     * @param tradeMode 交易模式：1 连板、2 普通首板、3 小市值首板
     * @return 与交易模式一一对应的常驻策略实例
     * @throws IllegalStateException 交易模式未设置或不受支持
     */
    private BuyStrategy buyStrategy(int tradeMode) {
        return switch (tradeMode) {
            case 1 -> relayStrategy;
            case 2 -> firstBoardStrategy;
            case 3 -> smallCapFirstBoardStrategy;
            default -> throw new IllegalStateException("订单簿未设置有效交易模式: " + tradeMode);
        };
    }
}
