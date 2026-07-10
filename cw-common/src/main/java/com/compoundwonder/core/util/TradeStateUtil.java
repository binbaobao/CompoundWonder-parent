package com.compoundwonder.core.util;

/**
 * 交易状态工具类（Bit Flag 设计）
 *
 * 设计目标：
 * 1. 一个 int 表达所有状态（方向 + 状态）
 * 2. 支持 买 / 卖 / 同时买卖（做T）
 * 3. 高性能（位运算，无GC）
 *
 * ========================
 * 位设计（从低到高）：
 *
 * 第0位：BUY（买方向）
 * 第1位：SELL（卖方向）
 *
 * 第2位：PENDING（待执行 / 未下单）
 * 第3位：ORDERED（已下单 / 挂单中）
 * 第4位：CANCELING（撤单中）
 * 第5位：DONE（已完成：成交 or 完全结束）
 *
 * ========================
 * 示例：
 *
 * 待买入        = BUY  | PENDING
 * 买入已下单    = BUY  | ORDERED
 * 买入撤单中    = BUY  | CANCELING
 * 买入完成      = BUY  | DONE
 *
 * 待卖出        = SELL | PENDING
 * 卖出已下单    = SELL | ORDERED
 * 卖出撤单中    = SELL | CANCELING
 * 卖出完成      = SELL | DONE
 *
 * 同时买卖（做T）= BUY | SELL | PENDING
 */
public final class TradeStateUtil {

    private TradeStateUtil() {}

    // ====== 方向位 ======
    public static final int BUY  = 1 << 0; // 000001
    public static final int SELL = 1 << 1; // 000010

    // ====== 状态位 ======
    public static final int PENDING    = 1 << 2; // 000100
    public static final int ORDERED    = 1 << 3; // 001000
    public static final int CANCELING  = 1 << 4; // 010000
    public static final int DONE       = 1 << 5; // 100000

    // =========================
    // ====== 创建状态 =========
    // =========================

    /** 创建：待买入 */
    public static int createBuyPending() {
        return BUY | PENDING;
    }

    /** 创建：待卖出 */
    public static int createSellPending() {
        return SELL | PENDING;
    }

    /** 创建：买卖都允许（做T） */
    public static int createBothPending() {
        return BUY | SELL | PENDING;
    }

    // =========================
    // ====== 状态流转 =========
    // =========================

    /** 设置为：已下单（挂单中） */
    public static int toOrdered(int state) {
        state = clearStatus(state);
        return state | ORDERED;
    }

    /** 设置为：撤单中 */
    public static int toCanceling(int state) {
        state = clearStatus(state);
        return state | CANCELING;
    }

    /** 设置为：已完成 */
    public static int toDone(int state) {
        state = clearStatus(state);
        return state | DONE;
    }

    /** 设置为：重新变成待处理（例如撤单后重试） */
    public static int toPending(int state) {
        state = clearStatus(state);
        return state | PENDING;
    }

    // =========================
    // ====== 方向修改 =========
    // =========================

    /** 增加买方向 */
    public static int addBuy(int state) {
        return state | BUY;
    }

    /** 增加卖方向 */
    public static int addSell(int state) {
        return state | SELL;
    }

    /** 移除买方向 */
    public static int removeBuy(int state) {
        return state & ~BUY;
    }

    /** 移除卖方向 */
    public static int removeSell(int state) {
        return state & ~SELL;
    }

    // =========================
    // ====== 判断方法 =========
    // =========================

    /** 是否包含买方向 */
    public static boolean isBuy(int state) {
        return (state & BUY) != 0;
    }

    /** 是否包含卖方向 */
    public static boolean isSell(int state) {
        return (state & SELL) != 0;
    }

    /** 是否待执行 */
    public static boolean isPending(int state) {
        return (state & PENDING) != 0;
    }

    /** 是否已下单 */
    public static boolean isOrdered(int state) {
        return (state & ORDERED) != 0;
    }

    /** 是否撤单中 */
    public static boolean isCanceling(int state) {
        return (state & CANCELING) != 0;
    }

    /** 是否完成 */
    public static boolean isDone(int state) {
        return (state & DONE) != 0;
    }

    /** 是否可以下单（待处理状态） */
    public static boolean canPlaceOrder(int state) {
        return isPending(state);
    }

    /** 是否可以撤单（已下单才允许撤） */
    public static boolean canCancel(int state) {
        return isOrdered(state);
    }

    /** 是否已经结束（不再参与任何流程） */
    public static boolean isFinished(int state) {
        return isDone(state);
    }

    // =========================
    // ====== 工具方法 =========
    // =========================

    /** 清除所有状态位（保留方向位） */
    private static int clearStatus(int state) {
        return state & (BUY | SELL);
    }

    /** 清除方向（保留状态） */
    public static int clearDirection(int state) {
        return state & ~(BUY | SELL);
    }

    /** 重置为0（完全清空） */
    public static int reset() {
        return 0;
    }

    // =========================
    // ====== Debug辅助 ========
    // =========================

    /** 转字符串（方便日志） */
    public static String toString(int state) {
        StringBuilder sb = new StringBuilder();

        if (isBuy(state)) sb.append("BUY|");
        if (isSell(state)) sb.append("SELL|");

        if (isPending(state)) sb.append("PENDING|");
        if (isOrdered(state)) sb.append("ORDERED|");
        if (isCanceling(state)) sb.append("CANCELING|");
        if (isDone(state)) sb.append("DONE|");

        if (sb.length() == 0) return "EMPTY";

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
