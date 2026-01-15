package com.patrick.benchmark.reporting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.patrick.benchmark.BenchmarkReport;
import com.patrick.benchmark.BenchmarkResult;
import com.patrick.benchmark.ProcessingMode;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class ConsoleReportPrinterTest {

    @Test
    void shouldPrintReportWithoutErrors() {
        Logger logger = Logger.getLogger(ConsoleReportPrinter.class.getName());
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        ScenarioReport scenario = new ScenarioReport("Total Records", 5_000_000L, 2L, Map.of());
        ProcessingSummary summary = new ProcessingSummary(2L, 0L, List.of(scenario));
        BenchmarkResult result = new BenchmarkResult("Test", 10_000_000L, 1024L, summary, 0L, null);
        BenchmarkReport report = new BenchmarkReport(ProcessingMode.SINGLE_PASS, List.of(result));

        ConsoleReportPrinter printer = new ConsoleReportPrinter();
        assertDoesNotThrow(() -> printer.print(List.of(report)));
    }

    @Test
    void shouldLogErrorMessageWhenPresent() {
        Logger logger = Logger.getLogger(ConsoleReportPrinter.class.getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        ScenarioReport scenario = new ScenarioReport("Total Records", 1_000_000L, 1L, Map.of());
        ProcessingSummary summary = new ProcessingSummary(1L, 0L, List.of(scenario));
        BenchmarkResult result = new BenchmarkResult("Test", 2_000_000L, 0L, summary, 1L, "boom");
        BenchmarkReport report = new BenchmarkReport(ProcessingMode.SINGLE_PASS, List.of(result));

        ConsoleReportPrinter printer = new ConsoleReportPrinter();
        assertDoesNotThrow(() -> printer.print(List.of(report)));
    }

    @Test
    void shouldHandleMissingScenarioInComparison() {
        Logger logger = Logger.getLogger(ConsoleReportPrinter.class.getName());
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        ScenarioReport shortScenario = new ScenarioReport("Short", 100_000L, 1L, Map.of());
        ScenarioReport longScenario = new ScenarioReport("Long", 123_456_789_000L, 1_234_567L, Map.of());
        ProcessingSummary summaryWithScenarios = new ProcessingSummary(
                2L,
                0L,
                List.of(shortScenario, longScenario)
        );
        ProcessingSummary summaryWithoutScenarios = new ProcessingSummary(0L, 0L, List.of());
        BenchmarkResult primary = new BenchmarkResult("Primary", 3_000_000L, 0L, summaryWithScenarios, 0L, null);
        BenchmarkResult secondary = new BenchmarkResult("Secondary", 4_000_000L, 0L, summaryWithoutScenarios, 0L, null);
        BenchmarkReport report = new BenchmarkReport(ProcessingMode.SINGLE_PASS, List.of(primary, secondary));

        ConsoleReportPrinter printer = new ConsoleReportPrinter();
        assertDoesNotThrow(() -> printer.print(List.of(report)));
    }
}
