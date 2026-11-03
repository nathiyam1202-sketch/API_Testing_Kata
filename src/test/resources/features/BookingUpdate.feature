@booking
Feature: Update booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  @happy @positive @BOOK_016 @BOOK_UPD_001 @BOOK_UPD_006
  Scenario: BOOK_016 Update a booking and verify the changes persist
    When I update the booking
    Then the booking response should have status code 200 and match all submitted booking fields
    When I retrieve the booking
    Then the booking response should have status code 200 and match all submitted booking fields
    And the API response should match the "booking" JSON schema

  @happy @positive @BOOK_UPD_002
  Scenario: Update booking dates with valid values and verify persistence
    When I update the booking dates with valid values
    Then the booking response should have status code 200 and match all submitted booking fields
    When I retrieve the booking
    Then the booking response should have status code 200 and match all submitted booking fields

  @happy @positive @BOOK_UPD_007
  Scenario: Update one booking without changing a separate booking
    When I create a second booking with distinct guest data
    Then the second booking should have a distinct ID and guest data
    When I update the booking
    Then the booking response should have status code 200 and match all submitted booking fields
    When I retrieve the second booking
    Then the second booking details should match its submitted values

  @unhappy @negative
  Scenario Outline: <id> Reject a <token_state> token when updating a booking
    When I call the protected booking endpoint "update" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | id       | token_state |
      | BOOK_018 | missing     |

    @invalid
    Examples:
      | id       | token_state |
      | BOOK_019 | invalid     |
