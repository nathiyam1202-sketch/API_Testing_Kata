@booking
Feature: Delete booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  @happy @positive
  Scenario: BOOK_020 Delete an existing booking
    When I delete the booking
    Then the API response status code should be 201

  @happy @positive @BOOK_DEL_005
  Scenario: Delete one booking without affecting another booking
    When I create a second booking with distinct guest data
    Then the second booking should have a distinct ID and guest data
    When I delete the booking
    Then the API response status code should be 201
    When I retrieve the second booking
    Then the second booking details should match its submitted values

  @unhappy @negative
  Scenario Outline: Reject a <token_state> token when deleting a booking
    When I call the protected booking endpoint "delete" with a "<token_state>" token
    Then the API response status code should be 401

    @BOOK_DEL_001
    Examples:
      | token_state |
      | missing     |

    @invalid @BOOK_DEL_002
    Examples:
      | token_state |
      | invalid     |
