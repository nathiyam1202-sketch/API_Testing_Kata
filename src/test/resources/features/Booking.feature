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

  Scenario Outline: Reject a booking with invalid <field>
    Given I have a booking request with invalid "<field>"
    When I create a booking
    Then the API response status code should be <status>
    And the API response error message should contain "<error>"

    Examples:
      | field     | status | error                               |
      | firstname | 400    | size must be between 3 and 18       |
      | lastname  | 400    | size must be between 3 and 30       |
      | email     | 400    | must be a well-formed email address |
      | phone     | 400    | size must be between 11 and 21      |
      | dates     | 409    | Failed to create booking            |
