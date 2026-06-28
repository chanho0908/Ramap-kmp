---
name: issue-branch
description: Create a GitHub issue and local branch from a short Korean work description. Use when the user invokes `/issue-branch` or asks to create an issue branch.
---

# Issue Branch

Create a GitHub issue and switch to a matching local branch for this repository.

## Usage

```text
/issue-branch <short description>
/issue-branch <type> <short description>
```

Examples:

```text
/issue-branch 카카오 맵 연동
/issue-branch feat 카카오 맵 연동
```

## Workflow

1. Read `AGENTS.md`, `.codex/agents/tier1/git-issue-branch.md`, and the relevant issue template in `.github/ISSUE_TEMPLATE`.
2. Inspect the current branch and worktree with:

```bash
git branch --show-current
git status --short
```

3. Treat the first token as a work type only when it is one of:
   `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `design`, `rename`, or `remove`.
4. If no type is provided, infer the conservative type from the description. For new functionality, default to `feat`.
5. Create the GitHub issue in Korean with `gh issue create`, using the matching template when practical.
6. Create and switch to a local branch from the current branch:

```text
<type>/<issue-number>-<short-kebab-summary>
```

7. Do not overwrite, revert, stage, or commit unrelated local changes.
8. Report the issue URL, new branch name, and previous base branch.

## Branch Examples

- `feat/23-kakao-map`
- `fix/24-supabase-config`
- `docs/25-update-conventions`
