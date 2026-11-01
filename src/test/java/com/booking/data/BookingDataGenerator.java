package com.booking.data;

import com.booking.model.BookingDates;
import com.booking.model.BookingRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BookingDataGenerator {
  private BookingDataGenerator() {}

  public static Map<String, Object> generateBooking() {
    BookingRequest request = generateBookingRequest();

    Map<String, Object> booking = new LinkedHashMap<>();
    booking.put("roomid", request.getRoomid());
    booking.put("firstname", request.getFirstname());
    booking.put("lastname", request.getLastname());
    booking.put("depositpaid", request.isDepositpaid());
    booking.put(
        "bookingdates",
        Map.of(
            "checkin", request.getBookingdates().getCheckin(),
            "checkout", request.getBookingdates().getCheckout()));
    booking.put("email", request.getEmail());
    booking.put("phone", request.getPhone());
    return booking;
  }

  public static BookingRequest generateBookingRequest() {
    LocalDate checkin = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(365, 3650));
    return new BookingRequest(
        2,
        "Test" + randomLetters(8),
        "Guest" + randomLetters(8),
        true,
        new BookingDates(checkin.toString(), checkin.plusDays(1).toString()),
        "test+" + UUID.randomUUID() + "@example.com",
        "1" + randomDigits(10));
  }

  private static String randomLetters(int length) {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";
    StringBuilder value = new StringBuilder(length);
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++) {
      value.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return value.toString();
  }

  private static String randomDigits(int length) {
    StringBuilder value = new StringBuilder(length);
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++) {
      value.append(random.nextInt(10));
    }
    return value.toString();
  }
}
