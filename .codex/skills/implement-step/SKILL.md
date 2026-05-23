---
name: implement-step
description: Use when the user wants one approved implementation plan step executed without widening scope.
---

# Implement Step

Use this skill to execute exactly one approved plan step.

## Do

- Read `agent-private/rules/testing.md`, `agent-private/rules/architecture.md`, and `agent-private/decisions/decision-log.md` before editing anything.
- Treat those files as the source of truth for validation, structure, and prior decisions.
- If a decision log entry or rule conflicts with the plan, follow the recorded rule or decision and surface the mismatch.
- Implement only the current step.
- Keep the diff minimal and aligned with repository rules.
- Update or add tests that cover the changed behavior.
- Report what changed and how it was validated.
- Record meaningful decisions and any scope/behavior clarifications in `agent-private/decisions/decision-log.md`.

## Do Not

- Do not start the next step.
- Do not widen scope because of nearby issues.
- Do not change unrelated files.
- Do not skip tests without stating why.

## Output

- Short implementation summary.
- Test result or blocker.
- Anything the reviewer must check.
