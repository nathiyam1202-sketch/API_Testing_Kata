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
import com.booking.model.BookingDates;
import com.booking.model.BookingRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class BookingSteps {
  private final BookingClient bookingClient = new BookingClient();

  @Given("I am authenticated")
  public void iAmAuthenticated() {
    AuthRequest credentials =
        new AuthRequest(ConfigReader.getAuthUsername(), ConfigReader.getAuthPassword());
    Response response = new AuthClient().login(credentials);
    response.then().statusCode(200).body("token", not(emptyOrNullString()));
    TestContext.setToken(response.jsonPath().getString("token"));
  }

  @Given("I have a valid booking request")
  public void iHaveAValidBookingRequest() {
    TestContext.setBookingRequest(BookingDataGenerator.generateBookingRequest());
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

  @When("I create a booking with invalid payload variation {string}")
  public void iCreateABookingWithInvalidPayloadVariation(String variation) {
    storeBookingCreationResponse(
        bookingClient.createBooking(BookingRequestVariations.withInvalidPayload(variation)));
  }

  @When("I create a booking with special characters in the guest names")
  public void iCreateABookingWithSpecialCharactersInTheGuestNames() {
    TestContext.setBookingRequest(BookingRequestVariations.withSpecialCharactersInNames());
    iCreateABooking();
  }

  @When("I submit the same booking twice")
  public void iSubmitTheSameBookingTwice() {
    Response initialResponse = bookingClient.createBooking(getBookingRequest());
    registerBookingCleanup(initialResponse);
    initialResponse.then().statusCode(201);
    storeResponse(bookingClient.createBooking(getBookingRequest()));
  }

  @When("I repeat booking creation and deletion twice")
  public void iRepeatBookingCreationAndDeletionTwice() {
    for (int cycle = 0; cycle < 2; cycle++) {
      BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();
      TestContext.setBookingRequest(bookingRequest);
      Response created = bookingClient.createBooking(bookingRequest);
      registerBookingCleanup(created);
      created.then().statusCode(201).body("bookingid", greaterThan(0));
      int bookingId = created.jsonPath().getInt("bookingid");

      Response deleted = bookingClient.deleteBooking(bookingId, getToken());
      deleted.then().statusCode(201);
      TestContext.removeCleanupAction(cleanupKeyForBooking(bookingId));
      storeResponse(deleted);
    }
  }

  @Given("I have a booking request with malformed email {string}")
  public void iHaveABookingRequestWithMalformedEmail(String email) {
    TestContext.setBookingRequest(BookingRequestVariations.withMalformedEmail(email));
  }

  @Given("I have a booking request violating date rule {string}")
  public void iHaveABookingRequestViolatingDateRule(String dateRule) {
    TestContext.setBookingRequest(BookingRequestVariations.violatingDateRule(dateRule));
  }

  @Given("I have a booking request with invalid {string}")
  public void iHaveABookingRequestWithInvalidField(String field) {
    TestContext.setBookingRequest(BookingRequestVariations.withInvalidField(field));
  }

  @Then("the booking should be created successfully")
  public void theBookingShouldBeCreatedSuccessfully() {
    assertResponseStatus(201);
    getBookingResponse()
        .then()
        .body("bookingid", greaterThan(0))
        .body("firstname", equalTo(getBookingRequest().getFirstname()))
        .body("lastname", equalTo(getBookingRequest().getLastname()));
  }

  @Then("the booking response should contain a valid generated ID")
  public void theBookingResponseShouldContainAValidGeneratedId() {
    assertResponseStatus(201);
    getBookingResponse().then().body("bookingid", greaterThan(0));
  }

  @Given("a booking exists")
  public void aBookingExists() {
    Response response = bookingClient.createBooking(getBookingRequest());
    registerBookingCleanup(response);
    response.then().statusCode(201).body("bookingid", greaterThan(0));
    TestContext.setBookingId(response.jsonPath().getInt("bookingid"));
    storeResponse(response);
  }

  @When("I create a second booking with distinct guest data")
  public void iCreateASecondBookingWithDistinctGuestData() {
    BookingRequest secondBooking = BookingDataGenerator.generateBookingRequest();
    secondBooking.setFirstname("OtherGuest");
    secondBooking.setLastname("IsolationGuest");
    Response response = bookingClient.createBooking(secondBooking);
    registerBookingCleanup(response);
    response.then().statusCode(201).body("bookingid", greaterThan(0));
    TestContext.setSecondBooking(secondBooking, response.jsonPath().getInt("bookingid"));
    storeResponse(response);
  }

  @When("I update the second booking with valid guest data")
  public void iUpdateTheSecondBookingWithValidGuestData() {
    BookingRequest original = TestContext.secondBookingRequest();
    BookingRequest updated =
        new BookingRequest(
            original.getRoomid(),
            "SecondUpdated",
            original.getLastname(),
            original.isDepositpaid(),
            original.getBookingdates(),
            original.getEmail(),
            original.getPhone());
    TestContext.setSecondBooking(updated, TestContext.secondBookingId());
    storeResponse(bookingClient.updateBooking(TestContext.secondBookingId(), updated, getToken()));
  }

  @Then("the second booking should have a distinct ID and guest data")
  public void theSecondBookingShouldHaveADistinctIdAndGuestData() {
    if (getBookingId() == TestContext.secondBookingId()) {
      throw new AssertionError("Expected distinct booking IDs for separate bookings");
    }
    storeResponse(bookingClient.getBooking(TestContext.secondBookingId(), getToken()));
    assertBookingDetails(TestContext.secondBookingRequest(), TestContext.secondBookingId());
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
    assertBookingDetails(getBookingRequest());
  }

  @When("I update the booking")
  public void iUpdateTheBooking() {
    BookingRequest updated = updatedBookingRequest();
    TestContext.setBookingRequest(updated);
    storeResponse(bookingClient.updateBooking(getBookingId(), updated, getToken()));
    TestContext.setUpdatedFirstName(updated.getFirstname());
  }

  @When("I update the booking dates with valid values")
  public void iUpdateTheBookingDatesWithValidValues() {
    BookingRequest updated = updatedBookingDates();
    TestContext.setBookingRequest(updated);
    storeResponse(bookingClient.updateBooking(getBookingId(), updated, getToken()));
  }

  @Then("the updated booking should be returned")
  public void theUpdatedBookingShouldBeReturned() {
    assertUpdatedFirstName();
  }

  @Then("the booking response should have status code {int} and match all submitted booking fields")
  public void theBookingResponseShouldMatchAllSubmittedBookingFields(int statusCode) {
    assertBookingDetails(getBookingRequest(), getBookingId(), statusCode);
  }

  @Then("the booking dates should use the documented ISO format")
  public void theBookingDatesShouldUseTheDocumentedIsoFormat() {
    assertIsoDate("bookingdates.checkin");
    assertIsoDate("bookingdates.checkout");
  }

  @When("I partially update the booking")
  public void iPartiallyUpdateTheBooking() {
    String updatedFirstName = "PatchedGuest";
    storeResponse(
        bookingClient.patchBooking(
            getBookingId(), Map.of("firstname", updatedFirstName), getToken()));
    TestContext.setUpdatedFirstName(updatedFirstName);
  }

  @When("I submit an empty partial update")
  public void iSubmitAnEmptyPartialUpdate() {
    storeResponse(bookingClient.patchBooking(getBookingId(), Map.of(), getToken()));
  }

  @Then("the partial update should be returned")
  public void thePartialUpdateShouldBeReturned() {
    assertUpdatedFirstName();
  }

  @Then("the partial update should preserve all omitted booking fields")
  public void thePartialUpdateShouldPreserveAllOmittedBookingFields() {
    assertBookingDetailsWithFirstName(getBookingRequest(), TestContext.updatedFirstName());
  }

  @Then("the empty partial update should preserve all booking fields")
  public void theEmptyPartialUpdateShouldPreserveAllBookingFields() {
    assertBookingDetails(getBookingRequest());
  }

  @When("I retrieve the second booking")
  public void iRetrieveTheSecondBooking() {
    storeResponse(bookingClient.getBooking(TestContext.secondBookingId(), getToken()));
  }

  @Then("the second booking details should match its submitted values")
  public void theSecondBookingDetailsShouldMatchItsSubmittedValues() {
    assertBookingDetails(TestContext.secondBookingRequest(), TestContext.secondBookingId());
  }

  @Then("the updated booking should retain its original ID")
  public void theUpdatedBookingShouldRetainItsOriginalId() {
    assertResponseStatus(200);
    getBookingResponse().then().body("bookingid", equalTo(getBookingId()));
  }

  @Then("the second booking should retain its original ID")
  public void theSecondBookingShouldRetainItsOriginalId() {
    assertResponseStatus(200);
    getBookingResponse().then().body("bookingid", equalTo(TestContext.secondBookingId()));
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
    assertResponseStatus(200);
    String expectedFirstName = TestContext.updatedFirstName();
    getBookingResponse().then().body("firstname", equalTo(expectedFirstName));
  }

  private void assertIsoDate(String jsonPath) {
    String date = getBookingResponse().jsonPath().getString(jsonPath);
    if (date == null || date.isBlank()) {
      throw new AssertionError("Expected response field \"" + jsonPath + "\" to contain a date");
    }
    try {
      if (!LocalDate.parse(date).toString().equals(date)) {
        throw new AssertionError(
            "Expected response field \"" + jsonPath + "\" to use yyyy-MM-dd format");
      }
    } catch (DateTimeParseException exception) {
      throw new AssertionError(
          "Expected response field \"" + jsonPath + "\" to be a valid ISO date", exception);
    }
  }

  private void assertBookingDetails(BookingRequest bookingRequest) {
    assertBookingDetails(bookingRequest, getBookingId(), 200);
  }

  private void assertBookingDetails(BookingRequest bookingRequest, int bookingId) {
    assertBookingDetails(bookingRequest, bookingId, 200);
  }

  private void assertBookingDetails(
      BookingRequest bookingRequest, int bookingId, int expectedStatusCode) {
    assertBookingDetailsWithFirstName(
        bookingRequest, bookingRequest.getFirstname(), bookingId, expectedStatusCode);
  }

  private void assertBookingDetailsWithFirstName(
      BookingRequest bookingRequest, String expectedFirstName) {
    assertBookingDetailsWithFirstName(bookingRequest, expectedFirstName, getBookingId(), 200);
  }

  private void assertBookingDetailsWithFirstName(
      BookingRequest bookingRequest, String expectedFirstName, int bookingId) {
    assertBookingDetailsWithFirstName(bookingRequest, expectedFirstName, bookingId, 200);
  }

  private void assertBookingDetailsWithFirstName(
      BookingRequest bookingRequest,
      String expectedFirstName,
      int bookingId,
      int expectedStatusCode) {
    assertResponseStatus(expectedStatusCode);
    getBookingResponse()
        .then()
        .body("bookingid", equalTo(bookingId))
        .body("roomid", equalTo(bookingRequest.getRoomid()))
        .body("firstname", equalTo(expectedFirstName))
        .body("lastname", equalTo(bookingRequest.getLastname()))
        .body("depositpaid", equalTo(bookingRequest.isDepositpaid()))
        .body("bookingdates.checkin", equalTo(bookingRequest.getBookingdates().getCheckin()))
        .body("bookingdates.checkout", equalTo(bookingRequest.getBookingdates().getCheckout()));
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

  private BookingRequest updatedBookingDates() {
    BookingRequest original = getBookingRequest();
    LocalDate checkin = LocalDate.parse(original.getBookingdates().getCheckin()).plusDays(2);
    return new BookingRequest(
        original.getRoomid(),
        original.getFirstname(),
        original.getLastname(),
        original.isDepositpaid(),
        new BookingDates(checkin.toString(), checkin.plusDays(1).toString()),
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
      case "invalid" -> "invalid-test-token";
      default -> throw new IllegalArgumentException("Unsupported token state: " + tokenState);
    };
  }

  private void storeBookingCreationResponse(Response response) {
    registerBookingCleanup(response);
    storeResponse(response);
    if (response.statusCode() == 201) {
      TestContext.setBookingId(response.jsonPath().getInt("bookingid"));
    }
  }

  private void storeResponse(Response response) {
    TestContext.setLatestResponse(response);
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
    return TestContext.bookingRequest();
  }

  private Response getBookingResponse() {
    return TestContext.latestResponse();
  }

  private int getBookingId() {
    return TestContext.bookingId();
  }

  private void assertResponseStatus(int expectedStatusCode) {
    int actualStatusCode = getBookingResponse().statusCode();
    if (actualStatusCode != expectedStatusCode) {
      throw new AssertionError(
          "Expected HTTP status " + expectedStatusCode + " but was " + actualStatusCode);
    }
  }

  private String getToken() {
    return TestContext.token();
  }
}
