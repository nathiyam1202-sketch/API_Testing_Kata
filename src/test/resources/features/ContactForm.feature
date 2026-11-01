@contact-form
Feature: Contact form
  Scenario: Submit a contact form successfully
    Given I have valid contact form details
    When I submit the contact form
    Then the API response status code should be 201
    And the API response field "message" should not be empty
    And the API response should match the "message" JSON schema
