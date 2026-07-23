package com.compoundwonder.strategy.sell;

import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.strategy.sell.common.CommonSellStrategy;
import com.compoundwonder.strategy.sell.eight_to_nine.EightToNineNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.eight_to_nine.EightToNineSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.five_to_six.FiveToSixNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.five_to_six.FiveToSixSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.four_to_five.FourToFiveNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.four_to_five.FourToFiveSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.high_board.HighBoardNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.high_board.HighBoardSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.seven_to_eight.SevenToEightNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.seven_to_eight.SevenToEightSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.six_to_seven.SixToSevenNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.six_to_seven.SixToSevenSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.three_to_four.ThreeToFourNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.three_to_four.ThreeToFourSmallCapSellStrategy;
import com.compoundwonder.strategy.sell.two_to_three.TwoToThreeNormalCapSellStrategy;
import com.compoundwonder.strategy.sell.two_to_three.TwoToThreeSmallCapSellStrategy;

/**
 * 持仓卖出场景分发器。
 *
 * <p>卖出不读取买入 {@code tradeMode}。所有已验证规则作为常驻规则目录注册，统一执行入口
 * 直接读取模板携带的板高与启动市值事实；不再先选择某个几进几执行器。</p>
 *
 * <p>执行顺序固定为：场景专属规则 -> 公共规则。场景规则命中后立即返回，只有未命中时
 * 才继续执行周末、节假日等不属于单个场景的公共卖出规则。</p>
 */
public final class SellStrategyDispatcher {

    private final BoardSellStrategy twoToThreeSmallCap = new TwoToThreeSmallCapSellStrategy();
    private final BoardSellStrategy twoToThreeNormalCap = new TwoToThreeNormalCapSellStrategy();
    private final BoardSellStrategy threeToFourSmallCap = new ThreeToFourSmallCapSellStrategy();
    private final BoardSellStrategy threeToFourNormalCap = new ThreeToFourNormalCapSellStrategy();
    private final BoardSellStrategy fourToFiveSmallCap = new FourToFiveSmallCapSellStrategy();
    private final BoardSellStrategy fourToFiveNormalCap = new FourToFiveNormalCapSellStrategy();
    private final BoardSellStrategy fiveToSixSmallCap = new FiveToSixSmallCapSellStrategy();
    private final BoardSellStrategy fiveToSixNormalCap = new FiveToSixNormalCapSellStrategy();
    private final BoardSellStrategy sixToSevenSmallCap = new SixToSevenSmallCapSellStrategy();
    private final BoardSellStrategy sixToSevenNormalCap = new SixToSevenNormalCapSellStrategy();
    private final BoardSellStrategy sevenToEightSmallCap = new SevenToEightSmallCapSellStrategy();
    private final BoardSellStrategy sevenToEightNormalCap = new SevenToEightNormalCapSellStrategy();
    private final BoardSellStrategy eightToNineSmallCap = new EightToNineSmallCapSellStrategy();
    private final BoardSellStrategy eightToNineNormalCap = new EightToNineNormalCapSellStrategy();
    private final BoardSellStrategy highBoardSmallCap = new HighBoardSmallCapSellStrategy();
    private final BoardSellStrategy highBoardNormalCap = new HighBoardNormalCapSellStrategy();
    private final BoardSellStrategy common = new CommonSellStrategy();
    private final SellRuleBinding[] ruleCatalog = {
            binding(2, false, twoToThreeSmallCap),
            binding(2, true, twoToThreeNormalCap),
            binding(3, false, threeToFourSmallCap),
            binding(3, true, threeToFourNormalCap),
            binding(4, false, fourToFiveSmallCap),
            binding(4, true, fourToFiveNormalCap),
            binding(5, false, fiveToSixSmallCap),
            binding(5, true, fiveToSixNormalCap),
            binding(6, false, sixToSevenSmallCap),
            binding(6, true, sixToSevenNormalCap),
            binding(7, false, sevenToEightSmallCap),
            binding(7, true, sevenToEightNormalCap),
            binding(8, false, eightToNineSmallCap),
            binding(8, true, eightToNineNormalCap),
            highBoardBinding(false, highBoardSmallCap),
            highBoardBinding(true, highBoardNormalCap)
    };

    /**
     * 按昨日板高和启动流通市值评估逐笔与盘口卖出。
     * 目录顺序是契约：任一板位只允许命中一个市值分支，再回退公共规则。
     */
    public boolean evaluateOrderBook(TradeMarketState market, TradeRuleRecord record) {
        int yesterdayBoardHeight = market.getLbcs();
        if (yesterdayBoardHeight < 2) {
            return false;
        }
        for (SellRuleBinding binding : ruleCatalog) {
            if (binding.matches(market)
                    && binding.rules().evaluateOrderBook(market, record)) {
                return true;
            }
        }
        return common.evaluateOrderBook(market, record);
    }

    /**
     * 按昨日板高和启动流通市值评估分钟价格与均价走势卖出。
     * 奇数状态代表当前封板，封板期间禁止均线规则抢先卖出。
     */
    public boolean evaluateAveragePrice(int index, TradeMarketState market, TradeRuleRecord record) {
        int yesterdayBoardHeight = market.getLbcs();
        if (yesterdayBoardHeight < 2) {
            return false;
        }
        // 均线不参与涨停状态的卖出
        if (market.getStatus() % 2 == 1) {
            return false;
        }
        for (SellRuleBinding binding : ruleCatalog) {
            if (binding.matches(market)
                    && binding.rules().evaluateAveragePrice(index, market, record)) {
                return true;
            }
        }
        return common.evaluateAveragePrice(index, market, record);
    }

    /** 将昨日板高映射为便于展示和诊断的卖出场景。 */
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

    /**
     * 解析实际执行的板高与市值组合策略。
     *
     * <p>包级可见用于锁定 16 个场景的分发测试；返回的都是构造期创建的常驻实例。</p>
     */
    public BoardSellStrategy resolveStrategy(int yesterdayBoardHeight, long initialMarketValue) {
        boolean smallCap = SellMarketCapBand.from(initialMarketValue) == SellMarketCapBand.SMALL_CAP;
        return switch (yesterdayBoardHeight) {
            case 2 -> smallCap ? twoToThreeSmallCap : twoToThreeNormalCap;
            case 3 -> smallCap ? threeToFourSmallCap : threeToFourNormalCap;
            case 4 -> smallCap ? fourToFiveSmallCap : fourToFiveNormalCap;
            case 5 -> smallCap ? fiveToSixSmallCap : fiveToSixNormalCap;
            case 6 -> smallCap ? sixToSevenSmallCap : sixToSevenNormalCap;
            case 7 -> smallCap ? sevenToEightSmallCap : sevenToEightNormalCap;
            case 8 -> smallCap ? eightToNineSmallCap : eightToNineNormalCap;
            default -> yesterdayBoardHeight >= 9
                    ? (smallCap ? highBoardSmallCap : highBoardNormalCap)
                    : null;
        };
    }

    private static SellRuleBinding binding(int boardHeight, boolean normalCap,
                                           BoardSellStrategy rules) {
        return new SellRuleBinding(boardHeight, false, normalCap, rules);
    }

    private static SellRuleBinding highBoardBinding(boolean normalCap,
                                                    BoardSellStrategy rules) {
        return new SellRuleBinding(9, true, normalCap, rules);
    }

    private record SellRuleBinding(int boardHeight, boolean highBoard,
                                   boolean normalCap, BoardSellStrategy rules) {
        boolean matches(TradeMarketState market) {
            boolean boardMatches = highBoard
                    ? market.getLbcs() >= boardHeight : market.getLbcs() == boardHeight;
            boolean isNormalCap = SellMarketCapBand.from(market.getInitialMarketValue())
                    == SellMarketCapBand.NORMAL_CAP;
            return boardMatches && normalCap == isNormalCap;
        }
    }
}
