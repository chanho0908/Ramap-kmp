---
name: git-pr
description: Create a pull request that follows this repository's Korean PR template and review checklist.
---

# Git PR Agent

Use this agent when the user asks to open or prepare a pull request.

## Required Context

Read these files first:

- `.github/PULL_REQUEST_TEMPLATE.md`
- `AGENTS.md`

## Repository Conventions

Write PR titles and bodies in Korean by default. English technical terms may be included when needed.

PR title rules:

- Use `[기능명] 작업 내용 요약` or `작업 내용 요약`.
- Do not use commit-style prefixes such as `feat:` or `fix:`.
- Examples: `로그인 UI 구현`, `API 연동 오류 수정`.

PR body rules:

- Fill out `.github/PULL_REQUEST_TEMPLATE.md`.
- Include overview, changes, test results, and related issue links.
- Add screenshots or recordings for UI changes.

## Workflow

1. Inspect branch state with `git status --short` and `git branch --show-current`.
2. Ensure local changes are committed or clearly report any uncommitted files.
3. Compare with the target branch, usually `develop` unless the user says otherwise.
4. Run or summarize relevant verification:
   - `./gradlew ktlintCheck`
   - `./gradlew test`
   - `./gradlew :androidApp:assembleDebug`
5. Push the branch if needed.
6. Create the PR with `gh pr create` using the repository PR template.

## PR Conventions

- Title: Korean summary without `feat:` or `fix:` prefixes.
- Body: include overview, changes, test results, screenshots or recordings for UI changes, related issues, and checklist.
- Link issues with `Fixes #<number>` when the PR should close an issue.

## Safety

Do not create a PR from a dirty worktree without calling it out. Do not invent test results; write exactly what was run and what was not run.
