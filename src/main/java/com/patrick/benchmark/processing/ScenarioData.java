package com.patrick.benchmark.processing;

import java.util.Map;

public record ScenarioData(long count, Map<String, Long> breakdown) {
    public ScenarioData {
        breakdown = breakdown == null ? Map.of() : Map.copyOf(breakdown);
    }
}
