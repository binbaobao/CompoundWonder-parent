package com.compoundwonder.common.strategy.trade;

import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.common.orderbook.TradeStaticFacts;

/**
 * 单只股票、单个交易日的预编译交易规则模板。
 *
 * <p>模板在订单簿会话初始化时按模式、昨日板高和启动市值解析一次。Handler
 * 只调用对应市场阶段的执行器，不再在每条行情上重复进行模式和卖出场景分发。</p>
 */
public interface TradeExecutionTemplate {

    TradeStaticFacts facts();

    ShanghaiOpeningAuctionBuyExecutor shanghaiOpeningAuctionBuy();

    ShenzhenOpeningAuctionBuyExecutor shenzhenOpeningAuctionBuy();

    ContinuousBuyExecutor continuousBuy();

    ContinuousSellExecutor continuousSell();

    AveragePriceSellExecutor averagePriceSell();

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
