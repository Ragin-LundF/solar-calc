---
name: static-analysis-cleanup
description: >
  Fix Detekt, SonarQube, ktlint, and formatting findings without changing behavior.
  Use when resolving static-analysis reports, lint warnings, or style violations.
  Do not use for feature work or bug fixes (use kotlin-code-generation) — this skill
  is behavior-preserving by definition.
---

# Skill: Static Analysis Cleanup

## When to use

- A static-analysis tool (Detekt, SonarQube, ktlint, editorconfig checks) reports findings to fix.
- CI fails on lint/style and the code itself is otherwise correct.

## When NOT to use

- The finding reveals a real bug whose fix changes behavior → switch to `kotlin-code-generation.skill.md` and say so; a behavior change must be visible as such, not hidden in a cleanup.
- New code is being written → apply the guidelines up front via `kotlin-code-generation.skill.md` instead of cleaning up afterwards.

## Required context (load before fixing)

1. `../instructions/static-analysis.md` — tool configuration and remediation policy. Authoritative.
2. `../instructions/editorconfig-style.md` — formatting rules.
3. `../harness/static-analysis-remediation-harness.md` — the remediation procedure.

## Workflow

1. **Collect and triage.** Gather all findings for the scope, then order them:
   1. Correctness and security findings (possible real bugs) — first.
   2. Maintainability/complexity findings — second.
   3. Pure style/formatting — last.
2. **Classify each finding** before touching code:
   - *Real issue* → fix the code.
   - *False positive* → suppress narrowly (see rules below).
   - *Symptom of a design problem* (e.g. complexity threshold) → refactor the smallest unit that resolves it; do not shuffle code just to silence the metric.
3. **Fix behavior-preservingly.**
   - Prefer the smallest code change that resolves the finding cleanly.
   - Keep each fix isolated; do not mix unrelated findings into one sweeping edit — reviewability matters.
   - Run existing tests for every touched module; behavior must be provably unchanged.
4. **Suppress only as a last resort.**
   - Narrowest possible scope: annotation on the single declaration or statement, never file- or module-wide.
   - Every suppression carries a short justification (comment or annotation reason).
   - Never change global thresholds, baseline files, or rule configuration to make a local finding disappear.
5. **Verify.** Re-run the static-analysis command(s) for the touched modules and confirm the findings are gone and no new ones appeared. Run the tests. If you cannot run either, list the exact commands for the user.

## Hard rules

- Behavior is preserved. If a fix would change observable behavior, stop and report it as a bug instead.
- Never weaken tests, delete assertions, or relax rule configuration to get a clean report.
- Never disable a rule globally for a local problem.
- Generated sources are excluded from cleanup; never edit them.

## Done checklist (self-verify before reporting)

- [ ] Findings fixed in priority order (correctness → maintainability → style).
- [ ] Zero broad suppressions; every suppression is narrow and justified.
- [ ] No rule config, threshold, or baseline was weakened.
- [ ] Static analysis re-run clean and tests pass, or exact skipped commands listed.
- [ ] Any suspected real bug found during cleanup is reported separately, not silently "fixed".