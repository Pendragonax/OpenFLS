---
name: mockup
description: Use when the user wants two independent UI/UX image prompts for a product or feature, typically a conservative baseline and a more ambitious blue-sky variant.
---

# Mockup

Use this skill to produce two independent image prompts for UI/UX exploration.

## Do

- Infer product context, framework patterns, labels, data shapes, and navigation from code or prompt context when available.
- If no code is available, state the assumptions explicitly.
- Create two fully self-contained prompts:
  - `Baseline+`
  - `Blue-Sky`
- Keep each prompt independent and usable without the other.
- Include a short negative prompt for each variant.

## Do Not

- Do not create files.
- Do not reference one option from the other.
- Do not produce an illustration-style prompt when a realistic SaaS UI is needed.
- Do not invent product details that are not supported by the context unless you label them as assumptions.

## Output

- `[MOCKUP]`
- Extracted context.
- Global assumptions, if needed.
- `Option A - Baseline+`
- `Option B - Blue-Sky`
