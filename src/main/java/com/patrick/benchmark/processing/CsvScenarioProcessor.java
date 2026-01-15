package com.patrick.benchmark.processing;

import java.util.ArrayList;
import java.util.List;

import com.patrick.benchmark.processing.scenario.ScenarioProcessor;

public final class CsvScenarioProcessor implements LineProcessor {

    private final CsvLineParser parser;
    private final List<ScenarioProcessor> scenarios;
    private final long[] scenarioNanos;
    private boolean headerSkipped;
    private long recordsProcessed;
    private long invalidLines;

    public CsvScenarioProcessor(List<ScenarioProcessor> scenarios, CsvLineParser parser) {
        this.parser = parser;
        this.scenarios = List.copyOf(scenarios);
        this.scenarioNanos = new long[this.scenarios.size()];
    }

    @Override
    public void process(String line) {
        if (!headerSkipped) {
            headerSkipped = true;
            return;
        }

        CsvRecord parsedRecord = parser.parse(line);
        if (parsedRecord == null) {
            invalidLines++;
            return;
        }

        recordsProcessed++;
        for (int i = 0; i < scenarios.size(); i++) {
            long start = System.nanoTime();
            scenarios.get(i).process(parsedRecord);
            scenarioNanos[i] += System.nanoTime() - start;
        }
    }

    @Override
    public ProcessingSummary summary() {
        List<ScenarioReport> reports = new ArrayList<>(scenarios.size());
        for (int i = 0; i < scenarios.size(); i++) {
            ScenarioProcessor scenario = scenarios.get(i);
            ScenarioData data = scenario.data();
            reports.add(new ScenarioReport(
                    scenario.name(),
                    scenarioNanos[i],
                    data.count(),
                    data.breakdown()));
        }

        return new ProcessingSummary(recordsProcessed, invalidLines, reports);
    }
}
