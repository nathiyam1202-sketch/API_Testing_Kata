@booking
Feature: Booking
  Background:
    Given I am authenticated
    And I have a valid booking request

  Scenario: Create a booking successfully
    When I create a booking
    Then the booking should be created successfully
    And the API response status code should be 201
    And the API response should match the "booking creation" JSON schema

  Scenario: Retrieve an existing booking
    Given a booking exists
    When I retrieve the booking
    Then the booking details should match the created booking
    And the API response status code should be 200
    And the API response should match the "booking" JSON schema

  Scenario: Update an existing booking
    Given a booking exists
    When I update the booking
    Then the updated booking should be returned
    And the API response status code should be 200
    And the API response should match the "booking" JSON schema

  Scenario: Partially update an existing booking
    Given a booking exists
    When I partially update the booking
    Then the partial update should be returned
    And the API response status code should be 200
    And the API response should match the "booking" JSON schema

  Scenario: Delete an existing booking
    Given a booking exists
    When I delete the booking
    Then the API response status code should be 201

  Scenario Outline: Reject <token_state> token on protected booking <operation>
    Given a booking exists
    When I call the protected booking endpoint "<operation>" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | operation       | token_state |
      | retrieve        | missing     |
      | update          | missing     |
      | partially update| missing     |
      | delete          | missing     |
      | retrieve        | expired     |
      | update          | expired     |
      | partially update| expired     |
      | delete          | expired     |
      | retrieve        | invalid     |
      | update          | invalid     |
      | partially update| invalid     |
      | delete          | invalid     |

  Scenario Outline: Reject a booking with invalid <field>
    Given I have a booking request with invalid "<field>"
    When I create a booking
    Then the API response status code should be <status>
    And the API response error message should contain "<error>"

    Examples:
      | field     | status | error                               |
      | firstname | 400    | size must be between 3 and 18       |
      | lastname  | 400    | size must be between 3 and 30       |
      | phone     | 400    | size must be between 11 and 21      |

  Scenario Outline: Reject a booking when <date_rule>
    Given I have a booking request violating date rule "<date_rule>"
    When I create a booking
    Then the API response status code should be 409
    And the API response error message should contain "Failed to create booking"

    Examples:
      | date_rule                 |
      | checkout_before_checkin   |
      | checkout_same_as_checkin  |

  Scenario Outline: Reject a booking when mandatory field <field> is missing
    When I create a booking with a missing "<field>" field
    Then the API response status code should be 400
    And the API response should contain a validation error

    Examples:
      | field                |
      | roomid               |
      | firstname            |
      | lastname             |
      | depositpaid          |
      | bookingdates         |
      | bookingdates.checkin |
      | bookingdates.checkout|

  Scenario Outline: Reject a booking with malformed email <email>
    Given I have a booking request with malformed email "<email>"
    When I create a booking
    Then the API response status code should be 400
    And the API response error message should contain "must be a well-formed email address"

    Examples:
      | email          |
      | not-an-email   |
      | user@          |
      | @example.com   |
