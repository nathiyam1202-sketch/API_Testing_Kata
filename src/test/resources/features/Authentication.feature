@authentication
Feature: Authentication
  Scenario: Authenticate successfully and receive a token
    Given I have valid authentication credentials
    When I request an authentication token
    Then the authentication response should be successful and contain a token
