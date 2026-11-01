package com.booking.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestContext {
  public static final String API_RESPONSE_CONTEXT_KEY = "api.response";
  private static final ThreadLocal<Map<String, Object>> SCENARIO_DATA = new ThreadLocal<>();
  private static final ThreadLocal<Map<String, Runnable>> CLEANUP_ACTIONS = new ThreadLocal<>();

  private TestContext() {}

  public static void startScenario() {
    SCENARIO_DATA.set(new HashMap<>());
    CLEANUP_ACTIONS.set(new LinkedHashMap<>());
  }

  public static void put(String key, Object value) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Context key must not be blank");
    }
    if (value == null) {
      throw new IllegalArgumentException("Context value must not be null");
    }

    getScenarioData().put(key, value);
  }

  public static <T> T get(String key, Class<T> valueType) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Context key must not be blank");
    }
    if (valueType == null) {
      throw new IllegalArgumentException("Context value type must not be null");
    }

    Object value = getScenarioData().get(key);
    if (value == null) {
      throw new IllegalStateException("No scenario context value found for key: " + key);
    }
    return valueType.cast(value);
  }

  public static void registerCleanupAction(String key, Runnable action) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Cleanup action key must not be blank");
    }
    if (action == null) {
      throw new IllegalArgumentException("Cleanup action must not be null");
    }

    cleanupActions().put(key, action);
  }

  public static void removeCleanupAction(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Cleanup action key must not be blank");
    }

    cleanupActions().remove(key);
  }

  public static void runCleanupActions() {
    Map<String, Runnable> actions = cleanupActions();
    List<Runnable> actionsToRun = new ArrayList<>(actions.values());
    actions.clear();

    RuntimeException cleanupFailure = null;
    for (int index = actionsToRun.size() - 1; index >= 0; index--) {
      try {
        actionsToRun.get(index).run();
      } catch (RuntimeException | AssertionError exception) {
        if (cleanupFailure == null) {
          cleanupFailure = new IllegalStateException("One or more scenario cleanup actions failed");
        }
        cleanupFailure.addSuppressed(exception);
      }
    }

    if (cleanupFailure != null) {
      throw cleanupFailure;
    }
  }

  public static void clear() {
    SCENARIO_DATA.remove();
    CLEANUP_ACTIONS.remove();
  }

  private static Map<String, Runnable> cleanupActions() {
    Map<String, Runnable> cleanupActions = CLEANUP_ACTIONS.get();
    if (cleanupActions == null) {
      throw new IllegalStateException("Scenario context has not been initialized");
    }
    return cleanupActions;
  }

  private static Map<String, Object> getScenarioData() {
    Map<String, Object> scenarioData = SCENARIO_DATA.get();
    if (scenarioData == null) {
      throw new IllegalStateException("Scenario context has not been initialized");
    }
    return scenarioData;
  }
}
