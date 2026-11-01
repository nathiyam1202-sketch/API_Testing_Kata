package com.booking.model;

public class BookingResponse {
    private int bookingid;
    private BookingRequest booking;

    public BookingResponse() {
    }

    public BookingResponse(int bookingid, BookingRequest booking) {
        this.bookingid = bookingid;
        this.booking = booking;
    }

    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }

    public BookingRequest getBooking() {
        return booking;
    }

    public void setBooking(BookingRequest booking) {
        this.booking = booking;
    }
}
