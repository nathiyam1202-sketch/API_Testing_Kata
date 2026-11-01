package com.booking.stepdefinitions;

import com.booking.context.TestContext;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class SchemaValidationSteps {
    private static final Map<String, String> SCHEMA_PATHS = Map.of(
            "authentication", "schemas/auth-response.schema.json",
            "booking", "schemas/booking-schema.json",
            "booking creation", "schemas/booking-create-response.schema.json",
            "message", "schemas/message-schema.json"
    );

    @Then("the API response should match the {string} JSON schema")
    public void theApiResponseShouldMatchTheJsonSchema(String schemaName) {
        String schemaPath = SCHEMA_PATHS.get(schemaName);
        if (schemaPath == null) {
            throw new IllegalArgumentException("Unsupported JSON schema name: " + schemaName);
        }

        Response response = TestContext.get(TestContext.API_RESPONSE_CONTEXT_KEY, Response.class);
        response.then().body(matchesJsonSchemaInClasspath(schemaPath));
    }
}
