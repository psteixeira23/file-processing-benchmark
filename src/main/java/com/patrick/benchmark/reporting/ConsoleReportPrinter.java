package com.patrick.benchmark.reporting;

import com.patrick.benchmark.BenchmarkReport;
import com.patrick.benchmark.BenchmarkResult;
import com.patrick.benchmark.ProcessingMode;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConsoleReportPrinter {

    private static final Logger LOGGER = Logger.getLogger(ConsoleReportPrinter.class.getName());

    public void print(List<BenchmarkReport> reports) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, buildReport(reports));
        }

        for (BenchmarkReport report : reports) {
            for (BenchmarkResult result : report.results()) {
                if (result.errorMessage() != null) {
                    LOGGER.log(Level.WARNING, "Error: {0}", result.errorMessage());
                }
            }
        }
    }

    private static String buildReport(List<BenchmarkReport> reports) {
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append(lineSeparator).append("=== File Processing Benchmark ===").append(lineSeparator);

        for (BenchmarkReport report : reports) {
            builder.append(lineSeparator)
                    .append("Mode: ")
                    .append(report.mode().displayName())
                    .append(lineSeparator)
                    .append("Timing basis: ")
                    .append(formatTimingBasis(report.mode()))
                    .append(lineSeparator);

            for (BenchmarkResult result : report.results()) {
                ProcessingSummary summary = result.summary();
                builder.append("Strategy: ").append(result.strategyName()).append(lineSeparator);
                builder.append("Total time (ms): ")
                        .append(DurationFormatter.formatMillis(result.durationNanos()))
                        .append(lineSeparator);
                builder.append("Memory delta (MB): ").append(formatMemory(result.memoryBytes()))
                        .append(lineSeparator);
                builder.append("Records processed: ").append(summary.recordsProcessed())
                        .append(lineSeparator);
                builder.append("Invalid lines: ").append(summary.invalidLines()).append(lineSeparator);
                builder.append("Errors: ").append(result.errorCount()).append(lineSeparator);
                builder.append("Scenario timings:").append(lineSeparator);

                for (ScenarioReport scenario : summary.scenarios()) {
                    builder.append(formatScenario(scenario)).append(lineSeparator);
                }

                builder.append(lineSeparator);
            }

            builder.append(buildScenarioComparison(report)).append(lineSeparator);
        }

        return builder.toString();
    }

    private static String formatTimingBasis(ProcessingMode mode) {
        if (mode == ProcessingMode.SINGLE_PASS) {
            return "Scenario time reflects processing inside one pass.";
        }
        return "Scenario time includes the full pass for each scenario.";
    }

    private static String buildScenarioComparison(BenchmarkReport report) {
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append("Scenario comparison (").append(report.mode().displayName()).append(")")
                .append(lineSeparator);

        List<String> strategies = report.results().stream()
                .map(BenchmarkResult::strategyName)
                .toList();

        List<String> scenarioNames = report.results().stream()
                .findFirst()
                .map(result -> result.summary().scenarios().stream()
                        .map(ScenarioReport::name)
                        .toList())
                .orElse(List.of());

        ComparisonLayout layout = buildComparisonLayout(report, strategies, scenarioNames);
        builder.append(formatComparisonHeader(layout)).append(lineSeparator);
        for (String scenarioName : scenarioNames) {
            builder.append(formatScenarioRow(scenarioName, layout)).append(lineSeparator);
        }

        return builder.toString();
    }

    private static ComparisonLayout buildComparisonLayout(
            BenchmarkReport report,
            List<String> strategies,
            List<String> scenarioNames
    ) {
        int scenarioWidth = Math.max("Scenario".length(), longestLength(scenarioNames));
        Map<String, Map<String, String>> values = new java.util.LinkedHashMap<>();
        Map<String, Integer> widths = new java.util.LinkedHashMap<>();

        for (String strategy : strategies) {
            Map<String, String> scenarioValues = new java.util.LinkedHashMap<>();
            int maxWidth = strategy.length();
            for (String scenarioName : scenarioNames) {
                ScenarioReport scenario = findScenario(report, strategy, scenarioName);
                String value = formatScenarioCell(scenario);
                scenarioValues.put(scenarioName, value);
                maxWidth = Math.max(maxWidth, value.length());
            }
            values.put(strategy, scenarioValues);
            widths.put(strategy, maxWidth);
        }

        return new ComparisonLayout(scenarioWidth, widths, values);
    }

    private static String formatComparisonHeader(ComparisonLayout layout) {
        StringBuilder header = new StringBuilder();
        header.append(padRight("Scenario", layout.scenarioWidth()));
        for (Map.Entry<String, Integer> entry : layout.strategyWidths().entrySet()) {
            header.append(" | ")
                    .append(padRight(entry.getKey(), entry.getValue()));
        }
        return header.toString();
    }

    private static String formatScenarioRow(String scenarioName, ComparisonLayout layout) {
        StringBuilder row = new StringBuilder();
        row.append(padRight(scenarioName, layout.scenarioWidth()));
        for (Map.Entry<String, Integer> entry : layout.strategyWidths().entrySet()) {
            String strategy = entry.getKey();
            String value = layout.values().get(strategy).getOrDefault(scenarioName, "-");
            row.append(" | ").append(padLeft(value, entry.getValue()));
        }
        return row.toString();
    }

    private static ScenarioReport findScenario(
            BenchmarkReport report,
            String strategyName,
            String scenarioName
    ) {
        for (BenchmarkResult result : report.results()) {
            if (!result.strategyName().equals(strategyName)) {
                continue;
            }
            for (ScenarioReport scenario : result.summary().scenarios()) {
                if (scenario.name().equals(scenarioName)) {
                    return scenario;
                }
            }
        }
        return new ScenarioReport(scenarioName, 0L, 0L, Map.of());
    }

    private static String formatScenarioCell(ScenarioReport scenario) {
        return String.format(
                Locale.ROOT,
                "%s ms / total %d",
                DurationFormatter.formatMillis(scenario.durationNanos()),
                scenario.count()
        );
    }

    private static int longestLength(List<String> values) {
        int max = 0;
        for (String value : values) {
            max = Math.max(max, value.length());
        }
        return max;
    }

    private static String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }

    private static String padLeft(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return " ".repeat(width - value.length()) + value;
    }

    private record ComparisonLayout(
            int scenarioWidth,
            Map<String, Integer> strategyWidths,
            Map<String, Map<String, String>> values
    ) {
    }

    private static String formatScenario(ScenarioReport scenario) {
        StringBuilder builder = new StringBuilder();
        builder.append("  - ")
                .append(scenario.name())
                .append(": time=")
                .append(DurationFormatter.formatMillis(scenario.durationNanos()))
                .append(" ms, count=")
                .append(scenario.count());

        if (!scenario.breakdown().isEmpty()) {
            builder.append(", breakdown=").append(formatBreakdown(scenario.breakdown()));
        }

        return builder.toString();
    }

    private static String formatBreakdown(Map<String, Long> breakdown) {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (Map.Entry<String, Long> entry : breakdown.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }

    private static String formatMemory(long bytes) {
        return String.format(Locale.ROOT, "%.2f", bytesToMb(bytes));
    }

    private static double bytesToMb(long bytes) {
        return bytes / 1024.0 / 1024.0;
    }
}
