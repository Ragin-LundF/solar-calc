---
name: review-harness
description: >
  Ordered review procedure for code review, self-review before completion, and PR
  preparation. Defines the review order (contract first, style last), the findings
  format, and the approval criteria. Use whenever reviewing a diff — your own or
  someone else's.
---

# Review Harness

Review in the order below — it is sorted by blast radius. A blocker found early makes later nitpicks irrelevant; do not lead with style comments while a contract break sits in the diff.

## Review order

1. **Contract** — REST API, DTOs, events, database schema, configuration, public functions. Look for breaking changes, undocumented additions, spec/implementation drift.
2. **Architecture** — is each change in the layer that owns it? Do module dependencies still point in the allowed direction? Any business logic leaking into input layers or DTOs?
3. **Behavior** — is the domain/application logic actually correct? Trace at least the main path and one failure path through the diff; do not review line-by-line only.
4. **Security** — authentication and authorization on new/changed endpoints, tenant/data isolation, sensitive data in logs, injection risks, secrets in code or config.
5. **Tests** — do meaningful tests cover the changed behavior? Apply `no-cheating-test-harness.md` to every new or modified test.
6. **Static analysis** — Detekt/SonarQube/ktlint findings introduced or suppressed by the diff; suspicious new suppressions or baseline changes.
7. **Maintainability** — names, function size, duplication, dead code, comment quality. Last, and only after everything above is clean.

## Findings format

Report every finding in this shape:

- **Severity:** `blocker` (must fix before merge) | `major` (fix before merge unless explicitly waived) | `minor` (should fix) | `nit` (optional).
- **Location:** `path/to/file.kt:line` (or spec path/operation).
- **Issue:** what is wrong, in one or two sentences.
- **Why it matters:** the concrete consequence (broken client, security hole, silent data loss, …).
- **Fix:** a concrete, actionable suggestion — not "consider improving".

No finding without a location. No severity inflation: a naming preference is a `nit`, not a `major`.

## Approval criteria

Approve only when **all** hold:

- The behavior change matches what was requested — nothing missing, nothing extra smuggled in.
- Relevant tests exist, are meaningful, and pass the no-cheating gate.
- REST changes have Cucumber scenario coverage per `rest-api-test-harness.md`.
- Static analysis and formatting are clean or explicitly accounted for.
- No visible security or backward-compatibility regression.

If a criterion cannot be verified from the diff alone (e.g. tests were not run), say so explicitly — an unverifiable criterion is not a met criterion.

## Self-review note

When reviewing your own change, apply the same order and the same severity honesty. Findings you decide not to fix go in the completion report as risks/follow-ups, not in the bin.