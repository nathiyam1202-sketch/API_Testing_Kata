package com.booking.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConfigReaderTest {
  @Test
  void missingPropertyExplainsHowToConfigureIt() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> ConfigReader.getProperty("test.required.property.missing"));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("src/test/resources/config.properties")),
        () -> assertTrue(exception.getMessage().contains("TEST_REQUIRED_PROPERTY_MISSING")),
        () -> assertTrue(exception.getMessage().contains("-Dtest.required.property.missing")));
  }
}
