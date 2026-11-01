package com.booking.stepdefinitions;

import com.booking.client.AuthClient;
import com.booking.config.ConfigReader;
import com.booking.context.TestContext;
import com.booking.model.AuthRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class AuthenticationSteps {
  private static final String AUTH_REQUEST_CONTEXT_KEY = "authentication.request";

  @Given("I have valid authentication credentials")
  public void iHaveValidAuthenticationCredentials() {
    TestContext.put(
        AUTH_REQUEST_CONTEXT_KEY,
        new AuthRequest(ConfigReader.getAuthUsername(), ConfigReader.getAuthPassword()));
  }

  @When("I request an authentication token")
  public void iRequestAnAuthenticationToken() {
    AuthRequest authRequest = TestContext.get(AUTH_REQUEST_CONTEXT_KEY, AuthRequest.class);
    Response response = new AuthClient().login(authRequest);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }
}
