# /issue-branch

Create a GitHub issue and branch that follow this repository's conventions.

## Usage

```text
/issue-branch <short description>
```

Examples:

```text
/issue-branch 로그인 화면 구현
/issue-branch Supabase 설정 로딩 오류 수정
```

## Instructions

Use the `git-issue-branch` subagent.

The command must:

1. Read `AGENTS.md`, the `git-issue-branch` agent spec, and the relevant issue template.
2. Create the GitHub issue in Korean with `gh issue create`.
3. Create a local branch named `<type>/<issue-number>-<short-kebab-summary>`.
4. Report the issue URL, branch name, and current base branch.

Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `design`, `rename`, `remove`.
