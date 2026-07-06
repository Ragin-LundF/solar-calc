---
name: rest-api-cucumber
description: >
  REST API Cucumber/Gherkin testing rules: policy, required scenario coverage,
  feature-file style, anti-cheating rules, and an example scenario skeleton.
  Load when adding or changing REST API tests. Pairs with the
  rest-api-cucumber-testing skill and rest-api-test-harness.
---

# REST API Cucumber / Gherkin Testing

Load when adding or changing REST API tests. The scenario matrix and assertion detail live in `../harness/rest-api-test-harness.md`; this file defines the policy and style.

## Policy

- REST APIs are tested through Cucumber/Gherkin using `bbd-cucumber-gherkin-lib` when it is available in the project.
- Scenarios describe business behavior at the HTTP/API boundary — a reader should understand *what the API promises* without reading step definitions.
- Step definitions reuse the library's request/response building and assertion primitives. Never build a second custom REST-testing mini-framework next to the library.
- Feature files use two-space indentation as defined in `.editorconfig`.

## Scenario coverage

For each endpoint or changed behavior, cover (full matrix in `rest-api-test-harness.md`):

- Successful request: expected status code *and* response body.
- Missing/invalid authentication → `401` on secured endpoints.
- Denied authorization scope → `403` where applicable.
- Request validation errors → `400`.
- Domain-unprocessable cases → `422`.
- Missing path resources → `404`.
- Paging via `page` and `size` on list/search endpoints.
- Sorting via `sorting` where supported.
- Search criteria via `POST .../search` body for sensitive data.
- Error object shape (`code`, `messages`, `datetime`, `traceId`, `endpoint`) whenever error responses change.

## Feature style

- Clear `Feature`, `Background`, `Scenario`, and `Scenario Outline` blocks.
- Exactly one behavior per scenario.
- Tables or docstrings for request bodies, headers, and expected fields when they improve readability.
- Semantic step names over implementation-oriented ones — `an authenticated client has scope "solar:write"`, not `set header Authorization`.
- No brittle assertions on generated values: match timestamps, UUIDs, and IDs with matchers (e.g. `${json-unit.matches:isValidUUID}`) unless the value is controlled by the test.

## Anti-cheating rules

Never do the following in REST behavior tests:

- Call controller methods directly — always go through HTTP.
- Bypass authentication filters for secured-endpoint scenarios, unless the scenario explicitly tests behavior *after* authentication and says so.
- Mock the endpoint under test or the layer it delegates to just to force a response.
- Assert only the status code when the body contract matters.
- Use hard-coded happy-path fixtures that make validation/security branches unreachable.

## Example scenario skeleton

```gherkin
Feature: Solar Calculator API

  Background:
    Given the API base path is "/api/v1"
    And an authenticated client has scope "solar:write"

    Scenario: [SolarCalculation][HP] Create a new entry
      Given that the body of the request is
      """
      {
        "firstname": "John",
        "lastname": "Doe",
        "email": "user@example.com",
        "phone": "+49 1234 12332111"
      }
      """
      When executing an authorized POST call to "/solar/data"
      Then I ensure that the status code of the response is 201
      And I ensure that the body of the response is equal to
      """
      {
        "id": "${json-unit.matches:isValidUUID}",
        "firstname": "John",
        "lastname": "Doe",
        "email": "user@example.com",
        "phone": "+49 1234 12332111"
      }
      """
```

Conventions shown above: scenario titles carry a `[Domain][Case]` tag prefix (`HP` = happy path), dynamic values use json-unit matchers, and bodies are asserted in full.
