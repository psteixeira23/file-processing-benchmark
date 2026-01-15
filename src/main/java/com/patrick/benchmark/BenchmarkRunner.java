package com.patrick.benchmark;

import com.patrick.benchmark.metrics.ExecutionTimer;
import com.patrick.benchmark.metrics.MemoryMeter;
import com.patrick.benchmark.metrics.MemoryUsage;
import com.patrick.benchmark.processing.CsvLineParser;
import com.patrick.benchmark.processing.CsvScenarioProcessor;
import com.patrick.benchmark.processing.LineProcessor;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import com.patrick.benchmark.processing.scenario.ScenarioCatalog;
import com.patrick.benchmark.processing.scenario.ScenarioProcessor;
import com.patrick.benchmark.readers.FileReadStrategy;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BenchmarkRunner {

    private final List<FileReadStrategy> strategies;
    private final ScenarioCatalog scenarioCatalog;
    private final MemoryMeter memoryMeter;

    public BenchmarkRunner(
            List<FileReadStrategy> strategies,
            ScenarioCatalog scenarioCatalog,
            MemoryMeter memoryMeter
    ) {
        this.strategies = List.copyOf(strategies);
        this.scenarioCatalog = scenarioCatalog;
        this.memoryMeter = memoryMeter;
    }

    public BenchmarkReport run(Path path, Charset charset, ProcessingMode mode) {
        List<BenchmarkResult> results = new ArrayList<>();

        for (FileReadStrategy strategy : strategies) {
            BenchmarkResult result = mode == ProcessingMode.SINGLE_PASS
                    ? runSinglePass(strategy, path, charset)
                    : runIsolated(strategy, path, charset);
            results.add(result);
        }

        return new BenchmarkReport(mode, results);
    }

    private BenchmarkResult runSinglePass(FileReadStrategy strategy, Path path, Charset charset) {
        LineProcessor processor = newProcessor(scenarioCatalog.createScenarios());
        long memoryBefore = memoryMeter.usedBytes();
        long startNanos = System.nanoTime();
        String errorMessage = null;
        boolean success = true;

        try {
            strategy.read(path, charset, processor);
        } catch (Exception ex) {
            success = false;
            errorMessage = ex.getMessage();
        }

        long durationNanos = ExecutionTimer.elapsedNanos(startNanos, System.nanoTime());
        long memoryAfter = memoryMeter.usedBytes();
        long memoryDelta = MemoryUsage.deltaBytes(memoryBefore, memoryAfter);
        ProcessingSummary summary = processor.summary();
        long errorCount = summary.invalidLines() + (success ? 0 : 1);

        return new BenchmarkResult(
                strategy.name(),
                durationNanos,
                memoryDelta,
                summary,
                errorCount,
                errorMessage
        );
    }

    private BenchmarkResult runIsolated(FileReadStrategy strategy, Path path, Charset charset) {
        List<ScenarioReport> scenarioReports = new ArrayList<>();
        long memoryBefore = memoryMeter.usedBytes();
        long totalDurationNanos = 0L;
        long recordsProcessed = 0L;
        long invalidLines = 0L;
        long errorCount = 0L;
        String errorMessage = null;

        List<ScenarioProcessor> scenarios = scenarioCatalog.createScenarios();
        for (int i = 0; i < scenarios.size(); i++) {
            LineProcessor processor = newProcessor(List.of(scenarios.get(i)));
            long startNanos = System.nanoTime();
            boolean success = true;

            try {
                strategy.read(path, charset, processor);
            } catch (Exception ex) {
                success = false;
                errorMessage = ex.getMessage();
            }

            long durationNanos = ExecutionTimer.elapsedNanos(startNanos, System.nanoTime());
            totalDurationNanos += durationNanos;

            ProcessingSummary summary = processor.summary();
            if (i == 0) {
                recordsProcessed = summary.recordsProcessed();
                invalidLines = summary.invalidLines();
            }

            ScenarioReport report = summary.scenarios().get(0);
            scenarioReports.add(new ScenarioReport(
                    report.name(),
                    durationNanos,
                    report.count(),
                    report.breakdown()
            ));

            if (!success) {
                errorCount++;
                break;
            }
        }

        long memoryAfter = memoryMeter.usedBytes();
        long memoryDelta = MemoryUsage.deltaBytes(memoryBefore, memoryAfter);
        ProcessingSummary summary = new ProcessingSummary(recordsProcessed, invalidLines, scenarioReports);
        long totalErrors = invalidLines + errorCount;

        return new BenchmarkResult(
                strategy.name(),
                totalDurationNanos,
                memoryDelta,
                summary,
                totalErrors,
                errorMessage
        );
    }

    private LineProcessor newProcessor(List<ScenarioProcessor> scenarios) {
        return new CsvScenarioProcessor(scenarios, new CsvLineParser());
    }
}
