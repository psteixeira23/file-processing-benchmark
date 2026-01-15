package com.patrick.benchmark.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScenarioReportTest {

    @Test
    void shouldDefaultToEmptyBreakdownWhenNull() {
        ScenarioReport report = new ScenarioReport("Test", 10L, 1L, null);

        assertTrue(report.breakdown().isEmpty());
    }

    @Test
    void shouldCopyAndProtectBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();
        breakdown.put("A", 1L);
        ScenarioReport report = new ScenarioReport("Test", 10L, 1L, breakdown);

        breakdown.put("B", 2L);

        assertEquals(1, report.breakdown().size());
        assertEquals(1L, report.breakdown().get("A"));
        assertThrows(UnsupportedOperationException.class, () -> report.breakdown().put("C", 3L));
    }
}
