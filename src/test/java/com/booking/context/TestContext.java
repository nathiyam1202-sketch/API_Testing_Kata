package com.booking.context;

import java.util.HashMap;
import java.util.Map;

public final class TestContext {
    private static final ThreadLocal<Map<String, Object>> SCENARIO_DATA = new ThreadLocal<>();

    private TestContext() {
    }

    public static void startScenario() {
        SCENARIO_DATA.set(new HashMap<>());
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

    public static void clear() {
        SCENARIO_DATA.remove();
    }

    private static Map<String, Object> getScenarioData() {
        Map<String, Object> scenarioData = SCENARIO_DATA.get();
        if (scenarioData == null) {
            throw new IllegalStateException("Scenario context has not been initialized");
        }
        return scenarioData;
    }
}
