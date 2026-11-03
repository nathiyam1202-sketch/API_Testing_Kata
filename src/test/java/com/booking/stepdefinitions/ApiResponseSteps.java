package com.booking.stepdefinitions;

import com.booking.context.TestContext;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiResponseSteps {
  @Then("the API response status code should be {int}")
  public void theApiResponseStatusCodeShouldBe(int expectedStatusCode) {
    assertStatusCode(expectedStatusCode);
  }

  @Then("the API response error message should contain {string}")
  public void theApiResponseErrorMessageShouldContain(String expectedMessage) {
    if (expectedMessage == null || expectedMessage.isBlank()) {
      throw new IllegalArgumentException("Expected error message must not be blank");
    }

    assertErrorMessageContains(expectedMessage);
  }

  @Then("the API response should have status code {int} and a validation error")
  public void theApiResponseShouldHaveStatusAndValidationError(int expectedStatusCode) {
    assertStatusCode(expectedStatusCode);
    assertValidationError();
  }

  @Then("the API response should have status code {int} and error message containing {string}")
  public void theApiResponseShouldHaveStatusAndErrorMessage(
      int expectedStatusCode, String expectedMessage) {
    assertStatusCode(expectedStatusCode);
    assertErrorMessageContains(expectedMessage);
  }

  private void assertErrorMessageContains(String expectedMessage) {
    if (expectedMessage == null || expectedMessage.isBlank()) {
      throw new IllegalArgumentException("Expected error message must not be blank");
    }

    Response response = getApiResponse();
    Object error = response.jsonPath().get("error");
    Object errors = response.jsonPath().get("errors");
    Object message = response.jsonPath().get("message");
    List<String> actualMessages = new ArrayList<>();
    collectMessages(error, actualMessages);
    collectMessages(errors, actualMessages);
    collectMessages(message, actualMessages);

    if (actualMessages.stream().noneMatch(actual -> actual.contains(expectedMessage))) {
      throw new AssertionError(
          "Expected error message containing \""
              + expectedMessage
              + "\" but found: "
              + actualMessages);
    }
  }

  @Then("the API response should contain a validation error")
  public void theApiResponseShouldContainAValidationError() {
    assertValidationError();
  }

  private void assertValidationError() {
    Object value = getApiResponse().jsonPath().get("errors");
    if (!(value instanceof List<?> errors) || errors.isEmpty()) {
      throw new AssertionError("Expected a non-empty errors array in the validation response");
    }
    for (Object error : errors) {
      if (!(error instanceof String message) || message.isBlank()) {
        throw new AssertionError("Expected each validation error to be a non-blank string");
      }
    }
  }

  @Then("the API response field {string} should equal {string}")
  public void theApiResponseFieldShouldEqual(String jsonPath, String expectedValue) {
    Object actualValue = getApiResponse().jsonPath().get(jsonPath);
    if (actualValue == null || !expectedValue.equals(actualValue.toString())) {
      throw new AssertionError(
          "Expected response field \""
              + jsonPath
              + "\" to equal \""
              + expectedValue
              + "\" but was: "
              + actualValue);
    }
  }

  @Then("the API response field {string} should not be empty")
  public void theApiResponseFieldShouldNotBeEmpty(String jsonPath) {
    Object actualValue = getApiResponse().jsonPath().get(jsonPath);
    if (actualValue == null || actualValue.toString().isBlank()) {
      throw new AssertionError("Expected response field \"" + jsonPath + "\" to be non-empty");
    }
  }

  private Response getApiResponse() {
    return TestContext.latestResponse();
  }

  private void assertStatusCode(int expectedStatusCode) {
    int actualStatusCode = getApiResponse().statusCode();
    if (actualStatusCode != expectedStatusCode) {
      throw new AssertionError(
          "Expected HTTP status " + expectedStatusCode + " but was " + actualStatusCode);
    }
  }

  private void collectMessages(Object value, List<String> messages) {
    if (value instanceof String text) {
      if (!text.isBlank()) {
        messages.add(text);
      }
    } else if (value instanceof Iterable<?> values) {
      for (Object item : values) {
        collectMessages(item, messages);
      }
    } else if (value instanceof Map<?, ?> values) {
      for (Object item : values.values()) {
        collectMessages(item, messages);
      }
    }
  }
}
