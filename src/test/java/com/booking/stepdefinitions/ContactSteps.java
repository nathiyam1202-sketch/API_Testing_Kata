package com.booking.stepdefinitions;

import com.booking.client.MessageClient;
import com.booking.context.TestContext;
import com.booking.model.MessageRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ContactSteps {
    private static final String MESSAGE_REQUEST_CONTEXT_KEY = "contactForm.request";

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
        TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
    }
}
