@room
Feature: Room listing
  Scenario: List available rooms
    When I request the room list
    Then the API response status code should be 200
    And I should receive the available rooms
