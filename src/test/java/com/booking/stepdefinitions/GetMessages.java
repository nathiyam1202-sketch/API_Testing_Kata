package com.booking.stepdefinitions;

import com.booking.config.ConfigReader;
import com.booking.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetMessages {
    private static final String RESPONSE_CONTEXT_KEY = "messages.response";

    @When("I want to read the messages")
    public void iWantToReadTheMessages() {

        Response response = given().log().all()
                        .when()
                        .get(ConfigReader.getBaseUrl() + "/message");
        TestContext.put(RESPONSE_CONTEXT_KEY, response);
    }

    @Then("I should receive all existing messages")
    public void iShouldReceiveAllExistingMessages() {
        Response response = TestContext.get(RESPONSE_CONTEXT_KEY, Response.class);
        response.then().statusCode(200)
                .body("messages.size()", greaterThan(0))
                .body("messages.id", everyItem(notNullValue()))
                .body("messages.name", everyItem(not(isEmptyOrNullString())))
                .body("messages.subject", everyItem(not(isEmptyOrNullString())))
                .body("messages.read", everyItem(notNullValue()))
                .log().all();
    }
}
