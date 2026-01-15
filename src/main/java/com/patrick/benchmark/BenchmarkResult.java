package com.patrick.benchmark;

import com.patrick.benchmark.processing.ProcessingSummary;

public record BenchmarkResult(
        String strategyName,
        long durationNanos,
        long memoryBytes,
        ProcessingSummary summary,
        long errorCount,
        String errorMessage
) {
}
