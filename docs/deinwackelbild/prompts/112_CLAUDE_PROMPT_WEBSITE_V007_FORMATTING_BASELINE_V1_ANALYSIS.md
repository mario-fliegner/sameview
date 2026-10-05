# Claude Prompt — SameView Website / v0.0.7 Formatting Baseline V1 — STEP 1 ANALYSIS ONLY

## Repository
`C:\data\work\privat\git-repos\sameview-website`

## Single problem
After the approved Privacy fix was committed/tagged as `v0.0.7`, it was discovered that the previous production tag `v0.0.6` did not point to the then-`main` commit. Instead, it reportedly pointed to a bot formatting commit (`9fa3994`) on `chore/auto-lint-format-fixes`, containing formatting changes across 36 files.

`v0.0.7` currently points to `619ae562e51dfc9648496830e9e59b2f928151ec` on `main`, so it may not contain those formatting-only changes.

Analyse only whether this creates a real production/release risk and what the minimal correction, if any, should be.

Do not modify files, merge branches, move/delete tags, create tags, commit, push, or trigger workflows.

## Mandatory checks

1. Read the website repository instructions and release/deploy workflow documentation first.
2. Verify current branch, HEAD, working tree and local/remote tag targets.
3. Verify the exact ancestry/relationship among:
   - `07b81caea384fcfab9780d23ceba013968c6dff7`
   - `9fa3994` (resolve full hash)
   - `619ae562e51dfc9648496830e9e59b2f928151ec`
   - tags `v0.0.6` and `v0.0.7`
   - `origin/main`
   - `chore/auto-lint-format-fixes`
4. Inspect the complete diff introduced by the formatting commit, not just samples.
5. Classify every changed file in that commit as:
   - formatting-only / semantically equivalent;
   - potentially semantic/runtime-affecting;
   - configuration/tooling-affecting;
   - uncertain.
6. Pay special attention to:
   - `biome.json`;
   - `src/i18n/home/de.ts`;
   - components;
   - styles;
   - utils;
   - tests;
   - any generated or config files.
7. Determine whether `v0.0.6` therefore contained any behavior/content/configuration not present in `v0.0.7`.
8. Determine whether the auto-format workflow has already created a new branch/commit/PR after the `619ae56` push, if this can be checked locally/remotely without changing anything.
9. Read the deploy workflow and establish exactly what a stable tag builds/deploys and whether `v0.0.7` has any structural issue merely because it points to `main` rather than a bot formatting commit.

## Verification

If practical and read-only with respect to tracked source files:
- compare builds from the relevant commits/tags only if repository tooling/worktree strategy allows this safely;
- otherwise explain what build comparison would be needed later.

Do not alter the current working tree to perform the comparison.

Do not rerun unrelated tests.

## Required output

Return:

1. repo state;
2. exact commit/tag graph;
3. exact role of the auto-format workflow;
4. complete classification of the `9fa3994` diff;
5. whether `v0.0.7` is missing any semantic behavior/content/configuration that production `v0.0.6` had;
6. whether the issue is:
   - no action required,
   - formatting consistency only,
   - or a real release risk;
7. minimal fix strategy if action is required;
8. exact files/commits that a later STEP 2 would need to consider;
9. risks of merging/cherry-picking/moving tags versus leaving `v0.0.7` as-is;
10. whether production deployment of `v0.0.7` should be considered safe based on the evidence;
11. confirmation that nothing was modified/staged/committed/pushed.

Do not implement anything.

Then STOP.
