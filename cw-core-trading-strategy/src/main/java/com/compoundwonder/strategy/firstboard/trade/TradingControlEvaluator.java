package com.compoundwonder.strategy.firstboard.trade;

import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.strategy.TradeMarketState;

/** 普通首板模式独立的交易时段与模式切换规则。 */
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
