# Booking API Testing Framework

A Java 17 test framework for the Booking API, using Rest-Assured for HTTP interactions, Cucumber/Gherkin for behavior scenarios, and Maven for builds and execution. The automated Cucumber runner discovers all feature files in `src/test/resources/features`.

## Requirements

- Java 17
- Maven 3.9 or later

## Configuration

Copy `src/test/resources/config.properties.example` to `src/test/resources/config.properties` and set the Booking API credentials:

```properties
base.url=https://automationintesting.online/api
auth.username=replace-with-configured-username
auth.password=replace-with-configured-password
```

The local `config.properties` file is ignored by Git. Authentication reads credentials through `ConfigReader`; they are not embedded in step definitions. Values can also be supplied as environment variables (`BASE_URL`, `AUTH_USERNAME`, `AUTH_PASSWORD`) or JVM properties, which take precedence over the local file. Missing or blank required values produce an actionable configuration error.

```shell
mvn verify -Dbase.url=https://your-api-host/api
```

For GitHub Actions, configure `BOOKING_API_USERNAME` and `BOOKING_API_PASSWORD` as repository or environment secrets. CI passes them as environment variables and does not print them or place them on the Maven command line. Never commit real or production credentials.

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

Booking scenarios are split by endpoint operation: `BookingCollection.feature` covers `POST /booking`; `BookingGet.feature`, `BookingUpdate.feature`, `BookingPartialUpdate.feature`, and `BookingDelete.feature` cover the respective `GET`, `PUT`, `PATCH`, and `DELETE /booking/{id}` operations. Creation tests cover every required field from the Booking schema, malformed emails, validation boundaries, and invalid date ordering. Protected endpoint tests cover missing and invalid tokens as described by the API specification.

The API specification does not define a separate expired-token behavior, so the suite does not label an invalid fixture as an expired token. Some booking read/mutation and cleanup operations have returned HTTP 500 from the public service. Live behavior depends on service availability and state.

Scenarios are classified with `@happy`/`@positive` for successful flows and `@unhappy`/`@negative` for rejected flows. `@invalid` selects malformed or invalid request/token cases. For example, run only invalid scenarios with `mvn test '-Dcucumber.filter.tags=@invalid'`.

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
- `maven-api-tests.yml` runs the `@booking` scenarios against the configured API when both `BOOKING_API_USERNAME` and `BOOKING_API_PASSWORD` secrets are set. If either secret is missing, it emits a notice and runs a dry-run to validate feature bindings instead; no live API behavior is tested in that case.
- Both workflows upload Cucumber and Surefire reports.

## Architecture

| Area | Design |
|---|---|
| BDD | Endpoint-focused Booking feature files cover collection creation and each `/booking/{id}` operation. `BookingSteps` binds scenarios to framework operations. |
| HTTP | `BookingClient` encapsulates Booking endpoints and uses the shared Rest-Assured request specification. |
| Models and data | POJOs represent requests/responses; `BookingRequestVariations` builds invalid/missing-field inputs; `BookingDataGenerator` creates unique valid requests. |
| State and lifecycle | `TestContext` holds per-thread scenario data and cleanup actions; Cucumber hooks initialize, clean, and clear the context. |
| Validation | Shared response steps validate status, error messages, fields, and named JSON schemas. |
| Configuration | `ConfigReader` resolves values from JVM properties, environment variables, then the ignored local `config.properties` file. |
| Reporting | Cucumber generates HTML, JSON, and JUnit XML reports; Surefire exposes failures to Maven. |
