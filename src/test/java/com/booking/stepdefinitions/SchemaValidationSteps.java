package com.booking.stepdefinitions;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.booking.context.TestContext;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import java.util.Map;

public class SchemaValidationSteps {
  private static final Map<String, String> SCHEMA_PATHS =
      Map.of(
          "booking", "schemas/booking-schema.json",
          "booking creation", "schemas/booking-create-response.schema.json");

  @Then("the API response should match the {string} JSON schema")
  public void theApiResponseShouldMatchTheJsonSchema(String schemaName) {
    String schemaPath = SCHEMA_PATHS.get(schemaName);
    if (schemaPath == null) {
      throw new IllegalArgumentException("Unsupported JSON schema name: " + schemaName);
    }

    Response response = TestContext.latestResponse();
    int expectedStatusCode = schemaName.equals("booking creation") ? 201 : 200;
    if (response.statusCode() != expectedStatusCode) {
      throw new AssertionError(
          "Expected HTTP status "
              + expectedStatusCode
              + " before validating the "
              + schemaName
              + " response schema, but was "
              + response.statusCode());
    }
    response.then().body(matchesJsonSchemaInClasspath(schemaPath));
  }
}
