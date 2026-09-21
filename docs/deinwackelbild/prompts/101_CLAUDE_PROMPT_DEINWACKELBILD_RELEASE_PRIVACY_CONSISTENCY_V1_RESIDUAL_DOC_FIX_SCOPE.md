# Claude Prompt — DeinWackelbild Release / Privacy Consistency V1 — Residual Documentation Fix — STEP 2 SCOPE CONFIRMATION

## Context

STEP 3 of the approved documentation consistency pass modified exactly eight docs and verification found one additional CURRENT documentation contradiction that had been missed during STEP 2:

`docs/GPS_RECREATION_SYSTEM_V1.md`, around line 548:

> This document does not imply `AndroidManifest.xml` has already been changed; the permission is not yet declared.

That statement is now false because `android.permission.INTERNET` was subsequently declared for the approved DeinWackelbild V1 order flow.

There is also a stale Kotlin comment in `WackelbildViewModel.kt`, but that is NOT part of this task. Keep this iteration documentation-only.

The previous eight documentation edits are currently uncommitted and must remain untouched except for verification. Do not rewrite or expand them.

## Workflow

This is STEP 2 — SCOPE CONFIRMATION ONLY.

Do NOT edit any file.

## Required checks

1. Run `git status --short`.
2. Confirm the only tracked modifications are the previously approved eight docs.
3. Confirm `docs/GPS_RECREATION_SYSTEM_V1.md` is currently unmodified.
4. Read the relevant section around the stale statement and enough surrounding context to determine whether it is historical or normative/current.
5. Cross-check the current DeinWackelbild network exception in:
   - `docs/CLAUDE_PROJECT_INSTRUCTION.md`
   - `docs/IMPLEMENTATION_NOTES.md`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
6. Do not inspect or modify unrelated code.

## Proposed minimal fix

The expected fix is ONE documentation file only:

`docs/GPS_RECREATION_SYSTEM_V1.md`

Preserve the original historical wording if it belongs to the original design state. Prefer appending a concise dated status note rather than rewriting history.

The note should establish only that:
- the original sentence reflected the state when this GPS Recreation spec was written;
- `android.permission.INTERNET` was later declared for the separate approved DeinWackelbild V1 order flow;
- GPS Recreation itself still does not require or use INTERNET;
- this does not change GPS Recreation's privacy/offline contract.

Do not add broader networking discussion.

## Explicit exclusions

Do NOT modify:
- the eight already-edited docs;
- `WackelbildViewModel.kt`;
- any other Kotlin/Java source;
- manifest;
- Gradle;
- strings;
- tests;
- `sameview-release`;
- website;
- Play Console.

No Gradle/test/device/API work is required for a documentation-only correction.

## Required output

Return:

1. branch/HEAD and current `git status --short`;
2. whether the stale GPS statement is historical or current/normative in context;
3. exact file and exact passage proposed for modification;
4. exact nature of the one-line/minimal dated status note;
5. confirmation that GPS Recreation remains offline and its behavior is unchanged;
6. confirmation that the previous eight modified docs will not be touched;
7. complete proposed modification scope — expected to be exactly one file;
8. verification planned for STEP 3:
   - `git status --short`
   - `git diff --check`
   - focused diff for `docs/GPS_RECREATION_SYSTEM_V1.md`
   - focused grep confirming the residual statement is either corrected/superseded;
9. confirmation no builds/tests/device/API work will be run;
10. confirmation nothing was modified, staged, committed or pushed in this STEP 2.

Then STOP and wait for explicit approval.
