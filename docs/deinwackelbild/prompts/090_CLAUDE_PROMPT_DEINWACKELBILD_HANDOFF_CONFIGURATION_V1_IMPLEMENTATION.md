# Claude Prompt — DeinWackelbild Handoff Configuration V1 — STEP 3 IMPLEMENTATION

## Approval

STEP 2 is approved.

Implement **exactly** the agreed scope, using **Option B** for 3:4 / 4:3 recognition.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and the relevant Source-of-Truth documents strictly.

Do not expand scope.

## Approved behavior

For the DeinWackelbild create-handoff request:

- Add optional top-level JSON fields:
  - `format`
  - `orientation`
  - `direction`
- `direction` is always explicitly `"horizontal"`.
- Derive orientation from the actual rendered JPEG dimensions:
  - width < height -> `"portrait"`
  - width > height -> `"landscape"`
  - square -> omit `orientation`
- Preselect `format = "18x24"` only for rendered dimensions that represent 3:4 / 4:3 within the approved small integer rounding tolerance.
- All other ratios omit `format`.
- No UI changes.
- No renderer/crop/date/preview/navigation changes.
- The separate missing-date-on-one-image issue remains out of scope.

## Approved ratio rule — Option B

Use integer arithmetic with a named tolerance constant of **20**.

Conceptually:

- portrait 3:4 match when `abs(4*w - 3*h) <= 20`
- landscape 4:3 match when `abs(3*w - 4*h) <= 20`

Use safe integer arithmetic. Current image dimensions are capped at 16,000 px, but use an implementation that remains obviously safe and readable.

Do not add mappings for any other print format.

For landscape 4:3, still send:
- `format = "18x24"`
- `orientation = "landscape"`

Do not invent `24x18`.

## Approved production scope

Modify only the production files confirmed in STEP 2:

1. `app/src/main/java/com/isardomains/sameview/net/deinwackelbild/DeinWackelbildDtos.kt`
   - Add nullable/default-null `format`, `orientation`, `direction` fields to `CreateHandoffRequest`.
   - Serialize each only when non-null, exactly like the existing nullable locale behavior.
   - Do not add response `configuration` parsing.

2. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildHandoffOrchestrator.kt`
   - Obtain actual dimensions from the two already-rendered JPEG files using the existing `readExifOrientedDimensions` helper through the defaulted injectable reader agreed in STEP 2.
   - Perform blocking dimension reads on `Dispatchers.IO`.
   - Only trust dimensions when both files are readable and dimensions are identical.
   - Build the create request once for the rendered pair.
   - Reuse that same request across the existing generation/restart behavior.
   - Add only the minimal small helper(s)/constant needed for the approved mapping.
   - Do not create a new mapper file.

Do **not** modify:
- `WackelbildPrintRenderer.kt`
- `WackelbildPrintPair`
- ViewModel production code
- UI
- strings
- rendering
- date badge behavior
- response DTOs
- status endpoint handling
- unrelated networking.

## Approved test scope

Modify only:

1. `app/src/test/java/com/isardomains/sameview/net/deinwackelbild/OkHttpDeinWackelbildApiClientTest.kt`
   - Prove exact serialized field names.
   - Prove null optional fields are omitted.

2. `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildHandoffOrchestratorTest.kt`
   - Record `CreateHandoffRequest` in the existing fake client.
   - Inject deterministic fake dimensions through the approved reader seam.
   - Cover:
     - 1800x2400 -> `18x24`, `portrait`, `horizontal`
     - 2400x1800 -> `18x24`, `landscape`, `horizontal`
     - a realistic rounded 3:4 result within tolerance -> still `18x24`
     - nearby wrong ratio such as 0.74 -> no `format`
     - 16:9 -> no `format`, correct orientation, `horizontal`
     - square -> no `format`, no orientation, `horizontal`
     - unreadable dimensions -> `direction` only
     - mismatched dimensions -> `direction` only
     - existing restart behavior reuses the identical request

Do not modify `WackelbildViewModelTest` unless compilation proves it is necessary. If it becomes necessary, STOP before modifying it and report why.

## Documentation scope

Modify only the documentation confirmed in STEP 2:

1. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - §27: document the three optional create-handoff fields.
   - §32: document that SameView now supplies orientation/direction and preselects `18x24` only for 3:4/4:3 rendered pairs under the approved rounding rule.
   - Preserve the rule that there is no user-facing size/orientation selector.

2. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
   - Update only the relevant §14.1 request DTO/contract description with the three optional fields.

3. `docs/IMPLEMENTATION_NOTES.md`
   - Add the minimal dated implementation note for this change in the existing style/location identified in STEP 2.

Do not update `CLAUDE_PROJECT_INSTRUCTION.md`.
Do not document the newly discovered status endpoint.
Do not rewrite unrelated historical sections.

## Existing untracked prompt files

STEP 2 reported these existing untracked files:

- prompt 085
- prompt 086
- prompt 087
- prompt 088

Leave them untouched. Do not stage, delete, rename, or edit them.

If `git status --short` now contains any unexpected tracked modification beyond the approved implementation work, STOP and report before editing.

## Verification

Run only the smallest relevant unit-test scope:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.isardomains.sameview.net.deinwackelbild.OkHttpDeinWackelbildApiClientTest" --tests "com.isardomains.sameview.ui.wackelbild.WackelbildHandoffOrchestratorTest"
```

Do **not** run:
- full `testDebugUnitTest`
- full instrumentation suite
- `connectedDebugAndroidTest`
- `clean`
- `lintDebug`
- `assembleDebug`
- release builds/bundles

unless an actual compilation/build dependency makes a broader command necessary. If so, explain why before broadening.

Do not suppress failures or weaken tests.

## Real-device validation

Do not perform a production order unless explicitly requested.

After implementation, state that real-device validation is still required:

1. A real 3:4 or 4:3 SameView session should open DeinWackelbild with:
   - 18×24
   - correct Hoch-/Querformat
   - Seitlich kippen

2. A non-3:4 session should keep the partner's default format while still getting:
   - correct orientation
   - Seitlich kippen

## Git discipline

After implementation and tests:

- Show `git status --short`.
- Show `git diff --stat`.
- Review the diff for scope compliance.
- Do not stage.
- Do not commit.
- Do not push.

## Required final report

Return:

1. exact files modified;
2. concise description of the implementation in each;
3. exact ratio/tolerance logic implemented;
4. confirmation of behavior for portrait, landscape, square, unreadable/mismatched dimensions and non-3:4 ratios;
5. documentation changes;
6. exact test command run;
7. test result with passed/failed counts if Gradle reports them;
8. tests/build commands explicitly not run;
9. whether real-device validation remains required;
10. final `git status --short`;
11. `git diff --stat`;
12. confirmation no unrelated files were changed;
13. confirmation nothing was staged, committed, or pushed.

If implementation requires any file outside this approved scope, STOP before changing it and explain why.
