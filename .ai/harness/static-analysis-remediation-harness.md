---
name: static-analysis-remediation-harness
description: >
  Step-by-step remediation procedure for Detekt, SonarQube, ktlint, and formatting
  findings. Used by the static-analysis-cleanup skill. Defines finding
  classification, the fix-over-suppress policy, and the prohibited shortcuts.
---

# Static Analysis Remediation Harness

Work findings one at a time through this procedure. Batch only findings of the same rule in the same module.

## Procedure

1. **Read the finding and the affected source.** Understand what the rule protects against before touching anything — never fix a finding you cannot explain.
2. **Classify** the finding:
   - `correctness` — possible real bug. Highest priority; may need to escalate to a proper bug fix with tests.
   - `security` — same priority as correctness.
   - `maintainability` — complexity, duplication, design smells.
   - `style` — formatting, naming, ordering. Lowest priority.
   - `generated-code noise` — finding in generated sources: exclude the path in configuration if not already excluded; never hand-edit generated files.
3. **Fix, don't silence.** Prefer the minimal design/readability fix that resolves the finding. If the honest fix would change behavior, stop treating it as cleanup — report it as a bug and handle it as a code change with tests.
4. **Keep behavior provably unchanged.** Run the existing tests for every touched module after refactoring. A "style-only" change with a red test suite is a behavior change.
5. **Verify the finding is gone.** Re-run the relevant static-analysis command for the touched modules and confirm: target findings resolved, no new findings introduced. If you cannot run it, list the exact command in your response.
6. **Suppress only as the documented last resort.** When a finding is a true false positive:
   - Scope: the single declaration or statement — never a file, package, or module.
   - Every suppression carries a justification (annotation reason or adjacent comment).
   - State each suppression and its reason in your completion response.

## Do not

- Reformat or "clean up" files unrelated to the findings being fixed.
- Change behavior while labeling the change style-only.
- Suppress an entire file or rule to silence one finding.
- Relax global configuration, thresholds, or baselines for a local issue.
- Lower a threshold at all unless there is a documented, project-wide decision to do so — and then reference that decision.
- Delete or weaken tests to make a refactoring pass.

## Completion report

- Findings fixed, grouped by rule, with the fix approach per group.
- Suppressions added, each with scope and justification.
- Suspected real bugs discovered during cleanup, reported separately.
- Verification commands run (with results) and commands skipped (with reason).