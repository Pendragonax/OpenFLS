---
name: review-implementation-step
description: Use when the user wants to review one implementation-plan step, ask confidence-building questions, inspect what is already done, and keep `agent-private/working-item/implementation-plan.md` and `agent-private/decisions/decision-log.md` up to date.
---

# Review Implementation Step

Use this skill to review one step of the implementation plan before coding or while refining an in-progress step.

## Do

- Read `agent-private/README.md`, `agent-private/working-item/current.md`, `agent-private/working-item/implementation-plan.md`, `agent-private/decisions/decision-log.md`, and the relevant rules in `agent-private/rules/` before asking questions.
- Inspect the codebase first so already-completed work is not re-litigated.
- Ask the user targeted questions that reduce uncertainty for the selected step.
- Prefer 1 to 3 short questions at a time.
- Capture any new decisions immediately in `agent-private/decisions/decision-log.md`.
- Update `agent-private/working-item/implementation-plan.md` whenever the step scope, dependencies, validation, assumptions, or confidence change.
- Recalculate the step confidence after each meaningful answer or code inspection.
- Keep the scope of the current plan step narrow and aligned with the existing repo structure.
- Treat repository workflow rules as mandatory and keep the plan and decision log synchronized.

## Do Not

- Do not change code unless the user explicitly asks to start implementation.
- Do not leave the plan or decision log stale after a decision has been made.
- Do not ask broad, open-ended questions when a narrower question can raise confidence.
- Do not overwrite prior decisions; append updates instead.
- Do not ignore the repository workflow rules or the step review process.

## How To Use

1. Identify the exact plan step and its current confidence.
2. Inspect the code paths relevant to that step.
3. Ask the user the minimum set of questions needed to raise confidence.
4. Update the implementation plan with any clarified scope or revised confidence.
5. Append the confirmed decisions to the decision log.
6. Repeat until the step is clear enough to implement or the user decides to stop.

## Output

- State what is already confirmed from the code.
- State the remaining uncertainty.
- Ask the next confidence-building questions.
- If confidence changes, state the new confidence and why it changed.
