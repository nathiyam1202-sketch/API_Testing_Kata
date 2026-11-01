@message

Feature: Get messages
  Scenario: Message
    When I want to read the messages
    Then the API response status code should be 200
    And I should receive all existing messages