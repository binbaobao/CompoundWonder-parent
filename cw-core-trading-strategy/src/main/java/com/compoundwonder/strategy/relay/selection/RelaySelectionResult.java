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
                                   List<SelectionTaskData> backupTasks,
                                   String fallbackDetail) {
    public RelaySelectionResult {
        candidateEvaluations = candidateEvaluations == null
                ? List.of() : List.copyOf(candidateEvaluations);
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
        backupTasks = backupTasks == null ? List.of() : List.copyOf(backupTasks);
    }

    /** 保留既有研究与测试调用的兼容构造器；未显式提供时不生成备用任务。 */
    public RelaySelectionResult(LocalDate selectionDate,
                                RelaySelectionPlan primaryPlan,
                                RelaySelectionPlan executedPlan,
                                List<RelayCandidateEvaluation> candidateEvaluations,
                                List<SelectionTaskData> tasks,
                                String fallbackDetail) {
        this(selectionDate, primaryPlan, executedPlan, candidateEvaluations,
                tasks, List.of(), fallbackDetail);
    }
}
