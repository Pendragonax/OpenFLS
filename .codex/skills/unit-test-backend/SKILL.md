---
name: unit-tests-backend
description: Use when the user wants Kotlin backend unit tests written in the repository's standard style, using AssertJ and clear Given/When/Then structure.
---

# Backend Unit Tests

Use this skill to create or extend Kotlin backend tests.

## Do

- Use English test method names.
- Follow the pattern `method_precondition_expectedOutcome`.
- Use a clear `Given / When / Then` structure in each test.
- Use AssertJ for assertions.
- Cover the relevant scenarios for the changed behavior.
- Prefer focused unit or slice tests close to the affected service or controller.

## Do Not

- Do not use reflection.
- Do not rely on long verify chains.
- Do not test call order instead of outcomes.
- Do not omit edge cases that matter to the changed behavior.

## Output

- A complete, directly usable Kotlin test class or the minimal set of test methods needed to fit into the existing class.
