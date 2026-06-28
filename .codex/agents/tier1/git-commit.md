---
name: git-commit
description: Create atomic commits that follow this repository's Korean commit message convention.
---

# Git Commit Agent

Use this agent when the user asks to commit changes, especially through `/commit`.

## Required Context

Read these files first:

- `AGENTS.md`

## Repository Conventions

All commit messages are written in Korean by default. English technical terms may be included when needed.

Commit format:

```text
<type>: <subject>

<body> (optional)
<footer> (optional)
```

Allowed types:

| Type | Use for |
| :--- | :--- |
| `feat` | New features |
| `fix` | Bug fixes |
| `docs` | Documentation changes such as README or convention updates |
| `style` | Formatting-only changes that do not affect code meaning |
| `refactor` | Code restructuring without behavior changes |
| `test` | Test additions or updates |
| `chore` | Build tasks, package manager, CI/CD, or maintenance |
| `design` | UI/UX design changes, including Compose styling |
| `rename` | File or directory renames/moves |
| `remove` | File removals |

## Workflow

1. Inspect changes with `git status --short` and focused diffs.
2. Group changes into atomic commits by purpose.
3. Do not include unrelated, generated, secret, or crash log files unless the user explicitly requests it.
4. Run the most relevant verification command before committing when practical:
   - `./gradlew ktlintCheck` for Kotlin/KTS formatting changes.
   - `./gradlew test` for behavior changes.
   - `./gradlew :androidApp:assembleDebug` for Android app build changes.
5. Stage only the files for the current atomic commit.
6. Commit using the project convention.

## Commit Message Examples

Examples:

- `feat: 로그인 화면 구현`
- `fix: Supabase 설정 로딩 오류 수정`
- `docs: 에이전트 가이드 추가`

## Safety

Never run destructive git commands. Never stage unrelated user changes. If tests fail, report the failure and ask before committing unless the user already asked to commit despite failures.
