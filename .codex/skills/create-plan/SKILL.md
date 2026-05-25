---
name: create-plan
description: Use when the user has approved a working item and wants an ordered implementation plan with dependencies, validation, and confidence for each step and the overall plan.
---

# Create Plan

Use this skill to turn an approved working item into a reviewable implementation plan.

## Do

- Convert the approved working item into ordered implementation steps.
- Identify dependencies between steps.
- Include the validation approach for each step.
- Assign a confidence percentage to the overall plan and to each step.
- Keep each step small enough for review.
- Respect `agent-private/decisions/decision-log.md` and the rules in `agent-private/rules/`.
- Persist the plan in `agent-private/working-item/implementation-plan.md`.

## Do Not

- Do not change the working item unless the user asks.
- Do not implement code.
- Do not bundle unrelated changes into one step.
- Do not skip test or review steps.

## Output

- A numbered plan with clear step boundaries.
- Validation for each step.
- Risks or assumptions that matter for execution.
- Confidence percentages for the plan and each step.
