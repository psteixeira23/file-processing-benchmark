package com.patrick.benchmark.reporting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.patrick.benchmark.BenchmarkReport;
import com.patrick.benchmark.BenchmarkResult;
import com.patrick.benchmark.ProcessingMode;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HtmlReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteHtmlReport() throws Exception {
        ScenarioReport scenario = new ScenarioReport("Total Records", 12_000_000L, 3L, Map.of("A", 1L));
        ProcessingSummary summary = new ProcessingSummary(3L, 0L, List.of(scenario));
        BenchmarkResult result = new BenchmarkResult("Test", 15_000_000L, 2048L, summary, 0L, null);
        BenchmarkReport report = new BenchmarkReport(ProcessingMode.SINGLE_PASS, List.of(result));

        HtmlReportWriter writer = new HtmlReportWriter();
        Path output = tempDir.resolve("report.html");
        writer.write(List.of(report), output);

        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("File Processing Benchmark Report"));
        assertTrue(content.contains("Total Records"));
    }

    @Test
    void shouldHandleMissingScenarioAndNulls() throws Exception {
        ScenarioReport scenario = new ScenarioReport("Present", 10_000_000L, 1L, Map.of("X", 2L));
        ProcessingSummary summaryWithScenario = new ProcessingSummary(1L, 0L, List.of(scenario));
        ProcessingSummary summaryWithoutScenario = new ProcessingSummary(0L, 0L, List.of());
        BenchmarkResult primary = new BenchmarkResult("Primary", 12_000_000L, 0L, summaryWithScenario, 0L, null);
        BenchmarkResult secondary = new BenchmarkResult("Secondary", 9_000_000L, 0L, summaryWithoutScenario, 0L, null);
        BenchmarkReport report = new BenchmarkReport(ProcessingMode.SINGLE_PASS, List.of(primary, secondary));

        HtmlReportWriter writer = new HtmlReportWriter();
        Path output = tempDir.resolve("report-missing.html");
        writer.write(List.of(report), output);

        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("0.000000 ms / total 0"));

        Method formatBreakdown = HtmlReportWriter.class.getDeclaredMethod("formatBreakdown", Map.class);
        formatBreakdown.setAccessible(true);
        assertEquals("", formatBreakdown.invoke(writer, new Object[] { null }));

        Method escape = HtmlReportWriter.class.getDeclaredMethod("escape", String.class);
        escape.setAccessible(true);
        assertEquals("", escape.invoke(writer, new Object[] { null }));
    }
}
