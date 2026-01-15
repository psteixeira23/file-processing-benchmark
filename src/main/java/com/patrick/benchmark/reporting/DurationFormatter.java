package com.patrick.benchmark.reporting;

import java.util.Locale;

final class DurationFormatter {

    private DurationFormatter() {
    }

    static String formatMillis(long durationNanos) {
        long safeNanos = Math.max(0L, durationNanos);
        long millis = safeNanos / 1_000_000L;
        long nanosRemainder = safeNanos % 1_000_000L;
        return String.format(Locale.ROOT, "%d.%06d", millis, nanosRemainder);
    }
}
