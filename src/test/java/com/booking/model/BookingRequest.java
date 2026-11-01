package com.booking.model;

public class BookingRequest {
  private int roomid;
  private String firstname;
  private String lastname;
  private boolean depositpaid;
  private BookingDates bookingdates;
  private String email;
  private String phone;

  public BookingRequest() {}

  public BookingRequest(
      int roomid,
      String firstname,
      String lastname,
      boolean depositpaid,
      BookingDates bookingdates,
      String email,
      String phone) {
    this.roomid = roomid;
    this.firstname = firstname;
    this.lastname = lastname;
    this.depositpaid = depositpaid;
    this.bookingdates = bookingdates;
    this.email = email;
    this.phone = phone;
  }

  public int getRoomid() {
    return roomid;
  }

  public void setRoomid(int roomid) {
    this.roomid = roomid;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public boolean isDepositpaid() {
    return depositpaid;
  }

  public void setDepositpaid(boolean depositpaid) {
    this.depositpaid = depositpaid;
  }

  public BookingDates getBookingdates() {
    return bookingdates;
  }

  public void setBookingdates(BookingDates bookingdates) {
    this.bookingdates = bookingdates;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
