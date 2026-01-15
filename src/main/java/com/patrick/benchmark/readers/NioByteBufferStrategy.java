package com.patrick.benchmark.readers;

import com.patrick.benchmark.processing.LineProcessor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;

public final class NioByteBufferStrategy implements FileReadStrategy {

    private static final int BUFFER_SIZE = 8 * 1024;

    @Override
    public String name() {
        return "NIO ByteBuffer";
    }

    @Override
    public void read(Path path, Charset charset, LineProcessor processor) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        ByteBufferLineReader decoder = new ByteBufferLineReader(charset, BUFFER_SIZE);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            while (channel.read(buffer) != -1) {
                buffer.flip();
                decoder.decode(buffer, false, processor);
                buffer.compact();
            }
            buffer.flip();
            decoder.decode(buffer, true, processor);
            decoder.finish(processor);
        }
    }
}
