---
name: kotlin-code-generation
description: >
  Write or change Kotlin production code in a layered, API-first module architecture.
  Use when creating, editing, or extending Kotlin classes, functions, services, entities,
  or mappers. Do not use for test-only changes (use rest-api-cucumber-testing or the
  test-generation harness) or for pure formatting fixes (use static-analysis-cleanup).
---

# Skill: Kotlin Code Generation

## When to use

- Creating or modifying Kotlin production code.
- Adding a feature, fixing a bug, or refactoring behavior-carrying code.

## When NOT to use

- Only tests change → `rest-api-cucumber-testing.skill.md` or `../harness/test-generation-harness.md`.
- Only lint/format findings change → `static-analysis-cleanup.skill.md`.
- Only `openapi-*.yaml` changes → `openapi-authoring.skill.md`.

## Required context (load before writing code)

1. `../instructions/coding-guidelines.md` — always.
2. `../instructions/testing.md` — always.
3. `../instructions/module-architecture.md` — when the change touches more than one module, adds a class, or you are unsure which module owns the code.

## Workflow

Follow these steps in order. Do not skip the verification steps.

1. **Locate the owning layer.** Identify the module and package that owns the change using the module-architecture rules. If the owning layer is ambiguous, state the candidates and your choice with a one-line reason before coding.
2. **Check for existing code.** Search the repository for an existing function, extension, mapper, or utility that already does the job. Reuse it; do not re-implement.
3. **Write or update tests first** when the expected behavior is clear from the task. Use the project's test conventions (`kotlin.test` for unit tests). If behavior is unclear, ask or state your assumption explicitly.
4. **Implement minimally.**
   - Small, focused functions with block bodies.
   - Immutable by default: `val`, read-only collections, data classes where the framework allows.
   - Named arguments on calls where they clarify intent (multiple parameters of the same type, booleans, nulls).
   - Handle nullability explicitly; no `!!` in production code.
5. **Respect layer boundaries.**
   - Business logic only in domain services.
   - REST/event input layers delegate; they never contain domain rules.
   - Never edit generated sources (`build/generated/**` or equivalent); change the generator input instead.
6. **Verify.** Compile, run the affected tests, and run static analysis (Detekt/ktlint) for the touched modules. If you cannot run them, say so explicitly and list the exact commands the user should run.
7. **New Dependencies.** Check always if a dependency exists in `gradle/libs.versions.toml`. If not, ask before adding new dependencies. New dependencies must be added to `gradle/libs.versions.toml` and referenced from there. Write dependencies always in brackets in `build.gradle.kts` files (e.g. `implementation (libs.my.library)`)

## Hard rules

- Do not weaken or delete existing tests to make code compile or pass.
- Do not change public APIs or DTO contracts unless the task explicitly asks for it; if a contract change is unavoidable, stop and explain before implementing.
- Do not introduce new dependencies when the standard library or an existing dependency covers the need.
- Do not suppress static-analysis findings as a shortcut; fix the code.

## Done checklist (self-verify before reporting)

- [ ] Change sits in the correct module/layer.
- [ ] New behavior is covered by at least one meaningful test.
- [ ] No generated sources were edited.
- [ ] No `!!`, no unused code, no dead parameters introduced.
- [ ] Build/tests/static analysis ran, or the exact skipped commands are listed in the response.
