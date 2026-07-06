# AI Instruction Index

This folder contains the canonical AI instructions for Kotlin/JVM projects following Project-style API-first, layered module architecture.
The goal is progressive loading: read only what is needed for the current task instead of loading every instruction file.

If possible and installed, use always the `codebase-memory` skill.

## Always read first

- `instructions/coding-guidelines.md` when creating, editing, reviewing, or testing Kotlin code.
- `instructions/testing.md` when tests are created, modified, reviewed, or when behavior changes.

## Load by task scope

### Repository architecture, modules, package layout, cross-module behavior
Read:

- `instructions/module-architecture.md`

Use this for changes that affect module boundaries, public APIs, DTO/domain boundaries, REST/event input layers, adapters, package layout, or Gradle/Maven module structure.

### REST API design or OpenAPI changes
Read:

- `instructions/api-guidelines.md`
- `instructions/security.md`
- `skills/openapi-authoring.skill.md`

Use this for `openapi-*.yaml`, endpoint design, status codes, paging, sorting, validation, error objects, scopes, and compatibility.

### REST API tests
Read:

- `instructions/testing.md`
- `instructions/rest-api-cucumber.md`
- `skills/rest-api-cucumber-testing.skill.md`
- `harness/rest-api-test-harness.md`

REST APIs must be tested through the BBD Cucumber Gherkin library approach when available. Do not replace REST API behavior tests with controller-only unit tests.

### Unit tests and test coverage
Read:

- `instructions/testing.md`
- `harness/test-generation-harness.md`
- `harness/no-cheating-test-harness.md`

Use `kotlin.test` for function and parameter tests. High coverage is mandatory, but meaningful assertions matter more than line execution.

### Static analysis, Detekt, SonarQube, ktlint, formatting
Read:

- `instructions/static-analysis.md`
- `instructions/editorconfig-style.md`
- `harness/static-analysis-remediation-harness.md`

Use `config/detekt/detekt.yml` and `.editorconfig` from this package as the baseline unless the host repository already has stricter settings.

### Code review or refactoring
Read:

- `harness/code-change-harness.md`
- `harness/review-harness.md`

Preserve behavior unless the task explicitly asks for behavior changes. Refactor only with tests or with a clear, minimal safety net.


## Conflict resolution

1. Prefer explicit user instructions in the current task.
2. Prefer repository-local instructions over this reusable package.
3. Prefer the most specific `.ai/instructions/*.md` file over a general one.
4. Prefer module-specific rules over repository-wide rules.
5. Preserve behavior before refactoring style.
6. Never weaken tests, security, static analysis, or compatibility to make implementation easier.
7. Explain unresolved conflicts before changing architecture, behavior, or public contracts.

## Token-saving rule

Do not load every file in `.ai/` by default. Start with this index, then load only the files named above that match the current task.

# General Instructions

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

