package com.compoundwonder.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RuleRecordBufferTest {

    @Test
    void nextRecordClearsEveryFieldFromAnUncommittedEvaluation() {
        RuleRecordBuffer buffer = new RuleRecordBuffer(1);
        RuleRecord first = buffer.nextRecord();
        first.fill(1, 114, "600000", 93_100_000, 1_100, 10D, "previous evaluation");

        RuleRecord reused = buffer.nextRecord();

        assertEquals(0, reused.actionType);
        assertEquals(0, reused.ruleCode);
        assertEquals(0, reused.price);
        assertNull(reused.remark);
    }
}
