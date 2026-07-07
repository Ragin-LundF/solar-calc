---
name: coding-guidelines
description: >
  Kotlin coding rules: style, design, error handling, parameters, and forbidden
  shortcuts. Load whenever creating, editing, reviewing, or testing Kotlin code.
  Authoritative unless a more specific repository instruction overrides a rule.
---

# Kotlin Coding Guidelines

These rules apply to all Kotlin code. A more specific repository instruction wins on conflict; otherwise these rules are binding, not suggestions.

## Kotlin style

- New services and modules are written in Kotlin.
- Use null-safety deliberately: a type is nullable only when `null` is a valid domain state — never as a convenience default.
- Prefer `val` over `var`; prefer immutable data structures and immutable data classes where practical.
- Use primary constructors; avoid boilerplate mapping constructors. Never use Lombok in Kotlin code.
- Use named arguments when calling Kotlin functions, constructors, assertions, or builders where they improve readability (multiple same-typed parameters, booleans, nulls). Do not attempt named arguments on Java APIs — Kotlin does not support them there.
- Implement functions with **block bodies**. Do not use expression-body functions.
- Keep functions small and single-purpose. Split functions that grow too long, nest too deeply, or mix responsibilities.
- Prefer early returns and guard clauses over deep nesting.
- Use `runCatching` when it keeps behavior correct and readable; use `try/catch` when `runCatching` would obscure or break behavior — `finally`, resource cleanup, coroutine cancellation propagation, or explicit exception flow.
- One top-level declaration (class/interface/enum/object) per file.
- Use descriptive names for variables, classes, and functions — no abbreviations that need decoding.
- Comment complex logic, non-obvious decisions, trade-offs, and domain rules. Never add comments that restate obvious code.
- Where this package defines no rule, follow the official Kotlin style guide.
- Follow `.editorconfig` exactly (see `editorconfig-style.md`): indentation, line endings, charset, final newlines, max line length, Gherkin indentation.
- DTO objects have always the suffix `Dto`

## Design expectations

- Single responsibility for classes, functions, and modules.
- Public APIs are explicit, predictable, and easy to test.
- Preserve existing behavior unless the task explicitly requests a behavior change.
- Do not hide complexity inside overly generic abstractions; write the concrete thing first.
- When a value has domain meaning, prefer typed identifiers, value classes, enums, and sealed types over raw strings.
- Generated code stays separate from handwritten code. Never manually edit generated sources unless the repository explicitly requires it — change the generator input instead.

## Error handling

- Domain-specific exceptions for unrecoverable structural failures.
- Validation result objects or diagnostics for semantic validation errors that should be collected and reported rather than aborting on the first hit.
- Never swallow exceptions silently.
- No broad `catch (Exception)` unless there is a precise boundary reason *and* the behavior is tested.

## Parameters and APIs

- Parameter names describe intent, not implementation detail.
- Avoid boolean parameters that obscure call-site meaning; prefer expressive enums or two well-named functions.
- Long parameter lists are tolerated only for generated code, DTO construction, or stable API boundaries. In handwritten domain logic, introduce a meaningful parameter object instead.

## Forbidden shortcuts

Never do any of the following:

- Disable or suppress static analysis without a narrow, written justification in code or commit notes.
- Reduce production visibility (`private` → `internal`/`public`) just to make testing easier when it worsens the design.
- Add unused production hooks that exist only for tests.
- Change production behavior to satisfy a brittle test, unless the behavior change itself is what the task requests.

## Dependencies (Kotlin)

- Do not use starters in any other submodule than the `solarcalc-server`. Use only the required dependency modules to avoid too big dependencies for modules.
