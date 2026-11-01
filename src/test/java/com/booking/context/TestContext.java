package com.booking.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestContext {
  public static final String API_RESPONSE_CONTEXT_KEY = "api.response";
  private static final ThreadLocal<ScenarioState> SCENARIO_STATE = new ThreadLocal<>();

  private TestContext() {}

  public static void startScenario() {
    SCENARIO_STATE.set(new ScenarioState());
  }

  public static void put(String key, Object value) {
    validateKey(key);
    if (value == null) {
      throw new IllegalArgumentException("Context value must not be null");
    }

    scenarioState().data.put(key, value);
  }

  public static <T> T get(String key, Class<T> valueType) {
    validateKey(key);
    if (valueType == null) {
      throw new IllegalArgumentException("Context value type must not be null");
    }

    Object value = scenarioState().data.get(key);
    if (value == null) {
      throw new IllegalStateException("No scenario context value found for key: " + key);
    }
    return valueType.cast(value);
  }

  public static void registerCleanupAction(String key, Runnable action) {
    validateKey(key);
    if (action == null) {
      throw new IllegalArgumentException("Cleanup action must not be null");
    }

    scenarioState().cleanupActions.put(key, action);
  }

  public static void removeCleanupAction(String key) {
    validateKey(key);
    scenarioState().cleanupActions.remove(key);
  }

  public static void runCleanupActions() {
    ScenarioState state = scenarioState();
    List<Runnable> actions = new ArrayList<>(state.cleanupActions.values());
    state.cleanupActions.clear();

    RuntimeException cleanupFailure = null;
    for (int index = actions.size() - 1; index >= 0; index--) {
      cleanupFailure = runCleanupAction(actions.get(index), cleanupFailure);
    }
    if (cleanupFailure != null) {
      throw cleanupFailure;
    }
  }

  public static void clear() {
    SCENARIO_STATE.remove();
  }

  private static RuntimeException runCleanupAction(
      Runnable action, RuntimeException cleanupFailure) {
    try {
      action.run();
    } catch (RuntimeException | AssertionError exception) {
      if (cleanupFailure == null) {
        cleanupFailure = new IllegalStateException("One or more scenario cleanup actions failed");
      }
      cleanupFailure.addSuppressed(exception);
    }
    return cleanupFailure;
  }

  private static ScenarioState scenarioState() {
    ScenarioState state = SCENARIO_STATE.get();
    if (state == null) {
      throw new IllegalStateException("Scenario context has not been initialized");
    }
    return state;
  }

  private static void validateKey(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Context key must not be blank");
    }
  }

  private static final class ScenarioState {
    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, Runnable> cleanupActions = new LinkedHashMap<>();
  }
}
