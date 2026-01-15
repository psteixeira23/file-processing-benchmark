package com.patrick.benchmark.metrics;

public final class ExecutionTimer {

    private ExecutionTimer() {
    }

    public static long elapsedNanos(long startNanos, long endNanos) {
        return Math.max(0L, endNanos - startNanos);
    }

    public static long elapsedMillis(long startNanos, long endNanos) {
        return elapsedNanos(startNanos, endNanos) / 1_000_000L;
    }
}
