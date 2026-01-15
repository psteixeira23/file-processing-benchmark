package com.patrick.benchmark.processing.scenario;

import com.patrick.benchmark.processing.CsvRecord;
import com.patrick.benchmark.processing.ScenarioData;
import java.util.Map;
import java.util.TreeMap;

public final class AgeRangeScenario implements ScenarioProcessor {

    private final Map<String, Long> counts = new TreeMap<>();
    private long total;

    @Override
    public String name() {
        return "By Age Range";
    }

    @Override
    public void process(CsvRecord record) {
        String ageRange = record.ageRange();
        counts.merge(ageRange, 1L, Long::sum);
        total++;
    }

    @Override
    public ScenarioData data() {
        return new ScenarioData(total, counts);
    }
}
