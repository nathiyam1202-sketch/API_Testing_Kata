package com.booking.stepdefinitions;

import com.booking.client.AuthClient;
import com.booking.context.TestContext;
import com.booking.model.AuthRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class AuthenticationSteps {
    private static final String AUTH_REQUEST_CONTEXT_KEY = "authentication.request";
    private static final String AUTH_RESPONSE_CONTEXT_KEY = "authentication.response";

    @Given("I have valid authentication credentials")
    public void iHaveValidAuthenticationCredentials() {
        TestContext.put(AUTH_REQUEST_CONTEXT_KEY, new AuthRequest("admin", "password"));
    }

    @When("I request an authentication token")
    public void iRequestAnAuthenticationToken() {
        AuthRequest authRequest = TestContext.get(AUTH_REQUEST_CONTEXT_KEY, AuthRequest.class);
        Response response = new AuthClient().login(authRequest);
        TestContext.put(AUTH_RESPONSE_CONTEXT_KEY, response);
    }

    @Then("the authentication response should be successful and contain a token")
    public void theAuthenticationResponseShouldBeSuccessfulAndContainAToken() {
        Response response = TestContext.get(AUTH_RESPONSE_CONTEXT_KEY, Response.class);
        response.then()
                .statusCode(200)
                .body("token", not(isEmptyOrNullString()));
    }
}
