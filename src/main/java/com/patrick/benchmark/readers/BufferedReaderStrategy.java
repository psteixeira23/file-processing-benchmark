package com.patrick.benchmark.readers;

import com.patrick.benchmark.processing.LineProcessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BufferedReaderStrategy implements FileReadStrategy {

    @Override
    public String name() {
        return "BufferedReader";
    }

    @Override
    public void read(Path path, Charset charset, LineProcessor processor) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processor.process(line);
            }
        }
    }
}
