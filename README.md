# API Testing Kata

A Java 17 API test automation framework for the Restful Booker practice API. It uses Rest-Assured for HTTP requests and assertions, Cucumber/Gherkin for BDD scenarios, Maven for builds, and Allure plus Cucumber reports for test results.

## Technology Stack

- **Java 17** — Programming language
- **Rest-Assured** — REST API testing and response validation
- **Cucumber** — Behavior-Driven Development (BDD)
- **Gherkin** — Readable API test scenarios
- **Maven** — Build and dependency management
- **Allure** — Test reporting
- **Git** — Version control
- **GitHub Actions** — Continuous integration

## Project Structure

```text
src/
└── test/
    ├── java/com/booking/
    │   ├── client/             # API clients
    │   ├── config/             # Configuration loading
    │   ├── context/            # Scenario-scoped state and cleanup
    │   ├── data/               # Test data generation
    │   ├── hooks/              # Cucumber lifecycle hooks
    │   ├── model/              # Request and response models
    │   ├── specification/      # Shared Rest-Assured request setup
    │   └── stepdefinitions/    # Cucumber step definitions
    └── resources/
        ├── features/           # Gherkin feature files
        ├── schemas/            # JSON response schemas
        ├── spec/               # OpenAPI specification
        └── config.properties   # Default API configuration
```

The framework code is in Maven's test source/resource directories and targets Java 17.

## Framework Features

- API request execution and response validation using Rest-Assured.
- BDD scenarios written in Gherkin and executed through Cucumber.
- Validation of HTTP status codes, response bodies, fields, and JSON schemas.
- Positive, negative, boundary, and invalid-input testing.
- Reusable API testing logic and configuration management.
- Externalized API base URL configuration with JVM system-property overrides.
- Maven-based test execution.
- Cucumber HTML, JSON, and XML reporting.
- Allure test execution reporting.
- GitHub Actions workflows for framework validation and automated API testing.

## API Configuration

The default API base URL is `https://automationintesting.online/api`, configured in `src/test/resources/config.properties`. The `base.url` JVM system property takes precedence over the properties file.

Override the configured base URL for a test run:

```bash
mvn verify -Dbase.url=https://your-api-host/api
```

The authentication scenarios use the practice API's sample credentials. Do not use real or privileged credentials in source control.

## Setup

Install Java 17 and Maven 3.9 or later, then clone the repository:

```bash
git clone https://github.com/nathiyam1202-sketch/API_Testing_Kata.git
cd API_Testing_Kata
```

No separate application build or database setup is required. The default configuration targets the public practice API; use `-Dbase.url=...` to target another compatible environment.

## Running Tests

Run all Cucumber scenarios against the configured API:

```bash
mvn verify
```

Run a selected set of tags (Cucumber tag expressions support `and`, `or`, `not`, and parentheses):

```bash
mvn verify '-Dcucumber.filter.tags=@authentication or @room or @message'
```

Run one feature area, exclude a tag, or validate step bindings without making API calls:

```bash
mvn verify '-Dcucumber.filter.tags=@booking'
mvn verify '-Dcucumber.filter.tags=not @contact-form'
mvn verify -Dcucumber.execution.dry-run=true
```

Combine the base URL and tag filter as needed:

```bash
mvn verify -Dbase.url=https://your-api-host/api '-Dcucumber.filter.tags=@room or @message'
```

Clean previous build artifacts and run the complete suite:

```bash
mvn clean verify
```

Apply the configured Google Java Format style or check formatting:

```bash
mvn spotless:apply
mvn spotless:check
```

Generate and serve the Allure report after test execution:

```bash
mvn allure:serve
```

The complete suite includes write operations. Contact form submissions may persist on the external service, and remote endpoint behavior may differ from the documented API contract.

## Cucumber Tags

Tags are declared at the feature level and select all scenarios in that feature:

| Tag | Feature area | Notes |
|---|---|---|
| `@authentication` | Authentication | Requests an authentication token |
| `@booking` | Bookings | Includes create/read/update/delete, validation, and authorization scenarios |
| `@room` | Room listing | Reads the room list |
| `@message` | Messages | Reads existing contact messages |
| `@contact-form` | Contact form | Includes submissions that may persist on the external service |

The GitHub Actions API workflow currently selects `@authentication`, `@room`, and `@message`. Use the booking and contact-form tags deliberately: those scenarios include write requests, and booking mutations/cleanup have shown instability against the configured service.

## Test Reports

Maven writes test reports to the `target/` directory:

| Report | Description |
|---|---|
| `cucumber-reports.html` | Cucumber HTML execution report |
| `cucumber.json` | Machine-readable Cucumber results |
| `cucumber.xml` | Cucumber JUnit XML report |
| `allure-results/` | Raw Allure execution results |
| `site/allure-maven-plugin/` | Generated Allure report |
| `surefire-reports/` | Maven Surefire test reports |

## Continuous Integration

GitHub Actions runs on pushes, pull requests, and manual dispatch.

- `maven.yml` compiles the project and dry-runs Cucumber to validate feature-to-step bindings without external API requests.
- `maven-api-tests.yml` runs `@authentication`, `@room`, and `@message` scenarios against the configured API.
- Both workflows upload generated reports as workflow artifacts.
- Contact-form submissions and booking mutation scenarios are excluded from the automated API workflow to avoid persistent submissions and remote mutation instability.

Run the complete API suite locally with `mvn verify` when write scenarios are appropriate for the target environment.

## Testing Approach

Current feature files cover:

- Successful authentication, room listing, message listing, and booking creation.
- Booking validation, including missing fields, malformed email values, and date rules.
- Protected booking operations with missing, invalid, and fixture "expired" tokens.
- Contact form validation for omitted fields and markup inputs.
- HTTP status, response-field, error-message, and JSON-schema assertions.

The token labeled `expired` is currently represented by a deliberately invalid test token; it does not verify time-based expiration by the server.

## Known Limitations

- CI's Cucumber dry-run verifies Java compilation and step bindings but does not execute API assertions.
- The tagged API workflow makes live requests to the external practice service and may be affected by service availability or state.
- Booking retrieval, update, and delete operations have returned HTTP 500 from the configured service in prior runs. Cleanup failures may leave remote booking test data behind.
- Contact form and markup boundary scenarios have not been run against the live service because they may create persistent data and the service's expected sanitization behavior has not been confirmed.

## Architecture Decisions

| Decision | Implementation | Rationale |
|---|---|---|
| BDD test organization | Feature files describe behavior; Java step definitions call API clients and validations. | Keeps scenarios readable while allowing request and assertion code to be reused. |
| API client boundary | Endpoint-specific clients wrap Rest-Assured calls and share a request specification. | Centralizes HTTP details and keeps step definitions focused on scenario intent. |
| Request/response models | POJOs represent structured booking, authentication, and message data; maps are used where a test must omit fields. | Provides typed requests for normal flows while allowing precise malformed or incomplete payloads. |
| Scenario isolation | `TestContext` stores scenario state in `ThreadLocal` maps and hooks initialize/clear it around each scenario. | Keeps scenario state isolated by execution thread and clears it after each scenario. |
| Test data and cleanup | Booking data is generated uniquely; scenario cleanup actions are registered and run in reverse order. | Reduces collisions and provides a place to clean up test-created data. Remote cleanup can still fail if the service is unavailable. |
| Shared assertions and schemas | Reusable response steps check status, fields, and error messages; named JSON schemas are loaded from test resources. | Keeps response contracts consistent and avoids duplicating assertions in features. |
| Configuration precedence | `base.url` from a JVM system property overrides `config.properties`. | Supports local, CI, and alternate-environment execution without changing source files. |
| Reporting and CI | Cucumber emits HTML/JSON/JUnit XML; the Allure adapter records scenario results; CI separates dry-run validation from selected live API tests. | Preserves readable and machine-readable reporting while limiting automated writes to the shared external service. |
