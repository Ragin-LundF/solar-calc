---
name: no-cheating-test-harness
description: >
  Gate that rejects dishonest or low-value tests. Apply to every test you write,
  modify, or review — after test-generation-harness (which defines what to build,
  this defines what disqualifies it). A test that fails this gate must be rewritten,
  not kept for coverage.
---

# No-Cheating Test Harness

Apply this gate to every test before calling it done. Coverage numbers earned by tests that fail this gate are worthless — treat such tests as bugs.

## Automatic rejections (red flags)

Reject or rewrite any test that:

- Mocks or stubs the class/function under test itself.
- Asserts only `not null` / `no exception` for outputs whose value is known.
- Asserts implementation details (internal calls, private state, invocation counts on incidental collaborators) instead of observable behavior.
- Computes the expected value by copying the production logic — the test then proves the code equals itself.
- Uses broad matchers (`any()`, wildcard captures) for arguments that carry the behavior under test.
- Depends on test execution order without a declared contract.
- Hides failing behavior via `@Disabled`, `assumeTrue`, early returns, environment checks, try/catch that swallows the failure, or commented-out assertions.
- Tests REST behavior by calling controller methods directly instead of going through the HTTP boundary.
- Removes or weakens meaningful assertions from an existing test.
- Changes an expected value without stating and justifying the corresponding behavior change in the response.

## Review questions (answer for each test)

1. **Regression value:** What concrete production bug would this test catch?
2. **Wrong-value sensitivity:** Would it fail if the implementation returned a *plausible but wrong* value (off-by-one, empty list, swapped fields) — not just if it threw?
3. **Contract vs. shape:** Does it verify the promised behavior, or only the current implementation shape?
4. **Coverage of the unhappy side:** Are negative and edge cases present, or only the happy path?
5. **Determinism:** Same result on every run, any machine, any order?

If any answer is unsatisfactory, the test fails the gate.

## Mandatory action on failure

A test that fails this gate is not kept "for coverage":

- **Default:** rewrite it so it passes the gate.
- **Documented exception only:** keep it with an explicit stated reason for being intentionally shallow (e.g. smoke test over generated code). The reason goes in the response and, where useful, in a test comment.
- Never delete a failing *production-revealing* test to get green — a test that correctly exposes a bug is the most valuable test in the suite; fix the code or report the bug.