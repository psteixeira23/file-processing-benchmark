package com.patrick.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ProcessingModeTest {

    @Test
    void shouldParseModeLabels() {
        assertEquals(ProcessingMode.SINGLE_PASS, ProcessingMode.fromLabel("single"));
        assertEquals(ProcessingMode.SINGLE_PASS, ProcessingMode.fromLabel("single-pass"));
        assertEquals(ProcessingMode.SINGLE_PASS, ProcessingMode.fromLabel("single_pass"));
        assertEquals(ProcessingMode.SINGLE_PASS, ProcessingMode.fromLabel("realistic"));
        assertEquals(ProcessingMode.ISOLATED, ProcessingMode.fromLabel("isolated"));
        assertEquals(ProcessingMode.ISOLATED, ProcessingMode.fromLabel("analytical"));
        assertNull(ProcessingMode.fromLabel("unknown"));
        assertNull(ProcessingMode.fromLabel(null));
    }
}
