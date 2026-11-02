@booking
Feature: Retrieve booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  Scenario: Retrieve an existing booking
    When I retrieve the booking
    Then the booking details should match the created booking
    And the API response status code should be 200
    And the API response should match the "booking" JSON schema

  Scenario Outline: Reject a <token_state> token when retrieving a booking
    When I call the protected booking endpoint "retrieve" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | token_state |
      | missing     |
      | invalid     |
