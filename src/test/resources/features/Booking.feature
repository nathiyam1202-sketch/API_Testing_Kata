@booking
Feature: Booking
  Background:
    Given I am authenticated
    And I have a valid booking request

  Scenario: Create a booking successfully
    When I create a booking
    Then the booking should be created successfully

  Scenario: Retrieve an existing booking
    Given a booking exists
    When I retrieve the booking
    Then the booking details should match the created booking

  Scenario: Update an existing booking
    Given a booking exists
    When I update the booking
    Then the updated booking should be returned

  Scenario: Partially update an existing booking
    Given a booking exists
    When I partially update the booking
    Then the partial update should be returned

  Scenario: Delete an existing booking
    Given a booking exists
    When I delete the booking
    Then the booking should be deleted successfully
