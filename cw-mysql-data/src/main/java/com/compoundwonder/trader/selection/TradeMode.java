package com.compoundwonder.trader.selection;

/**
 * 选股交易模式协议。
 *
 * <p>枚举值直接写入 {@code stock_watching_task.trade_mode}，编号属于数据库和回测
 * 共同使用的稳定协议，不能因为代码包调整而重新编号。</p>
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

    /** 返回数据库使用的稳定模式编号。 */
    public int code() {
        return code;
    }
}
