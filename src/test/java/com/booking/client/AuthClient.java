package com.booking.client;

import static io.restassured.RestAssured.given;

import com.booking.model.AuthRequest;
import com.booking.specification.SpecBuilder;
import io.restassured.response.Response;

public class AuthClient {
  public Response login(AuthRequest authRequest) {
    return given().spec(SpecBuilder.getRequestSpec()).body(authRequest).post("/auth/login");
  }
}
