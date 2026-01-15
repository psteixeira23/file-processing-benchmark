package com.patrick.benchmark.processing;

import java.util.Map;

public record ScenarioReport(
        String name,
        long durationNanos,
        long count,
        Map<String, Long> breakdown
) {
    public ScenarioReport {
        breakdown = breakdown == null ? Map.of() : Map.copyOf(breakdown);
    }
}
