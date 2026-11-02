package com.booking.context;

import com.booking.model.BookingRequest;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestContext {
  private static final ThreadLocal<ScenarioState> SCENARIO_STATE = new ThreadLocal<>();

  private TestContext() {}

  public static void startScenario() {
    SCENARIO_STATE.set(new ScenarioState());
  }

  public static void setBookingRequest(BookingRequest bookingRequest) {
    scenarioState().bookingRequest = requireValue(bookingRequest, "Booking request");
  }

  public static BookingRequest bookingRequest() {
    return requireValue(scenarioState().bookingRequest, "Booking request");
  }

  public static void setBookingId(int bookingId) {
    if (bookingId <= 0) {
      throw new IllegalArgumentException("Booking ID must be positive");
    }
    scenarioState().bookingId = bookingId;
  }

  public static int bookingId() {
    int bookingId = scenarioState().bookingId;
    if (bookingId <= 0) {
      throw new IllegalStateException("No booking ID is available in the current scenario");
    }
    return bookingId;
  }

  public static void setToken(String token) {
    scenarioState().token = requireValue(token, "Authentication token");
  }

  public static String token() {
    return requireValue(scenarioState().token, "Authentication token");
  }

  public static void setUpdatedFirstName(String firstName) {
    scenarioState().updatedFirstName = requireValue(firstName, "Updated booking first name");
  }

  public static String updatedFirstName() {
    return requireValue(scenarioState().updatedFirstName, "Updated booking first name");
  }

  public static void setLatestResponse(Response response) {
    scenarioState().latestResponse = requireValue(response, "API response");
  }

  public static Response latestResponse() {
    return requireValue(scenarioState().latestResponse, "API response");
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

  private static <T> T requireValue(T value, String description) {
    if (value == null) {
      throw new IllegalStateException(description + " is not available in the current scenario");
    }
    return value;
  }

  private static void validateKey(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Context key must not be blank");
    }
  }

  private static final class ScenarioState {
    private BookingRequest bookingRequest;
    private int bookingId;
    private String token;
    private String updatedFirstName;
    private Response latestResponse;
    private final Map<String, Runnable> cleanupActions = new LinkedHashMap<>();
  }
}
