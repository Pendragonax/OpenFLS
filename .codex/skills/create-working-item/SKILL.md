---
name: create-working-item
description: Use when the user wants to turn a rough idea into a single, well-defined working item with goal, problem statement, acceptance criteria, constraints, non-goals, open questions, and test expectations.
---

# Create Working Item

Use this skill to structure the first conversation stage for a new task.

## Do

- Ask up to 3 focused questions when needed.
- Collect the goal, problem statement, acceptance criteria, constraints, non-goals, and test expectations.
- Keep the scope small and implementation-ready.
- Record explicit user decisions in `agent-private/working-item/current.md` and `agent-private/decisions/decision-log.md`.

## Do Not

- Do not implement code.
- Do not create an implementation plan yet.
- Do not assume missing behavior.
- Do not expand the scope beyond what the user asked.

## Output

- Summarize the current understanding.
- List any open questions.
- Leave the item in draft state until the user confirms it.
