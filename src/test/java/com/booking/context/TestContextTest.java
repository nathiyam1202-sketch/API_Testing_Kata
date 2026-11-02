package com.booking.context;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TestContextTest {
  @Test
  void scenarioStateDoesNotLeakAcrossScenarios() {
    try {
      TestContext.startScenario();
      TestContext.setToken("scenario-token");

      TestContext.startScenario();

      assertThrows(IllegalStateException.class, TestContext::token);
    } finally {
      TestContext.clear();
    }
  }
}
