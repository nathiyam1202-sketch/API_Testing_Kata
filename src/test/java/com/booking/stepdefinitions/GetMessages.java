package com.booking.stepdefinitions;

import com.booking.config.ConfigReader;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetMessages {
    private Response response;

    @When("I want to read the messages")
    public void iWantToReadTheMessages() {

        response = given().log().all()
                        .when()
                        .get(ConfigReader.getBaseUrl() + "/message");
    }

    @Then("I should receive all existing messages")
    public void iShouldReceiveAllExistingMessages() {
        response.then().statusCode(200)
                .body("messages.size()", greaterThan(0))
                .body("messages.id", everyItem(notNullValue()))
                .body("messages.name", everyItem(not(isEmptyOrNullString())))
                .body("messages.subject", everyItem(not(isEmptyOrNullString())))
                .body("messages.read", everyItem(notNullValue()))
                .log().all();
    }
}
