package com.patrick.benchmark.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.patrick.benchmark.processing.LineProcessor;
import com.patrick.benchmark.processing.ProcessingSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MemoryMappedFileStrategyTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnWhenFileIsEmpty() throws Exception {
        Path file = tempDir.resolve("empty.csv");
        Files.write(file, new byte[0]);

        CollectingProcessor processor = new CollectingProcessor();
        new MemoryMappedFileStrategy().read(file, StandardCharsets.UTF_8, processor);

        assertEquals(List.of(), processor.lines());
    }

    @Test
    void shouldReadLinesFromMappedFile() throws Exception {
        Path file = tempDir.resolve("sample.csv");
        Files.writeString(file, "A\nB", StandardCharsets.UTF_8);

        CollectingProcessor processor = new CollectingProcessor();
        new MemoryMappedFileStrategy().read(file, StandardCharsets.UTF_8, processor);

        assertEquals(List.of("A", "B"), processor.lines());
    }

    private static final class CollectingProcessor implements LineProcessor {

        private final List<String> lines = new ArrayList<>();

        @Override
        public void process(String line) {
            lines.add(line);
        }

        @Override
        public ProcessingSummary summary() {
            return new ProcessingSummary(lines.size(), 0L, List.of());
        }

        private List<String> lines() {
            return List.copyOf(lines);
        }
    }
}
