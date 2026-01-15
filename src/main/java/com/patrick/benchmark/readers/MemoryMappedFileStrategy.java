package com.patrick.benchmark.readers;

import com.patrick.benchmark.processing.LineProcessor;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class MemoryMappedFileStrategy implements FileReadStrategy {

    private static final int CHAR_BUFFER_SIZE = 8 * 1024;

    @Override
    public String name() {
        return "MemoryMapped";
    }

    @Override
    public void read(Path path, Charset charset, LineProcessor processor) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long size = channel.size();
            if (size == 0) {
                return;
            }
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            ByteBufferLineReader decoder = new ByteBufferLineReader(charset, CHAR_BUFFER_SIZE);
            decoder.decode(mapped, true, processor);
            decoder.finish(processor);
        }
    }
}
