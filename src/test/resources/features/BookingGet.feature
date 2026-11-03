@booking
Feature: Retrieve booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  @happy @positive @BOOK_GET_003 @BOOK_GET_005 @BOOK_GET_006 @BOOK_GET_007 @BOOK_GET_008 @BOOK_INT_003
  Scenario: BOOK_003 Retrieve a booking using its ID
    When I retrieve the booking
    Then the API response should match the "booking" JSON schema
    And the booking dates should use the documented ISO format

  @happy @positive @BOOK_004
  Scenario: BOOK_004 Retrieved booking details match submitted values
    When I retrieve the booking
    Then the booking details should match the created booking

  @unhappy @negative
  Scenario Outline: Reject a <token_state> token when retrieving a booking
    When I call the protected booking endpoint "retrieve" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | token_state |
      | missing     |

    @invalid
    Examples:
      | token_state |
      | invalid     |
