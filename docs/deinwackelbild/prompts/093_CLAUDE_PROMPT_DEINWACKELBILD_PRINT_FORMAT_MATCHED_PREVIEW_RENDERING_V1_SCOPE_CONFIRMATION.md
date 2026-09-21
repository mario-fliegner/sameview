# Claude Prompt — DeinWackelbild Print-Format-Matched Preview & Rendering — STEP 2 SCOPE CONFIRMATION

## Workflow state

STEP 1 analysis from prompt 092 is complete.

The two blocking product decisions are now explicitly approved:

1. **Source-of-Truth amendment approved:** the Wackelbild-specific composition is allowed to use a centered print-format crop. The previous Wackelbild-specific "full session frame / never cropped" contract must be amended accordingly.
2. **Canonical format policy approved:**
   - 2:3 family -> `10x15`
   - A-series family -> `a6`
   - 3:4 family -> `15x20`
   - 1:1 family -> `15x15`

The earlier temporary `18x24` policy is superseded.

This prompt is **STEP 2 only**. Do not implement anything.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and all relevant Source-of-Truth documents strictly.

## Approved product behavior

The final intended flow is:

`SameView session geometry`
-> select nearest supported DeinWackelbild ratio family
-> select canonical format slug
-> calculate one centered print crop
-> show exactly that crop in the SameView Wackelbild preview
-> render both transfer JPEGs using exactly that crop
-> draw the optional date badge inside that cropped output
-> hand off the same format slug + orientation + `direction=horizontal`
-> DeinWackelbild receives images already matching the selected print aspect ratio.

No user-facing format selector or crop/reframe control is added.

This crop is Wackelbild-specific only. It must not modify:
- session metadata;
- `reference.jpg`;
- `capture.jpg`;
- Camera;
- normal Compare;
- originals/export originals;
- unrelated share/export features.

## Approved crop semantics

Use one deterministic centered crop in shared session/frame coordinates.

No stretching.
No letterboxing.
No independent reference/capture crop decisions.

For portrait 9:16 -> portrait 2:3:
- crop the long vertical axis equally at top and bottom.

For landscape 16:9 -> landscape 3:2:
- crop the long horizontal axis equally at left and right.

Reference and capture must receive the same normalized/session-space crop.

## Approved family selection

Use orientation-independent:

`r = shortSide / longSide`

Choose the family with the smallest **relative cover-crop loss** among the canonical targets:

- 2:3 -> `10x15`
- A6's actual ratio 10.5 / 14.9 -> `a6`
- 3:4 -> `15x20`
- 1:1 -> `15x15`

The previous analysis calculated approximate boundaries around:
- 2:3 / A6: 0.6854
- A6 / 3:4: 0.7270
- 3:4 / square: 0.8660

In STEP 2, verify the exact deterministic comparison/maths that should be implemented rather than hard-coding rounded boundary literals if direct error comparison is simpler and safer.

Expected mappings include:
- 9:16 / 16:9 -> `10x15`
- 2:3 -> `10x15`
- A-series-like -> `a6`
- 3:4 / 4:3 -> `15x20`
- 4:5 / 5:4 -> `15x20`
- square -> `15x15`

## Mandatory pre-check

Before defining scope:

1. Run `git status --short`.
2. Re-read the current working-tree versions of the files already modified by the uncommitted previous implementation.
3. Read the relevant current Source-of-Truth sections identified in STEP 1:
   - `CLAUDE_PROJECT_INSTRUCTION.md`
   - `CAMERA_WORKFLOW_UX_V1.md`
   - `COMPARE_SESSION_RENDERING_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
   - `docs/IMPLEMENTATION_NOTES.md`
   - any directly governing Wackelbild/date spec.
4. Treat the existing 7 modified files from the previous implementation as the current baseline, not as unrelated changes.
5. Leave all untracked prompt files untouched.

If there are any unexpected tracked changes beyond that known baseline, STOP and report.

## STEP 2 task

Produce the exact minimal implementation scope.

### Shared target model/helper

STEP 1 proposed a small pure `WackelbildPrintTarget` helper/model under `image/wackelbild/`.

Confirm the smallest API it needs to expose for:
- family/slug selection;
- orientation-aware target aspect;
- centered crop rectangle;
- preview sizing;
- renderer verification.

Do not over-engineer it.

The selection should happen once from stable session geometry before CTA and the same selected target should flow through preview, renderer and handoff.

Avoid independently re-selecting a family from rounded output JPEG dimensions.

### ViewModel/state flow

Confirm exactly:
- how `readSessionViewport(sessionDir)` is used;
- where target selection occurs;
- what state is exposed to `WackelbildScreen`;
- how the same target reaches `WackelbildPrintRenderer`;
- how the same target reaches `WackelbildHandoffOrchestrator`.

Preserve existing lifecycle/state/navigation behavior.

### Preview

Define the exact minimal changes in `WackelbildScreen.kt`.

The preview must display the selected target crop, not the full session frame.

Confirm:
- target box aspect;
- `ContentScale.Crop` or equivalent;
- both images use identical geometry;
- date badge remains anchored inside target box;
- ridge overlay, clip, border, perspective, gesture and blend operate on the target box unchanged.

No new labels, controls, explanatory copy or format selector.

### Renderer

Define the exact crop insertion point.

The crop must happen before `DateBadgeRenderer.draw`, so the date is positioned relative to the final print output.

Confirm:
- HQ path;
- fallback path;
- size-loop;
- bitmap ownership/recycling;
- file-size behavior;
- identical output dimensions;
- no source-file mutation.

Explicitly assess the STEP 1 memory-risk note that cropping may temporarily hold both full and cropped bitmaps. If a minimal implementation can avoid unnecessary duplicate peak memory without architectural change, describe it. Do not introduce an unrelated optimization project.

### Handoff

The previous uncommitted implementation currently derives format from output geometry and uses `18x24` for 3:4.

Scope the replacement so:
- `format` comes from the already-selected `WackelbildPrintTarget.slug`;
- `orientation` remains consistent with the final rendered output;
- square omits orientation;
- `direction = horizontal`;
- the final rendered JPEG ratio is verified against the selected target within a minimal rounding allowance;
- no independent family re-selection remains in the orchestrator.

Do not add retry-without-format behavior.

### Date

Do not redesign date rendering.

Scope only what is required so:
- preview badge is inside final crop;
- rendered badge is drawn after crop;
- both represent the same final composition.

### Failure behavior

Scope the agreed safe behavior:
- unknown/invalid session geometry -> `target=null`, preserve current full-frame preview/render and omit `format`;
- if a target was shown in preview but renderer cannot produce matching target output -> fail locally using existing failure behavior rather than send mismatched images;
- partner rejects selected slug -> existing non-retryable failure; no silent fallback to another crop.

Confirm whether target resolution should wait before preview display to avoid a full-frame-to-cropped visual flash.

## Source-of-Truth amendments

The product decision explicitly approves changing the Wackelbild-specific "never cropped" contract.

Identify the exact sections/wording that must change, limited to what becomes false/incomplete.

At minimum inspect the STEP 1 findings:
- Integration §7
- §16 / §16.2 if applicable
- §17
- §20 if applicable
- §32
- §48-4
- Plan §6.4
- Plan §9
- Plan §9.2
- `IMPLEMENTATION_NOTES.md`

Preserve the important distinction:
- stored SameView session images remain uncropped and authoritative source material;
- only the Wackelbild print presentation/temp transfer output uses the selected print crop.

Do not alter `CLAUDE_PROJECT_INSTRUCTION.md` unless you find a direct contradiction not already ruled out in STEP 1.

## Existing previous implementation

STEP 3 of the earlier narrow solution already modified:
- `DeinWackelbildDtos.kt`
- `WackelbildHandoffOrchestrator.kt`
- `OkHttpDeinWackelbildApiClientTest.kt`
- `WackelbildHandoffOrchestratorTest.kt`
- integration spec
- implementation plan
- implementation notes

Do not blindly add more changes.

For each of those, state whether the existing modification:
- remains unchanged;
- is adjusted;
- is superseded/removed.

In particular:
- DTO support for `format`, `orientation`, `direction` should remain if still correct.
- DTO serialization tests should remain if still correct.
- the old 3:4/`18x24` orchestrator mapping must be replaced.
- docs must no longer describe `18x24` as the chosen 3:4 policy.

## Test scope

Define the smallest relevant later verification.

Expected affected tests may include:

### Pure unit
- new `WackelbildPrintTargetTest`
  - family centers;
  - exact comparison boundaries/ties;
  - 9:16/16:9 -> 10x15;
  - A-like -> a6;
  - 3:4 -> 15x20;
  - 4:5 -> 15x20;
  - square -> 15x15;
  - portrait/landscape same family;
  - centered crop rectangles.

### Existing unit
- `WackelbildHandoffOrchestratorTest`
  - receives selected target slug;
  - orientation;
  - horizontal direction;
  - square;
  - null target;
  - rendered-ratio verification;
  - restart reuse.

- `WackelbildViewModelTest`
  - target resolved once from session geometry;
  - same target passed into rendering/handoff state flow;
  - null target fallback.

### Instrumented
- `WackelbildPrintRendererInstrumentedTest`
  - target output ratio;
  - center crop;
  - same crop for reference/capture;
  - portrait/landscape;
  - HQ and fallback;
  - date drawn inside final cropped output;
  - source session files unchanged.

- `WackelbildScreenTest`
  - target aspect is displayed;
  - preview uses final crop semantics;
  - date remains inside target preview;
  - existing blend/ridge/tilt behavior not regressed where relevant.

Do not prescribe unrelated test classes.

Do not run tests in STEP 2.

For STEP 3, propose exact targeted Gradle commands. Do not default to the full suite.

## Real-device validation

Keep later manual validation focused:

1. Real 9:16 portrait session:
   - SameView preview visibly cropped to 2:3;
   - date, when enabled, inside preview;
   - partner opens `10x15`, portrait, Seitlich kippen;
   - partner composition visually matches SameView.

2. Real 16:9 landscape session:
   - SameView preview visibly cropped to 3:2;
   - partner opens `10x15`, landscape, Seitlich kippen;
   - composition matches.

3. Real 3:4 session if readily available:
   - SameView preview 3:4;
   - partner opens `15x20`.

Do not require a production purchase.

## Risks to highlight

At minimum address:
- center crop removes about 7.8% from each end of the long axis for 9:16 -> 2:3;
- no user reframing control;
- preview/renderer drift risk;
- memory peak from bitmap crop;
- partner's small inner-preview inset;
- retired/changed partner slug;
- fallback path consistency;
- date safe margin;
- accessibility/Compose geometry regression;
- release stability.

## Required STEP 2 output

Return:

1. current `git status --short`;
2. relevant Source-of-Truth amendments/conflicts;
3. exact production files to modify;
4. exact test files to modify/add;
5. exact docs to modify;
6. precise change per file;
7. minimal `WackelbildPrintTarget` API/model;
8. exact family-selection algorithm and tie behavior;
9. exact centered-crop math;
10. exact ViewModel/state flow;
11. exact preview changes;
12. exact renderer changes including bitmap/recycling implications;
13. exact handoff changes;
14. exact date-geometry behavior;
15. failure/null-target behavior;
16. treatment of every file already modified by the previous narrow implementation;
17. smallest targeted STEP 3 test commands/classes;
18. real-device validation still required;
19. regression risks;
20. confirmation Camera/Compare/stored session/originals/unrelated exports remain untouched;
21. confirmation no unrelated code/docs will be touched;
22. confirmation STEP 2 modified nothing and nothing was staged/committed/pushed.

Then STOP and wait for explicit approval.

Do not implement, edit, run tests, stage, commit or push in STEP 2.
