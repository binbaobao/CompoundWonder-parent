package com.compoundwonder.strategy.relay.trade;

import com.compoundwonder.constant.ConstantUtil;
import com.compoundwonder.common.orderbook.TradeMarketState;

/** 连板接力模式独立的交易时段与模式切换规则。 */
final class TradingControlEvaluator {

    private TradingControlEvaluator() {
    }

    /**
     * 判断连板标的 09:39 前是否已从开盘位置回落至少 5 个百分点并跌至水下。
     * 满足时允许上层切换到首板交易逻辑，但本方法不直接修改交易模式。
     */
    static boolean shouldEnableFirstBoardTradingMode(TradeMarketState market) {
        return market.getTime() < ConstantUtil.TIME_939
                && market.getOpenIncrease() - market.getIncrease() >= 5
                && market.getIncrease() <= -1
                && market.getLbcs() > 1;
    }

    /** 连续竞价买入信号只允许在 14:30 之前产生。 */
    static boolean isContinuousBuyTimeAllowed(int time) {
        return time < ConstantUtil.TIME_1430;
    }
}
