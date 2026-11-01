@authentication
Feature: Authentication
  Scenario: Authenticate successfully and receive a token
    Given I have valid authentication credentials
    When I request an authentication token
    Then the API response status code should be 200
    And the API response field "token" should not be empty
    And the API response should match the "authentication" JSON schema
