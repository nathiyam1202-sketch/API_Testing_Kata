package com.booking.stepdefinitions;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.booking.client.RoomClient;
import com.booking.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class RoomSteps {
  private static final String ROOM_RESPONSE_CONTEXT_KEY = "rooms.response";

  @When("I request the room list")
  public void iRequestTheRoomList() {
    Response response = new RoomClient().getRooms();
    TestContext.put(ROOM_RESPONSE_CONTEXT_KEY, response);
    TestContext.put(TestContext.API_RESPONSE_CONTEXT_KEY, response);
  }

  @Then("I should receive the available rooms")
  public void iShouldReceiveTheAvailableRooms() {
    Response response = TestContext.get(ROOM_RESPONSE_CONTEXT_KEY, Response.class);
    response
        .then()
        .body("rooms.size()", greaterThan(0))
        .body("rooms.roomid", everyItem(notNullValue()))
        .body("rooms.roomName", everyItem(not(isEmptyOrNullString())));
  }
}
