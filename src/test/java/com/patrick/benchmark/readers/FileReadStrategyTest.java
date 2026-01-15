package com.patrick.benchmark.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.patrick.benchmark.processing.CsvLineParser;
import com.patrick.benchmark.processing.CsvScenarioProcessor;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import com.patrick.benchmark.processing.scenario.DefaultScenarioCatalog;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FileReadStrategyTest {

    @Test
    void shouldReadCsvWithAllStrategies() throws Exception {
        Path file = resourcePath("benchmark-input-test.csv");
        ProcessingSummary expected = expectedSummary(file);
        Map<String, ScenarioReport> expectedScenarios = expected.scenarios().stream()
                .collect(Collectors.toMap(ScenarioReport::name, Function.identity()));

        List<FileReadStrategy> strategies = List.of(
                new BufferedReaderStrategy(),
                new FilesLinesStrategy(),
                new NioByteBufferStrategy(),
                new MemoryMappedFileStrategy()
        );

        for (FileReadStrategy strategy : strategies) {
            CsvScenarioProcessor processor = new CsvScenarioProcessor(
                    new DefaultScenarioCatalog().createScenarios(),
                    new CsvLineParser()
            );
            strategy.read(file, StandardCharsets.UTF_8, processor);
            ProcessingSummary summary = processor.summary();

            assertEquals(expected.recordsProcessed(), summary.recordsProcessed(), strategy.name());
            assertEquals(expected.invalidLines(), summary.invalidLines(), strategy.name());

            Map<String, ScenarioReport> scenarioMap = summary.scenarios().stream()
                    .collect(Collectors.toMap(ScenarioReport::name, Function.identity()));
            assertScenarioMatches(expectedScenarios, scenarioMap, strategy.name());
        }
    }

    private static ProcessingSummary expectedSummary(Path file) throws Exception {
        CsvScenarioProcessor processor = new CsvScenarioProcessor(
                new DefaultScenarioCatalog().createScenarios(),
                new CsvLineParser()
        );
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            processor.process(line);
        }
        return processor.summary();
    }

    private static Path resourcePath(String resourceName) throws Exception {
        URL url = FileReadStrategyTest.class.getClassLoader().getResource(resourceName);
        assertNotNull(url, "Missing test resource: " + resourceName);
        return Path.of(Objects.requireNonNull(url).toURI());
    }

    private static void assertScenarioMatches(
            Map<String, ScenarioReport> expected,
            Map<String, ScenarioReport> actual,
            String strategyName
    ) {
        assertEquals(expected.keySet(), actual.keySet(), strategyName);
        for (Map.Entry<String, ScenarioReport> entry : expected.entrySet()) {
            ScenarioReport expectedReport = entry.getValue();
            ScenarioReport actualReport = actual.get(entry.getKey());
            assertNotNull(actualReport, strategyName + ": " + entry.getKey());
            assertEquals(expectedReport.count(), actualReport.count(), strategyName);
            assertEquals(expectedReport.breakdown(), actualReport.breakdown(), strategyName);
        }
    }
}
