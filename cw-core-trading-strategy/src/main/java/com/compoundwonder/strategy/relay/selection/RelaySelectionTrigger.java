package com.compoundwonder.strategy.relay.selection;

/** 连板接力的互斥主触发点以及主链后的卡位兜底。 */
public enum RelaySelectionTrigger {
    NONE,
    HEIGHT_SUPPRESSION,
    HIGH_TO_LOW_SECOND,
    HIGH_TO_LOW_BREAK,
    WEAK_FIVE_CARD
}
