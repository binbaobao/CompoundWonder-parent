package com.compoundwonder.core.engine;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongUnaryOperator;

/** 单日交易执行状态；与纯盘口数据分开，由对应 Handler 单线程推进。 */
public final class TradeExecutionState {
    private BuyExecutionState buyState;
    private SellExecutionState sellState;
    private PositionState positionState;
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
        this.compatibilityStateSource = compatibilityStateSource;
        this.compatibilityStateMirror = compatibilityStateMirror;
        this.compatibilityAuctionRecorder = compatibilityAuctionRecorder;
        applyLegacyStatus(transactionStatus);
    }

    public int transactionStatus() {
        if (compatibilityStateSource != null) {
            int legacyStatus = compatibilityStateSource.getAsInt();
            applyLegacyStatus(legacyStatus);
            return legacyStatus;
        }
        if (buyState == BuyExecutionState.MONITORING) return 1;
        if (buyState == BuyExecutionState.ORDER_PENDING) return 2;
        if (sellState == SellExecutionState.MONITORING) return -1;
        if (sellState == SellExecutionState.ORDER_PENDING) return -2;
        return 0;
    }
    public void transactionStatus(int value) {
        applyLegacyStatus(value);
        compatibilityStateMirror.accept(value);
    }

    public BuyExecutionState buyState() {
        synchronizeCompatibilityState();
        return buyState;
    }

    public SellExecutionState sellState() {
        synchronizeCompatibilityState();
        return sellState;
    }

    public PositionState positionState() {
        synchronizeCompatibilityState();
        return positionState;
    }

    public boolean isBuyMonitoring() { return buyState() == BuyExecutionState.MONITORING; }
    public boolean isBuyOrderPending() { return buyState() == BuyExecutionState.ORDER_PENDING; }
    public boolean isSellMonitoring() { return sellState() == SellExecutionState.MONITORING; }
    public boolean isSellOrderPending() { return sellState() == SellExecutionState.ORDER_PENDING; }

    public void beginBuyMonitoring() { transactionStatus(1); }
    public void beginBuyOrder() { transactionStatus(2); }
    public void beginSellMonitoring() { transactionStatus(-1); }
    public void beginSellOrder() { transactionStatus(-2); }
    public void disable() { transactionStatus(0); }

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
        disable();
    }

    private void synchronizeCompatibilityState() {
        if (compatibilityStateSource != null) {
            applyLegacyStatus(compatibilityStateSource.getAsInt());
        }
    }

    private void applyLegacyStatus(int value) {
        switch (value) {
            case 1 -> {
                buyState = BuyExecutionState.MONITORING;
                sellState = SellExecutionState.INACTIVE;
                positionState = PositionState.FLAT;
            }
            case 2 -> {
                buyState = BuyExecutionState.ORDER_PENDING;
                sellState = SellExecutionState.INACTIVE;
                positionState = PositionState.FLAT;
            }
            case -1 -> {
                buyState = BuyExecutionState.INACTIVE;
                sellState = SellExecutionState.MONITORING;
                positionState = PositionState.HELD;
            }
            case -2 -> {
                buyState = BuyExecutionState.INACTIVE;
                sellState = SellExecutionState.ORDER_PENDING;
                positionState = PositionState.HELD;
            }
            case 0 -> {
                buyState = BuyExecutionState.INACTIVE;
                sellState = SellExecutionState.INACTIVE;
                positionState = PositionState.FLAT;
            }
            default -> throw new IllegalArgumentException("不支持的交易状态: " + value);
        }
    }
}
