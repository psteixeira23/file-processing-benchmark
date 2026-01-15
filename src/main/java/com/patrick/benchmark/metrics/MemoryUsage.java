package com.patrick.benchmark.metrics;

public final class MemoryUsage {

    private MemoryUsage() {
    }

    public static long deltaBytes(long beforeBytes, long afterBytes) {
        return Math.max(0L, afterBytes - beforeBytes);
    }
}
