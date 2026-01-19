package com.patrick.benchmark.reporting;

import com.patrick.benchmark.BenchmarkReport;
import com.patrick.benchmark.BenchmarkResult;
import com.patrick.benchmark.ProcessingMode;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HtmlReportWriter {

    public void write(List<BenchmarkReport> reports, Path outputPath) throws IOException {
        writeAggregated(List.of(reports), outputPath);
    }

    public void writeAggregated(List<List<BenchmarkReport>> runs, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        String html = buildHtml(runs);
        Files.writeString(outputPath, html, StandardCharsets.UTF_8);
    }

    private String buildHtml(List<List<BenchmarkReport>> runs) {
        int runCount = Math.max(1, runs.size());
        List<AggregatedReport> reports = aggregateReports(runs);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String timingLabel = runCount > 1 ? "Total time (ms, mean +/- std)" : "Total time (ms)";
        String scenarioLabel = runCount > 1 ? "Time (ms, mean +/- std)" : "Time (ms)";
        String memoryLabel = runCount > 1 ? "Memory delta (MB, mean +/- std)" : "Memory delta (MB)";
        StringBuilder builder = new StringBuilder();
        builder.append("<!doctype html>")
                .append("<html lang=\"en\">")
                .append("<head>")
                .append("<meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>File Processing Benchmark Report</title>")
                .append("<style>")
                .append(css())
                .append("</style>")
                .append("</head>")
                .append("<body><main>")
                .append("<header>")
                .append("<h1>File Processing Benchmark Report</h1>")
                .append("<div class=\"header-meta\">Generated at ")
                .append(escape(timestamp))
                .append("</div>")
                .append("<div class=\"header-meta\">Runs: ")
                .append(runCount)
                .append(runCount > 1 ? " (mean +/- std dev)</div>" : "</div>")
                .append("</header>");

        for (AggregatedReport report : reports) {
            builder.append("<section>")
                    .append("<div class=\"mode-title\">Mode: ")
                    .append(escape(report.mode().displayName()))
                    .append("</div>")
                    .append("<div class=\"mode-subtitle\">")
                    .append(escape(timingBasis(report.mode().displayName())))
                    .append("</div>")
                    .append("<div class=\"card\">")
                    .append("<table class=\"table\"><thead><tr>")
                    .append("<th>Strategy</th><th>").append(escape(timingLabel)).append("</th>")
                    .append("<th>").append(escape(memoryLabel)).append("</th><th>Records</th>")
                    .append("<th>Invalid</th><th>Errors</th>")
                    .append("</tr></thead><tbody>");

            for (AggregatedResult result : report.results()) {
                ProcessingSummary summary = result.summary();
                builder.append("<tr>")
                        .append("<td><span class=\"badge\">")
                        .append(escape(result.strategyName()))
                        .append("</span></td>")
                        .append("<td>").append(formatDuration(result.duration(), runCount)).append("</td>")
                        .append("<td>").append(formatMemory(result.memory(), runCount)).append("</td>")
                        .append("<td>").append(summary.recordsProcessed()).append("</td>")
                        .append("<td>").append(summary.invalidLines()).append("</td>")
                        .append("<td>").append(result.errorCount()).append("</td>")
                        .append("</tr>");
            }

            builder.append("</tbody></table></div>")
                    .append("<div class=\"grid\" style=\"margin-top: 16px;\">");

            for (AggregatedResult result : report.results()) {
                builder.append("<details class=\"details\">")
                        .append("<summary>")
                        .append(escape(result.strategyName()))
                        .append(" - Scenario timings</summary>")
                        .append("<table class=\"table\" style=\"margin-top: 10px;\">\n")
                        .append("<thead><tr><th>Scenario</th><th>").append(escape(scenarioLabel)).append("</th>")
                        .append("<th>Total</th><th>Breakdown</th></tr></thead><tbody>");

                for (ScenarioAggregate scenario : result.scenarios()) {
                    builder.append("<tr>")
                            .append("<td>").append(escape(scenario.name())).append("</td>")
                            .append("<td>").append(formatDuration(scenario.duration(), runCount)).append("</td>")
                            .append("<td>").append(scenario.count()).append("</td>")
                            .append("<td class=\"breakdown\">")
                            .append(escape(formatBreakdown(scenario.breakdown())))
                            .append("</td>")
                            .append("</tr>");
                }

                builder.append("</tbody></table></details>");
            }

            builder.append("</div>")
                    .append(buildComparison(report, runCount))
                    .append("</section>");
        }

        builder.append("</main></body></html>");
        return builder.toString();
    }

    private String buildComparison(AggregatedReport report, int runCount) {
        List<String> strategies = report.results().stream()
                .map(AggregatedResult::strategyName)
                .toList();
        List<String> scenarioNames = report.results().stream()
                .findFirst()
                .map(result -> result.scenarios().stream()
                        .map(ScenarioAggregate::name)
                        .toList())
                .orElse(List.of());

        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"card\" style=\"margin-top: 18px;\">")
                .append("<div class=\"mode-subtitle\">Scenario comparison</div>")
                .append("<table class=\"table\"><thead><tr>")
                .append("<th>Scenario</th>");

        for (String strategy : strategies) {
            builder.append("<th>").append(escape(strategy)).append("</th>");
        }

        builder.append("</tr></thead><tbody>");

        for (String scenarioName : scenarioNames) {
            builder.append("<tr>")
                    .append("<td>").append(escape(scenarioName)).append("</td>");
            for (String strategy : strategies) {
                ScenarioAggregate scenario = findScenario(report, strategy, scenarioName);
                builder.append("<td>")
                        .append(escape(formatScenarioCell(scenario, runCount)))
                        .append("</td>");
            }
            builder.append("</tr>");
        }

        builder.append("</tbody></table></div>");
        return builder.toString();
    }

    private String timingBasis(String modeDisplayName) {
        if (modeDisplayName.startsWith("Single-Pass")) {
            return "Scenario time reflects processing inside one pass.";
        }
        return "Scenario time includes the full pass for each scenario.";
    }

    private String formatMemory(Stats stats, int runCount) {
        if (runCount <= 1) {
            return String.format(Locale.ROOT, "%.2f", bytesToMb(stats.mean()));
        }
        return String.format(
                Locale.ROOT,
                "%.2f +/- %.2f",
                bytesToMb(stats.mean()),
                bytesToMb(stats.std())
        );
    }

    private String formatScenarioCell(ScenarioAggregate scenario, int runCount) {
        return String.format(
                Locale.ROOT,
                "%s ms / total %d",
                formatDuration(scenario.duration(), runCount),
                scenario.count()
        );
    }

    private ScenarioAggregate findScenario(
            AggregatedReport report,
            String strategyName,
            String scenarioName
    ) {
        for (AggregatedResult result : report.results()) {
            if (!result.strategyName().equals(strategyName)) {
                continue;
            }
            for (ScenarioAggregate scenario : result.scenarios()) {
                if (scenario.name().equals(scenarioName)) {
                    return scenario;
                }
            }
        }
        return new ScenarioAggregate(scenarioName, new Stats(0.0, 0.0), 0L, Map.of());
    }

    private String formatBreakdown(Map<String, Long> breakdown) {
        if (breakdown == null || breakdown.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Long> entry : breakdown.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        builder.append("}");
        return builder.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String css() {
        return """
                :root {
                  --bg: #f4f1ec;
                  --ink: #1b1b1b;
                  --muted: #5f5a53;
                  --card: #ffffff;
                  --accent: #1f5f5b;
                  --border: #d8d2c8;
                }
                * {
                  box-sizing: border-box;
                }
                body {
                  margin: 0;
                  font-family: \"Space Grotesk\", \"Noto Sans\", sans-serif;
                  color: var(--ink);
                  background: linear-gradient(180deg, #f4f1ec 0%, #f9f7f3 100%);
                }
                main {
                  max-width: 1200px;
                  margin: 0 auto;
                  padding: 40px 24px 80px;
                }
                header {
                  margin-bottom: 32px;
                }
                h1 {
                  margin: 0 0 8px;
                  font-size: 32px;
                  letter-spacing: -0.5px;
                }
                .header-meta {
                  color: var(--muted);
                }
                section {
                  margin-bottom: 48px;
                }
                .mode-title {
                  font-size: 24px;
                  margin-bottom: 8px;
                }
                .mode-subtitle {
                  color: var(--muted);
                  margin-bottom: 16px;
                }
                .card {
                  background: var(--card);
                  border: 1px solid var(--border);
                  border-radius: 12px;
                  padding: 16px;
                  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
                }
                .grid {
                  display: grid;
                  gap: 16px;
                }
                .table {
                  width: 100%;
                  border-collapse: collapse;
                  font-size: 14px;
                }
                .table th,
                .table td {
                  border-bottom: 1px solid var(--border);
                  padding: 10px 8px;
                  text-align: left;
                }
                .table th {
                  color: var(--muted);
                  font-weight: 600;
                }
                .badge {
                  display: inline-flex;
                  padding: 2px 8px;
                  border-radius: 999px;
                  background: rgba(31, 95, 91, 0.12);
                  color: var(--accent);
                  font-size: 12px;
                  font-weight: 600;
                }
                .details {
                  border: 1px solid var(--border);
                  border-radius: 10px;
                  padding: 12px 16px;
                  background: #fffdf9;
                }
                .details summary {
                  cursor: pointer;
                  font-weight: 600;
                  display: block;
                  width: 100%;
                  padding: 4px 0;
                }
                .breakdown {
                  color: var(--muted);
                  font-size: 13px;
                  margin-top: 8px;
                }
                @media (max-width: 900px) {
                  main {
                    padding: 24px 16px 48px;
                  }
                  h1 {
                    font-size: 26px;
                  }
                }
                """;
    }

    private List<AggregatedReport> aggregateReports(List<List<BenchmarkReport>> runs) {
        if (runs.isEmpty()) {
            return List.of();
        }
        List<BenchmarkReport> baseline = runs.get(0);
        List<Map<ProcessingMode, Map<String, BenchmarkResult>>> indexedRuns = new java.util.ArrayList<>();

        for (List<BenchmarkReport> run : runs) {
            Map<ProcessingMode, Map<String, BenchmarkResult>> byMode = new java.util.EnumMap<>(ProcessingMode.class);
            for (BenchmarkReport report : run) {
                Map<String, BenchmarkResult> byStrategy = new java.util.LinkedHashMap<>();
                for (BenchmarkResult result : report.results()) {
                    byStrategy.put(result.strategyName(), result);
                }
                byMode.put(report.mode(), byStrategy);
            }
            indexedRuns.add(byMode);
        }

        List<AggregatedReport> aggregated = new java.util.ArrayList<>();
        for (BenchmarkReport report : baseline) {
            List<AggregatedResult> results = new java.util.ArrayList<>();
            for (BenchmarkResult baselineResult : report.results()) {
                String strategyName = baselineResult.strategyName();
                List<Long> durations = new java.util.ArrayList<>();
                List<Long> memoryBytes = new java.util.ArrayList<>();
                List<Long> errorCounts = new java.util.ArrayList<>();

                Map<String, List<Long>> scenarioDurations = new java.util.LinkedHashMap<>();
                for (ScenarioReport scenario : baselineResult.summary().scenarios()) {
                    scenarioDurations.put(scenario.name(), new java.util.ArrayList<>());
                }

                for (Map<ProcessingMode, Map<String, BenchmarkResult>> run : indexedRuns) {
                    Map<String, BenchmarkResult> modeResults = run.get(report.mode());
                    if (modeResults == null) {
                        continue;
                    }
                    BenchmarkResult result = modeResults.get(strategyName);
                    if (result == null) {
                        continue;
                    }
                    durations.add(result.durationNanos());
                    memoryBytes.add(result.memoryBytes());
                    errorCounts.add(result.errorCount());
                    for (ScenarioReport scenario : result.summary().scenarios()) {
                        List<Long> values = scenarioDurations.get(scenario.name());
                        if (values != null) {
                            values.add(scenario.durationNanos());
                        }
                    }
                }

                List<ScenarioAggregate> scenarios = new java.util.ArrayList<>();
                for (ScenarioReport scenario : baselineResult.summary().scenarios()) {
                    List<Long> values = scenarioDurations.getOrDefault(scenario.name(), List.of());
                    scenarios.add(new ScenarioAggregate(
                            scenario.name(),
                            Stats.from(values),
                            scenario.count(),
                            scenario.breakdown()
                    ));
                }

                results.add(new AggregatedResult(
                        strategyName,
                        Stats.from(durations),
                        Stats.from(memoryBytes),
                        baselineResult.summary(),
                        sum(errorCounts),
                        baselineResult.errorMessage(),
                        scenarios
                ));
            }
            aggregated.add(new AggregatedReport(report.mode(), results));
        }

        return aggregated;
    }

    private String formatDuration(Stats stats, int runCount) {
        if (runCount <= 1) {
            return DurationFormatter.formatMillis(Math.round(stats.mean()));
        }
        return String.format(
                Locale.ROOT,
                "%s +/- %s",
                DurationFormatter.formatMillis(Math.round(stats.mean())),
                DurationFormatter.formatMillis(Math.round(stats.std()))
        );
    }

    private double bytesToMb(double bytes) {
        return bytes / 1024.0 / 1024.0;
    }

    private long sum(List<Long> values) {
        long total = 0L;
        for (Long value : values) {
            if (value != null) {
                total += value;
            }
        }
        return total;
    }

    private record AggregatedReport(ProcessingMode mode, List<AggregatedResult> results) {
    }

    private record AggregatedResult(
            String strategyName,
            Stats duration,
            Stats memory,
            ProcessingSummary summary,
            long errorCount,
            String errorMessage,
            List<ScenarioAggregate> scenarios
    ) {
    }

    private record ScenarioAggregate(
            String name,
            Stats duration,
            long count,
            Map<String, Long> breakdown
    ) {
    }

    private record Stats(double mean, double std) {
        private static Stats from(List<Long> values) {
            int count = values.size();
            if (count == 0) {
                return new Stats(0.0, 0.0);
            }
            double sum = 0.0;
            for (Long value : values) {
                sum += value;
            }
            double mean = sum / count;
            if (count == 1) {
                return new Stats(mean, 0.0);
            }
            double variance = 0.0;
            for (Long value : values) {
                double delta = value - mean;
                variance += delta * delta;
            }
            variance /= (count - 1);
            return new Stats(mean, Math.sqrt(variance));
        }
    }
}
