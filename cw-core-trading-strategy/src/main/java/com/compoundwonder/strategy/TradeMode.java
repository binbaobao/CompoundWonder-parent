package com.compoundwonder.strategy;

/**
 * 选股与交易共同使用的稳定模式协议。
 *
 * <p>编号会写入 {@code stock_watching_task.trade_mode} 和回测持仓记录，禁止随包名调整而改号。</p>
 */
public enum TradeMode {

    /** 连板接力。 */
    RELAY_LIMIT_UP(1),

    /** 普通首板。 */
    FIRST_BOARD(2),

    /** 小市值首板。 */
    SMALL_CAP_FIRST_BOARD(3);

    private final int code;

    TradeMode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static TradeMode fromCode(int code) {
        return switch (code) {
            case 1 -> RELAY_LIMIT_UP;
            case 2 -> FIRST_BOARD;
            case 3 -> SMALL_CAP_FIRST_BOARD;
            default -> throw new IllegalArgumentException("未知交易模式: " + code);
        };
    }
}
