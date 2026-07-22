package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;

/** 原始候选从触发板数池进入过滤直至是否入选 Top3 的完整轨迹。 */
public record RelayCandidateEvaluation(RelaySelectionTrigger trigger,
                                       RelaySelectionStrength strength,
                                       StockDailyData daily,
                                       RelaySelectionAssist assist,
                                       boolean hasConvertibleBond,
                                       boolean eligible,
                                       Integer score,
                                       boolean selected,
                                       Integer selectedRank,
                                       String decisionLayer,
                                       String decisionDetail) {

    public RelayCandidateEvaluation selectedAt(int rank) {
        return new RelayCandidateEvaluation(trigger, strength, daily, assist,
                hasConvertibleBond, eligible, score,
                true, rank, decisionLayer, decisionDetail);
    }
}
