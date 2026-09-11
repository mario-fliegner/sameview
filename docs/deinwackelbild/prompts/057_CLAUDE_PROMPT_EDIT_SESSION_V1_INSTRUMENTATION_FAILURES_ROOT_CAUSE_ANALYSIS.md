# Claude Prompt — EditSessionScreen Instrumentation Failures — STEP 1 Root-Cause Analysis

## STEP 1 — Analysis Only

A full real-device `./gradlew connectedDebugAndroidTest` run is still failing after the separate `CompareScreen` empty-locale defect was fixed.

The previous run had 13 failures:
- 4 `CompareScreenTest` failures
- 9 `EditSessionScreenTest` failures

The `CompareScreen` production fix was then implemented in exactly one file:

- `app/src/main/java/com/isardomains/sameview/ui/compare/CompareScreen.kt`

The fix changed the locale lookup from:

`LocalConfiguration.current.locales.get(0)`

to a null-safe fallback using `Locale.getDefault()`.

After that fix:

- `./gradlew testDebugUnitTest` → BUILD SUCCESSFUL
- `./gradlew assembleDebug` → BUILD SUCCESSFUL
- `./gradlew connectedDebugAndroidTest` → BUILD FAILED
- the 4 `CompareScreenTest` failures disappeared completely
- the same 9 `EditSessionScreenTest` failures remained unchanged

Therefore, the earlier hypothesis that the EditSession failures were merely downstream fallout from the CompareScreen NPE is disproven.

This prompt is for a **separate root-cause analysis of those 9 remaining EditSession instrumentation failures only**.

Do **not** implement any fix.

Do **not** modify any file.

Do **not** refactor unrelated code.

## Source-of-Truth / project rules

Before analysis, re-read the relevant project instructions and specifications, especially:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/SESSION_METADATA_V1.md` or the active equivalent in this repository
- any active Edit Session / metadata editor specification
- `COMPARE_SESSION_RENDERING_V1.md` if it governs shared compare/edit behavior
- localization specifications relevant to the country picker and reference-date validation
- any implementation notes that cover `EditSessionScreen`

If code and Source-of-Truth documentation conflict, document the conflict explicitly and treat the MD specification as authoritative unless the project history clearly marks it obsolete.

## Exact remaining failures

The real-device suite still fails in exactly these 9 tests:

1. `countryPicker_cancelViaBackPress_preservesOriginalValue`
2. `countryPickerSheet_filtersFromFirstCharacter`
3. `countryPickerSheet_de_showsGermanNames_listVisibleImmediately`
4. `referenceDate_laterThanCapture_showsOrderErrorText_de`
5. `countryPickerSheet_selectingRow_invokesCallback_withMatchingCodeAndName`
6. `countryPickerSheet_en_showsEnglishNames_listVisibleImmediately`
7. `countryPickerSheet_isoCodeSearchMatch`
8. `countryPickerSheet_noResults_showsEmptyState`
9. `countryPickerSheet_rows_haveAccessibleContentDescription`

Observed failure forms from the prior real-device report include:

- `RootViewPicker$RootViewWithoutFocusException`
- `No compose hierarchies found in the app`
- failed `performScrollTo()` because the expected German reference-date error text could not be found

The failures persisted byte-for-byte after the unrelated CompareScreen defect was fixed.

## Required analysis

Perform a **causal root-cause analysis** of these nine failures.

Do not guess.

Do not repair tests simply because they are failing.

Determine whether the defect lies in:

- production code,
- test fixture / test harness,
- lifecycle / activity hosting,
- modal sheet handling,
- focus/window behavior,
- locale/configuration handling,
- Compose synchronization,
- stale test assumptions after a prior legitimate behavior change,
- or another proven cause.

### 1. Establish regression provenance

Inspect git history and blame for:

- `EditSessionScreen.kt`
- `EditSessionScreenTest.kt`
- any country-picker composable/helper used by these tests
- the reference-date validation UI and logic
- test-host utilities used by these tests
- localization resources involved in the German error assertion

Identify the commit(s) that introduced the currently failing behavior or changed the corresponding tests.

Answer:

- When was each failing test last known compatible with production code?
- Did one commit introduce all or most of these failures?
- Are there multiple independent regressions?

Do not infer chronology from commit timestamps alone if stronger evidence is available.

### 2. Group failures by likely causal family

Analyze whether the 9 failures form one root cause or multiple independent groups.

At minimum, investigate separately:

#### A. Country-picker / modal-sheet family

- `countryPicker_cancelViaBackPress_preservesOriginalValue`
- `countryPickerSheet_filtersFromFirstCharacter`
- `countryPickerSheet_de_showsGermanNames_listVisibleImmediately`
- `countryPickerSheet_selectingRow_invokesCallback_withMatchingCodeAndName`
- `countryPickerSheet_en_showsEnglishNames_listVisibleImmediately`
- `countryPickerSheet_isoCodeSearchMatch`
- `countryPickerSheet_noResults_showsEmptyState`
- `countryPickerSheet_rows_haveAccessibleContentDescription`

#### B. Reference-date validation family

- `referenceDate_laterThanCapture_showsOrderErrorText_de`

Do not assume B is caused by A merely because they are in the same test class.

### 3. Inspect the test host and Activity lifecycle

Audit `EditSessionScreenTest.kt` carefully.

Pay particular attention to:

- Compose test rule type
- `ActivityScenario`
- custom `setContent` / host helpers
- whether the Activity is launched before or after the Compose rule is ready
- scenario close / teardown behavior
- modal bottom sheet lifecycle
- Espresso `pressBack()`
- window focus transfer
- IME / text-field focus
- orientation or configuration overrides
- locale overrides
- synchronization / `waitForIdle`
- state shared between tests
- remembered mutable state surviving across test cases
- any helper that may leave the host Activity without focus
- any test ordering assumptions

For the `No compose hierarchies found` failures, determine exactly why the Compose hierarchy is absent at the moment of assertion.

For the `RootViewWithoutFocusException`, determine which window owns focus and why.

### 4. Inspect production country-picker behavior

Audit the current country-picker implementation used by `EditSessionScreen`.

Determine:

- whether the picker is still a modal bottom sheet / dialog / popup as the tests assume
- whether search input is composed immediately or only after animation / state transition
- whether the sheet can be dismissed with back press
- whether its state is hoisted or internally remembered
- whether callbacks still match the test expectations
- whether locale-dependent country labels are produced synchronously
- whether semantics/content descriptions changed
- whether the empty-state behavior changed
- whether focus behavior changed

Compare current implementation against the active Source-of-Truth specification.

If the production behavior is correct and the tests are stale, prove that from the spec and history.

If the production behavior violates the spec, identify the exact defect.

### 5. Investigate the reference-date failure independently

For:

`referenceDate_laterThanCapture_showsOrderErrorText_de`

trace the full path:

- test setup
- reference date input
- capture date input
- validation trigger
- ViewModel / state logic
- localization resource resolution
- resulting UI semantics/text
- scroll container behavior

Determine exactly why the expected German error text is absent when `performScrollTo()` runs.

Check whether:

- validation now occurs at a different time
- the error text wording changed
- the test locale is not actually German
- the error moved to supporting text / semantics
- the date values no longer create the intended invalid state
- the screen is not fully composed
- or another cause is proven

Do not change localization or validation logic during analysis.

### 6. Run targeted isolation experiments

Use a connected device/emulator if available.

Run the smallest possible commands to establish causality.

At minimum, if supported by the project/test runner, execute:

- the full `EditSessionScreenTest` class alone
- one failing country-picker test alone
- one passing nearby `EditSessionScreenTest` alone
- the reference-date failing test alone

Where useful, also run a short sequence of two tests to detect teardown/order dependence.

Record exact commands and exact outcomes.

If a failing test passes in isolation but fails in the full class/suite, investigate shared state / lifecycle leakage.

If it fails in isolation, identify the direct local cause.

Do not add production or test code merely to make diagnosis easier unless absolutely unavoidable.

### 7. Check whether failures are deterministic

For at least one representative country-picker failure and the reference-date failure, determine whether behavior is:

- deterministic,
- order-dependent,
- timing-dependent,
- device-specific,
- or flaky.

If repeated runs differ, report that explicitly.

### 8. Do not touch Wackelbild

The current DeinWackelbild Block 3 files are out of scope for this analysis.

Do not modify or blame them without direct evidence.

The currently observed nine failures already existed before the Wackelbild instrumentation tests executed in the earlier real-device run, and they remained unchanged after the CompareScreen fix.

## Required output

Return only:

1. **Exact root cause(s)**, if proven.
2. **Failure grouping** — which tests share which root cause.
3. **Evidence** for each conclusion.
4. **Whether each cause is production-code defect, test defect, fixture defect, timing/lifecycle defect, or spec/test mismatch.**
5. **Exact commit/history provenance** where determinable.
6. **Minimal fix strategy for each proven root cause.**
7. **Exact file(s) that would need modification for the minimal fix.**
8. **Whether documentation must also change.**
9. **Diagnostic commands run and exact results.**
10. **Any remaining uncertainty.**

Do **not** provide implementation code.

Do **not** modify files.

Do **not** apply any fix.

Do **not** combine unrelated fixes.

STOP after the analysis and wait for explicit scope approval.
