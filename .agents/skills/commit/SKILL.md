---
name: commit
description: Create one or more atomic commits following this repository's Korean commit convention. Use when the user invokes `/commit` or asks to commit changes.
---

# Commit

Create atomic commits for this repository.

## Usage

```text
/commit
/commit <optional commit intent>
```

## Workflow

1. Read `AGENTS.md` and `.codex/agents/tier1/git-commit.md`.
2. Inspect `git status --short` and focused diffs.
3. Exclude unrelated files, generated files, secrets, local config, and crash logs unless explicitly requested.
4. Run relevant checks when practical and report the results.
5. Stage only the intended files.
6. Commit with this format:

```text
<type>: <subject>
```

Use Korean for the subject. Allowed types are `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `design`, `rename`, and `remove`.
