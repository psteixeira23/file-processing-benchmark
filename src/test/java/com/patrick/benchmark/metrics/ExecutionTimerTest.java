package com.patrick.benchmark.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExecutionTimerTest {

    @Test
    void shouldReturnElapsedNanos() {
        long start = 1_000_000_000L;
        long end = 3_500_000_000L;

        assertEquals(2_500_000_000L, ExecutionTimer.elapsedNanos(start, end));
    }

    @Test
    void shouldConvertNanosToMillis() {
        long start = 1_000_000_000L;
        long end = 3_500_000_000L;

        assertEquals(2500L, ExecutionTimer.elapsedMillis(start, end));
    }
}
