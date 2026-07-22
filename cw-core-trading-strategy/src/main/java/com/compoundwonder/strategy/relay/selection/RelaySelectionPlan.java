package com.compoundwonder.strategy.relay.selection;

import java.util.List;

/** 一次选股日解析出的确定性连板选股计划。 */
public record RelaySelectionPlan(RelaySelectionTrigger trigger,
                                 List<RelayBoardPlan> boardPlans,
                                 int taskLimit,
                                 String detail) {

    public RelaySelectionPlan {
        boardPlans = boardPlans == null ? List.of() : List.copyOf(boardPlans);
    }

    public static RelaySelectionPlan none(String detail) {
        return new RelaySelectionPlan(RelaySelectionTrigger.NONE, List.of(), 0, detail);
    }
}
