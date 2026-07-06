---
name: editorconfig-style
description: >
  Formatting rules from .editorconfig: charset, line endings, indentation, line
  length, ktlint rule exceptions, and Gherkin style. Load for formatting
  questions or when static-analysis/formatting findings are being fixed.
  The repository .editorconfig file itself is the source of truth.
---

# EditorConfig Style

The bundled `.editorconfig` is the formatting source of truth, unless the host repository already has stricter project-specific settings. When in doubt, read the actual `.editorconfig` — this file only summarizes it.

## Global defaults

| Setting | Value |
|---------|-------|
| Charset | UTF-8 |
| Line endings | LF |
| Indentation | 4 spaces |
| Tab width | 4 |
| Final newline | Required |
| Max line length | 120 |
| Continuation indent | 8 |
| Formatter tags | `@formatter:off` / `@formatter:on` |

## Kotlin / ktlint exceptions

The bundled `.editorconfig` disables selected ktlint rules to avoid formatter conflicts with generated code:

- `ktlint_standard_no-wildcard-imports = disabled`
- `ktlint_standard_max-line-length = disabled`
- `ktlint_standard_enum-entry-name-case = disabled`

These exceptions exist for **generated** code. In handwritten sources, still keep imports explicit, lines within reasonable length, and enum names clear — a disabled lint rule is not a style permission.

## Gherkin

- `*.feature` files use **two-space** indentation (not four).
- Scenario steps stay readable and business-focused (see `rest-api-cucumber.md`).

## Adoption into an existing repository

1. Compare the existing `.editorconfig` with the bundled file.
2. Preserve stricter repository-specific settings, unless they conflict with required generated-code behavior.
3. Never silently replace team-specific settings — surface differences for review.