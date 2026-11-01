@contact-form
Feature: Contact form
  Scenario: Submit a contact form successfully
    Given I have valid contact form details
    When I submit the contact form
    Then the contact form should be submitted successfully
