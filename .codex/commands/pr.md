# /pr

Create a pull request following this repository's Korean PR convention.

## Usage

```text
/pr
/pr <target branch>
```

Examples:

```text
/pr
/pr develop
```

## Instructions

Use the `git-pr` subagent.

The command must:

1. Read `AGENTS.md`, the `git-pr` agent spec, and `.github/PULL_REQUEST_TEMPLATE.md`.
2. Confirm the current branch and target branch, defaulting to `develop`.
3. Ensure all intended changes are committed.
4. Run or report relevant checks.
5. Push the branch when needed.
6. Create the PR with a Korean title and a body based on `.github/PULL_REQUEST_TEMPLATE.md`.

Do not use commit-style prefixes in the PR title. Include `Fixes #<issue>` when applicable.
