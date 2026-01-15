package com.patrick.benchmark.processing.scenario;

import com.patrick.benchmark.processing.CsvRecord;
import com.patrick.benchmark.processing.ScenarioData;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SexScenario implements ScenarioProcessor {

    private final Map<String, Long> counts = new LinkedHashMap<>();
    private long total;

    public SexScenario() {
        counts.put("Male", 0L);
        counts.put("Female", 0L);
        counts.put("Other", 0L);
    }

    @Override
    public String name() {
        return "By Sex";
    }

    @Override
    public void process(CsvRecord record) {
        String value = record.sex();
        String key;
        if ("M".equalsIgnoreCase(value)) {
            key = "Male";
        } else if ("F".equalsIgnoreCase(value)) {
            key = "Female";
        } else {
            key = "Other";
        }

        counts.merge(key, 1L, Long::sum);
        total++;
    }

    @Override
    public ScenarioData data() {
        return new ScenarioData(total, counts);
    }
}
