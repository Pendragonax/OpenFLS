# AGENTS

This repository uses `agent-private/` as the private working area for task state, decisions, prompts, and working-item tracking.

## Operating Model

1. Capture a working item first.
1. Clarify acceptance criteria and constraints in dialog before implementation.
1. Build an implementation plan only after the working item is approved.
1. Execute the plan step by step.
1. Review every step before moving on.
1. Record decisions and remaining concerns in `agent-private/`.

## What To Read Before Working

- `agent-private/README.md`
- `agent-private/working-item/current.md`
- `agent-private/working-item/implementation-plan.md`
- `agent-private/decisions/decision-log.md`
- `agent-private/rules/testing.md`
- `agent-private/rules/architecture.md`
- `agent-private/rules/review.md`

## General Rules

- Keep changes aligned with the existing codebase structure.
- Do not start implementation until the working item and acceptance criteria exist.
- Ask clarifying questions when scope, behavior, or constraints are ambiguous.
- Keep each implementation step small and reviewable.
- Prefer targeted tests over broad test runs unless the change touches shared infrastructure.
- Report clearly when tests were not run and why.
- Do not overwrite prior decisions; append updates instead.

## Documentation Sources

- Use the Spring Boot documentation MCP when Spring-related behavior or APIs are in scope.
- Use the Angular documentation MCP when Angular-related behavior or APIs are in scope.
- If an MCP source is unavailable, fall back to the official vendor documentation.

## Prompt Templates

Use the templates in `agent-private/prompts/` to drive the workflow:

- `agent-private/prompts/create-working-item.prompt.md`
- `agent-private/prompts/run-workflow.prompt.md`
- `agent-private/prompts/create-plan.prompt.md`
- `agent-private/prompts/implement-step.prompt.md`
- `agent-private/prompts/review-step.prompt.md`

