package com.patrick.benchmark.processing;

public record CsvRecord(
        String uf,
        String diseaseType,
        String deathLocation,
        String ageRange,
        String sex,
        long total
) {
}
