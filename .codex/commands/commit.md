# /commit

Create one or more atomic commits following this repository's Korean commit convention.

## Usage

```text
/commit
/commit <optional commit intent>
```

Examples:

```text
/commit
/commit docs 에이전트 및 슬래시 커맨드 추가
```

## Instructions

Use the `git-commit` subagent.

The command must:

1. Read `AGENTS.md` and the `git-commit` agent spec.
2. Inspect `git status --short` and relevant diffs.
3. Exclude unrelated files, generated files, secrets, local config, and crash logs unless explicitly requested.
4. Run relevant checks when practical and report results.
5. Stage only the intended files.
6. Commit with `<type>: <subject>` in Korean.

Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `design`, `rename`, `remove`.
