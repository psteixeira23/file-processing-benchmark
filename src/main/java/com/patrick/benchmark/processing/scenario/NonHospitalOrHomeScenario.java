package com.patrick.benchmark.processing.scenario;

import com.patrick.benchmark.processing.CsvRecord;
import com.patrick.benchmark.processing.ScenarioData;

public final class NonHospitalOrHomeScenario implements ScenarioProcessor {

    private long count;

    @Override
    public String name() {
        return "Non Hospital or Home";
    }

    @Override
    public void process(CsvRecord record) {
        String location = record.deathLocation();
        if (!"HOSPITAL".equalsIgnoreCase(location) && !"DOMICILIO".equalsIgnoreCase(location)) {
            count++;
        }
    }

    @Override
    public ScenarioData data() {
        return new ScenarioData(count, null);
    }
}
