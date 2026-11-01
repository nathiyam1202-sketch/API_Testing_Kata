package com.booking.client;

import com.booking.model.MessageRequest;
import com.booking.specification.SpecBuilder;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class MessageClient {
    public Response getMessages() {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .get("/message");
    }

    public Response createMessage(MessageRequest messageRequest) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .body(messageRequest)
                .post("/message");
    }
}
