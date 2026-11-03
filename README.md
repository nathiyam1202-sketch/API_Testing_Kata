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

Booking scenarios are split by endpoint operation: `BookingCollection.feature` covers `POST /booking`; `BookingGet.feature`, `BookingUpdate.feature`, `BookingPartialUpdate.feature`, and `BookingDelete.feature` cover the respective `GET`, `PUT`, `PATCH`, and `DELETE /booking/{id}` operations. `BookingLifecycle.feature` exercises cross-endpoint creation, retrieval, update, and deletion. The current suite contains 47 Cucumber scenarios/examples. It covers the original implemented BOOK_001–BOOK_004, BOOK_006–BOOK_010, BOOK_013, BOOK_015, BOOK_016, and BOOK_018–BOOK_020, plus the contract-backed additions BOOK_NEW_001–BOOK_NEW_012, BOOK_GET_005–BOOK_GET_008, BOOK_UPD_001–BOOK_UPD_002 and BOOK_UPD_006–BOOK_UPD_007, BOOK_PATCH_001–BOOK_PATCH_002, BOOK_PATCH_004, BOOK_PATCH_007–BOOK_PATCH_008, BOOK_DEL_001–BOOK_DEL_002 and BOOK_DEL_005, and BOOK_INT_001–BOOK_INT_004 and BOOK_INT_006–BOOK_INT_008. Existing creation/retrieval, update, and auth tests are reused where they already cover the requested behavior; repeated checks are not added as duplicate scenarios. The retrieved-booking contract does not include email or phone, so retrieval comparisons are limited to the response fields defined by the spec.

The checked-in OpenAPI contract does not define the expected response for unknown or malformed booking IDs, repeat deletion, invalid PUT/PATCH payloads, or retrieval after deletion. Those requested cases remain pending: BOOK_005, BOOK_011–BOOK_012, BOOK_014, BOOK_017, BOOK_021–BOOK_022, BOOK_GET_001–BOOK_GET_004, BOOK_UPD_003–BOOK_UPD_005 and BOOK_UPD_008, BOOK_PATCH_003 and BOOK_PATCH_005–BOOK_PATCH_006, BOOK_DEL_003–BOOK_DEL_004 and BOOK_DEL_006, and BOOK_INT_005 (explicitly shuffled execution order). The contract contains no booking-list endpoint for BOOK_023–BOOK_024 or summary endpoint for BOOK_025–BOOK_026. These behaviors must not be assigned guessed status codes or paths. The assignment's Swagger UI URLs returned 404 during implementation. The API specification also does not define a separate expired-token behavior, so the suite does not label an invalid fixture as an expired token. Some booking read/mutation and cleanup operations have returned HTTP 500 from the public service. Live behavior depends on service availability and state.

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
| Models and data | Request POJOs model Booking payloads; Rest-Assured `Response` objects represent API responses; `BookingRequestVariations` builds invalid/missing-field inputs; `BookingDataGenerator` creates unique valid requests. |
| State and lifecycle | `TestContext` holds per-thread scenario data and cleanup actions; Cucumber hooks initialize, clean, and clear the context. |
| Validation | Shared response steps validate status, error messages, fields, and named JSON schemas. |
| Configuration | `ConfigReader` resolves values from JVM properties, environment variables, then the ignored local `config.properties` file. |
| Reporting | Cucumber generates HTML, JSON, and JUnit XML reports; Surefire exposes failures to Maven. |
