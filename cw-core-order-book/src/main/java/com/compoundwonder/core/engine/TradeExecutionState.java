package com.compoundwonder.core.engine;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongUnaryOperator;

/** 单日交易执行状态；与纯盘口数据分开，由对应 Handler 单线程推进。 */
public final class TradeExecutionState {
    private int transactionStatus;
    private long previousShanghaiAuctionBuyVolume = -1L;
    private int firstLimitUpTradeTime;
    private boolean rejectedForDay;
    private final IntSupplier compatibilityStateSource;
    private final IntConsumer compatibilityStateMirror;
    private final LongUnaryOperator compatibilityAuctionRecorder;

    public TradeExecutionState(int transactionStatus) {
        this(transactionStatus, null, ignored -> { }, null);
    }

    TradeExecutionState(int transactionStatus, IntSupplier compatibilityStateSource,
                        IntConsumer compatibilityStateMirror,
                        LongUnaryOperator compatibilityAuctionRecorder) {
        this.transactionStatus = transactionStatus;
        this.compatibilityStateSource = compatibilityStateSource;
        this.compatibilityStateMirror = compatibilityStateMirror;
        this.compatibilityAuctionRecorder = compatibilityAuctionRecorder;
    }

    public int transactionStatus() {
        return compatibilityStateSource == null
                ? transactionStatus : compatibilityStateSource.getAsInt();
    }
    public void transactionStatus(int value) {
        transactionStatus = value;
        compatibilityStateMirror.accept(value);
    }

    public long recordShanghaiAuctionBuyVolume(long currentBuyVolume) {
        if (compatibilityAuctionRecorder != null) {
            return compatibilityAuctionRecorder.applyAsLong(currentBuyVolume);
        }
        long previous = previousShanghaiAuctionBuyVolume;
        previousShanghaiAuctionBuyVolume = currentBuyVolume;
        return previous;
    }

    public int firstLimitUpTradeTime() { return firstLimitUpTradeTime; }

    public void recordLimitUpTrade(int time) {
        if (firstLimitUpTradeTime == 0 && time > 0) {
            firstLimitUpTradeTime = time;
        }
    }

    public boolean rejectedForDay() { return rejectedForDay; }

    public void rejectForDay() {
        rejectedForDay = true;
        transactionStatus(0);
    }
}
