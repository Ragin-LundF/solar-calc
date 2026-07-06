---
name: static-analysis
description: >
  Static analysis and formatting rules: tools (Detekt, SonarQube, ktlint,
  .editorconfig), the project's Detekt threshold overrides, suppression policy,
  cleanup policy, and verification commands. Load when fixing or preventing
  Detekt/SonarQube/ktlint/formatting findings.
---

# Static Analysis and Formatting

Load for Detekt, SonarQube, ktlint, `.editorconfig`, and lint cleanup. The remediation procedure lives in `../harness/static-analysis-remediation-harness.md`; this file defines the tools, thresholds, and policies.

## Tools

- **Detekt** for Kotlin static analysis.
- **SonarQube** standard rules where the host project configures them.
- **`.editorconfig`** as the formatting source of truth (see `editorconfig-style.md`).
- The bundled `config/detekt/detekt.yml` contains the project-specific overrides below; when adopting into a host project, merge it with the existing config rather than replacing it.

## Detekt threshold overrides (bundled config)

| Rule | Override |
|------|----------|
| `ReturnCount` | Active; guard clauses excluded; max returns `5` |
| `MagicNumber` | Ignores annotations |
| `ForbiddenComment` | Disabled |
| `NamedArguments` | Active; allowed arguments `15`; matching names not ignored |
| `LongParameterList` | Ignores default parameters; `25` function / `15` constructor parameters |
| `TooManyFunctions` | `20` functions per class/interface/object |
| `SpreadOperator` | Disabled (Kotlin compiler optimizations reduce the concern) |

These thresholds are relaxed to cut noise in real projects — they are **not** permission to write unfocused code. Aim well below the limits; the limits are the ceiling, not the target.

## SonarQube expectations

- Introduce no new code smells, duplicated logic, security hotspots, or reliability issues.
- Refactor duplication into an abstraction only when the abstraction genuinely improves clarity — deduplication that obscures is worse than duplication.
- Avoid suppressing Sonar findings; if unavoidable, keep it local and document why.

## Suppression policy

Suppression is the last resort, in this order:

1. Understand the finding — never suppress what you cannot explain.
2. Try a small design or readability improvement first.
3. Confirm behavior remains tested after the change.
4. Only then suppress, at the narrowest possible scope (single declaration/statement).
5. Document why the suppression is correct, at the suppression site.

Never suppress to hide generated bad code, missing tests, security weaknesses, or a rushed implementation.

## Cleanup policy

- Preserve behavior before refactoring style — a cleanup with a red test suite is a behavior change.
- Separate mechanical formatting changes from behavioral changes wherever possible (separate commits/changes).
- Do not reformat unrelated files unless the task is explicitly a formatting-only cleanup.
- Run the relevant checks before declaring completion.

## Verification commands

Use the commands that exist in the host repository. Typical options:

```bash
./gradlew test detekt
./gradlew check
mvn test
mvn verify
```

If a command cannot be run, state exactly which commands were skipped and why — never imply a check passed that did not run.