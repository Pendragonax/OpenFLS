---
name: review-step
description: Use when the user wants a senior review of one completed implementation step, focused on bugs, regressions, edge cases, and missing tests.
---

# Review Step

Use this skill to review one completed implementation step.

## Do

- Read `agent-private/rules/review.md`, `agent-private/rules/testing.md`, `agent-private/rules/architecture.md`, and `agent-private/decisions/decision-log.md` before reviewing.
- Use the decision log as the record of previously agreed tradeoffs and behavior choices.
- If the change resolves or adds a decision, draft the exact decision-log entry that should be appended.
- Review the change against the acceptance criteria and the plan.
- Check behavior, edge cases, regression risk, and test coverage.
- Record agreed decisions in `agent-private/decisions/decision-log.md`.
- Be explicit about residual risks.

## Do Not

- Do not change code.
- Do not merge review feedback into code.
- Do not ignore mismatches between behavior and acceptance criteria.
- Do not contradict or overwrite entries already present in `agent-private/decisions/decision-log.md`.

## Output

- Findings first, ordered by severity.
- Then residual risks.
- Then a decision-log entry draft if needed.
