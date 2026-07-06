---
name: api-guidelines
description: >
  REST API and OpenAPI contract rules: file naming, versioning and backward
  compatibility, paths, HTTP methods, status codes, searches, headers, schemas
  and validation, the standard error object, paging, sorting, and YAML formatting.
  Load when designing or changing REST APIs or openapi-*.yaml files. Authoritative
  for all API contract decisions.
---

# REST API and OpenAPI Guidelines

Load when designing or changing REST APIs. These rules define the contract; implementations follow the spec, never the other way around.

## OpenAPI contract

- APIs are defined as Interfaces with Controller (`@RestController` annotation). API Models are defined as `<name>Dto` classes in the `solarcalc-rest-api´ module.
- The spec is a contract between backend and clients. Treat every change as a contract change.
- Endpoints represent business resources and concrete responsibilities — never RPC-style actions hidden in parameters.

## Versioning and compatibility

- Set the concrete API version in `info.version`, following semantic versioning `<major>.<minor>.<bugfix>`.
- Paths include only the major version: `/api/v1`.
- A versioned path must remain backward compatible for its lifetime.
- Implement multiple API versions in one file only when necessary.

**Breaking (never allowed within a version path):**

- Changing URIs.
- Removing operations, parameters, or response fields.
- Making optional request fields mandatory.
- Adding values to response enums that clients must handle exhaustively.
- Adding new response codes the client must handle.
- Changing list/page result sets in a breaking way.

**Compatible (allowed):**

- Adding new operations.
- Adding new fields to resources.
- Adding optional parameters.
- Extending or fixing documentation.

When a task requires a breaking change: stop and propose a new major path or a compatibility layer — never apply it silently.

## Paths

- All API paths start with `/api/v<major>`, e.g. `/api/v1`.
- Paths are resources, not methods. No action prefixes like `/create/myData` — the HTTP method expresses the action.
- camelCase for multi-word path elements: `/api/v1/dataSource`.
- Plural nouns for collection resources with CRUD; singular only for true single-instance resources such as `/version`.
- Never place personal, protected, or sensitive data in path or query parameters (see `security.md`).

## HTTP methods

| Method | Use | Rules |
|--------|-----|-------|
| `GET` | Read data | Must not change server state; must not have a body |
| `POST` | Create data, or submit sensitive criteria (searches) | — |
| `PUT` | Replace an existing complete resource | — |
| `DELETE` | Delete an existing resource | — |
| `PATCH` | Partial update | Avoid unless project and framework handle it clearly |

## HTTP status codes

| Code | Meaning |
|------|---------|
| `200 OK` | Successful GET, PUT, or POST that creates no new resource |
| `201 Created` | Successful creation (usually POST) |
| `400 Bad Request` | Invalid request shape, missing required fields, validation mismatch |
| `401 Unauthorized` | Authentication missing or invalid (e.g. missing bearer token) |
| `403 Forbidden` | Authenticated but not allowed to access endpoint or data |
| `404 Not Found` | Endpoint or path resource not found |
| `422 Unprocessable Entity` | Resource exists but cannot be processed as requested |
| `423 Locked` | Resource is locked |
| `451 Unavailable For Legal Reasons` | Access blocked for legal reasons |
| `500` | Unexpected server-side failure |

## Searches

- Searches are `POST` with a request body and a `200` response.
- Path is the resource plus `/search`: `/api/v1/reports/search`.
- Paging and sorting remain **query parameters** even for POST searches — never body fields.

## Headers

- `Content-Type: application/json` for JSON bodies.
- `Authorization: Bearer <token>` for bearer-token authentication.

## Objects and validation

- Required fields go in the schema `required` array; optional nullable fields carry `nullable: true`.
- UTF-8 for request/response bodies unless explicitly specified otherwise.
- RFC 3339 for temporal values: dates as `yyyy-mm-dd` (`type: string`, `format: date`); date-times as `yyyy-mm-ddThh:mm:ss.SSSZ` (`type: string`, `format: date-time`).
- Put validation constraints (`minLength`, `pattern`, `minimum`, …) into the spec so generated validation is reused.
- No validation `pattern` on enums — enum values are already the constraint.

## Error object

All error responses use one consistent shape:

```json
{
    "code": "UNEXPECTED_ERROR",
    "messages": [
        {
            "message": "An unexpected error occurred",
            "field": "accountId"
        }
    ],
    "datetime": "2022-01-01T00:00:00.000Z",
    "traceId": "5e8h12a9bc",
    "endpoint": "https://solar-calc.tld/api/v1/called/endpoint"
}
```

- `code`: uppercase technical code, e.g. `INVALID_PARAMETER`, `ENTITY_NOT_FOUND`, `UNEXPECTED_ERROR`.
- `messages[]`: user-meaningful explanations; one code may carry multiple messages; `field` names the offending field where applicable.
- `datetime`: time of the error, for log correlation.
- `traceId`: trace/log correlation identifier.
- `endpoint`: the called endpoint URL, for support diagnostics.

Define the error schema once (shared file/schema) and `$ref` it — never redefine it per endpoint.

## Paging

- List endpoints with potentially large result sets must support paging.
- Query parameters: `page` (zero-based, default `0`) and `size` (default documented per endpoint).
- Paged responses include metadata: `number`, `size`, `totalElements`, `totalPages`.
- Define a reusable `Paging` schema and combine with response schemas via `allOf`.

## Sorting

- The sorting query parameter is always named `sorting`.
- Format: comma-separated field names with `:asc`/`:desc` suffixes, e.g. `sorting=username:asc,creationDate:desc`.
- Sorting stays a query parameter for POST methods too.
