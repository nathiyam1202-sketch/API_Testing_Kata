package com.booking.stepdefinitions;

import com.booking.client.MessageClient;
import com.booking.context.TestContext;
import com.booking.model.MessageRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;

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
            "This is a test contact form submission."));
  }

  @When("I submit the contact form")
  public void iSubmitTheContactForm() {
    MessageRequest messageRequest =
        TestContext.get(MESSAGE_REQUEST_CONTEXT_KEY, MessageRequest.class);
    Response response = new MessageClient().createMessage(messageRequest);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @When("I submit the contact form without the {string} field")
  public void iSubmitTheContactFormWithoutTheField(String field) {
    Map<String, Object> messageRequest = new LinkedHashMap<>();
    messageRequest.put("name", "Test Contact");
    messageRequest.put("email", "test.contact@example.com");
    messageRequest.put("phone", "12345678901");
    messageRequest.put("subject", "API test contact");
    messageRequest.put("description", "This is a test contact form submission.");

    if (!messageRequest.containsKey(field)
        || !(field.equals("name") || field.equals("email") || field.equals("description"))) {
      throw new IllegalArgumentException("Unsupported mandatory contact field: " + field);
    }
    messageRequest.remove(field);

    Response response = new MessageClient().createMessage(messageRequest);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @When("I submit the contact form with {string} markup in the {string} field")
  public void iSubmitTheContactFormWithMarkupInTheField(String markupType, String field) {
    MessageRequest messageRequest =
        TestContext.get(MESSAGE_REQUEST_CONTEXT_KEY, MessageRequest.class);
    String markup =
        switch (markupType) {
          case "HTML" -> "<b>Boundary test</b>";
          case "script" -> "<script>alert(1)</script>";
          default ->
              throw new IllegalArgumentException(
                  "Unsupported contact form markup type: " + markupType);
        };

    switch (field) {
      case "name" -> messageRequest.setName(markup);
      case "subject" -> messageRequest.setSubject(markup);
      case "description" -> messageRequest.setDescription(markup);
      default -> throw new IllegalArgumentException("Unsupported contact form field: " + field);
    }

    Response response = new MessageClient().createMessage(messageRequest);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }
}
