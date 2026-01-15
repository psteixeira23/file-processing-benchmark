package com.patrick.benchmark.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.patrick.benchmark.processing.LineProcessor;
import com.patrick.benchmark.processing.ProcessingSummary;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ByteBufferLineReaderTest {

    @Test
    void shouldDecodeLinesAndIgnoreCarriageReturn() throws Exception {
        ByteBufferLineReader reader = new ByteBufferLineReader(StandardCharsets.UTF_8, 4);
        CollectingProcessor processor = new CollectingProcessor();
        ByteBuffer buffer = ByteBuffer.wrap("A\r\nB".getBytes(StandardCharsets.UTF_8));

        reader.decode(buffer, true, processor);
        reader.finish(processor);

        assertEquals(List.of("A", "B"), processor.lines());
    }

    @Test
    void shouldHandleOverflowDuringDecode() throws Exception {
        ByteBufferLineReader reader = new ByteBufferLineReader(StandardCharsets.UTF_8, 1);
        CollectingProcessor processor = new CollectingProcessor();

        reader.decode(ByteBuffer.wrap("AB\n".getBytes(StandardCharsets.UTF_8)), true, processor);
        reader.finish(processor);

        assertEquals(List.of("AB"), processor.lines());
    }

    @Test
    void shouldThrowOnInvalidUtf8Sequence() {
        ByteBufferLineReader reader = new ByteBufferLineReader(StandardCharsets.UTF_8, 2);
        CollectingProcessor processor = new CollectingProcessor();

        ByteBuffer invalid = ByteBuffer.wrap(new byte[] { (byte) 0xC3, (byte) 0x28 });

        assertThrows(CharacterCodingException.class, () -> reader.decode(invalid, true, processor));
    }

    @Test
    void shouldHandleOverflowDuringFlush() throws Exception {
        Charset charset = customCharset(cs -> new FixedDecoder(
                cs,
                new CoderResult[] { CoderResult.UNDERFLOW },
                new CoderResult[] { CoderResult.OVERFLOW, CoderResult.UNDERFLOW }
        ));
        ByteBufferLineReader reader = new ByteBufferLineReader(charset, 2);
        CollectingProcessor processor = new CollectingProcessor();

        reader.decode(ByteBuffer.allocate(0), true, processor);
        reader.finish(processor);

        assertEquals(List.of(), processor.lines());
    }

    @Test
    void shouldThrowOnFlushError() throws Exception {
        Charset charset = customCharset(cs -> new FixedDecoder(
                cs,
                new CoderResult[] { CoderResult.UNDERFLOW },
                new CoderResult[] { CoderResult.unmappableForLength(1) }
        ));
        ByteBufferLineReader reader = new ByteBufferLineReader(charset, 2);
        CollectingProcessor processor = new CollectingProcessor();

        reader.decode(ByteBuffer.allocate(0), true, processor);

        assertThrows(CharacterCodingException.class, () -> reader.finish(processor));
    }

    @Test
    void shouldMapCoderResultsToExceptions() throws Exception {
        Method method = ByteBufferLineReader.class.getDeclaredMethod("toException", CoderResult.class);
        method.setAccessible(true);

        CharacterCodingException malformed = (CharacterCodingException) method.invoke(
                null,
                CoderResult.malformedForLength(1)
        );
        CharacterCodingException unmappable = (CharacterCodingException) method.invoke(
                null,
                CoderResult.unmappableForLength(1)
        );
        CharacterCodingException fallback = (CharacterCodingException) method.invoke(
                null,
                CoderResult.UNDERFLOW
        );

        assertEquals(MalformedInputException.class, malformed.getClass());
        assertEquals(UnmappableCharacterException.class, unmappable.getClass());
        assertEquals(CharacterCodingException.class, fallback.getClass());
    }

    private static Charset customCharset(Function<Charset, CharsetDecoder> decoderFactory) {
        return new Charset("x-test", new String[0]) {
            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return decoderFactory.apply(this);
            }

            @Override
            public CharsetEncoder newEncoder() {
                return StandardCharsets.UTF_8.newEncoder();
            }
        };
    }

    private static final class FixedDecoder extends CharsetDecoder {

        private final CoderResult[] decodeResults;
        private final CoderResult[] flushResults;
        private int decodeIndex;
        private int flushIndex;

        private FixedDecoder(Charset charset, CoderResult[] decodeResults, CoderResult[] flushResults) {
            super(charset, 1.0f, 1.0f);
            this.decodeResults = decodeResults;
            this.flushResults = flushResults;
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            if (in.hasRemaining() && out.hasRemaining()) {
                out.put((char) in.get());
            }
            int index = Math.min(decodeIndex, decodeResults.length - 1);
            decodeIndex++;
            return decodeResults[index];
        }

        @Override
        protected CoderResult implFlush(CharBuffer out) {
            int index = Math.min(flushIndex, flushResults.length - 1);
            flushIndex++;
            return flushResults[index];
        }
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
