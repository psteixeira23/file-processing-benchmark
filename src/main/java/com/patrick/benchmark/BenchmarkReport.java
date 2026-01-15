package com.patrick.benchmark;

import java.util.List;

public record BenchmarkReport(ProcessingMode mode, List<BenchmarkResult> results) {
    public BenchmarkReport {
        results = List.copyOf(results);
    }
}
