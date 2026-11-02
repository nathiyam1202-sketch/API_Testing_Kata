@booking
Feature: Delete booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  Scenario: Delete an existing booking
    When I delete the booking
    Then the API response status code should be 201

  Scenario Outline: Reject a <token_state> token when deleting a booking
    When I call the protected booking endpoint "delete" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | token_state |
      | missing     |
      | invalid     |
