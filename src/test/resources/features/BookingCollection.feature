@booking
Feature: Booking collection
  Background:
    Given I am authenticated
    And I have a valid booking request

  @happy @positive
  Scenario: BOOK_001 Create a booking with valid details
    When I create a booking
    Then the booking should be created successfully
    And the API response should match the "booking creation" JSON schema

  @happy @positive
  Scenario: BOOK_002 Return a generated booking ID after creation
    When I create a booking
    Then the booking response should contain a valid generated ID

  @happy @positive
  Scenario: BOOK_NEW_008 Create a booking with special characters in guest names
    When I create a booking with special characters in the guest names
    Then the booking response should have status code 201 and match all submitted booking fields

  @unhappy @negative @invalid
  Scenario Outline: Reject a booking with invalid <field>
    Given I have a booking request with invalid "<field>"
    When I create a booking
    Then the API response should have status code <status> and error message containing "<error>"

    Examples:
      | field     | status | error                         |
      | firstname | 400    | size must be between 3 and 18 |
      | lastname  | 400    | size must be between 3 and 30 |
      | phone     | 400    | size must be between 11 and 21 |

  @unhappy @negative @invalid
  Scenario: BOOK_013 Reject checkout before check-in
    Given I have a booking request violating date rule "checkout_before_checkin"
    When I create a booking
    Then the API response should have status code 409 and error message containing "Failed to create booking"

  @unhappy @negative @invalid
  Scenario: BOOK_015 Reject overlapping bookings for the same room and dates
    When I submit the same booking twice
    Then the API response status code should be 409

  @unhappy @negative @invalid @BOOK_INT_008
  Scenario Outline: <id> Reject booking when mandatory field <field> is missing
    When I create a booking with a missing "<field>" field
    Then the API response should have status code 400 and a validation error

    Examples:
      | id       | field     |
      | BOOK_006 | firstname |
      | BOOK_007 | lastname  |
      | BOOK_008 | email     |
      | BOOK_010 | roomid    |

  @unhappy @negative @invalid
  Scenario Outline: <id> Reject booking when required data <field> is missing
    When I create a booking with a missing "<field>" field
    Then the API response should have status code 400 and a validation error

    Examples:
      | id            | field                 |
      | BOOK_NEW_001  | phone                 |
      | BOOK_NEW_009  | bookingdates          |
      | BOOK_NEW_010  | bookingdates.checkin  |
      | BOOK_NEW_010  | bookingdates.checkout |

  @unhappy @negative @invalid
  Scenario Outline: <id> Reject an invalid booking payload variation
    When I create a booking with invalid payload variation "<variation>"
    Then the API response should have status code 400 and a validation error

    Examples:
      | id            | variation           |
      | BOOK_NEW_002  | empty_firstname     |
      | BOOK_NEW_003  | blank_lastname      |
      | BOOK_NEW_004  | null_email          |
      | BOOK_NEW_005  | empty_email         |
      | BOOK_NEW_006  | malformed_phone     |
      | BOOK_NEW_007  | long_lastname       |
      | BOOK_NEW_011  | invalid_date_format |
      | BOOK_NEW_012  | invalid_roomid_type |

  @happy @positive @BOOK_INT_007
  Scenario: Repeat a booking create and delete cycle twice
    When I repeat booking creation and deletion twice

  @unhappy @negative @invalid
  Scenario Outline: BOOK_009 Reject a booking with malformed email <email>
    Given I have a booking request with malformed email "<email>"
    When I create a booking
    Then the API response should have status code 400 and error message containing "must be a well-formed email address"

    Examples:
      | email        |
      | not-an-email |
      | user@        |
      | @example.com |
