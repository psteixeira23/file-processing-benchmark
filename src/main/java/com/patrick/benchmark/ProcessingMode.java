package com.patrick.benchmark;

import java.util.Locale;

public enum ProcessingMode {
    SINGLE_PASS("Single-Pass (Realistic)"),
    ISOLATED("Isolated (Analytical)");

    private final String displayName;

    ProcessingMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static ProcessingMode fromLabel(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "single", "single-pass", "single_pass", "realistic" -> SINGLE_PASS;
            case "isolated", "analytical", "analytic" -> ISOLATED;
            default -> null;
        };
    }
}
