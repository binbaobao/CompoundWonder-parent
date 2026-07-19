package com.compoundwonder.strategy.sell;

/**
 * 持仓卖出场景。
 *
 * <p>板高使用订单簿 {@code lbcs}：它表示昨日已经完成的连板高度，
 * 因此 {@link #TWO_TO_THREE} 表示昨日 2 板、今日尝试晋级 3 板。</p>
 */
enum SellScene {
    UNSUPPORTED,
    TWO_TO_THREE,
    THREE_TO_FOUR,
    FOUR_TO_FIVE,
    FIVE_TO_SIX,
    SIX_TO_SEVEN,
    SEVEN_TO_EIGHT,
    EIGHT_TO_NINE,
    HIGH_BOARD
}
