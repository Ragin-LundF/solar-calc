---
name: security
description: >
  API and data security rules: endpoint security requirements, OpenAPI security
  definitions, bearer-token validation, scope strategy, and sensitive-data
  handling. Load for authentication, authorization, scopes, sensitive data, or
  API exposure decisions. Security rules are never weakened for convenience.
---

# API and Data Security

Load for any decision touching authentication, authorization, scopes, sensitive data, or API exposure. These rules are non-negotiable: never trade them away to simplify an implementation or make a test pass.

## Endpoint security

- Every API endpoint has security in place unless there is a deliberate, **documented** exception.
- Internal-only APIs still require security — infrastructure compromise and internal misuse are realistic attack vectors, "internal" is not a trust boundary.
- Accepted exceptions: login endpoints and health endpoints.
- Health endpoints expose only OK/error-style health data — never business data.

## OpenAPI security definition

- Define OAuth2/bearer security in the OpenAPI file itself.
- Either secure each operation with an operation-level `security` block, or define global security and override only where needed.
- Every endpoint has an explicit scope strategy — "inherited by accident" does not count.

## Bearer tokens

- APIs validate bearer tokens from the `Authorization` header.
- JWT tokens are validated against the issuer or with issuer certificates, depending on the environment.
- Opaque tokens are always validated against the issuer.

## Scopes

- No generic role scopes such as `admin` or `user` for API permissions.
- Prefer domain-based scopes under `io.github.raginlundf.<module-or-domain>`.
- Use concrete permissions where needed; the only exception is login.
- Define scopes even when the current environment does not yet enforce JWT scope checks — the API must be ready for OAuth-based infrastructure without a breaking change later.

## Data security

- Never expose guessable internal database IDs in paths or externally visible identifiers — use UUIDs or other non-enumerable identifiers externally.
- Never put personal or protected data (account numbers, IBANs, names, …) into paths.
- Avoid sensitive query parameters entirely: proxies and firewalls log URLs. Use `POST` with a body for sensitive search criteria.
- Review logs and error responses on every change: no secrets, tokens, credentials, PII, or internal infrastructure details may leak. Use log obfuscation/skip mechanisms for sensitive parameters where the project provides them.

## When writing or reviewing code

- New endpoint → verify security scheme, scope, and that tests cover both the allowed and the denied path.
- New log statement or error message → verify it cannot leak sensitive data.
- If a task appears to require weakening any rule here, stop and raise it explicitly instead of implementing.
