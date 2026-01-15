package com.patrick.benchmark.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MemoryUsageTest {

    @Test
    void shouldCalculatePositiveDelta() {
        assertEquals(128L, MemoryUsage.deltaBytes(256L, 384L));
    }

    @Test
    void shouldAvoidNegativeDelta() {
        assertEquals(0L, MemoryUsage.deltaBytes(512L, 128L));
    }
}
