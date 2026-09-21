# Claude Prompt — DeinWackelbild Transfer Date Safe Margin V1 — STEP 3 IMPLEMENTATION

## Approval
STEP 2 is explicitly approved.

Implement exactly the confirmed minimal fix:
- transfer-JPEG date badge edge margin: `8f / 360f` -> `12f / 360f`;
- same proportional margin for RIGHT and BOTTOM;
- same rule for portrait and landscape;
- SameView preview remains unchanged at `8.dp`;
- make only the approved small Source-of-Truth amendments.

No unrelated fixes/refactors.

## Mandatory pre-check
Before editing:
1. Run `git status --short`.
2. Confirm the known uncommitted baseline remains unchanged and nothing is staged.
3. Confirm `DateBadgeRenderer.kt` and `DateBadgeRendererTest.kt` are still clean against HEAD.
4. Re-read the current working-tree versions of every file in the approved scope.
5. If unexpected tracked changes exist, STOP.

## Production change
Modify only:

`app/src/main/java/com/isardomains/sameview/image/wackelbild/DateBadgeRenderer.kt`

Change exactly the transfer edge-margin constant:

```kotlin
internal const val EDGE_MARGIN_FRACTION = 8f / 360f
```

to:

```kotlin
internal const val EDGE_MARGIN_FRACTION = 12f / 360f
```

Update only the immediately relevant comment(s) so they no longer incorrectly imply that the transfer edge margin is derived from the preview's 8.dp value.

Do NOT change:
- text size;
- horizontal/vertical padding;
- corner radius;
- badge color/style;
- date text/source;
- drawing order;
- crop geometry;
- renderer logic;
- any other production file.

Do not introduce another margin constant, orientation-specific logic, per-axis values, configuration, state, or preview/transfer plumbing.

## Test change
Modify only:

`app/src/test/java/com/isardomains/sameview/image/wackelbild/DateBadgeRendererTest.kt`

Add one focused test that independently pins the intentional `12/360` transfer contract using literals rather than calculating expectations from `EDGE_MARGIN_FRACTION`.

Verify at minimum:

### Portrait 1080×1620
- edge margin = 36 px;
- badge rect right = 1044;
- badge rect bottom = 1584.

### Landscape 1620×1080
- edge margin = 36 px;
- badge rect right = 1584;
- badge rect bottom = 1044.

Verify right and bottom margins are equal in both orientations.

Do not alter unrelated existing tests or weaken assertions.

## Documentation

### `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
Make only the approved narrow wording changes:

- §9.3: document the deliberate exception that the transfer JPEG uses a slightly larger proportional date-badge edge safe margin than the preview.
- §9.6: preserve the consistent/proportional/same-orientation rule and document that the transfer margin is deliberately slightly larger to tolerate partner display/print inset/crop. Preserve visual equivalence in style, content, relative scale and final print composition.
- §20: remove wording that falsely requires exact edge-margin equality; preserve bottom-right anchoring and the intentional §9.6 safe-margin exception.

Do not broadly weaken WYSIWYG.

### `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
In §8.3 only:
- transfer edge margin becomes `0.0333 (12/360)`;
- explicitly state transfer-JPEG only;
- preview remains `8.dp`;
- same transfer rule for right/bottom and portrait/landscape;
- reason is the partner-side inset/crop;
- preserve “independently tuned but visually equivalent”.

Do not alter the preview's existing 8.dp statement.

### `docs/IMPLEMENTATION_NOTES.md`
Make only the two small approved edits to the current uncommitted Wackelbild entry:
- note transfer badge `12/360`, preview remains `8.dp`, reason and manual portrait/landscape validation;
- update the docs bullet to include the amended sections.

Do not add a separate contradictory history entry.

## Explicitly untouched
Do NOT modify:
- `WackelbildScreen.kt`;
- `WackelbildScreenTest.kt`;
- `WackelbildPrintRenderer.kt`;
- `WackelbildPrintRendererInstrumentedTest.kt`;
- print-target code/tests;
- ViewModel;
- orchestrator;
- DTO/API client;
- Camera;
- Compare;
- session/original files;
- networking/privacy;
- dispatcher injection;
- any unrelated code/docs.

## Verification
Run the smallest mandatory test only:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.isardomains.sameview.image.wackelbild.DateBadgeRendererTest"
```

If it passes, optionally run ONLY the two existing renderer date/crop instrumentation methods:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.image.wackelbild.WackelbildPrintRendererInstrumentedTest#target_dateBadge_isInsideTheCroppedOutput_onBothSides,com.isardomains.sameview.image.wackelbild.WackelbildPrintRendererInstrumentedTest#target_dateBadge_isInsideTheCroppedOutput_onFallbackToo"
```

If method filtering does not work reliably, do NOT broaden automatically. Report it first. The full renderer class is only a fallback after explicit approval.

Do NOT run:
- full unit suite;
- full instrumentation suite;
- clean;
- lint;
- assemble;
- release/bundle.

Do not suppress failures.

## Manual validation still required
After automated verification, do not commit yet.

Manual phone validation:
1. portrait session, date enabled -> inspect DeinWackelbild configurator;
2. landscape session, date enabled -> inspect DeinWackelbild configurator.

Goal: visibly more comfortable right/bottom breathing room without making the badge look detached from the corner.

No purchase required.

## Required final report
Report:
1. final `git status --short`;
2. exact files modified in this fix;
3. exact production value change;
4. exact test added;
5. exact documentation amendments;
6. confirmation SameView preview remains unchanged at 8.dp;
7. exact test command(s) and result counts;
8. tests explicitly NOT run;
9. manual portrait/landscape validation still required;
10. any failures and their actual fixes;
11. remaining risks;
12. confirmation no unrelated files changed;
13. confirmation nothing staged, committed or pushed.

If anything requires scope expansion, STOP before making that extra change.
