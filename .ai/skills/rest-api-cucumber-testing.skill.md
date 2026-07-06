---
name: rest-api-cucumber-testing
description: >
  Write or change REST API behavior tests using Cucumber/Gherkin at the HTTP boundary.
  Use when testing endpoints, status codes, error objects, paging, sorting, or security
  behavior. REST behavior must be tested through HTTP scenarios — controller-only unit
  tests are not an acceptable substitute. Do not use for plain unit tests
  (use the test-generation harness) or for authoring the spec (use openapi-authoring).
---

# Skill: REST API Cucumber Testing

## When to use

- Creating or modifying tests for REST endpoints.
- An endpoint's behavior, contract, or security changed and coverage must follow.

## When NOT to use

- Unit tests for domain logic → `../harness/test-generation-harness.md`.
- Changing the API spec itself → `openapi-authoring.skill.md`.

## Required context (load before writing tests)

1. `../instructions/rest-api-cucumber.md` — policy, scenario coverage list, feature style, anti-cheating rules. Authoritative.
2. `../instructions/api-guidelines.md` — expected status codes, error object, paging/sorting contract.
3. `../harness/rest-api-test-harness.md` — the scenario matrix to build from.
4. `../harness/no-cheating-test-harness.md` — what disqualifies a test.

## Workflow

1. **Read the contract first.** Open the OpenAPI operation (or, if none, the controller endpoint) for the behavior under test. The spec defines what to assert — status codes, body shape, error object, paging metadata.
2. **Build the scenario matrix** from the harness. At minimum, for each endpoint or changed behavior:
   - Happy path: expected status and full response body.
   - Missing/invalid authentication → `401` (when secured).
   - Insufficient scope/authorization → `403` (where applicable).
   - Validation errors → `400` with the standard error object.
   - Domain-unprocessable cases → `422`.
   - Missing path resource → `404`.
   - `page`/`size` behavior for list/search endpoints; `sorting` where supported.
   Drop a row only if it genuinely does not apply, and say why in the response.
3. **Write feature scenarios** using the project's Gherkin step library (e.g. `bbd-cucumber-gherkin-lib`) when available:
   - Reuse existing library steps first. Only add a step definition when no existing step covers the need — and keep it a thin wrapper over library primitives.
   - One behavior per scenario; semantic step names; tables/docstrings for bodies where readable.
   - Follow the example skeleton and tagging style in `rest-api-cucumber.md`.
   - Match dynamic values (IDs, timestamps) with matchers (e.g. `${json-unit.matches:isValidUUID}`) instead of hard-coding.
4. **Assert the full contract**, not just the status code: body shape, error object fields, paging metadata, and security behavior.
5. **Prove the tests can fail.** For each new scenario, confirm it would fail on a meaningful regression (wrong status, missing field, security bypass). If you ran the suite, report the actual results; never claim green without running.

## Hard rules (anti-cheating)

- Never call controller methods directly in a REST behavior test — go through HTTP.
- Never mock the endpoint under test or the layer it delegates to just to force a response.
- Never bypass authentication filters for secured-endpoint scenarios, except when the scenario explicitly tests post-authentication behavior.
- Never assert only the status code when the body contract matters.
- Never weaken or delete a failing scenario to get green; fix the code or flag the mismatch.

## Done checklist (self-verify before reporting)

- [ ] Scenario matrix covered (or omissions justified in the response).
- [ ] Existing library steps reused; new step definitions are minimal and necessary.
- [ ] Assertions cover body, errors, paging, and security — not just status codes.
- [ ] Tests were run and results reported, or the exact command to run them is listed.
- [ ] No anti-cheating rule was violated.