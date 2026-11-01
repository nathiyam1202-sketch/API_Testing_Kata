package com.booking.client;

import static io.restassured.RestAssured.given;

import com.booking.specification.SpecBuilder;
import io.restassured.response.Response;
import java.util.Map;

public class RoomClient {
  public Response getRooms() {
    return given().spec(SpecBuilder.getRequestSpec()).get("/room");
  }

  public Response getRoom(int roomId) {
    return given().spec(SpecBuilder.getRequestSpec()).get("/room/{roomId}", roomId);
  }

  public Response createRoom(Map<String, Object> room, String token) {
    return given()
        .spec(SpecBuilder.getRequestSpec())
        .cookie("token", token)
        .body(room)
        .post("/room");
  }

  public Response updateRoom(int roomId, Map<String, Object> room, String token) {
    return given()
        .spec(SpecBuilder.getRequestSpec())
        .cookie("token", token)
        .body(room)
        .put("/room/{roomId}", roomId);
  }

  public Response deleteRoom(int roomId, String token) {
    return given()
        .spec(SpecBuilder.getRequestSpec())
        .cookie("token", token)
        .delete("/room/{roomId}", roomId);
  }
}
