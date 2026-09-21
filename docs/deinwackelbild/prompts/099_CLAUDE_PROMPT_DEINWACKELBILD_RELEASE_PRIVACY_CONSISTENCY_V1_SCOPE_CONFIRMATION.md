# Claude Prompt — DeinWackelbild Release / Privacy Consistency V1 — STEP 2 SCOPE CONFIRMATION

## Approved direction

STEP 1 established:

- the shipped Android behavior itself is consistent;
- the current DE/EN pre-transfer disclosure is adequate under the existing project contract;
- the manifest, partner integration and key injection do not require changes;
- the Android repository contains stale Source-of-Truth documentation that still states or implies no INTERNET permission / no upload / only one network exception;
- separate `sameview-release`, website and Play Console work exists but is OUT OF SCOPE for this Android-repository iteration.

This STEP 2 is therefore limited to **Android-repository documentation consistency only**.

Do NOT implement anything yet.

## Mandatory baseline check

1. Run `git status --short`.
2. Report branch and HEAD.
3. Confirm HEAD still contains the committed, validated Wackelbild print-format/date-margin work.
4. Confirm the working tree is clean before this documentation task.
5. If unexpected tracked changes exist, STOP.

## Mandatory Source-of-Truth re-read

Read the current versions of every proposed documentation file before confirming scope:

- `docs/CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/IMPLEMENTATION_NOTES.md`
- `docs/RELEASE_HARDENING_AUDIT_V2.md`
- `docs/SESSION_BACKUP_EXPORT_V1.md`
- `docs/VIDEO_EXPORT_V1.md`
- `docs/SHARE_COMPARISON_IMAGE_V1.md`
- `docs/ABOUT_SCREEN.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

Also inspect any exact cross-references needed to avoid creating contradictions.

## Governing wording principle

The documentation must preserve SameView's privacy/offline positioning accurately and narrowly:

- SameView's normal/local functionality remains offline.
- DeinWackelbild is an explicit, optional, user-initiated network exception.
- Only after the user starts the order flow, two prepared metadata-free images are transferred to DeinWackelbild.de and its checkout is opened.
- `INTERNET` permission exists for this approved feature.
- No analytics, telemetry or tracking is introduced by this feature.
- Do NOT turn this into a broad claim that SameView generally uploads user data.
- Do NOT weaken feature-specific offline guarantees that remain true.
- Do NOT claim facts about partner retention that are not established by the Android implementation/spec.

Use existing project terminology wherever possible.

## Required scope decisions

For every candidate below, decide whether it MUST be modified in this task, SHOULD be modified for consistency, or should remain untouched.

### 1. `docs/CLAUDE_PROJECT_INSTRUCTION.md`

STEP 1 identified stale statements around:
- lines ~223/225: Hosted Comparison described as the “sole” approved exception despite the later DeinWackelbild addendum defining a second exception;
- lines ~827/903: INTERNET permission described as not yet declared/used;
- lines ~700/708: older exception wording that may carve out only Hosted Comparison.

Determine the smallest exact amendments needed so the document consistently recognizes both approved network exceptions without weakening the normal offline/privacy rules.

Historical context should be preserved where appropriate; current normative statements must be accurate.

### 2. `docs/IMPLEMENTATION_NOTES.md`

STEP 1 identified stale statements around:
- line ~31: no user-triggerable DeinWackelbild network call because Block 11 was not wired;
- lines ~385–387: CAMERA-only manifest / no upload/network feature.

Determine whether these are current-state notes or historical snapshots. Amend only current-state statements that are now false. Preserve historical records when clearly dated/contextualized rather than rewriting history.

### 3. `docs/RELEASE_HARDENING_AUDIT_V2.md`

STEP 1 identified audit item #15 saying there is no INTERNET permission.

The DeinWackelbild plan already expects a release-hardening re-review/addendum.

Determine the smallest correct treatment:
- update the current audit result if the document is normative/current, or
- append a clearly dated superseding addendum if preserving the original audit snapshot is important.

Do not silently rewrite historical audit evidence if an addendum is the more accurate documentation pattern.

### 4. `docs/SESSION_BACKUP_EXPORT_V1.md`

STEP 1 found §12.1 states that INTERNET permission is not declared or used.

Determine whether the statement is intended to describe:
- the backup-export feature specifically, or
- the whole app.

Preserve the feature's offline guarantee while removing only any now-false app-wide permission claim.

### 5. `docs/VIDEO_EXPORT_V1.md`

STEP 1 found:
- §23.1 says INTERNET permission is not declared/used;
- earlier “fully offline / no network calls / no uploads” wording may read app-wide.

Preserve the fact that VIDEO EXPORT itself remains offline. Reword only app-wide implications that became false.

### 6. `docs/SHARE_COMPARISON_IMAGE_V1.md`

STEP 1 found “fully offline / no network calls / app makes no uploads” wording that may overstate beyond this feature.

Determine whether a narrow wording correction is needed so Share Comparison Image remains explicitly offline without falsely describing the entire app.

### 7. `docs/ABOUT_SCREEN.md`

STEP 1 found spec-only trust statements such as:
- “Photos stay on your device”;
- “stays local/offline”.

The current shipped About screen does NOT render those statements.

Determine the minimal Source-of-Truth correction needed so these stale statements cannot later be implemented verbatim as false app-wide claims.

Do NOT change actual Android strings or UI.

### 8. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

STEP 1 found §27/§28 wording saying the existing privacy/play section already anticipated the exception and no further edit was expected, although that earlier section only described Hosted Comparison.

Correct only this stale planning statement so the plan reflects that the compliance consistency pass became necessary.

Do not rewrite completed implementation history.

## Explicitly OUT OF SCOPE

Do not modify:
- Kotlin/Java code;
- `AndroidManifest.xml`;
- Gradle/build configuration;
- `strings.xml` / `values-de/strings.xml`;
- Wackelbild UI/disclosure;
- tests;
- API/network behavior;
- key injection;
- camera/Compare/session behavior;
- print/crop/date rendering.

Also do NOT modify or attempt to modify:
- the external/local `sameview-release` folder;
- SameView website repository/content;
- Play Console declarations.

Those are separate follow-up tasks.

## Important release-folder finding to preserve for follow-up

Do not fix these now, but include them in the STEP 2 report as explicit separate follow-up scope:

- `sameview-release/04_PlayConsole/DataSafety.txt` contains stale “fully offline / no INTERNET / no network / all data remains on device” claims.
- internal release notes contain “no Internet permission / nothing uploaded”.
- store descriptions contain potentially misleading device-only storage wording.
- release privacy-policy copies require a separate content review.
- live Play Data Safety state cannot be determined from the Android repo.
- SameView website `/de/privacy`, `/en/privacy` and relevant terms require separate website-project verification.

## STEP 3 verification discipline

Because this task should be documentation-only:

- no Gradle build;
- no unit tests;
- no instrumentation tests;
- no real-device validation.

Verification should consist only of:
1. focused diff review;
2. repository search for residual stale CURRENT claims, including:
   - `sole approved exception`
   - `not yet declared`
   - `INTERNET permission is not declared`
   - `not yet user-triggerable`
   - app-wide `no uploads`
   - app-wide `fully offline`
3. distinguish intentionally preserved historical statements from active/normative contradictions.

Do not “fix” every textual match mechanically.

## Required STEP 2 output

Return:

1. branch, HEAD and `git status --short`;
2. confirmation that this is docs-only;
3. for EACH of the eight candidate docs above:
   - MUST / SHOULD / NO CHANGE;
   - exact section/line;
   - exact nature of the minimal wording change;
   - whether historical wording is preserved or superseded;
4. final exhaustive list of files proposed for modification;
5. files explicitly NOT modified;
6. confirmation Android strings/UI/manifest/code/tests remain untouched;
7. exact STEP 3 verification commands/searches, with no Gradle tasks;
8. separate follow-up list for `sameview-release`;
9. separate follow-up list for SameView website;
10. separate Play Console manual re-check items;
11. risks of the documentation-only change;
12. confirmation STEP 2 modified nothing, ran no tests/builds/API calls, and staged/committed/pushed nothing.

Then STOP and wait for explicit approval.

Do not implement any documentation edits in STEP 2.
