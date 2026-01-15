package com.patrick.benchmark.processing.scenario;

import com.patrick.benchmark.processing.CsvRecord;
import com.patrick.benchmark.processing.ScenarioData;

public interface ScenarioProcessor {

    String name();

    void process(CsvRecord record);

    ScenarioData data();
}
