package com.patrick.benchmark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPrintUsageWhenNoArgs() {
        assertDoesNotThrow(() -> App.main(new String[0]));
    }

    @Test
    void shouldWarnWhenFileMissing() {
        assertDoesNotThrow(() -> App.main(new String[] {
                tempDir.resolve("missing.csv").toString()
        }));
    }

    @Test
    void shouldRunWithSingleMode() throws Exception {
        Path file = writeSampleFile("sample.csv");

        assertDoesNotThrow(() -> App.main(new String[] {
                file.toString(),
                "--mode=single"
        }));
    }

    @Test
    void shouldReturnOnUnknownMode() throws Exception {
        Path file = writeSampleFile("sample.csv");

        assertDoesNotThrow(() -> App.main(new String[] {
                file.toString(),
                "--mode=unknown"
        }));
    }

    @Test
    void shouldRunWithCharsetFlag() throws Exception {
        Path file = writeSampleFile("sample-charset.csv");

        assertDoesNotThrow(() -> App.main(new String[] {
                file.toString(),
                "--charset=US-ASCII"
        }));
    }

    @Test
    void shouldRunWithPositionalCharset() throws Exception {
        Path file = writeSampleFile("sample-positional.csv");

        assertDoesNotThrow(() -> App.main(new String[] {
                file.toString(),
                "US-ASCII"
        }));
    }

    @Test
    void shouldIgnoreExtraNonFlagArguments() throws Exception {
        Path file = writeSampleFile("sample-extra.csv");

        assertDoesNotThrow(() -> App.main(new String[] {
                file.toString(),
                "US-ASCII",
                "ignored"
        }));
    }

    @Test
    void shouldHandleHtmlWriteFailure() throws Exception {
        Path file = writeSampleFile("sample-report-error.csv");
        Path projectRoot = Path.of("").toAbsolutePath();
        Path reportsDir = projectRoot.resolve("reports");
        Path backupDir = projectRoot.resolve("reports_backup_test");
        Path reportsAsFile = reportsDir;

        boolean hasReportsDir = Files.exists(reportsDir);
        if (hasReportsDir) {
            Files.move(reportsDir, backupDir, StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            Files.writeString(reportsAsFile, "not-a-directory", StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> App.main(new String[] {
                file.toString(),
                "--mode=single"
            }));
        } finally {
            Files.deleteIfExists(reportsAsFile);
            if (Files.exists(backupDir)) {
                Files.move(backupDir, reportsDir, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private Path writeSampleFile(String filename) throws Exception {
        Path file = tempDir.resolve(filename);
        String content = """
                uf,tipo_doenca,local_obito,faixa_etaria,sexo,total
                AC,OUTRAS,DOMICILIO,"< 9",F,11
                """;
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }
}
