@room
Feature: Room listing
  Scenario: List available rooms
    When I request the room list
    Then I should receive the available rooms
