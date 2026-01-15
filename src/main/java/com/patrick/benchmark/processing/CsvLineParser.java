package com.patrick.benchmark.processing;

import java.util.ArrayList;
import java.util.List;

public final class CsvLineParser {

    private static final int EXPECTED_FIELDS = 6;

    public CsvRecord parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        List<String> fields = split(line);
        if (fields.size() != EXPECTED_FIELDS) {
            return null;
        }

        long total;
        try {
            total = Long.parseLong(fields.get(5));
        } catch (NumberFormatException ex) {
            return null;
        }

        return new CsvRecord(
                fields.get(0),
                fields.get(1),
                fields.get(2),
                fields.get(3),
                fields.get(4),
                total
        );
    }

    private List<String> split(String line) {
        List<String> fields = new ArrayList<>(EXPECTED_FIELDS);
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char value = line.charAt(i);
            if (value == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (value == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }

        fields.add(current.toString().trim());
        return fields;
    }
}
