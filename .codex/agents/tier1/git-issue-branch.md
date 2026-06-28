---
name: git-issue-branch
description: Create a GitHub issue and local branch that follow this repository's Korean workflow conventions.
---

# Git Issue & Branch Agent

Use this agent when the user asks to create a GitHub issue and start a branch for the work.

## Required Context

Read these files first:

- `.github/ISSUE_TEMPLATE/feature_request.md` or `.github/ISSUE_TEMPLATE/bug_report.md`
- `AGENTS.md`

## Repository Conventions

Write issue titles and bodies in Korean by default. English technical terms may be included when they are clearer.

Allowed work types:

| Type | Use for |
| :--- | :--- |
| `feat` | New features |
| `fix` | Bug fixes |
| `docs` | Documentation changes |
| `style` | Formatting-only changes that do not affect behavior |
| `refactor` | Code restructuring without behavior changes |
| `test` | Test additions or updates |
| `chore` | Build tasks, package manager, CI/CD, or maintenance |
| `design` | UI/UX design changes, including Compose styling |
| `rename` | File or directory renames/moves |
| `remove` | File or feature removals |

## Workflow

1. Inspect the current worktree with `git status --short`.
2. Clarify only if the issue type, goal, or target branch is ambiguous.
3. Draft the issue in Korean unless the user explicitly requests another language.
4. Use the matching issue template when possible.
5. Create the issue with `gh issue create`.
6. Create a branch from the current base branch.

## Branch Naming

Use this pattern:

```text
<type>/<issue-number>-<short-kebab-summary>
```

Examples:

- `feat/23-login-ui`
- `fix/24-supabase-config`
- `docs/25-update-conventions`

Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `design`, `rename`, `remove`.

## Safety

Do not overwrite local changes. If the worktree has unrelated changes, report them and create the branch without modifying those files.
