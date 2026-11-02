@booking
Feature: Booking collection
  Background:
    Given I am authenticated
    And I have a valid booking request

  Scenario: Create a booking successfully
    When I create a booking
    Then the booking should be created successfully
    And the API response status code should be 201
    And the API response should match the "booking creation" JSON schema

  Scenario Outline: Reject a booking with invalid <field>
    Given I have a booking request with invalid "<field>"
    When I create a booking
    Then the API response status code should be <status>
    And the API response error message should contain "<error>"

    Examples:
      | field     | status | error                         |
      | firstname | 400    | size must be between 3 and 18 |
      | lastname  | 400    | size must be between 3 and 30 |
      | phone     | 400    | size must be between 11 and 21 |

  Scenario Outline: Reject a booking when <date_rule>
    Given I have a booking request violating date rule "<date_rule>"
    When I create a booking
    Then the API response status code should be 409
    And the API response error message should contain "Failed to create booking"

    Examples:
      | date_rule                |
      | checkout_before_checkin  |
      | checkout_same_as_checkin |

  Scenario Outline: Reject a booking when mandatory field <field> is missing
    When I create a booking with a missing "<field>" field
    Then the API response status code should be 400
    And the API response should contain a validation error

    Examples:
      | field                 |
      | roomid                |
      | firstname             |
      | lastname              |
      | depositpaid           |
      | bookingdates          |
      | bookingdates.checkin  |
      | bookingdates.checkout |
      | email                 |
      | phone                 |

  Scenario Outline: Reject a booking with malformed email <email>
    Given I have a booking request with malformed email "<email>"
    When I create a booking
    Then the API response status code should be 400
    And the API response error message should contain "must be a well-formed email address"

    Examples:
      | email        |
      | not-an-email |
      | user@        |
      | @example.com |
