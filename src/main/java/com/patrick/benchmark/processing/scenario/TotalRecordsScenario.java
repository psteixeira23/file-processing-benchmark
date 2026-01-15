package com.patrick.benchmark.processing.scenario;

import com.patrick.benchmark.processing.CsvRecord;
import com.patrick.benchmark.processing.ScenarioData;

public final class TotalRecordsScenario implements ScenarioProcessor {

    private long count;

    @Override
    public String name() {
        return "Total Records";
    }

    @Override
    public void process(CsvRecord record) {
        count++;
    }

    @Override
    public ScenarioData data() {
        return new ScenarioData(count, null);
    }
}
