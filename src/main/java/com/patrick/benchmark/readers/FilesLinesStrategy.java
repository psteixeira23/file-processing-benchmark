package com.patrick.benchmark.readers;

import com.patrick.benchmark.processing.LineProcessor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class FilesLinesStrategy implements FileReadStrategy {

    @Override
    public String name() {
        return "Files.lines";
    }

    @Override
    public void read(Path path, Charset charset, LineProcessor processor) throws IOException {
        try (Stream<String> lines = Files.lines(path, charset)) {
            lines.forEach(processor::process);
        }
    }
}
