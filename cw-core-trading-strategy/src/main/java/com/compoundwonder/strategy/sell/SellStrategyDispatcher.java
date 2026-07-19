package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.stage.EightToNineSellStrategy;
import com.compoundwonder.strategy.sell.stage.FiveToSixSellStrategy;
import com.compoundwonder.strategy.sell.stage.FourToFiveSellStrategy;
import com.compoundwonder.strategy.sell.stage.HighBoardSellStrategy;
import com.compoundwonder.strategy.sell.stage.SevenToEightSellStrategy;
import com.compoundwonder.strategy.sell.stage.SixToSevenSellStrategy;
import com.compoundwonder.strategy.sell.stage.ThreeToFourSellStrategy;
import com.compoundwonder.strategy.sell.stage.TwoToThreeSellStrategy;

/**
 * 持仓卖出场景分发器。
 *
 * <p>卖出不读取买入 {@code tradeMode}，只读取订单簿中的昨日板高 {@code lbcs}。
 * 各场景实例常驻，热路径只执行一次 {@code switch}，不会使用 Map、反射、
 * Spring Bean 查找，也不会为每条行情创建上下文对象。</p>
 */
public final class SellStrategyDispatcher {

    private final BoardSellStrategy twoToThree = new TwoToThreeSellStrategy();
    private final BoardSellStrategy threeToFour = new ThreeToFourSellStrategy();
    private final BoardSellStrategy fourToFive = new FourToFiveSellStrategy();
    private final BoardSellStrategy fiveToSix = new FiveToSixSellStrategy();
    private final BoardSellStrategy sixToSeven = new SixToSevenSellStrategy();
    private final BoardSellStrategy sevenToEight = new SevenToEightSellStrategy();
    private final BoardSellStrategy eightToNine = new EightToNineSellStrategy();
    private final BoardSellStrategy highBoard = new HighBoardSellStrategy();

    /** 按昨日板高评估逐笔与盘口卖出；低于 2 板不属于当前持仓卖出范围。 */
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        BoardSellStrategy strategy = strategy(market.getLbcs());
        return strategy != null && strategy.evaluateOrderBook(market, record);
    }

    /** 按昨日板高评估分钟均价卖出；低于 2 板不属于当前持仓卖出范围。 */
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        BoardSellStrategy strategy = strategy(market.getLbcs());
        return strategy != null && strategy.evaluateAveragePrice(index, market, record);
    }

    SellScene resolveScene(int yesterdayBoardHeight) {
        return switch (yesterdayBoardHeight) {
            case 2 -> SellScene.TWO_TO_THREE;
            case 3 -> SellScene.THREE_TO_FOUR;
            case 4 -> SellScene.FOUR_TO_FIVE;
            case 5 -> SellScene.FIVE_TO_SIX;
            case 6 -> SellScene.SIX_TO_SEVEN;
            case 7 -> SellScene.SEVEN_TO_EIGHT;
            case 8 -> SellScene.EIGHT_TO_NINE;
            default -> yesterdayBoardHeight >= 9 ? SellScene.HIGH_BOARD : SellScene.UNSUPPORTED;
        };
    }

    private BoardSellStrategy strategy(int yesterdayBoardHeight) {
        return switch (yesterdayBoardHeight) {
            case 2 -> twoToThree;
            case 3 -> threeToFour;
            case 4 -> fourToFive;
            case 5 -> fiveToSix;
            case 6 -> sixToSeven;
            case 7 -> sevenToEight;
            case 8 -> eightToNine;
            default -> yesterdayBoardHeight >= 9 ? highBoard : null;
        };
    }
}
