---
name: pr
description: Create a pull request following this repository's Korean PR convention. Use when the user invokes `/pr` or asks to open a PR.
---

# Pull Request

Create a pull request for this repository.

## Usage

```text
/pr
/pr <target branch>
```

## Workflow

1. Read `AGENTS.md`, `.codex/agents/tier1/git-pr.md`, and `.github/PULL_REQUEST_TEMPLATE.md`.
2. Confirm the current branch and target branch, defaulting to `develop` when no target is provided.
3. Ensure intended changes are committed, or clearly report uncommitted files.
4. Run or report relevant checks.
5. Push the branch when needed.
6. Create the PR with a Korean title and a body based on `.github/PULL_REQUEST_TEMPLATE.md`.

Do not use commit-style prefixes in the PR title. Include `Fixes #<issue>` when applicable.
