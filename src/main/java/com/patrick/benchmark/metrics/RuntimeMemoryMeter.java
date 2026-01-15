package com.patrick.benchmark.metrics;

public final class RuntimeMemoryMeter implements MemoryMeter {

    @Override
    public long usedBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
