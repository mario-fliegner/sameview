# Claude Prompt — DeinWackelbild Transfer Date Safe Margin V1 — STEP 2 SCOPE CONFIRMATION

## Approved decision

STEP 1 analysis is complete.

The following product/spec decision is explicitly approved:

- Increase the embedded TRANSFER-JPEG date badge right/bottom margin from `8f / 360f` to `12f / 360f`.
- Use the same `12/360` proportional margin for both right and bottom.
- Apply it equally to portrait and landscape.
- Keep the SameView in-app preview badge unchanged at its existing `8.dp`.
- The intentional preview/transfer margin difference is approved because the transferred image needs additional safe space for the observed DeinWackelbild configurator inset/crop.
- Badge style, text, size, padding, corner radius, date source, toggle behavior, print-format selection and print crop must remain unchanged.

This is STEP 2 only. Do NOT implement anything.

## Mandatory pre-check

Before defining scope:

1. Run `git status --short`.
2. Confirm the current working tree is still the known uncommitted baseline from the print-format implementation:
   - 13 modified tracked files;
   - new `WackelbildPrintTarget.kt`;
   - new `WackelbildPrintTargetTest.kt`;
   - existing untracked prompt files;
   - nothing staged.
3. Verify `DateBadgeRenderer.kt` is still clean against HEAD.
4. Re-read the current working-tree versions of:
   - `CLAUDE_PROJECT_INSTRUCTION.md`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
   - `docs/IMPLEMENTATION_NOTES.md`
   - `DateBadgeRenderer.kt`
   - `DateBadgeRendererTest.kt`
5. If there are unexpected tracked changes, STOP and report them.

## Exact intended fix

The production behavior change should be only:

`EDGE_MARGIN_FRACTION = 8f / 360f`

to:

`EDGE_MARGIN_FRACTION = 12f / 360f`

for the badge embedded into the Wackelbild transfer JPEG.

Confirm whether this is literally the only production-code line/value that needs behavioral modification, apart from an adjacent comment if necessary.

Do not introduce:
- a second partner-specific margin constant;
- orientation-specific margins;
- per-axis margins;
- new configuration/state;
- new UI;
- shared preview/transfer margin plumbing.

## Preview remains unchanged

Confirm explicitly that:
- `WackelbildScreen.kt` requires no modification;
- `WackelbildScreenTest.kt` requires no modification;
- the preview remains at `8.dp`;
- the preview/transfer difference is intentional and documented.

Do not attempt to restore exact margin equality between preview and transfer.

## Source-of-Truth amendment

The approved product decision supersedes only the documentation language that requires or implies exact preview/transfer edge-margin equality.

Identify the exact minimal wording changes required in:

### `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
Inspect especially:
- §9.3;
- §9.6;
- §20.

The resulting contract should say, in substance:
- preview and transfer remain visually equivalent in date style/content/relative scale and final print composition;
- the transfer JPEG may use a slightly larger proportional edge safe margin than the preview to tolerate partner-side display/print inset/crop;
- transfer margin remains consistent on right/bottom and proportional to short edge;
- portrait and landscape use the same relative transfer rule.

Do not broadly weaken WYSIWYG requirements beyond this intentional margin exception.

### `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
Inspect especially §8.3 and any row/text deriving transfer margin directly from preview `8.dp / 360`.

Document the approved transfer value `12/360` and preserve the existing principle that preview and transfer badges are independently tuned but visually equivalent.

### `docs/IMPLEMENTATION_NOTES.md`
Determine the smallest coherent addition/update to the current uncommitted Wackelbild implementation entry. Do not create unnecessary historical noise.

## Tests

Confirm the smallest test change.

Expected:
- `DateBadgeRendererTest.kt` only.
- Add or adjust one focused assertion/test that deliberately pins the transfer margin contract to `12/360`, rather than merely referring to the production constant and therefore being unable to detect accidental value changes.

Do not change unrelated date rendering tests.

Assess whether the two existing target/date renderer instrumentation tests need to be run after implementation. They should not require source changes unless a real assertion depends on the old numeric margin.

No ViewModel, Screen, orchestrator, API-client or print-target tests should change.

## STEP 3 verification plan

Propose the smallest exact test commands.

Preferred mandatory JVM command:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.isardomains.sameview.image.wackelbild.DateBadgeRendererTest"
```

If the existing instrumented renderer tests can be targeted by exact method names for the two date/target cases, provide those exact commands/names. If exact method targeting is awkward or unreliable, state whether running only `WackelbildPrintRendererInstrumentedTest` is justified.

Do NOT propose:
- full unit suite;
- full instrumentation suite;
- `clean`;
- lint;
- assemble;
- release/bundle tasks.

Manual validation after implementation remains:
- one portrait session with date enabled in DeinWackelbild;
- one landscape session with date enabled in DeinWackelbild.

No production purchase is required.

## Scope exclusions

Explicitly keep untouched:
- `WackelbildScreen.kt`;
- `WackelbildScreenTest.kt`;
- print-format target selection;
- center crop geometry;
- `WackelbildPrintRenderer.kt` unless analysis discovers the margin is not actually owned solely by `DateBadgeRenderer.kt`;
- ViewModel;
- orchestrator;
- DTO/API client;
- camera;
- Compare;
- stored sessions/originals;
- networking/privacy;
- date styling/text/size/padding/radius;
- optional dispatcher injection.

No unrelated cleanup/refactoring/reformatting.

## Required STEP 2 output

Return:

1. `git status --short` summary;
2. confirmation whether `DateBadgeRenderer.kt` is clean before this fix;
3. exact Source-of-Truth wording/sections requiring amendment;
4. exact production file(s) to modify;
5. exact behavioral line/value change;
6. exact test file(s) to modify;
7. exact documentation files to modify;
8. confirmation preview code/tests remain untouched;
9. exact targeted STEP 3 test commands/classes/methods;
10. manual portrait/landscape validation required;
11. risks;
12. confirmation no unrelated code/docs will be touched;
13. confirmation STEP 2 modified nothing and ran no tests/API calls;
14. confirmation nothing was staged, committed or pushed.

Then STOP and wait for explicit approval.

Do not implement, edit, test, stage, commit or push in STEP 2.
