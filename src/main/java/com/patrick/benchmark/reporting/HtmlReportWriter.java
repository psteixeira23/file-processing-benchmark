package com.patrick.benchmark.reporting;

import com.patrick.benchmark.BenchmarkReport;
import com.patrick.benchmark.BenchmarkResult;
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
        Files.createDirectories(outputPath.getParent());
        String html = buildHtml(reports);
        Files.writeString(outputPath, html, StandardCharsets.UTF_8);
    }

    private String buildHtml(List<BenchmarkReport> reports) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
                .append("</header>");

        for (BenchmarkReport report : reports) {
            builder.append("<section>")
                    .append("<div class=\"mode-title\">Mode: ")
                    .append(escape(report.mode().displayName()))
                    .append("</div>")
                    .append("<div class=\"mode-subtitle\">")
                    .append(escape(timingBasis(report.mode().displayName())))
                    .append("</div>")
                    .append("<div class=\"card\">")
                    .append("<table class=\"table\"><thead><tr>")
                    .append("<th>Strategy</th><th>Total time (ms)</th>")
                    .append("<th>Memory delta (MB)</th><th>Records</th>")
                    .append("<th>Invalid</th><th>Errors</th>")
                    .append("</tr></thead><tbody>");

            for (BenchmarkResult result : report.results()) {
                ProcessingSummary summary = result.summary();
                builder.append("<tr>")
                        .append("<td><span class=\"badge\">")
                        .append(escape(result.strategyName()))
                        .append("</span></td>")
                        .append("<td>").append(DurationFormatter.formatMillis(result.durationNanos())).append("</td>")
                        .append("<td>").append(formatMemory(result.memoryBytes())).append("</td>")
                        .append("<td>").append(summary.recordsProcessed()).append("</td>")
                        .append("<td>").append(summary.invalidLines()).append("</td>")
                        .append("<td>").append(result.errorCount()).append("</td>")
                        .append("</tr>");
            }

            builder.append("</tbody></table></div>")
                    .append("<div class=\"grid\" style=\"margin-top: 16px;\">");

            for (BenchmarkResult result : report.results()) {
                builder.append("<details class=\"details\">")
                        .append("<summary>")
                        .append(escape(result.strategyName()))
                        .append(" - Scenario timings</summary>")
                        .append("<table class=\"table\" style=\"margin-top: 10px;\">\n")
                        .append("<thead><tr><th>Scenario</th><th>Time (ms)</th>")
                        .append("<th>Total</th><th>Breakdown</th></tr></thead><tbody>");

                for (ScenarioReport scenario : result.summary().scenarios()) {
                    builder.append("<tr>")
                            .append("<td>").append(escape(scenario.name())).append("</td>")
                            .append("<td>").append(DurationFormatter.formatMillis(scenario.durationNanos())).append("</td>")
                            .append("<td>").append(scenario.count()).append("</td>")
                            .append("<td class=\"breakdown\">")
                            .append(escape(formatBreakdown(scenario.breakdown())))
                            .append("</td>")
                            .append("</tr>");
                }

                builder.append("</tbody></table></details>");
            }

            builder.append("</div>")
                    .append(buildComparison(report))
                    .append("</section>");
        }

        builder.append("</main></body></html>");
        return builder.toString();
    }

    private String buildComparison(BenchmarkReport report) {
        List<String> strategies = report.results().stream()
                .map(BenchmarkResult::strategyName)
                .toList();
        List<String> scenarioNames = report.results().stream()
                .findFirst()
                .map(result -> result.summary().scenarios().stream()
                        .map(ScenarioReport::name)
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
                ScenarioReport scenario = findScenario(report, strategy, scenarioName);
                builder.append("<td>")
                        .append(escape(formatScenarioCell(scenario)))
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

    private String formatMemory(long bytes) {
        return String.format(Locale.ROOT, "%.2f", bytes / 1024.0 / 1024.0);
    }

    private String formatScenarioCell(ScenarioReport scenario) {
        return String.format(
                Locale.ROOT,
                "%s ms / total %d",
                DurationFormatter.formatMillis(scenario.durationNanos()),
                scenario.count()
        );
    }

    private ScenarioReport findScenario(BenchmarkReport report, String strategyName, String scenarioName) {
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
}
