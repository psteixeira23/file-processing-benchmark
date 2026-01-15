package com.patrick.benchmark.readers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

import com.patrick.benchmark.processing.LineProcessor;

final class ByteBufferLineReader {

    private final CharsetDecoder decoder;
    private final CharBuffer charBuffer;
    private final StringBuilder lineBuffer;

    ByteBufferLineReader(Charset charset, int bufferSize) {
        this.decoder = charset.newDecoder();
        this.charBuffer = CharBuffer.allocate(bufferSize);
        this.lineBuffer = new StringBuilder(bufferSize);
    }

    void decode(ByteBuffer byteBuffer, boolean endOfInput, LineProcessor processor)
            throws CharacterCodingException {
        boolean done = false;
        while (!done) {
            CoderResult result = decoder.decode(byteBuffer, charBuffer, endOfInput);
            charBuffer.flip();
            drainChars(processor);
            charBuffer.clear();

            if (result.isUnderflow()) {
                done = true;
            } else if (!result.isOverflow()) {
                throw toException(result);
            }
        }
    }

    void finish(LineProcessor processor) throws CharacterCodingException {
        boolean done = false;
        while (!done) {
            CoderResult result = decoder.flush(charBuffer);
            charBuffer.flip();
            drainChars(processor);
            charBuffer.clear();

            if (result.isUnderflow()) {
                done = true;
            } else if (!result.isOverflow()) {
                throw toException(result);
            }
        }

        if (!lineBuffer.isEmpty()) {
            processor.process(lineBuffer.toString());
            lineBuffer.setLength(0);
        }
    }

    private void drainChars(LineProcessor processor) {
        while (charBuffer.hasRemaining()) {
            char value = charBuffer.get();
            if (value == '\n') {
                processor.process(lineBuffer.toString());
                lineBuffer.setLength(0);
            } else if (value != '\r') {
                lineBuffer.append(value);
            }
        }
    }

    private static CharacterCodingException toException(CoderResult result) {
        if (result.isMalformed()) {
            return new MalformedInputException(result.length());
        }
        if (result.isUnmappable()) {
            return new UnmappableCharacterException(result.length());
        }
        return new CharacterCodingException();
    }
}
