package com.booking.client;

import com.booking.model.BookingRequest;
import com.booking.specification.SpecBuilder;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class BookingClient {
    public Response createBooking(BookingRequest bookingRequest) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .body(bookingRequest)
                .post("/booking");
    }

    public Response getBooking(int bookingId, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .cookie("token", token)
                .get("/booking/{id}", bookingId);
    }

    public Response updateBooking(int bookingId, BookingRequest bookingRequest, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .cookie("token", token)
                .body(bookingRequest)
                .put("/booking/{id}", bookingId);
    }

    public Response patchBooking(int bookingId, Map<String, Object> updates, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .cookie("token", token)
                .body(updates)
                .patch("/booking/{id}", bookingId);
    }

    public Response deleteBooking(int bookingId, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .cookie("token", token)
                .delete("/booking/{id}", bookingId);
    }
}
