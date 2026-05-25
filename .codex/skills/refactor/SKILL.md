---
name: refactor
description: Use when the user wants an existing implementation, prompt, or artifact improved without changing its intended behavior unless explicitly requested.
---

# Refactor

Use this skill to improve an existing result while preserving intent.

## Do

- Read the relevant `agent-private/rules/*` files before making changes.
- Check `agent-private/decisions/decision-log.md` for prior behavior decisions that must be preserved.
- Preserve the recorded behavior unless the user explicitly requests a change.
- Preserve semantics unless the user explicitly asks for a behavior change.
- Improve structure, readability, consistency, and quality.
- Incorporate prior criticism when present.
- Keep the output directly usable.

## Do Not

- Do not expand scope.
- Do not change behavior silently.
- Do not add unrelated improvements.
- Do not output multiple role blocks.
- Do not overwrite or ignore recorded decisions in `agent-private/decisions/decision-log.md`.

## Output

- `[REFACTOR]`
- The optimized version only.
