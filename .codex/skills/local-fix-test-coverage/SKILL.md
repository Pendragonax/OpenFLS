---
name: local-fix-test-coverage
description: Raise or repair test coverage for all changes on the current branch compared with main while editing tests only. Use when asked to fix coverage, improve branch-diff test coverage, satisfy a coverage quality gate, add tests for changed code, or make every current-branch production change reach an 83% line-coverage gate without changing production code. Follow AGENTS.md, agent-context/testing.md, and the repo's agent-private README, decision log, and rules before making test changes.
---

# Local Fix Test Coverage

Use this skill to bring the current branch's production changes, compared with the main branch, to at least 83% meaningful line coverage by adding or adjusting tests only.

## Required Context

Before editing, read:

1. `AGENTS.md`
2. `agent-context/README.md`
3. `agent-context/testing.md`
4. `agent-private/README.md`
5. `agent-private/working-item/current.md`
6. `agent-private/working-item/implementation-plan.md`
7. `agent-private/decisions/decision-log.md`
8. `agent-private/rules/testing.md`
9. `agent-private/rules/architecture.md`
10. `agent-private/rules/review.md`
11. Current `git status`
12. Current branch name and diff against the selected main-branch base
13. Relevant changed production files and existing tests

If changed production code touches Spring Boot, Spring Framework, or Spring AI behavior, use the configured documentation MCP before relying on version-sensitive framework behavior.

## Hard Rules

- Do not change production code, generated contracts, migrations, configuration used by production runtime, or application behavior.
- Edit only test code, test fixtures, test resources, and test-specific build or coverage configuration when the repository already treats that configuration as test-only.
- Do not weaken, remove, or bypass coverage verification.
- Do not lower coverage thresholds. The quality gate for this skill is at least 83% line coverage for the production changes introduced by the current branch.
- Do not use reflection unless the user explicitly confirms it.
- Follow `agent-context/testing.md`: English test names in `methodName_precondition_expectedResult` form, Given/When/Then sections, AssertJ assertions, meaningful positive and negative scenarios, and public methods as test entry points.
- Apply relevant decisions and rules from `agent-private/decisions/decision-log.md` and `agent-private/rules/`.
- Do not revert user changes.

If satisfying coverage appears to require production-code changes, stop and explain why.

## Select The Base Branch

If the user provides a base branch or commit, use it.

Otherwise:

1. Compare the current branch against the main branch by default.
2. Resolve the base using common main refs in this order: `origin/main`, `main`, `origin/master`, `master`.
3. Use `git merge-base HEAD <main-ref>` as the selected base commit.
4. Do not use the current branch's same-named upstream, such as `origin/feature/...`, as the comparison base unless the user explicitly asks for it.
5. If no main or master base can be resolved, ask the user for the base branch and suggest concrete local refs.

Use the selected main-branch merge base to identify all branch changes, including committed, staged, and unstaged changes.

## Workflow

1. Inspect branch changes.
   - List changed production files, changed test files, and relevant generated or contract files.
   - Identify changed executable production lines relative to the selected main-branch merge base that need meaningful test coverage.
   - Treat renamed or moved production files as changed branch files and inspect their executable lines under the new path.
   - Separate unrelated dirty working-tree changes if they are clearly not part of the current request.

2. Run the smallest useful validation first.
   - For backend changes, prefer focused Gradle tests for the changed area.
   - For frontend changes, prefer the relevant package test command.
   - If the focused command fails because the tested code appears broken, summarize the failures and ask the user what to do before changing production code.
   - If the focused command fails because tests are stale, incomplete, or incorrectly asserted, fix the tests within the hard rules.

3. Run coverage verification.
   - Discover the repository's existing coverage task instead of inventing one.
   - For this repository's backend, prefer JaCoCo verification, for example `./gradlew :backend:jacocoTestCoverageVerification`, and confirm the line coverage gate is at least 83%.
   - If the configured repository gate is below 83%, use an additional report or verification method to prove at least 83% line coverage for the current branch's changed production lines relative to main without lowering existing gates.
   - When using JaCoCo XML directly, map changed executable source lines from the main-branch diff to their `<line>` entries and calculate branch-diff line coverage as `covered changed executable lines / total changed executable lines`.
   - Aim for every changed production file to meet 83% branch-diff line coverage. If a file-level measurement is impossible or misleading because the diff contains only non-executable declarations, state that and prove the changed area or affected module reaches at least 83%.
   - If the project already has an 83% or higher line gate, use that gate.

4. Add or adjust tests only.
   - Cover behavior through public APIs, application services, controllers, adapters, or other public entry points that match existing patterns.
   - Prefer tests that demonstrate real behavior and failure modes over superficial invocation-only coverage.
   - Cover relevant positive and negative scenarios from the branch diff.
   - Keep changes scoped to current-branch production changes.

5. Re-run validation.
   - Run focused tests that cover the new or changed tests.
   - Run coverage verification proving at least 83% line coverage.
   - If practical, run broader tests when changed tests touch shared setup or test infrastructure.

6. Self-review.
   - Confirm the final diff contains no production-code changes introduced by this skill.
   - Confirm no coverage gate was weakened.
   - Confirm coverage was checked against the current branch's production changes compared with main, not against the branch's same-named upstream.
   - Confirm tests follow `agent-context/testing.md` and relevant ADRs.

## Failure Handling

When tests or coverage fail, classify the failure before editing:

- Test gap: add or adjust tests only.
- Test defect: fix the test expectation or fixture only.
- Production/code-under-test defect: stop, summarize the failing behavior, include the relevant command and short failure excerpt, and ask the user whether to fix production code, keep this skill scoped to tests only, or defer the issue.
- Existing unrelated baseline failure: document the baseline failure, continue only if coverage work can be validated independently, and report the residual risk.

Do not silently encode broken behavior as expected just to raise coverage.

## Final Response

Report:

- base branch or commit used
- production files inspected
- test files changed
- validation commands and results
- measured or verified branch-diff line coverage for changed production lines, including how the 83% gate was proven
- any changed production files that could not be measured at file-level and the fallback changed-area or module coverage used
- any failures that appear to reveal production-code defects
- remaining risks
- suggested commit message, using `test:` unless the user requested a different convention
