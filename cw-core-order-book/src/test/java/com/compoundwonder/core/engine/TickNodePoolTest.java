package com.compoundwonder.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TickNodePoolTest {

    @Test
    void reusesAndClearsReleasedNode() {
        TickNodePool pool = new TickNodePool(1);
        TickNode node = pool.borrowNode();
        node.setPrice(1_000);
        node.setQuantity(100);
        node.setNext(new TickNode());

        pool.release(node);
        TickNode reused = pool.borrowNode();

        assertSame(node, reused);
        assertEquals(0, reused.getPrice());
        assertEquals(0, reused.getQuantity());
        assertNull(reused.getNext());
    }

    @Test
    void rejectsInvalidInitialSize() {
        assertThrows(IllegalArgumentException.class, () -> new TickNodePool(-1));
        assertThrows(IllegalArgumentException.class, () -> new TickNodePool(200_001));
    }
}
