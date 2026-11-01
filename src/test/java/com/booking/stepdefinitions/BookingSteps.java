package com.booking.stepdefinitions;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import com.booking.client.AuthClient;
import com.booking.client.BookingClient;
import com.booking.config.ConfigReader;
import com.booking.context.TestContext;
import com.booking.data.BookingDataGenerator;
import com.booking.data.BookingRequestVariations;
import com.booking.model.AuthRequest;
import com.booking.model.BookingRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
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
    AuthRequest credentials =
        new AuthRequest(ConfigReader.getAuthUsername(), ConfigReader.getAuthPassword());
    Response response = new AuthClient().login(credentials);
    response.then().statusCode(200).body("token", not(emptyOrNullString()));
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
    Map<String, Object> payload = BookingRequestVariations.withMissingField(field);
    storeBookingCreationResponse(bookingClient.createBooking(payload));
  }

  @Given("I have a booking request with malformed email {string}")
  public void iHaveABookingRequestWithMalformedEmail(String email) {
    TestContext.put(
        BOOKING_REQUEST_CONTEXT_KEY, BookingRequestVariations.withMalformedEmail(email));
  }

  @Given("I have a booking request violating date rule {string}")
  public void iHaveABookingRequestViolatingDateRule(String dateRule) {
    TestContext.put(
        BOOKING_REQUEST_CONTEXT_KEY, BookingRequestVariations.violatingDateRule(dateRule));
  }

  @Given("I have a booking request with invalid {string}")
  public void iHaveABookingRequestWithInvalidField(String field) {
    TestContext.put(BOOKING_REQUEST_CONTEXT_KEY, BookingRequestVariations.withInvalidField(field));
  }

  @Then("the booking should be created successfully")
  public void theBookingShouldBeCreatedSuccessfully() {
    getBookingResponse()
        .then()
        .body("bookingid", greaterThan(0))
        .body("firstname", equalTo(getBookingRequest().getFirstname()))
        .body("lastname", equalTo(getBookingRequest().getLastname()));
  }

  @Given("a booking exists")
  public void aBookingExists() {
    Response response = bookingClient.createBooking(getBookingRequest());
    registerBookingCleanup(response);
    response.then().statusCode(201).body("bookingid", greaterThan(0));
    TestContext.put(BOOKING_ID_CONTEXT_KEY, response.jsonPath().getInt("bookingid"));
    storeResponse(response);
  }

  @When("I retrieve the booking")
  public void iRetrieveTheBooking() {
    storeResponse(bookingClient.getBooking(getBookingId(), getToken()));
  }

  @When("I call the protected booking endpoint {string} with a {string} token")
  public void iCallTheProtectedBookingEndpointWithAToken(String operation, String tokenState) {
    String token = tokenForState(tokenState);
    storeResponse(callProtectedOperation(operation, token));
  }

  @Then("the booking details should match the created booking")
  public void theBookingDetailsShouldMatchTheCreatedBooking() {
    BookingRequest bookingRequest = getBookingRequest();
    getBookingResponse()
        .then()
        .body("firstname", equalTo(bookingRequest.getFirstname()))
        .body("lastname", equalTo(bookingRequest.getLastname()));
  }

  @When("I update the booking")
  public void iUpdateTheBooking() {
    BookingRequest updated = updatedBookingRequest();
    storeResponse(bookingClient.updateBooking(getBookingId(), updated, getToken()));
    TestContext.put(UPDATED_FIRST_NAME_CONTEXT_KEY, updated.getFirstname());
  }

  @Then("the updated booking should be returned")
  public void theUpdatedBookingShouldBeReturned() {
    assertUpdatedFirstName();
  }

  @When("I partially update the booking")
  public void iPartiallyUpdateTheBooking() {
    String updatedFirstName = "PatchedGuest";
    storeResponse(
        bookingClient.patchBooking(
            getBookingId(), Map.of("firstname", updatedFirstName), getToken()));
    TestContext.put(UPDATED_FIRST_NAME_CONTEXT_KEY, updatedFirstName);
  }

  @Then("the partial update should be returned")
  public void thePartialUpdateShouldBeReturned() {
    assertUpdatedFirstName();
  }

  @When("I delete the booking")
  public void iDeleteTheBooking() {
    Response response = bookingClient.deleteBooking(getBookingId(), getToken());
    storeResponse(response);
    if (response.statusCode() == 201) {
      TestContext.removeCleanupAction(cleanupKeyForBooking(getBookingId()));
    }
  }

  private void assertUpdatedFirstName() {
    String expectedFirstName = TestContext.get(UPDATED_FIRST_NAME_CONTEXT_KEY, String.class);
    getBookingResponse().then().body("firstname", equalTo(expectedFirstName));
  }

  private BookingRequest updatedBookingRequest() {
    BookingRequest original = getBookingRequest();
    return new BookingRequest(
        original.getRoomid(),
        "UpdatedGuest",
        original.getLastname(),
        original.isDepositpaid(),
        original.getBookingdates(),
        original.getEmail(),
        original.getPhone());
  }

  private Response callProtectedOperation(String operation, String token) {
    return switch (operation) {
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
  }

  private String tokenForState(String tokenState) {
    return switch (tokenState) {
      case "missing" -> null;
      case "expired" -> "expired-test-token";
      case "invalid" -> "invalid-test-token";
      default -> throw new IllegalArgumentException("Unsupported token state: " + tokenState);
    };
  }

  private void storeBookingCreationResponse(Response response) {
    registerBookingCleanup(response);
    storeResponse(response);
  }

  private void storeResponse(Response response) {
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

    TestContext.registerCleanupAction(
        cleanupKeyForBooking(bookingId), () -> deleteBookingForCleanup(bookingId, getToken()));
  }

  private void deleteBookingForCleanup(int bookingId, String token) {
    Response response = bookingClient.deleteBooking(bookingId, token);
    response.then().statusCode(201);
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
