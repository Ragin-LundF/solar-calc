---
name: openapi-authoring
description: >
  Create or update OpenAPI 3 specifications (openapi-*.yaml) in an API-first project
  where server interfaces and DTOs are generated from the spec. Use when adding
  endpoints, changing request/response schemas, error objects, paging, sorting, or
  security definitions. Do not use for implementing the generated interfaces
  (use kotlin-code-generation) or testing endpoints (use rest-api-cucumber-testing).
---

# Skill: OpenAPI Authoring

## When to use

- Any change to an `openapi-*.yaml` file.
- Designing a new endpoint, resource, schema, or error shape.
- Reviewing an API change for backward compatibility.

## When NOT to use

- Implementing controllers/services for an existing spec → `kotlin-code-generation.skill.md`.
- Writing tests for endpoints → `rest-api-cucumber-testing.skill.md`.

## Required context (load before editing)

1. `../instructions/api-guidelines.md` — paths, methods, status codes, paging, sorting, error object, formatting. This file is authoritative; on conflict with your defaults, it wins.
2. `../instructions/security.md` — auth, scopes, sensitive data rules.

## Workflow

1. **Compatibility gate (do this FIRST for existing APIs).** Classify every change as compatible or breaking using the "Versioning and compatibility" rules in `api-guidelines.md`. If any change is breaking:
   - Stop. Do not silently apply it.
   - Propose either a new major version path (`/api/v<major+1>`) or a compatibility layer, and wait for confirmation.
2. **Design the resource.**
   - Resource-oriented path (plural nouns, no verbs, camelCase segments), correct HTTP method.
   - Searches with sensitive criteria: `POST <resource>/search` with body; paging/sorting stay query parameters.
3. **Define security.** Every operation declares its security scheme and required scopes. No unsecured endpoints unless the guidelines explicitly allow one.
4. **Define schemas.**
   - Required fields in the `required` array; optional nullable fields marked `nullable: true`.
   - Validation constraints (`minLength`, `maxLength`, `pattern`, `minimum`, …) in the spec so generated validation is reused — but never `pattern` on enums.
   - RFC 3339 for `date` / `date-time` formats.
   - Reuse shared schemas (error object, paging) via `$ref`; do not duplicate them inline.
5. **Define responses.**
   - Success code per the status-code table in `api-guidelines.md` (`200` vs `201`).
   - Standard error responses (`400`, `401`, `403`, `404`, `422` as applicable) referencing the shared error schema.
   - List endpoints: paging parameters `page`/`size` and paging metadata in the response; `sorting` parameter where supported.
6. **Format descriptions** per the "OpenAPI formatting" section: block style (`description: |`), one sentence per line, `<code>` for technical tokens, `> Note:` for notes.
7. **Verify.** Validate the YAML (spec linter or the project's generator task). If generation is part of the build, run it and confirm it succeeds. If you cannot run it, list the exact command for the user.

## Hard rules

- The spec is the contract. Never adjust the spec to match an implementation shortcut.
- Never remove or rename fields, parameters, paths, or response codes in a released version path.
- Never make an optional request field required in a released version path.
- Every new operation defines: security, at least one success response, and the standard error responses.

## Done checklist (self-verify before reporting)

- [ ] All changes classified compatible, or a breaking-change proposal was raised instead.
- [ ] Paths/methods/status codes follow `api-guidelines.md` exactly.
- [ ] `required` arrays and `nullable` flags are complete and correct.
- [ ] Errors, paging, and sorting reference shared schemas — nothing duplicated.
- [ ] Spec validates / generator runs, or the skipped command is listed in the response.