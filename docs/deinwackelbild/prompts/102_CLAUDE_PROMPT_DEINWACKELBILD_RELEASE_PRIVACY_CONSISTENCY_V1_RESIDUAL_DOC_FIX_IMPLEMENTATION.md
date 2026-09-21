# Claude Prompt — DeinWackelbild Release / Privacy Consistency V1 — Residual Documentation Fix — STEP 3 IMPLEMENTATION

## Approval

The STEP 2 scope is approved exactly as proposed.

Modify exactly ONE file:

`docs/GPS_RECREATION_SYSTEM_V1.md`

Do not modify any other file.

The eight documentation files already modified by the preceding privacy-consistency pass must remain byte-for-byte unchanged by this step.

## Mandatory pre-check

Before editing:

1. Run `git status --short`.
2. Confirm branch is `main` and HEAD is still `b2ed20b` unless the user independently advanced it.
3. Confirm the only tracked modifications are the previously approved eight documentation files.
4. Confirm `docs/GPS_RECREATION_SYSTEM_V1.md` is currently unmodified.
5. Record a reliable before-state for the existing eight-doc diff so you can prove this step did not alter it.
6. If any unexpected tracked change exists, STOP before editing.

## Exact implementation

In:

`docs/GPS_RECREATION_SYSTEM_V1.md`

locate the final bullet in the “Hosted Comparison exception” subsection of §11, currently containing the historical sentence:

`This document does not imply AndroidManifest.xml has already been changed; the permission is not yet declared.`

Preserve the existing wording.

Append one concise dated status note to that bullet establishing exactly these facts:

- the final sentence reflected the state when that subsection was written alongside the 2026-08-19 Hosted Comparison addendum;
- `android.permission.INTERNET` was subsequently declared for the separate approved DeinWackelbild V1 order flow;
- GPS Recreation itself still requires and uses no INTERNET permission;
- GPS Recreation's privacy/offline contract is unchanged.

Use the wording proposed and approved in STEP 2, with only formatting adjustments required to match the existing Markdown style.

Do not add broader network/privacy discussion.

Preserve the file's LF line endings.

## Explicit exclusions

Do NOT modify:

- any of the eight previously edited docs;
- `WackelbildViewModel.kt`;
- any Kotlin/Java source;
- `AndroidManifest.xml`;
- Gradle/build files;
- strings;
- tests;
- `sameview-release`;
- website files;
- Play Console material;
- any other documentation.

Do not fix the stale `WackelbildViewModel.kt` comment in this step.

## Verification

Run only:

```powershell
git status --short
git diff --check
git diff -- docs/GPS_RECREATION_SYSTEM_V1.md
git grep -n "the permission is not yet declared" -- docs ':!docs/sameview_prompts' ':!docs/deinwackelbild/prompts'
git grep -n "Status note, added 2026-09-21" -- docs/GPS_RECREATION_SYSTEM_V1.md
```

Also:

- verify the pre-existing eight-doc diff is unchanged from the before-state;
- verify `docs/GPS_RECREATION_SYSTEM_V1.md` still contains no CR characters;
- confirm the GPS diff is only the approved status-note addition.

Do NOT run:
- Gradle;
- unit tests;
- instrumentation tests;
- lint;
- assemble;
- clean;
- release/bundle;
- device validation;
- API calls.

## Required final report

Return:

1. branch/HEAD and final `git status --short`;
2. exact file modified in this step;
3. exact wording/status note added;
4. confirmation the original historical sentence was preserved;
5. confirmation GPS Recreation behavior/privacy contract remains unchanged;
6. `git diff --check` result;
7. grep results and why the remaining historical match is now safely superseded;
8. confirmation the previous eight-doc diff is unchanged;
9. confirmation no other files were modified by this step;
10. confirmation no builds/tests/device/API work was run;
11. confirmation nothing was staged, committed or pushed;
12. remind that the stale `WackelbildViewModel.kt` INTERNET comment remains a separate tiny follow-up.

If any second file appears necessary, STOP without changing it and report why.
