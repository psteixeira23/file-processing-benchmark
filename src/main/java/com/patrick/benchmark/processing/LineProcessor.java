package com.patrick.benchmark.processing;

public interface LineProcessor {

    void process(String line);

    ProcessingSummary summary();
}
