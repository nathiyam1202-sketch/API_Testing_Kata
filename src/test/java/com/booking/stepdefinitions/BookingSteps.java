package com.booking.stepdefinitions;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import com.booking.client.AuthClient;
import com.booking.client.BookingClient;
import com.booking.context.TestContext;
import com.booking.data.BookingDataGenerator;
import com.booking.model.AuthRequest;
import com.booking.model.BookingDates;
import com.booking.model.BookingRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.Map;

public class BookingSteps {
  private static final String BOOKING_REQUEST_CONTEXT_KEY = "booking.request";
  private static final String BOOKING_ID_CONTEXT_KEY = "booking.id";
  private static final String BOOKING_RESPONSE_CONTEXT_KEY = "booking.response";
  private static final String TOKEN_CONTEXT_KEY = "authentication.token";
  private static final String UPDATED_FIRST_NAME_CONTEXT_KEY = "booking.updatedFirstName";
  private final BookingClient bookingClient = new BookingClient();

  @Given("I am authenticated")
  public void iAmAuthenticated() {
    Response response = new AuthClient().login(new AuthRequest("admin", "password"));
    response.then().statusCode(200).body("token", not(isEmptyOrNullString()));
    TestContext.put(TOKEN_CONTEXT_KEY, response.jsonPath().getString("token"));
  }

  @Given("I have a valid booking request")
  public void iHaveAValidBookingRequest() {
    TestContext.put(BOOKING_REQUEST_CONTEXT_KEY, BookingDataGenerator.generateBookingRequest());
  }

  @When("I create a booking")
  public void iCreateABooking() {
    storeBookingCreationResponse(bookingClient.createBooking(getBookingRequest()));
  }

  @When("I create a booking with a missing {string} field")
  public void iCreateABookingWithAMissingField(String field) {
    Map<String, Object> bookingPayload = BookingDataGenerator.generateBooking();
    if (field.startsWith("bookingdates.")) {
      Object value = bookingPayload.get("bookingdates");
      if (!(value instanceof Map<?, ?> existingDates)) {
        throw new IllegalStateException("Generated booking payload has no bookingdates object");
      }

      Map<String, Object> bookingDates = new java.util.LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : existingDates.entrySet()) {
        if (!(entry.getKey() instanceof String key)) {
          throw new IllegalStateException("Booking date field names must be strings");
        }
        bookingDates.put(key, entry.getValue());
      }
      String dateField = field.substring("bookingdates.".length());
      if (!bookingDates.containsKey(dateField)) {
        throw new IllegalArgumentException("Unsupported missing booking field: " + field);
      }
      bookingDates.remove(dateField);
      bookingPayload.put("bookingdates", bookingDates);
    } else if (bookingPayload.containsKey(field)) {
      bookingPayload.remove(field);
    } else {
      throw new IllegalArgumentException("Unsupported missing booking field: " + field);
    }

    storeBookingCreationResponse(bookingClient.createBooking(bookingPayload));
  }

  @Given("I have a booking request with malformed email {string}")
  public void iHaveABookingRequestWithMalformedEmail(String email) {
    BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();
    bookingRequest.setEmail(email);
    TestContext.put(BOOKING_REQUEST_CONTEXT_KEY, bookingRequest);
  }

  @Given("I have a booking request violating date rule {string}")
  public void iHaveABookingRequestViolatingDateRule(String dateRule) {
    BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();
    LocalDate checkin = LocalDate.parse(bookingRequest.getBookingdates().getCheckin());
    LocalDate checkout = LocalDate.parse(bookingRequest.getBookingdates().getCheckout());

    switch (dateRule) {
      case "checkout_before_checkin" -> checkout = checkin.minusDays(1);
      case "checkout_same_as_checkin" -> checkout = checkin;
      default -> throw new IllegalArgumentException("Unsupported booking date rule: " + dateRule);
    }

    bookingRequest.setBookingdates(new BookingDates(checkin.toString(), checkout.toString()));
    TestContext.put(BOOKING_REQUEST_CONTEXT_KEY, bookingRequest);
  }

  private void storeBookingCreationResponse(Response response) {
    registerBookingCleanup(response);
    TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @Then("the booking should be created successfully")
  public void theBookingShouldBeCreatedSuccessfully() {
    Response response = getBookingResponse();
    response
        .then()
        .body("bookingid", greaterThan(0))
        .body("firstname", equalTo(getBookingRequest().getFirstname()))
        .body("lastname", equalTo(getBookingRequest().getLastname()));
  }

  @Given("I have a booking request with invalid {string}")
  public void iHaveABookingRequestWithInvalidField(String field) {
    BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();

    switch (field) {
      case "firstname" -> bookingRequest.setFirstname("Jo");
      case "lastname" -> bookingRequest.setLastname("D");
      case "phone" -> bookingRequest.setPhone("123");
      default -> throw new IllegalArgumentException("Unsupported invalid booking field: " + field);
    }

    TestContext.put(BOOKING_REQUEST_CONTEXT_KEY, bookingRequest);
  }

  @Given("a booking exists")
  public void aBookingExists() {
    Response response = bookingClient.createBooking(getBookingRequest());
    registerBookingCleanup(response);
    response.then().statusCode(201).body("bookingid", greaterThan(0));
    TestContext.put(BOOKING_ID_CONTEXT_KEY, response.jsonPath().getInt("bookingid"));
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @When("I retrieve the booking")
  public void iRetrieveTheBooking() {
    Response response = bookingClient.getBooking(getBookingId(), getToken());
    TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @When("I call the protected booking endpoint {string} with a {string} token")
  public void iCallTheProtectedBookingEndpointWithAToken(String operation, String tokenState) {
    String token =
        switch (tokenState) {
          case "missing" -> null;
          case "expired" -> "expired-test-token";
          case "invalid" -> "invalid-test-token";
          default -> throw new IllegalArgumentException("Unsupported token state: " + tokenState);
        };

    Response response =
        switch (operation) {
          case "retrieve" -> bookingClient.getBooking(getBookingId(), token);
          case "update" -> bookingClient.updateBooking(getBookingId(), getBookingRequest(), token);
          case "partially update" ->
              bookingClient.patchBooking(
                  getBookingId(), Map.of("firstname", "UnauthorizedUpdate"), token);
          case "delete" -> bookingClient.deleteBooking(getBookingId(), token);
          default ->
              throw new IllegalArgumentException(
                  "Unsupported protected booking operation: " + operation);
        };

    TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @Then("the booking details should match the created booking")
  public void theBookingDetailsShouldMatchTheCreatedBooking() {
    Response response = getBookingResponse();
    BookingRequest bookingRequest = getBookingRequest();
    response
        .then()
        .body("firstname", equalTo(bookingRequest.getFirstname()))
        .body("lastname", equalTo(bookingRequest.getLastname()));
  }

  @When("I update the booking")
  public void iUpdateTheBooking() {
    BookingRequest original = getBookingRequest();
    BookingRequest updated =
        new BookingRequest(
            original.getRoomid(),
            "UpdatedGuest",
            original.getLastname(),
            original.isDepositpaid(),
            original.getBookingdates(),
            original.getEmail(),
            original.getPhone());
    Response response = bookingClient.updateBooking(getBookingId(), updated, getToken());
    TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(UPDATED_FIRST_NAME_CONTEXT_KEY, updated.getFirstname());
  }

  @Then("the updated booking should be returned")
  public void theUpdatedBookingShouldBeReturned() {
    getBookingResponse()
        .then()
        .body("firstname", equalTo(TestContext.get(UPDATED_FIRST_NAME_CONTEXT_KEY, String.class)));
  }

  @When("I partially update the booking")
  public void iPartiallyUpdateTheBooking() {
    String updatedFirstName = "PatchedGuest";
    Response response =
        bookingClient.patchBooking(
            getBookingId(), Map.of("firstname", updatedFirstName), getToken());
    TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(UPDATED_FIRST_NAME_CONTEXT_KEY, updatedFirstName);
  }

  @Then("the partial update should be returned")
  public void thePartialUpdateShouldBeReturned() {
    getBookingResponse()
        .then()
        .body("firstname", equalTo(TestContext.get(UPDATED_FIRST_NAME_CONTEXT_KEY, String.class)));
  }

  @When("I delete the booking")
  public void iDeleteTheBooking() {
    Response response = bookingClient.deleteBooking(getBookingId(), getToken());
    TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  private void registerBookingCleanup(Response response) {
    if (response.statusCode() != 201) {
      return;
    }

    int bookingId = response.jsonPath().getInt("bookingid");
    if (bookingId <= 0) {
      throw new IllegalStateException("Booking creation succeeded without a valid booking ID");
    }

    String token = getToken();
    TestContext.registerCleanupAction(
        cleanupKeyForBooking(bookingId),
        () -> {
          if (getBookingResponse().statusCode() == 201) {
            return;
          }
          Response cleanupResponse = bookingClient.deleteBooking(bookingId, token);
          cleanupResponse.then().statusCode(201);
        });
  }

  private String cleanupKeyForBooking(int bookingId) {
    return "booking:" + bookingId;
  }

  private BookingRequest getBookingRequest() {
    return TestContext.get(BOOKING_REQUEST_CONTEXT_KEY, BookingRequest.class);
  }

  private Response getBookingResponse() {
    return TestContext.get(BOOKING_RESPONSE_CONTEXT_KEY, Response.class);
  }

  private int getBookingId() {
    return TestContext.get(BOOKING_ID_CONTEXT_KEY, Integer.class);
  }

  private String getToken() {
    return TestContext.get(TOKEN_CONTEXT_KEY, String.class);
  }
}
