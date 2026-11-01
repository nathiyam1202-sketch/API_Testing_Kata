package com.booking.data;

import com.booking.model.BookingDates;
import com.booking.model.BookingRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BookingRequestVariations {
  private static final String BOOKING_DATES_FIELD = "bookingdates";

  private BookingRequestVariations() {}

  public static Map<String, Object> withMissingField(String field) {
    Map<String, Object> bookingPayload = BookingDataGenerator.generateBooking();
    if (field.startsWith(BOOKING_DATES_FIELD + ".")) {
      removeBookingDateField(bookingPayload, field.substring(BOOKING_DATES_FIELD.length() + 1));
    } else if (bookingPayload.remove(field) == null) {
      throw new IllegalArgumentException("Unsupported missing booking field: " + field);
    }
    return bookingPayload;
  }

  public static BookingRequest withMalformedEmail(String email) {
    BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();
    bookingRequest.setEmail(email);
    return bookingRequest;
  }

  public static BookingRequest violatingDateRule(String dateRule) {
    BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();
    LocalDate checkin = LocalDate.parse(bookingRequest.getBookingdates().getCheckin());
    LocalDate checkout =
        switch (dateRule) {
          case "checkout_before_checkin" -> checkin.minusDays(1);
          case "checkout_same_as_checkin" -> checkin;
          default ->
              throw new IllegalArgumentException("Unsupported booking date rule: " + dateRule);
        };
    bookingRequest.setBookingdates(new BookingDates(checkin.toString(), checkout.toString()));
    return bookingRequest;
  }

  public static BookingRequest withInvalidField(String field) {
    BookingRequest bookingRequest = BookingDataGenerator.generateBookingRequest();
    switch (field) {
      case "firstname" -> bookingRequest.setFirstname("Jo");
      case "lastname" -> bookingRequest.setLastname("D");
      case "phone" -> bookingRequest.setPhone("123");
      default -> throw new IllegalArgumentException("Unsupported invalid booking field: " + field);
    }
    return bookingRequest;
  }

  private static void removeBookingDateField(Map<String, Object> bookingPayload, String field) {
    Object value = bookingPayload.get(BOOKING_DATES_FIELD);
    if (!(value instanceof Map<?, ?> existingDates)) {
      throw new IllegalStateException("Generated booking payload has no bookingdates object");
    }

    Map<String, Object> bookingDates = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : existingDates.entrySet()) {
      if (!(entry.getKey() instanceof String key)) {
        throw new IllegalStateException("Booking date field names must be strings");
      }
      bookingDates.put(key, entry.getValue());
    }
    if (bookingDates.remove(field) == null) {
      throw new IllegalArgumentException(
          "Unsupported missing booking field: " + BOOKING_DATES_FIELD + "." + field);
    }
    bookingPayload.put(BOOKING_DATES_FIELD, bookingDates);
  }
}
