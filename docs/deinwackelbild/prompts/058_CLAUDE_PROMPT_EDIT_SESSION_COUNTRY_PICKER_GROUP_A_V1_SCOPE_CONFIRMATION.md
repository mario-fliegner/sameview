# Claude Prompt — EditSession Country Picker Test Host — STEP 2 Scope Confirmation

## STEP 2 — Scope Confirmation Only

The completed root-cause analysis identified three independent failure groups in `EditSessionScreenTest`.

This prompt concerns **Group A only**.

Do **not** implement the fix yet.

Do **not** modify any file.

Do **not** address Group B or Group C in this iteration.

## Proven Group A root cause

The following seven tests fail because `EditSessionScreenTest.kt`'s helper `setCountryPickerSheetContent()` does not apply the same Activity/window wake and lock-screen flags that the existing `setEditSessionContent()` helper and equivalent instrumentation hosts elsewhere in the project already apply:

- `countryPickerSheet_filtersFromFirstCharacter`
- `countryPickerSheet_de_showsGermanNames_listVisibleImmediately`
- `countryPickerSheet_selectingRow_invokesCallback_withMatchingCodeAndName`
- `countryPickerSheet_en_showsEnglishNames_listVisibleImmediately`
- `countryPickerSheet_isoCodeSearchMatch`
- `countryPickerSheet_noResults_showsEmptyState`
- `countryPickerSheet_rows_haveAccessibleContentDescription`

The missing calls are:

- `activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)`
- `activity.setShowWhenLocked(true)` on supported API levels
- `activity.setTurnScreenOn(true)` on supported API levels

The existing `setEditSessionContent()` helper already uses this established pattern.

The failure was reproduced in isolation on the real SM-S911B device, so this is not merely cross-test contamination.

## Source-of-Truth / project rules

Before confirming scope, re-read:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- the relevant active Edit Session / metadata specifications
- any active localization specification covering the country picker

If any Source-of-Truth document conflicts with this proposed test-only change, report the conflict and STOP.

## Required scope confirmation

Confirm whether the minimal fix can be limited to exactly this file:

- `app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

The intended change is only:

- update `setCountryPickerSheetContent()` so it applies the same already-established Activity/window wake and lock-screen flags as `setEditSessionContent()`

Do not alter production code.

Do not alter country-picker behavior.

Do not alter test assertions.

Do not change test data.

Do not change test names.

Do not refactor helpers.

Do not touch unrelated setup/teardown code.

Do not modify:

- `EditSessionScreen.kt`
- `CountryPickerSheet.kt`
- `EditSessionViewModel.kt`
- `CompareLabelLogic.kt`
- `SessionStorage.kt`
- resource files
- any Wackelbild file
- any CompareScreen file

## Explicitly out of scope

### Group B

Do not touch:

- `countryPicker_cancelViaBackPress_preservesOriginalValue`

Its separate failure involves `Espresso.pressBack()` and ModalBottomSheet/window-focus timing.

### Group C

Do not touch:

- `referenceDate_laterThanCapture_showsOrderErrorText_de`

Its separate failure involves the Activity window stopping during an IME transition.

Neither Group B nor Group C is part of this fix.

## Risk assessment required

State whether this Group A fix can affect:

- production behavior
- app lifecycle outside instrumentation
- localization behavior
- modal bottom-sheet behavior
- other instrumentation host helpers

The expected answer should be that this is test-host-only, but verify rather than assume.

Also identify any risk of duplicating existing setup logic incorrectly.

## Verification plan for the later STEP 3 implementation

If the one-file scope is valid, propose the exact verification commands for the later implementation step.

At minimum, the plan should include:

1. the seven Group A tests, preferably as targeted connected instrumentation tests
2. `./gradlew testDebugUnitTest`
3. `./gradlew connectedDebugAndroidTest`
4. `./gradlew assembleDebug`

The later implementation must not be considered successful unless the seven Group A tests are green.

If the full connected suite still fails only in Group B and/or Group C afterwards, those failures must be reported unchanged and left for separate iterations.

## Required output

Return only:

1. **Exact file to modify**
2. **Exact helper/method to modify**
3. **Exact behavior to add**
4. **Confirmation that no production file is required**
5. **Confirmation that Group B and Group C remain untouched**
6. **Risk assessment**
7. **Documentation impact**
8. **Exact verification commands proposed for STEP 3**
9. **Any uncertainty or reason scope expansion would be necessary**

Do **not** provide implementation code.

Do **not** modify files.

Do **not** implement the fix.

STOP after scope confirmation and wait for explicit approval.
