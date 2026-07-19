package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;

/** 连板接力模式独立的交易时段与模式切换规则。 */
final class TradingControlEvaluator {

    private TradingControlEvaluator() {
    }

    static boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
        return market.getTime() < ConstantUtil.TIME_939
                && market.getOpenIncrease() - market.getIncrease() >= 5
                && market.getIncrease() <= -1
                && market.getLbcs() > 1;
    }

    static boolean isContinuousBuyTimeAllowed(int time) {
        return time < ConstantUtil.TIME_1430;
    }
}
