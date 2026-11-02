@booking
Feature: Partially update booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  Scenario: Partially update an existing booking
    When I partially update the booking
    Then the partial update should be returned
    And the API response status code should be 200
    And the API response should match the "booking" JSON schema

  Scenario Outline: Reject a <token_state> token when partially updating a booking
    When I call the protected booking endpoint "partially update" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | token_state |
      | missing     |
      | invalid     |
