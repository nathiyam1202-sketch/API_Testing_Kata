@booking
Feature: Booking lifecycle and data integrity
  Background:
    Given I am authenticated
    And I have a valid booking request

  @happy @positive @BOOK_INT_001 @BOOK_INT_004
  Scenario: Create, retrieve, update, retrieve, and delete a booking
    When I create a booking
    Then the booking should be created successfully
    When I retrieve the booking
    Then the booking details should match the created booking
    When I update the booking
    Then the booking response should have status code 200 and match all submitted booking fields
    When I retrieve the booking
    Then the booking response should have status code 200 and match all submitted booking fields
    When I delete the booking
    Then the API response status code should be 201

  @happy @positive @BOOK_INT_002 @BOOK_INT_006
  Scenario: Keep independent booking data isolated between scenario operations
    When I create a booking
    Then the booking should be created successfully
    When I create a second booking with distinct guest data
    Then the second booking should have a distinct ID and guest data
    When I retrieve the booking
    Then the booking details should match the created booking
    When I update the second booking with valid guest data
    Then the second booking should retain its original ID
    When I retrieve the second booking
    Then the second booking details should match its submitted values
    When I retrieve the booking
    Then the booking details should match the created booking
