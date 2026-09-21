# Claude Prompt — DeinWackelbild Print-Format-Matched Preview & Rendering — STEP 3 IMPLEMENTATION

## Approval
STEP 2 is explicitly approved. Use the **recommended renderer-lambda arity change**. Do not use the closure alternative. Implement exactly the approved scope from prompts 092/093 and the STEP 2 response. No additional fixes/refactors.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and all relevant Source-of-Truth documents.

## Pre-check
Before editing:
1. Run `git status --short`.
2. Confirm exactly the known baseline: 7 previously modified tracked files plus untracked prompt files, no unexpected tracked changes.
3. Re-read every file to be modified and the relevant Source-of-Truth sections.
4. If unexpected tracked changes exist, STOP.

Preserve the existing uncommitted handoff work; supersede only the narrow 3:4/`18x24` behavior.

## 1. Shared pure print target
Create:
`app/src/main/java/com/isardomains/sameview/image/wackelbild/WackelbildPrintTarget.kt`

Pure Kotlin, no Android dependency. Implement the minimal STEP-2 model:
- `WackelbildCropRect`
- `WackelbildPrintTarget`: slug, shortOverLong, selected frame width/height, orientation-aware aspect, centered `cropRect(width,height)`, `matchesOutput(width,height)`, and `select(frameWidth,frameHeight)`.

Canonical families in fixed order:
- 2:3 -> `10x15`
- A6 actual ratio 10.5/14.9 -> `a6`
- 3:4 -> `15x20`
- 1:1 -> `15x15`

Selection:
`r = min(width,height)/max(width,height)`.
Compare `max(r,t)/min(r,t)` with strict `<`; fixed table order is formal tie-break. Do not hard-code rounded boundaries. Invalid/non-positive dimensions -> null.

Centered crop:
- portrait target aspect `A=t`;
- landscape `A=1/t`;
- square remains square;
- crop excess long-axis content only;
- no stretch/letterbox;
- use approved even-dimension handling;
- center crop.

Expected:
1080×1920 at 2:3 -> `(0,150,1080,1620)`.
1920×1080 at 2:3 -> `(150,0,1620,1080)`.

Use the minimal output-ratio rounding tolerance agreed in STEP 2.

## 2. Resolve target once in WackelbildViewModel
Resolve once from stable session geometry before CTA:
- `readSessionViewport(sessionDir)`;
- verify reference/capture geometry as specified in STEP 2;
- invalid/untrusted -> `Resolved(null)`;
- otherwise `WackelbildPrintTarget.select(viewport)`.

Expose `Pending` and `Resolved(target?)`. Target stays fixed for ViewModel lifetime. While Pending, do not flash the old full-frame preview. `startOperation` must use/await the exact target displayed by preview.

Do not mutate session metadata or persisted images.

## 3. Same target through operation
Implement the approved renderer-lambda **arity change**.

Pass the exact `WackelbildPrintTarget?`:
`ViewModel -> Orchestrator -> Renderer`.

Do not re-select from final JPEG dimensions. Change only necessary lambda/call sites/tests.

## 4. Preview = final print crop
Modify `WackelbildScreen.kt`.

For non-null target:
- preview box uses target orientation-aware aspect;
- reference and capture use identical centered crop semantics;
- `ContentScale.Crop`, centered.

For null target preserve existing full-frame behavior.

Do not change alpha blend, tilt mapping/calibration/hysteresis, ridges, perspective constants, gestures, semantics/accessibility, styling, date toggle, navigation, CTA, or wording. Date badge remains anchored inside target box.

## 5. Crop transfer output before date
Modify `WackelbildPrintRenderer.kt`.

Add approved nullable target parameter. Apply centered crop at the shared insertion point before `DateBadgeRenderer.draw`.

Requirements:
- HQ and fallback obey target;
- exact same crop geometry for reference/capture;
- no per-image selection;
- final pair dimensions identical;
- no source mutation;
- date positioned against final cropped output;
- existing JPEG/file-size loop and size resolution unchanged;
- preserve OOM/fallback behavior.

Use the minimal bitmap ownership/recycling approach from STEP 2. No region-decode or broader optimization. Dimension inconsistency must use existing preparation/fallback failure behavior, never silently change composition.

## 6. Handoff
Modify `WackelbildHandoffOrchestrator.kt`.

Preserve existing correct optional request fields, dimension-reader seam, orientation derivation, horizontal direction, and once-built request/restart behavior.

Remove:
- `PRINT_FORMAT_18X24`;
- 3:4-only tolerance/helper;
- family selection from output dimensions.

Use:
- non-null target -> `format=target.slug`;
- null -> omit format;
- orientation from rendered dimensions;
- square -> omit orientation;
- `direction=horizontal`;
- non-null target requires readable matching pair dimensions and `target.matchesOutput(...)`;
- mismatch -> existing local `PREPARATION_FAILED` before network.

No retry without format or alternative format.

## 7. Date
No date styling/source/text/visibility/margin/toggle changes. Preview badge stays in target box; rendered badge is drawn after crop.

## 8. Failure/null behavior
Unknown/invalid target -> current full-frame preview/transfer, no format.
Target shown but renderer cannot honor it -> local existing preparation failure; do not upload mismatch.
Partner rejects slug -> existing non-retryable failure; no silent fallback.

## Documentation
Update only approved contracts.

### `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
Amend affected §3.5, §7, §16.1, §16.2, §17 (and §17.1 if appropriate), §20, §32, §44, §48-4, §56 flow.

Explicitly preserve:
- stored `reference.jpg`/`capture.jpg` remain unchanged source material;
- only Wackelbild preview/temp outputs use deterministic print crop;
- preview and transfer use same target;
- canonical families: `10x15`, `a6`, `15x20`, `15x15`;
- date relative to final print output;
- null target preserves full-frame behavior.
Remove/supersede `18x24` policy.

### `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
Update only affected §6.4, relevant §9 statements, §9.2, §14.1, §21 matrix. Keep mismatch protections.

### `docs/IMPLEMENTATION_NOTES.md`
Rewrite the current uncommitted narrow handoff entry into one coherent final entry; do not append contradictory history.

Do not modify `CLAUDE_PROJECT_INSTRUCTION.md` unless a new direct contradiction is discovered; if so STOP first.

## Previous narrow implementation
- `DeinWackelbildDtos.kt`: leave existing modifications unchanged.
- `OkHttpDeinWackelbildApiClientTest.kt`: unchanged.
- `WackelbildHandoffOrchestrator.kt`: adjust as above.
- `WackelbildHandoffOrchestratorTest.kt`: keep useful request/reader/restart coverage; replace 3:4/18x24 tests with target-driven coverage.
- Docs: rewrite affected sections only.

## Tests to add/update
JVM only:
- new `WackelbildPrintTargetTest`;
- `WackelbildHandoffOrchestratorTest`;
- `WackelbildViewModelTest`.

Cover 9:16/16:9 -> 10x15; 2:3 -> 10x15; A-like -> a6; 3:4/4:3 -> 15x20; 4:5/5:4 -> 15x20; square -> 15x15; invalid -> null; crop rectangles; deterministic boundaries/tie rule; target flow; null target; ratio verification; orientation; square orientation omission; horizontal direction; restart/retry request reuse.

Instrumented only:
- `WackelbildPrintRendererInstrumentedTest`;
- `WackelbildScreenTest`.

Cover output ratio, center crop, identical pair crop, portrait/landscape, HQ/fallback, date inside final output, source files unchanged, target preview aspect/crop, null-target behavior, and relevant existing lenticular behavior.

Do not weaken assertions except where the newly approved contract intentionally changes them.

## Test execution
After implementation run ONLY:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.isardomains.sameview.image.wackelbild.WackelbildPrintTargetTest" --tests "com.isardomains.sameview.ui.wackelbild.WackelbildHandoffOrchestratorTest" --tests "com.isardomains.sameview.ui.wackelbild.WackelbildViewModelTest"
```

Then:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.image.wackelbild.WackelbildPrintRendererInstrumentedTest,com.isardomains.sameview.ui.wackelbild.WackelbildScreenTest
```

Do NOT run full unit/instrumentation suites, clean, lint, assemble, release or bundle. If targeted tests expose a concrete need to broaden scope, STOP and explain before doing so. Never suppress/disable failures.

## Manual validation after automated tests
Do not commit yet. Report required real-device checks:
1. 9:16 portrait -> SameView preview 2:3 centered crop, date inside, partner `10x15` portrait + Seitlich kippen, matching composition.
2. 16:9 landscape -> preview 3:2, partner `10x15` landscape + Seitlich kippen, matching composition.
3. 3:4 if readily available -> `15x20`.
4. Null-target/missing viewport if practical -> full-frame behavior.
No production purchase required.

## Scope exclusions
No unrelated refactor/rename/reformat. Do not alter Camera, normal Compare, stored sessions, originals, unrelated exports/sharing, UI controls, crop/reframe controls, date styling, ridge/tilt/perspective behavior, privacy/network behavior, analytics, permissions, API error classification, retry-without-format. Do not stage, commit or push.

## Required final report
Report:
1. final `git status --short`;
2. every modified/added file;
3. exact behavior per file;
4. one-target flow Preview -> Renderer -> Handoff;
5. canonical mapping;
6. crop semantics;
7. date behavior;
8. null-target behavior;
9. confirmation Camera/Compare/stored session/originals/unrelated exports untouched;
10. docs changes;
11. exact JVM command/results;
12. exact instrumentation command/results;
13. failures and actual fixes;
14. tests NOT run;
15. real-device validation still required;
16. known risks including partner inner-preview inset and bitmap-memory peak;
17. confirmation nothing staged/committed/pushed.

If implementation reveals a contradiction requiring scope expansion, STOP before making that extra change.
