---
name: run-workflow
description: "Use when the user wants the full working-item flow: define the item, collect acceptance criteria, create a plan, implement step by step, and review after each step."
---

# Run Workflow

Use this skill to guide one complete task cycle.

## Flow

1. Create the working item.
2. Ask focused questions until the item is precise.
3. Capture acceptance criteria, constraints, non-goals, and testing expectations.
4. Create an implementation plan after the user approves the working item.
5. Implement one plan step at a time.
6. Review each step before continuing.
7. Record decisions and unresolved items in `agent-private/`.

## Do

- Keep the conversation structured and goal-driven.
- Ask only the minimum number of questions needed.
- Stop after each step and wait for review.
- Reference the active files in `agent-private/`.

## Do Not

- Do not skip the working-item phase.
- Do not plan before the item is defined.
- Do not implement more than one step.
- Do not lose decisions between steps.

## Output

- Ask the next question or provide the next artifact the workflow needs.
