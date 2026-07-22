package com.compoundwonder.strategy.relay.selection;

import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelayTriggerResolverTest {

    @Test
    void heightTwoSelectsOnlyRelaxedTwoBoard() {
        RelaySelectionPlan plan = RelayTriggerResolver.resolve(context(2, 6, 7,
                null, null, null, null));

        assertEquals(RelaySelectionTrigger.HEIGHT_SUPPRESSION, plan.trigger());
        assertEquals(List.of(new RelayBoardPlan(2, RelaySelectionStrength.RELAXED)),
                plan.boardPlans());
        assertEquals(3, plan.taskLimit());
    }

    @Test
    void heightThreeOrFourRanksRelaxedThreeBoardBeforeNormalTwoBoard() {
        RelaySelectionPlan plan = RelayTriggerResolver.resolve(context(4, 4, 6,
                null, null, null, null));

        assertEquals(RelaySelectionTrigger.HEIGHT_SUPPRESSION, plan.trigger());
        assertEquals(List.of(
                        new RelayBoardPlan(3, RelaySelectionStrength.RELAXED),
                        new RelayBoardPlan(2, RelaySelectionStrength.NORMAL)),
                plan.boardPlans());
    }

    @Test
    void secondSupplementHasPriorityOverNewBreakOnContinuousRetreat() {
        RelaySelectionPlan plan = RelayTriggerResolver.resolve(context(5, 5, 6,
                null, null, null, null));

        assertEquals(RelaySelectionTrigger.HIGH_TO_LOW_SECOND, plan.trigger());
        assertEquals(List.of(new RelayBoardPlan(3, RelaySelectionStrength.NORMAL)),
                plan.boardPlans());
    }

    @Test
    void fiveOrSixBoardBreakDoesNotRequireCycleOccupation() {
        RelaySelectionPlan plan = RelayTriggerResolver.resolve(context(5, 6, 7,
                null, "600001", "600099", "600088"));

        assertEquals(RelaySelectionTrigger.HIGH_TO_LOW_BREAK, plan.trigger());
        assertEquals(List.of(
                        new RelayBoardPlan(3, RelaySelectionStrength.STRICT),
                        new RelayBoardPlan(2, RelaySelectionStrength.STRICT)),
                plan.boardPlans());
    }

    @Test
    void sevenBoardOrHigherBreakRequiresHighestStockToBeCycleOwner() {
        RelaySelectionPlan followerBreak = RelayTriggerResolver.resolve(context(7, 8, 6,
                "600010", "600001", "600099", "600001"));
        RelaySelectionPlan ownerBreak = RelayTriggerResolver.resolve(context(7, 8, 6,
                "600010", "600001", "600099", "600010"));

        assertEquals(RelaySelectionTrigger.NONE, followerBreak.trigger());
        assertEquals(RelaySelectionTrigger.HIGH_TO_LOW_BREAK, ownerBreak.trigger());
    }

    @Test
    void newCycleOwnerBackfillCancelsSecondSupplement() {
        RelaySelectionPlan plan = RelayTriggerResolver.resolve(context(9, 8, 8,
                "600008", "600008", "600009", "600009"));

        assertEquals(RelaySelectionTrigger.NONE, plan.trigger());
    }

    private RelayTriggerContext context(int todayHeight,
                                        int yesterdayHeight,
                                        int dayBeforeHeight,
                                        String yesterdayHighestCode,
                                        String dayBeforeHighestCode,
                                        String todayOwner,
                                        String yesterdayOwner) {
        LocalDate today = LocalDate.of(2026, 7, 21);
        return new RelayTriggerContext(
                new MarketEmotionData(today, todayHeight, todayOwner),
                new MarketEmotionData(today.minusDays(1), yesterdayHeight, yesterdayOwner),
                new MarketEmotionData(today.minusDays(2), dayBeforeHeight, yesterdayOwner),
                yesterdayHighestCode,
                dayBeforeHighestCode);
    }
}
