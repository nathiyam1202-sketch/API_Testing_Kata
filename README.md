# Booking API Testing Framework

A Java 17 test framework for the Booking API, using Rest-Assured for HTTP interactions, Cucumber/Gherkin for behavior scenarios, and Maven for builds and execution. The automated Cucumber runner is scoped to the Booking feature.

## Requirements

- Java 17
- Maven 3.9 or later

## Configuration

Configuration is in `src/test/resources/config.properties`:

```properties
base.url=https://automationintesting.online/api
```

The same file defines `auth.username` and `auth.password`; authentication steps always read both values from it. Credentials are not embedded in step definitions. The base URL can be overridden with the `base.url` JVM property:

```shell
mvn verify -Dbase.url=https://your-api-host/api
```

The checked-in credentials are the public practice API sample credentials. Do not place private or production credentials in this file.

## Run Tests

Run the Booking scenarios:

```shell
mvn test
```

Run a clean build and test suite:

```shell
mvn clean verify
```

Run only the Booking tag:

```shell
mvn test '-Dcucumber.filter.tags=@booking'
```

Validate step bindings without making API requests:

```shell
mvn test -Dcucumber.execution.dry-run=true
```

Format Java sources or check the configured Google Java Format style:

```shell
mvn spotless:apply
mvn spotless:check
```

## Booking Coverage

The Booking feature covers creation, retrieval, update, partial update, deletion, invalid and missing fields, malformed email, date rules, and protected operations with missing or invalid token fixtures. Each scenario authenticates in its shared setup.

The `expired` token case currently sends an invalid fixture token; it does not test time-based token expiration. Some booking read/mutation and cleanup operations have returned HTTP 500 from the public service. Live behavior depends on service availability and state.

Other API features and step definitions may remain in the repository, but the Cucumber runner and automated API CI job execute Booking only.

## Reports

Cucumber writes reports to `target/`:

| File | Description |
|---|---|
| `cucumber-reports.html` | Cucumber HTML report |
| `cucumber.json` | Machine-readable Cucumber results |
| `cucumber.xml` | Cucumber JUnit XML report |
| `surefire-reports/` | Maven/Surefire test results |

## CI

GitHub Actions runs on pushes, pull requests, and manual dispatch:

- `maven.yml` compiles and dry-runs the Booking feature to validate step bindings without external API requests.
- `maven-api-tests.yml` runs the `@booking` scenarios against the configured API.
- Both workflows upload Cucumber and Surefire reports.

## Architecture

| Area | Design |
|---|---|
| BDD | `Booking.feature` describes behavior; `BookingSteps` binds scenarios to framework operations. |
| HTTP | `BookingClient` encapsulates Booking endpoints and uses the shared Rest-Assured request specification. |
| Models and data | POJOs represent requests/responses; `BookingRequestVariations` builds invalid/missing-field inputs; `BookingDataGenerator` creates unique valid requests. |
| State and lifecycle | `TestContext` holds per-thread scenario data and cleanup actions; Cucumber hooks initialize, clean, and clear the context. |
| Validation | Shared response steps validate status, error messages, fields, and named JSON schemas. |
| Configuration | `ConfigReader` reads `config.properties`; the `base.url` system property can override the configured URL. |
| Reporting | Cucumber generates HTML, JSON, and JUnit XML reports; Surefire exposes failures to Maven. |
