package com.patrick.benchmark.processing;

import java.util.List;

public record ProcessingSummary(
        long recordsProcessed,
        long invalidLines,
        List<ScenarioReport> scenarios
) {
    public ProcessingSummary {
        scenarios = List.copyOf(scenarios);
    }
}
