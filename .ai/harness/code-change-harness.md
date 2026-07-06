---
name: code-change-harness
description: >
  Procedure to run before and during any non-trivial production code change:
  intake, impact analysis, planning checklist, implementation rules, and the
  completion report format. Use for feature work, bug fixes, and refactorings.
  Trivial changes (typos, single-line doc fixes) may skip it.
---

# Code Change Harness

Run this procedure for every non-trivial code change. It defines *how* to approach a change; the language and layer rules come from the instruction files.

## Phase 1 — Intake (before writing any code)

1. Restate the requested behavior change in one sentence. If you cannot, the task is underspecified — ask before coding.
2. Identify the touched modules and load the matching instruction files via `.ai/README.md`. Do not load everything; load what the task needs.
3. List every public contract the change might affect: REST API (`openapi-*.yaml`), DTOs, events, database schema, configuration keys, CLI, published libraries. A contract change requires explicit confirmation from the task — never change one as a side effect.
4. Decide which tests must exist before you consider the change done (unit and/or REST scenarios).

## Phase 2 — Planning checklist

Answer each question; write down anything non-obvious in your response:

- What is the behavior today? (Read the code — do not assume.)
- What exactly should change?
- Which module/layer owns the change?
- Does the OpenAPI spec need to change first? (API-first: spec before implementation.)
- Do generated sources need regeneration after the change?
- Which unit tests must be added or updated?
- Which REST Cucumber scenarios must be added or updated?
- Which static-analysis findings will the change likely trigger?

## Phase 3 — Implementation rules

- Make the smallest coherent change that fulfills the task. No drive-by refactoring; if you spot unrelated debt, report it, don't fix it in the same change.
- Business logic goes in domain services only. REST/event layers map and delegate. Application/orchestration layers coordinate — they are not business-rule dumping grounds.
- Add or update tests in the same change, next to the changed behavior.
- Never edit generated sources; change the generator input and regenerate.
- Run formatting, static analysis, and tests for the touched modules as you go, not only at the end.

## Phase 4 — Completion report (mandatory format)

Every completion response includes:

1. **Changed behavior** — what is different now, in behavioral terms.
2. **Tests** — which were added/updated, and what regression each protects against.
3. **Verification run** — exact commands executed and their real results. Never report success for a command you did not run.
4. **Verification skipped** — exact commands not run, and why.
5. **Risks / follow-ups** — anything unresolved, assumed, or deferred. "None" is a valid entry, silence is not.