package com.patrick.benchmark.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CsvLineParserTest {

    @Test
    void shouldParseValidLine() {
        CsvLineParser parser = new CsvLineParser();
        CsvRecord parsedCsvRecord = parser.parse("AC,OUTRAS,DOMICILIO,\"< 9\",F,11");

        assertEquals("AC", parsedCsvRecord.uf());
        assertEquals("OUTRAS", parsedCsvRecord.diseaseType());
        assertEquals("DOMICILIO", parsedCsvRecord.deathLocation());
        assertEquals("< 9", parsedCsvRecord.ageRange());
        assertEquals("F", parsedCsvRecord.sex());
        assertEquals(11L, parsedCsvRecord.total());
    }

    @Test
    void shouldReturnNullForInvalidLines() {
        CsvLineParser parser = new CsvLineParser();

        assertNull(parser.parse(null));
        assertNull(parser.parse(""));
        assertNull(parser.parse("   "));
        assertNull(parser.parse("only,three,columns"));
        assertNull(parser.parse("AC,OUTRAS,DOMICILIO,< 9,F,invalid"));
    }

    @Test
    void shouldHandleQuotedCommas() {
        CsvLineParser parser = new CsvLineParser();
        CsvRecord parsedCsvRecord = parser.parse("AC,OUTRAS,\"DOMICILIO,URBANO\",\"< 9\",F,11");

        assertEquals("DOMICILIO,URBANO", parsedCsvRecord.deathLocation());
        assertEquals("< 9", parsedCsvRecord.ageRange());
        assertEquals(11L, parsedCsvRecord.total());
    }
}
