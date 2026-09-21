# Claude Prompt — DeinWackelbild Release / Privacy Consistency V1 — STEP 3 IMPLEMENTATION

## Approval

STEP 2 scope is approved exactly as confirmed.

This iteration is documentation-only and modifies exactly these eight Android-repository files:

1. `docs/CLAUDE_PROJECT_INSTRUCTION.md`
2. `docs/IMPLEMENTATION_NOTES.md`
3. `docs/RELEASE_HARDENING_AUDIT_V2.md`
4. `docs/SESSION_BACKUP_EXPORT_V1.md`
5. `docs/VIDEO_EXPORT_V1.md`
6. `docs/SHARE_COMPARISON_IMAGE_V1.md`
7. `docs/ABOUT_SCREEN.md`
8. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

Implement only the STEP 2 wording decisions. No code, strings, manifest, Gradle, tests, release-folder, website or Play Console changes.

## Mandatory pre-check

Before editing:

1. Run `git status --short`.
2. Confirm branch is still `main`.
3. Confirm HEAD is still `b2ed20b feat(wackelbild): match transfer output to print formats` unless the user has independently advanced it.
4. Confirm the working tree is clean.
5. Re-read the exact current passages in all eight approved files.
6. If unexpected tracked changes exist, STOP before editing.

## Governing rule

Preserve the narrow privacy model:

- normal/local SameView functionality remains offline;
- Hosted Comparison and DeinWackelbild V1 are the approved narrow online exceptions;
- DeinWackelbild networking is optional, explicit and user-initiated;
- after the user starts the order flow, exactly two prepared metadata-free images are transferred to DeinWackelbild.de and its checkout is opened;
- `INTERNET` exists for the implemented DeinWackelbild V1 flow;
- no analytics, telemetry, tracking or background/automatic upload is introduced;
- feature-specific offline guarantees remain intact.

Do not make claims about partner retention.

## Exact edits

### 1. `docs/CLAUDE_PROJECT_INSTRUCTION.md`

Implement the confirmed minimal consistency changes:

- Current governing statement around line ~223:
  replace the claim that Hosted Comparison is the **sole** approved exception with wording that identifies both Hosted Comparison and DeinWackelbild V1 as narrow, explicit, user-initiated online capabilities and states that normal SameView use remains governed by the offline/privacy rules.
- Around ~225:
  change “beyond the approved Hosted Comparison exception” to cover both approved exceptions.
- Preserve the historical statements around ~827 and ~903. Do NOT rewrite history.
  Append concise dated status notes:
  - the earlier statement was true at that historical point;
  - `INTERNET` was later declared for DeinWackelbild V1;
  - at the later point, Block 10 declared it and Block 11 made the order flow user-reachable.
- For the older scoped list around ~700/~708:
  keep the feature-specific list items unchanged and add only the narrow note confirmed in STEP 2 explaining that the separate approved DeinWackelbild V1 exception does not invalidate those Share/Video/backup guarantees.
- Leave the normal/local “No network calls / No uploads / Fully offline by default” rules unchanged where their existing preface already scopes them correctly.

Do not broadly rewrite the governing privacy section.

### 2. `docs/IMPLEMENTATION_NOTES.md`

Update only current-state ledger statements:

- Replace the stale current INTERNET/user-triggerability statement with the actual state:
  - declared in Block 10 for DeinWackelbild V1;
  - used only by the explicit order flow;
  - user-reachable since Block 11;
  - Hosted Comparison remains approved but not implemented;
  - no background networking, analytics or tracking.
- Replace the stale “Manifest declares CAMERA only” current-state statement with the actual current permission list confirmed in STEP 2:
  - CAMERA
  - ACCESS_FINE_LOCATION
  - ACCESS_COARSE_LOCATION
  - ACCESS_MEDIA_LOCATION
  - INTERNET
- Remove the stale “not yet user-triggerable” current-state clause.
- Replace the stale blanket “No analytics, telemetry, tracking, upload, or network feature is implemented” statement with the narrow accurate statement:
  - no analytics, telemetry, tracking or background/automatic upload;
  - the only upload is the explicit user-initiated DeinWackelbild transfer of two prepared metadata-free images.

Preserve dated Blocks 7–9 historical entries unchanged.

### 3. `docs/RELEASE_HARDENING_AUDIT_V2.md`

Preserve this document as a historical audit snapshot.

Do NOT rewrite the original item 15 evidence.

- Add the confirmed dated table-of-contents entry for:
  `10. Nachtrag (2026-09-21): INTERNET-Permission`
- Add a concise pointer at item 15 stating that the INTERNET statement reflects the 2026-07-08 audit state and is superseded for current behavior by section 10.
- Add section 10 before the colophon, in German, recording only:
  - `INTERNET` has been declared since DeinWackelbild Block 10 for that V1 feature;
  - current network use is the explicit user-initiated order transfer;
  - no analytics/tracking was introduced;
  - the old P-05 location evidence can no longer rely on absence of INTERNET and instead rests on the current specification/implementation;
  - Privacy Policy and Play Data Safety require re-review for this new network category and are not resolved by this Android-doc task.

Do not mark the external compliance follow-ups as completed.

### 4. `docs/SESSION_BACKUP_EXPORT_V1.md`

In §12.1:
- preserve the statement that backup export itself makes no network calls;
- replace only the false app-wide “INTERNET permission is not declared/used” statement;
- state that backup export itself remains offline while the app declares INTERNET solely for the separately approved DeinWackelbild V1 feature.

Leave other feature-scoped offline statements unchanged.

### 5. `docs/VIDEO_EXPORT_V1.md`

- §23.1: same narrow correction as backup export — video export remains offline, while the app separately declares INTERNET for DeinWackelbild V1.
- Around line ~26: scope “the app makes no uploads” to this feature.
- Around line ~50: scope “The app makes no network calls” to Video export.
- Leave §23.3/§23.4 unchanged.

### 6. `docs/SHARE_COMPARISON_IMAGE_V1.md`

Around line ~29:
- change the app-wide wording from “Fully offline: no network calls; the app makes no uploads” to the feature-scoped equivalent:
  `Fully offline: this feature makes no network calls and no uploads.`
- Preserve §18.3 unchanged.

### 7. `docs/ABOUT_SCREEN.md`

Do not change shipped UI strings.

- In §7, preserve the original historical/spec list but add the dated correction note confirmed in STEP 2:
  - “Photos stay on your device” / “stays local/offline” must not later be implemented verbatim as app-wide claims;
  - the optional explicit DeinWackelbild order flow transfers two prepared images;
  - future trust wording must be feature-scoped and accurate;
  - “No tracking” and “No cloud sync” remain valid.
- In §18, change only “that it stays local/offline” to “that its normal use is local/offline”.

### 8. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

- §27 around ~1308:
  replace the incorrect prediction that the existing compliance section already anticipated this exception and no edit would be needed.
  Record that the earlier section named only Hosted Comparison and that a minimal documentation consistency pass became necessary and was performed on 2026-09-21.
- §28 row around ~1325:
  replace “No further change planned…” with the confirmed status:
  `Minimal consistency edit made 2026-09-21 (exception wording and INTERNET-status notes); nothing else changed.`
- Preserve all other implementation history.

## Explicit exclusions

Do NOT modify:

- any Kotlin/Java source;
- `WackelbildViewModel.kt`, including its newly noticed stale code comment;
- `AndroidManifest.xml`;
- Gradle/build configuration;
- `strings.xml` / `values-de/strings.xml`;
- any test;
- Wackelbild UI/disclosure;
- networking/API/key injection;
- Camera/Compare/session/rendering behavior;
- `RELEASE_HARDENING_AUDIT_V1.md`;
- `GPS_RECREATION_SYSTEM_V1.md`;
- `SETTINGS_UX_V1.md`;
- `DEINWACKELBILD_INTEGRATION_V1.md`;
- any historical implementation plan not listed above;
- `sameview-release`;
- SameView website;
- Play Console.

The stale comment in `WackelbildViewModel.kt` is a separate later issue because this iteration is docs-only. Do not silently include it.

## Verification — no Gradle

After editing, run only documentation/repository checks:

```powershell
git status --short
git diff --stat
git diff --check
git diff -- docs/
git grep -n -i -E "sole approved exception|not yet declared|INTERNET.{0,20}permission is not declared|not yet user-triggerable|currently user-triggerable|Manifest declares CAMERA only|Photos stay on your device|makes no uploads" -- docs ':!docs/sameview_prompts' ':!docs/deinwackelbild/prompts'
git grep -n -i -E "fully offline|no uploads" -- docs ':!docs/sameview_prompts' ':!docs/deinwackelbild/prompts'
```

Review residual matches manually.

Expected legitimate residual categories include:
- preserved dated historical statements with superseding status notes;
- genuinely feature-scoped offline statements;
- unrelated historical/feature-specific matches.

Do NOT mechanically eliminate every grep result.

Do NOT run:
- Gradle;
- unit tests;
- instrumentation tests;
- lint;
- assemble;
- clean;
- release/bundle.

No real-device validation is required.

## Separate follow-ups — report only, do not modify

At the end, retain a concise follow-up checklist for:

### `sameview-release`
- `04_PlayConsole/DataSafety.txt`
- internal EN/DE release notes
- EN/DE store descriptions
- `06_Legal/PrivacyPolicy_*.txt`
- release checklist / Data Safety completion state

### SameView website
- `/de/privacy`
- `/en/privacy`
- relevant terms/AGB
- partner/commission disclosure if applicable

### Play Console
- Data Safety treatment of the optional third-party photo transfer
- purpose / optionality / sharing/collection classification
- encryption in transit
- store-listing device-only claim
- privacy-policy URL/content
- current live form state

Do not make legal conclusions in this implementation task.

## Required final report

Return:

1. final branch/HEAD and `git status --short`;
2. exact eight files modified;
3. concise per-file summary of the implemented wording changes;
4. confirmation historical statements were preserved where required;
5. `git diff --check` result;
6. grep results categorized into legitimate residual matches versus any unresolved current contradiction;
7. confirmation no Kotlin/manifest/Gradle/strings/tests were modified;
8. confirmation no Gradle/test/build/device/API work was run;
9. separate remaining `sameview-release` follow-up;
10. separate remaining website follow-up;
11. separate Play Console manual follow-up;
12. explicitly note the stale `WackelbildViewModel.kt` INTERNET comment remains untouched and requires a separate tiny code-comment task if we choose to fix it;
13. confirmation nothing was staged, committed or pushed.

If implementation reveals that any ninth Android-repository file must be changed, STOP before changing it and report why.
