package com.patrick.benchmark;

import com.patrick.benchmark.metrics.RuntimeMemoryMeter;
import com.patrick.benchmark.processing.scenario.DefaultScenarioCatalog;
import com.patrick.benchmark.readers.BufferedReaderStrategy;
import com.patrick.benchmark.readers.FileReadStrategy;
import com.patrick.benchmark.readers.FilesLinesStrategy;
import com.patrick.benchmark.readers.MemoryMappedFileStrategy;
import com.patrick.benchmark.readers.NioByteBufferStrategy;
import com.patrick.benchmark.reporting.ConsoleReportPrinter;
import com.patrick.benchmark.reporting.HtmlReportWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private App() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        Path path = Path.of(args[0]);
        if (Files.notExists(path)) {
            LOGGER.log(Level.WARNING, "File not found: {0}", path);
            return;
        }

        Charset charset = StandardCharsets.UTF_8;
        ProcessingMode mode = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--mode=")) {
                mode = ProcessingMode.fromLabel(arg.substring("--mode=".length()));
                if (mode == null) {
                    LOGGER.log(Level.WARNING, "Unknown mode: {0}", arg);
                    return;
                }
            } else if (arg.startsWith("--charset=")) {
                charset = Charset.forName(arg.substring("--charset=".length()));
            } else if (i == 1) {
                charset = Charset.forName(arg);
            }
        }

        List<FileReadStrategy> strategies = List.of(
                new BufferedReaderStrategy(),
                new FilesLinesStrategy(),
                new NioByteBufferStrategy(),
                new MemoryMappedFileStrategy()
        );

        BenchmarkRunner runner = new BenchmarkRunner(
                strategies,
                new DefaultScenarioCatalog(),
                new RuntimeMemoryMeter()
        );

        List<BenchmarkReport> reports = new ArrayList<>();
        if (mode == null) {
            reports.add(runner.run(path, charset, ProcessingMode.SINGLE_PASS));
            reports.add(runner.run(path, charset, ProcessingMode.ISOLATED));
        } else {
            reports.add(runner.run(path, charset, mode));
        }

        new ConsoleReportPrinter().print(reports);
        writeHtmlReport(reports);
    }

    private static void printUsage() {
        LOGGER.info("Usage: com.patrick.benchmark.App <file> [--mode=single|isolated] [--charset=UTF-8]");
        LOGGER.info("Example: com.patrick.benchmark.App ./src/main/resources/benchmark-input.csv --mode=isolated");
    }

    private static void writeHtmlReport(List<BenchmarkReport> reports) {
        HtmlReportWriter writer = new HtmlReportWriter();
        Path outputPath = Path.of("reports", "benchmark-report.html");
        try {
            writer.write(reports, outputPath);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to write HTML report: {0}", ex.getMessage());
        }
    }
}
