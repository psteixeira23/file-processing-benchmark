package com.patrick.benchmark.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.patrick.benchmark.processing.scenario.TotalRecordsScenario;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvScenarioProcessorTest {

    @Test
    void shouldSkipHeaderAndTrackInvalidLines() {
        CsvScenarioProcessor processor = new CsvScenarioProcessor(
                List.of(new TotalRecordsScenario()),
                new CsvLineParser()
        );

        processor.process("uf,tipo_doenca,local_obito,faixa_etaria,sexo,total");
        processor.process("AC,OUTRAS,DOMICILIO,\"< 9\",F,11");
        processor.process("invalid,line");

        ProcessingSummary summary = processor.summary();

        assertEquals(1L, summary.recordsProcessed());
        assertEquals(1L, summary.invalidLines());
        assertEquals(1L, summary.scenarios().get(0).count());
    }
}
