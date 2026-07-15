package com.compoundwonder.constant;


/**
 * 规则常量
 */
public class RuleConstant {

    // 交易方式，买卖撤单
    public static final int TRADING_MODE_BUY = 1;
    public static final int TRADING_MODE_SELL = 2;
    public static final int TRADING_MODE_CANCEL = 3;

    // 涨停盘口卖出规则（101-199）
    public static final int SELL_LIMIT_UP_SMALL_CAP_SEAL_WEAKENING = 101;
    public static final int SELL_LIMIT_UP_AFTERNOON_SHRINKING_BOARD = 102;
    public static final int SELL_LIMIT_UP_HIGH_TURNOVER_MULTI_BREAK = 103;
    public static final int SELL_LIMIT_UP_HIGH_TURNOVER_SEAL_WEAKENING = 104;
    public static final int SELL_LIMIT_UP_HOLIDAY_HIGH_TURNOVER = 105;
    public static final int SELL_LIMIT_UP_CONSECUTIVE_HIGH_TURNOVER = 106;
    public static final int SELL_LIMIT_UP_MORNING_HIGH_TURNOVER_WEAK_SEAL = 107;
    public static final int SELL_LIMIT_UP_MULTI_DAY_HIGH_TURNOVER = 108;
    public static final int SELL_LIMIT_UP_SMALL_CAP_ONE_WORD_WEAKENING = 109;
    public static final int SELL_LIMIT_UP_DEEP_BREAK_WEAK_SEAL = 110;
    public static final int SELL_LIMIT_UP_THREE_ONE_WORD_WEAKENING = 111;
    public static final int SELL_LIMIT_UP_HIGH_BOARD_GAP_SHRINKING = 112;
    public static final int SELL_LIMIT_UP_HIGH_AMPLITUDE = 113;
    public static final int SELL_LIMIT_UP_HIGH_BOARD_LOW_TURNOVER = 114;
    public static final int SELL_LIMIT_UP_HOLIDAY_HIGH_BOARD = 115;
    public static final int SELL_LIMIT_UP_AVERAGE_HEIGHT_FAST_SEAL = 116;
    public static final int SELL_LIMIT_UP_AVERAGE_HEIGHT_WEAK_SEAL = 117;
    public static final int SELL_LIMIT_UP_TWO_TO_THREE_MULTI_BREAK = 118;

    // 均价走势卖出规则（201-299）
    public static final int SELL_AVERAGE_LOW_OPEN_WEAKENING = 201;
    public static final int SELL_AVERAGE_TWO_TO_THREE_WEAKENING = 202;
    public static final int SELL_AVERAGE_TWO_TO_THREE_BREAK_AVERAGE = 203;
    public static final int SELL_AVERAGE_NEAR_LIMIT_UP_PRESSURE = 204;
    public static final int SELL_AVERAGE_PEAK_DRAWDOWN = 205;
    public static final int SELL_AVERAGE_LARGE_AMPLITUDE_WEAKENING = 206;
    public static final int SELL_AVERAGE_BREAK_WITH_LARGE_AMPLITUDE = 207;
    public static final int SELL_AVERAGE_BREAK_WITH_EXTREME_AMPLITUDE = 208;
    public static final int SELL_AVERAGE_BREAK_WITH_PEAK_DRAWDOWN = 209;
    public static final int SELL_AVERAGE_BREAK_LATE_OR_HIGH_TURNOVER = 210;

    // 回测确定性卖出规则（301-399）
    public static final int SELL_BACKTEST_LIMIT_UP_BREAK_NEXT_OPEN = 301;

}
