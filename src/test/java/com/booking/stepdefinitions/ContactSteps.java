package com.booking.stepdefinitions;

import com.booking.client.MessageClient;
import com.booking.context.TestContext;
import com.booking.model.MessageRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class ContactSteps {
    private static final String MESSAGE_REQUEST_CONTEXT_KEY = "contactForm.request";
    private static final String MESSAGE_RESPONSE_CONTEXT_KEY = "contactForm.response";

    @Given("I have valid contact form details")
    public void iHaveValidContactFormDetails() {
        TestContext.put(
                MESSAGE_REQUEST_CONTEXT_KEY,
                new MessageRequest(
                        "Test Contact",
                        "test.contact@example.com",
                        "12345678901",
                        "API test contact",
                        "This is a test contact form submission."
                )
        );
    }

    @When("I submit the contact form")
    public void iSubmitTheContactForm() {
        MessageRequest messageRequest = TestContext.get(MESSAGE_REQUEST_CONTEXT_KEY, MessageRequest.class);
        Response response = new MessageClient().createMessage(messageRequest);
        TestContext.put(MESSAGE_RESPONSE_CONTEXT_KEY, response);
    }

    @Then("the contact form should be submitted successfully")
    public void theContactFormShouldBeSubmittedSuccessfully() {
        Response response = TestContext.get(MESSAGE_RESPONSE_CONTEXT_KEY, Response.class);
        response.then()
                .statusCode(201)
                .body("message", not(isEmptyOrNullString()));
    }
}
