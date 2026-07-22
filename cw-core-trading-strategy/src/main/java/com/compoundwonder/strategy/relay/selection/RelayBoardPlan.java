package com.compoundwonder.strategy.relay.selection;

/** 一个触发点内某个候选板数对应的过滤强度。 */
public record RelayBoardPlan(int board, RelaySelectionStrength strength) {
}
