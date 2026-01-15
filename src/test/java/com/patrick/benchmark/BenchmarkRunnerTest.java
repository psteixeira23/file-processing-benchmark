package com.patrick.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.patrick.benchmark.metrics.MemoryMeter;
import com.patrick.benchmark.processing.LineProcessor;
import com.patrick.benchmark.processing.ProcessingSummary;
import com.patrick.benchmark.processing.ScenarioReport;
import com.patrick.benchmark.processing.scenario.DefaultScenarioCatalog;
import com.patrick.benchmark.readers.FileReadStrategy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class BenchmarkRunnerTest {

    @Test
    void shouldRunSinglePassAndIsolatedModes() {
        FileReadStrategy strategy = new FileReadStrategy() {
            @Override
            public String name() {
                return "TestStrategy";
            }

            @Override
            public void read(Path path, java.nio.charset.Charset charset, LineProcessor processor) {
                processor.process("uf,tipo_doenca,local_obito,faixa_etaria,sexo,total");
                processor.process("AC,OUTRAS,DOMICILIO,\"< 9\",F,11");
                processor.process("AL,COVID,OUTRO,10-19,I,3");
            }
        };

        MemoryMeter memoryMeter = () -> 0L;
        BenchmarkRunner runner = new BenchmarkRunner(
                List.of(strategy),
                new DefaultScenarioCatalog(),
                memoryMeter
        );

        BenchmarkReport singlePass = runner.run(
                Path.of("ignored"),
                StandardCharsets.UTF_8,
                ProcessingMode.SINGLE_PASS
        );
        BenchmarkResult singleResult = singlePass.results().get(0);
        ProcessingSummary singleSummary = singleResult.summary();

        assertEquals(2L, singleSummary.recordsProcessed());
        assertEquals(0L, singleSummary.invalidLines());

        Map<String, ScenarioReport> singleScenarioMap = singleSummary.scenarios().stream()
                .collect(Collectors.toMap(ScenarioReport::name, Function.identity()));
        assertEquals(2L, singleScenarioMap.get("Total Records").count());
        assertEquals(1L, singleScenarioMap.get("Non Hospital or Home").count());

        BenchmarkReport isolated = runner.run(
                Path.of("ignored"),
                StandardCharsets.UTF_8,
                ProcessingMode.ISOLATED
        );
        BenchmarkResult isolatedResult = isolated.results().get(0);
        ProcessingSummary isolatedSummary = isolatedResult.summary();

        assertEquals(2L, isolatedSummary.recordsProcessed());
        assertEquals(0L, isolatedSummary.invalidLines());
        assertEquals(5, isolatedSummary.scenarios().size());
        assertNotNull(isolatedSummary.scenarios().get(0).name());
    }

    @Test
    void shouldCaptureErrorInSinglePass() {
        FileReadStrategy strategy = new FileReadStrategy() {
            @Override
            public String name() {
                return "FailingStrategy";
            }

            @Override
            public void read(Path path, java.nio.charset.Charset charset, LineProcessor processor) {
                processor.process("uf,tipo_doenca,local_obito,faixa_etaria,sexo,total");
                processor.process("AC,OUTRAS,DOMICILIO,\"< 9\",F,11");
                throw new IllegalStateException("boom");
            }
        };

        MemoryMeter memoryMeter = () -> 0L;
        BenchmarkRunner runner = new BenchmarkRunner(
                List.of(strategy),
                new DefaultScenarioCatalog(),
                memoryMeter
        );

        BenchmarkReport report = runner.run(
                Path.of("ignored"),
                StandardCharsets.UTF_8,
                ProcessingMode.SINGLE_PASS
        );
        BenchmarkResult result = report.results().get(0);

        assertEquals(1L, result.errorCount());
        assertEquals("boom", result.errorMessage());
        assertEquals(1L, result.summary().recordsProcessed());
    }

    @Test
    void shouldStopIsolatedOnError() {
        FileReadStrategy strategy = new FileReadStrategy() {
            @Override
            public String name() {
                return "FailingStrategy";
            }

            @Override
            public void read(Path path, java.nio.charset.Charset charset, LineProcessor processor) {
                processor.process("uf,tipo_doenca,local_obito,faixa_etaria,sexo,total");
                throw new IllegalStateException("boom");
            }
        };

        MemoryMeter memoryMeter = () -> 0L;
        BenchmarkRunner runner = new BenchmarkRunner(
                List.of(strategy),
                new DefaultScenarioCatalog(),
                memoryMeter
        );

        BenchmarkReport report = runner.run(
                Path.of("ignored"),
                StandardCharsets.UTF_8,
                ProcessingMode.ISOLATED
        );
        BenchmarkResult result = report.results().get(0);

        assertEquals(1L, result.errorCount());
        assertEquals("boom", result.errorMessage());
        assertEquals(1, result.summary().scenarios().size());
    }
}
