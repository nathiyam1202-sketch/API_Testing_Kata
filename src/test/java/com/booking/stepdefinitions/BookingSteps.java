package com.booking.stepdefinitions;

import com.booking.client.AuthClient;
import com.booking.client.BookingClient;
import com.booking.context.TestContext;
import com.booking.model.AuthRequest;
import com.booking.model.BookingDates;
import com.booking.model.BookingRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

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
        LocalDate checkin = LocalDate.now().plusDays(1);
        BookingRequest bookingRequest = new BookingRequest(
                2,
                "TestGuest",
                "BookingGuest",
                true,
                new BookingDates(checkin.toString(), checkin.plusDays(1).toString()),
                "test@example.com",
                "12345678901"
        );
        TestContext.put(BOOKING_REQUEST_CONTEXT_KEY, bookingRequest);
    }

    @When("I create a booking")
    public void iCreateABooking() {
        BookingRequest bookingRequest = getBookingRequest();
        Response response = bookingClient.createBooking(bookingRequest);
        TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    }

    @Then("the booking should be created successfully")
    public void theBookingShouldBeCreatedSuccessfully() {
        Response response = getBookingResponse();
        response.then()
                .statusCode(200)
                .body("bookingid", greaterThan(0))
                .body("booking.firstname", equalTo(getBookingRequest().getFirstname()))
                .body("booking.lastname", equalTo(getBookingRequest().getLastname()));
    }

    @Given("a booking exists")
    public void aBookingExists() {
        Response response = bookingClient.createBooking(getBookingRequest());
        response.then().statusCode(200).body("bookingid", greaterThan(0));
        TestContext.put(BOOKING_ID_CONTEXT_KEY, response.jsonPath().getInt("bookingid"));
    }

    @When("I retrieve the booking")
    public void iRetrieveTheBooking() {
        Response response = bookingClient.getBooking(getBookingId(), getToken());
        TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    }

    @Then("the booking details should match the created booking")
    public void theBookingDetailsShouldMatchTheCreatedBooking() {
        Response response = getBookingResponse();
        BookingRequest bookingRequest = getBookingRequest();
        response.then()
                .statusCode(200)
                .body("firstname", equalTo(bookingRequest.getFirstname()))
                .body("lastname", equalTo(bookingRequest.getLastname()))
                .body("email", equalTo(bookingRequest.getEmail()));
    }

    @When("I update the booking")
    public void iUpdateTheBooking() {
        BookingRequest original = getBookingRequest();
        BookingRequest updated = new BookingRequest(
                original.getRoomid(),
                "UpdatedGuest",
                original.getLastname(),
                original.isDepositpaid(),
                original.getBookingdates(),
                original.getEmail(),
                original.getPhone()
        );
        Response response = bookingClient.updateBooking(getBookingId(), updated, getToken());
        TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
        TestContext.put(UPDATED_FIRST_NAME_CONTEXT_KEY, updated.getFirstname());
    }

    @Then("the updated booking should be returned")
    public void theUpdatedBookingShouldBeReturned() {
        getBookingResponse().then()
                .statusCode(200)
                .body("firstname", equalTo(TestContext.get(UPDATED_FIRST_NAME_CONTEXT_KEY, String.class)));
    }

    @When("I partially update the booking")
    public void iPartiallyUpdateTheBooking() {
        String updatedFirstName = "PatchedGuest";
        Response response = bookingClient.patchBooking(
                getBookingId(),
                Map.of("firstname", updatedFirstName),
                getToken()
        );
        TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
        TestContext.put(UPDATED_FIRST_NAME_CONTEXT_KEY, updatedFirstName);
    }

    @Then("the partial update should be returned")
    public void thePartialUpdateShouldBeReturned() {
        getBookingResponse().then()
                .statusCode(200)
                .body("firstname", equalTo(TestContext.get(UPDATED_FIRST_NAME_CONTEXT_KEY, String.class)));
    }

    @When("I delete the booking")
    public void iDeleteTheBooking() {
        Response response = bookingClient.deleteBooking(getBookingId(), getToken());
        TestContext.put(BOOKING_RESPONSE_CONTEXT_KEY, response);
    }

    @Then("the booking should be deleted successfully")
    public void theBookingShouldBeDeletedSuccessfully() {
        getBookingResponse().then().statusCode(201);
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
