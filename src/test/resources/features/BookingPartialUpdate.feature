@booking
Feature: Partially update booking
  Background:
    Given I am authenticated
    And I have a valid booking request
    And a booking exists

  @happy @positive @BOOK_PATCH_001 @BOOK_PATCH_002 @BOOK_PATCH_007
  Scenario: Partially update the guest name and preserve omitted fields
    When I partially update the booking
    Then the partial update should be returned
    And the API response should match the "booking" JSON schema
    When I retrieve the booking
    Then the partial update should preserve all omitted booking fields

  @happy @positive @BOOK_PATCH_004
  Scenario: Submit an empty partial update
    When I submit an empty partial update
    Then the empty partial update should preserve all booking fields

  @unhappy @negative
  Scenario Outline: Reject a <token_state> token when partially updating a booking
    When I call the protected booking endpoint "partially update" with a "<token_state>" token
    Then the API response status code should be 401

    Examples:
      | token_state |
      | missing     |

    @invalid @BOOK_PATCH_008
    Examples:
      | token_state |
      | invalid     |
