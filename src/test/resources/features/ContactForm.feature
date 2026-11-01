@contact-form
Feature: Contact form
  Scenario: Submit a contact form successfully
    Given I have valid contact form details
    When I submit the contact form
    Then the API response status code should be 201
    And the API response field "message" should not be empty
    And the API response should match the "message" JSON schema

  Scenario Outline: Reject a contact form without mandatory <field>
    When I submit the contact form without the "<field>" field
    Then the API response status code should be 400
    And the API response should contain a validation error

    Examples:
      | field       |
      | name        |
      | email       |
      | description |

  Scenario Outline: Reject <markup_type> markup in contact form <field>
    Given I have valid contact form details
    When I submit the contact form with "<markup_type>" markup in the "<field>" field
    Then the API response status code should be 400
    And the API response should contain a validation error

    Examples:
      | markup_type | field       |
      | HTML        | name        |
      | script      | name        |
      | HTML        | subject     |
      | script      | subject     |
      | HTML        | description |
      | script      | description |
