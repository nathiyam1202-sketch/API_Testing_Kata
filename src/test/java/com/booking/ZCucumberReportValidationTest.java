package com.booking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZCucumberReportValidationTest {
    private static final Path CUCUMBER_REPORT = Path.of("target", "cucumber.json");
    private static final Pattern FAILED_RESULT = Pattern.compile("\"status\"\\s*:\\s*\"failed\"");

    @Test
    void cucumberReportContainsNoFailedStepsOrHooks() throws IOException {
        assertTrue(Files.isRegularFile(CUCUMBER_REPORT), "Cucumber JSON report was not generated");

        String report = Files.readString(CUCUMBER_REPORT);
        assertFalse(
                FAILED_RESULT.matcher(report).find(),
                "Cucumber reported failed steps or hooks; inspect target/cucumber.json"
        );
    }
}
