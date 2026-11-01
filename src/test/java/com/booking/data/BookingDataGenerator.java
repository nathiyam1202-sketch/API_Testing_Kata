package com.booking.data;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BookingDataGenerator {
    private BookingDataGenerator() {
    }

    public static Map<String, Object> generateBooking() {
        LocalDate checkin = LocalDate.now().plusDays(1);
        Map<String, String> bookingDates = new LinkedHashMap<>();
        bookingDates.put("checkin", checkin.toString());
        bookingDates.put("checkout", checkin.plusDays(1).toString());

        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("roomid", 2);
        booking.put("firstname", "Test" + randomLetters(8));
        booking.put("lastname", "Guest" + randomLetters(8));
        booking.put("depositpaid", true);
        booking.put("bookingdates", bookingDates);
        booking.put("email", "test+" + UUID.randomUUID() + "@example.com");
        booking.put("phone", "1" + randomDigits(10));
        return booking;
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
