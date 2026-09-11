# Claude Prompt — DeinWackelbild V1 Block 3 Instrumentation Regression Analysis

## STEP 1 — Analysis Only

A regression has appeared immediately after the approved Block 3 implementation.

Before this Block 3 change, `./gradlew connectedDebugAndroidTest` was green on the physical SM-S911B device.

After the implementation, the full connected instrumentation suite now finishes with 13 failures.

Do **not** implement any fix yet.

Do **not** modify any file.

Do **not** repair failing Compare/EditSession tests directly unless you can prove the Block 3 change is not causal.

The current working assumption is:

> Because the suite was green before the six-file Block 3 change and is now red afterwards, this must be treated as a regression caused by the Block 3 iteration until proven otherwise.

## Source-of-Truth / project rules

Before analysis, re-read the relevant project instructions and DeinWackelbild Source-of-Truth / implementation-plan documents, especially:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

The previously approved implementation scope was exactly these six files:

### Modified

- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
- `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`

### Created

- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapper.kt`
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapperTest.kt`

No other file was changed.

## Observed connected-test failures

The full suite was run on SM-S911B / Android 16 and completed all tests.

The visible failures are:

### CompareScreenTest

Four landscape metadata-header tests fail with the same exception:

- `metadataHeader_landscape_showsLocationWhenNoTitle`
- `metadataHeader_landscape_noSeparateHeaderComponent`
- `metadataHeader_landscape_showsCreatedFallback_whenNoMetadata`
- `metadataHeader_landscape_showsTitleAndLocation`

All fail with:

- `java.lang.NullPointerException`
- at `CompareScreen.kt:207`

### EditSessionScreenTest

Nine failures follow, including:

- `countryPicker_cancelViaBackPress_preservesOriginalValue`
- `countryPickerSheet_filtersFromFirstCharacter`
- `countryPickerSheet_de_showsGermanNames_listVisibleImmediately`
- `referenceDate_laterThanCapture_showsOrderErrorText_de`
- `countryPickerSheet_selectingRow_invokesCallback_withMatchingCodeAndName`
- `countryPickerSheet_en_showsEnglishNames_listVisibleImmediately`
- `countryPickerSheet_isoCodeSearchMatch`
- `countryPickerSheet_noResults_showsEmptyState`
- `countryPickerSheet_rows_haveAccessibleContentDescription`

Failure forms include:

- `RootViewWithoutFocusException`
- `No compose hierarchies found in the app`
- missing expected semantics node / failed `performScrollTo()`

The suite ends with 13 failures total.

## Required analysis

Perform a **causal regression analysis** focused on the six Block 3 changes.

Do not guess.

Determine the **first actual failing cause**, not merely the downstream symptoms.

### 1. Diff analysis

Inspect the exact diff introduced by the six Block 3 files against the immediately previous green baseline.

Identify every change that could affect instrumentation globally, especially:

- Activity lifecycle
- orientation
- requested orientation
- window focus
- Compose test host state
- semantics
- density / graphics layer
- test harness cleanup
- test rule behavior
- global mutable/static state
- coroutine/sensor lifecycle
- callbacks surviving test teardown
- `ActivityScenario`
- test ordering or leaked state
- anything in `WackelbildScreenTest.kt` that could leave an Activity, composition, orientation, focus state, sensor listener, or global setting behind for subsequent tests

### 2. Failure ordering

Establish where the Wackelbild instrumentation tests execute relative to the first failing `CompareScreenTest`.

Determine whether:

- Wackelbild tests run earlier and leave state behind;
- one specific Wackelbild test is the last test before the first failing Compare test;
- or the first failure can occur independently of prior Wackelbild tests.

Use the connected-test reports / ordering information where available.

### 3. Isolation experiments

Run the minimum targeted diagnostic commands needed to establish causality.

Examples may include, if appropriate:

- the failing CompareScreen tests in isolation;
- the failing EditSessionScreen tests in isolation;
- the Wackelbild instrumentation test class alone;
- a narrow sequence containing WackelbildScreenTest followed by one failing CompareScreenTest;
- the failing Compare test after a fresh app/test process;
- any existing Gradle instrumentation filtering mechanism already supported by the project.

Do not add test code just to perform this analysis unless absolutely unavoidable.

Record exactly which commands were run and the result of each.

### 4. Specific investigation of `WackelbildScreenTest.kt`

Audit the newly changed instrumentation test file for teardown / isolation regressions.

Pay particular attention to:

- `WackelbildTestState`
- `launch()`
- any `ActivityScenario` handling
- orientation changes
- `setContent`
- lifecycle cleanup
- state remembered across tests
- custom semantics property
- callbacks that mutate shared state
- any new test that renders blend extremes
- the ridge-overlay test
- any test that might finish, rotate, background, or otherwise destabilize the host Activity

### 5. Specific investigation of production changes

Audit the production changes only for mechanisms that could plausibly affect later tests globally.

Pay particular attention to:

- sensor registration / unregister behavior
- lifecycle calls in `WackelbildViewModel`
- `previewBlendFraction` collection
- `graphicsLayer`
- `cameraDistance`
- density access
- semantics property
- any state or callback that survives navigation / composition disposal

Do not speculate that a local draw/alpha change caused unrelated failures unless you can trace a plausible mechanism.

### 6. CompareScreen.kt:207

Inspect the exact expression at `CompareScreen.kt:207`.

Explain:

- what is null there;
- why that null becomes possible in the failing instrumentation context;
- whether this condition existed before Block 3;
- whether the Block 3 change can alter the prerequisite state leading to that null;
- whether the NPE is the root cause or only the first visible victim of a previously corrupted Activity/test environment.

Do not modify `CompareScreen.kt` during this analysis.

### 7. EditSession cascade

Determine whether the nine EditSession failures are:

- independent regressions;
- or downstream fallout from the earlier Compare test / Activity / focus / process state corruption.

The clustered `No compose hierarchies found` and `RootViewWithoutFocusException` failures make cascade behavior plausible, but this must be proven rather than assumed.

## Output required

Return only:

1. **Exact root cause**, if proven.
2. **Evidence** supporting it.
3. **Which Block 3 change caused it**, if causal.
4. **Which failures are primary vs downstream/cascade**, if determinable.
5. **Minimal fix strategy**.
6. **Exact file(s) that would need modification** for the minimal fix.
7. **Whether the original six-file scope is sufficient or must be expanded**.
8. **Diagnostic commands run and exact results**.
9. **Any remaining uncertainty**.

Do **not** provide code.

Do **not** modify files.

Do **not** apply the fix.

Do **not** refactor anything.

Do **not** touch unrelated failing tests.

STOP after the analysis and wait for explicit scope approval.
