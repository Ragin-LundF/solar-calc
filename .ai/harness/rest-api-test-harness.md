---
name: rest-api-test-harness
description: >
  Scenario matrix and assertion requirements for REST endpoint tests. Use together
  with the rest-api-cucumber-testing skill whenever an endpoint is added or its
  behavior, contract, or security changes. Defines what must be covered; the skill
  and rest-api-cucumber.md define how to write it.
---

# REST API Test Harness

Apply this harness to every added or changed REST endpoint. Work through it in order: inventory → matrix → assertions.

## Step 1 — Endpoint inventory

Before writing scenarios, extract from the OpenAPI operation (preferred) or the controller:

- Method and path.
- Required security scheme and scopes.
- Required/optional request headers.
- Path and query parameters with their constraints.
- Request body schema (required fields, nullable fields, validation rules).
- Response body schema per status code.
- Success status code (`200` vs `201`).
- Documented error status codes.
- Paging (`page`/`size`) and sorting (`sorting`) support.

If the spec and the implementation disagree, stop and report the mismatch — do not write tests that cement the wrong side.

## Step 2 — Scenario matrix

Create or update a scenario for every applicable row. A row may only be skipped when its condition does not apply, and the skip must be stated in your response.

| # | Case | Applies to | Expected |
|---|------|-----------|----------|
| 1 | Valid authenticated request | Always | Success status + full body contract |
| 2 | Missing/invalid bearer token | Secured endpoints | `401` + error object |
| 3 | Token without required scope | Scoped endpoints | `403` + error object |
| 4 | Invalid request body (missing required field, wrong type, constraint violation) | Body endpoints | `400` + error object naming the field |
| 5 | Invalid path/query parameter | Parameterized endpoints | `400` + error object |
| 6 | Path resource does not exist | Dynamic path resources | `404` + error object |
| 7 | Domain cannot process the request (valid shape, invalid state) | Endpoints with domain rules / external state | `422` + error object |
| 8 | Paging: defaults and explicit `page`/`size` | List/search endpoints | Correct slice + paging metadata |
| 9 | Sorting via `sorting` parameter | Sortable endpoints | Correct order |
| 10 | Error object shape (`code`, `message(s)`, timestamp, trace/request id, `endpoint`) | Whenever the error contract is touched | Exact contract fields |

## Step 3 — Assertion requirements

For each scenario, assert everything the contract promises:

- Status code — always.
- Relevant response body fields — exact values where the contract defines them, matchers only for genuinely dynamic values (IDs, timestamps).
- Error `code` and a meaningful `message` for every error case — never the status code alone.
- Paging metadata (`number`, `size`, `totalElements`, `totalPages`) for paged responses.
- Actual element order for sorted responses.
- Response headers that are part of the contract.

## Prohibited shortcuts

- No direct controller method calls for REST behavior scenarios — go through HTTP.
- No bypassing security filters, except in tests explicitly scoped below the HTTP boundary and labeled as such.
- No status-code-only tests for endpoints that define a response body contract.
- No copying the implementation's output into the expected value without checking it against the spec first.