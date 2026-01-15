package com.patrick.benchmark.processing.scenario;

import java.util.List;

public final class DefaultScenarioCatalog implements ScenarioCatalog {

    @Override
    public List<ScenarioProcessor> createScenarios() {
        return List.of(
                new TotalRecordsScenario(),
                new NonHospitalOrHomeScenario(),
                new AgeRangeScenario(),
                new SexScenario(),
                new UfScenario()
        );
    }
}
