package com.patrick.benchmark.readers;

import com.patrick.benchmark.processing.LineProcessor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

public interface FileReadStrategy {

    String name();

    void read(Path path, Charset charset, LineProcessor processor) throws IOException;
}
