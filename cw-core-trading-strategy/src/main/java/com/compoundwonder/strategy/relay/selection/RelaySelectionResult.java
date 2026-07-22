package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;

import java.time.LocalDate;
import java.util.List;

/** 一次连板选股的触发计划、全量板内候选过滤轨迹和最终任务。 */
public record RelaySelectionResult(LocalDate selectionDate,
                                   RelaySelectionPlan primaryPlan,
                                   RelaySelectionPlan executedPlan,
                                   List<RelayCandidateEvaluation> candidateEvaluations,
                                   List<SelectionTaskData> tasks,
                                   String fallbackDetail) {
    public RelaySelectionResult {
        candidateEvaluations = candidateEvaluations == null
                ? List.of() : List.copyOf(candidateEvaluations);
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }
}
