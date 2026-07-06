---
name: test-generation-harness
description: >
  Procedure for generating or updating unit tests: behavior inventory, required
  test cases (positive/negative/edge), assertion requirements, and test-data rules.
  Always pair with no-cheating-test-harness, which gates the result. For REST
  endpoint behavior use rest-api-test-harness instead.
---

# Test Generation Harness

Use for unit and component tests. REST endpoint behavior is covered by `rest-api-test-harness.md`; both are gated by `no-cheating-test-harness.md`.

## Step 1 — Behavior inventory

Before writing any test:

1. Identify the unit under test and its *public* behavior — the contract callers rely on, not the private mechanics.
2. List its inputs, outputs, side effects (persistence, cache, events, logging that is part of the contract), and error modes (exceptions, error returns, rejected states).
3. Check what tests already exist for the unit. Extend or fix them before adding parallel new ones.

## Step 2 — Required test cases

For every behavior in the inventory, generate:

- **Positive path** — at least one test with realistic input asserting the exact expected outcome.
- **Negative paths** — invalid inputs, rejected states, and each declared error mode. Assert the specific error (type/code/message contract), not just "it throws".
- **Edge/boundary cases** — parameter limits, empty collections, min/max values, and nullability wherever `null` is a valid input or output.

Omit a category only when it genuinely does not exist for the unit, and say so in your response.

## Step 3 — Assertion requirements

- Use `kotlin.test` assertions with named arguments (`expected = …, actual = …`).
- Assert exact values when the contract defines them — `assertEquals`, not `assertNotNull`.
- For collections: assert size *and* the relevant contents, not size alone.
- For mapped DTOs/domain models: assert every field the mapping is responsible for, not a sample.
- For security-relevant logic: assert both the allowed and the denied path.
- One behavior per test; a failure message must point at exactly one broken thing.
- Remove duplicate and tautological assertions — every assertion must be able to fail.

## Step 4 — Test data rules

- Minimal fixtures: only the fields the behavior under test needs; defaults for everything else.
- Builders/helpers only when repeated setup would otherwise drown the signal — not preemptively.
- No random data unless the seed is fixed or randomness is itself under test.
- No shared giant fixture for unrelated behaviors — coupling fixtures couples failures.
- No production logic in expected-value construction; expected values are literals or independently derived.

## Step 5 — Regression check

For each generated test, verify: *if a realistic bug were introduced (off-by-one, swapped field, missing null check, inverted condition), would this test fail?* If not, strengthen or delete it.

## Quality gate

A test suite that only raises line coverage without checking behavior is unacceptable — coverage is a byproduct of asserting the contract, never the goal. Every suite must additionally pass `no-cheating-test-harness.md`.