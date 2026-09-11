# Claude Prompt — EditSession Country Picker Test Host — STEP 3 Implementation

## Approval

**Approved for STEP 3 implementation.**

Implement exactly the previously confirmed Group A test-host fix.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

Do not expand scope.

## Exact approved file

Modify exactly:

- `app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

No other file may be modified.

## Exact approved method

Modify only:

- `setCountryPickerSheetContent()`

## Exact required change

Inside the existing:

`scenario?.onActivity { activity -> ... }`

block, before `activity.setContent { ... }`, add exactly the same host-window setup already used by the existing `setEditSessionContent()` helper in this same file:

- `activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)`
- guarded by `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)`:
  - `activity.setShowWhenLocked(true)`
  - `activity.setTurnScreenOn(true)`

Use the same call structure and API guard as `setEditSessionContent()`.

`WindowManager` and `Build` are already imported. Do not add imports unless the current file unexpectedly differs from the analyzed state; if so, STOP and report rather than broadening the change.

## Preserve everything else

Do not change:

- `wakeTestDevice()`
- `ActivityScenario.launch(...)`
- `CountryPickerSheet(...)`
- `composeRule.waitForIdle()`
- test assertions
- test data
- test names
- any other helper
- setup/teardown logic
- production code
- resources
- documentation

Do not refactor or reformat unrelated code.

## Explicitly out of scope

Do not touch Group B:

- `countryPicker_cancelViaBackPress_preservesOriginalValue`

Do not touch Group C:

- `referenceDate_laterThanCapture_showsOrderErrorText_de`

Do not modify any Wackelbild or CompareScreen production/test file.

## Required verification

After implementing the one-method change, run the following.

### 1. Targeted Group A instrumentation verification

Run exactly the seven Group A tests, using the project's supported instrumentation filtering syntax.

The seven tests are:

- `countryPickerSheet_filtersFromFirstCharacter`
- `countryPickerSheet_de_showsGermanNames_listVisibleImmediately`
- `countryPickerSheet_selectingRow_invokesCallback_withMatchingCodeAndName`
- `countryPickerSheet_en_showsEnglishNames_listVisibleImmediately`
- `countryPickerSheet_isoCodeSearchMatch`
- `countryPickerSheet_noResults_showsEmptyState`
- `countryPickerSheet_rows_haveAccessibleContentDescription`

Use this command if supported by the current Gradle/runner setup:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_filtersFromFirstCharacter,com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_de_showsGermanNames_listVisibleImmediately,com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_selectingRow_invokesCallback_withMatchingCodeAndName,com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_en_showsEnglishNames_listVisibleImmediately,com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_isoCodeSearchMatch,com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_noResults_showsEmptyState,com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPickerSheet_rows_haveAccessibleContentDescription
```

If the runner does not accept that multi-method form, use the smallest equivalent supported targeted commands instead. Do not alter test code to work around filtering.

All seven Group A tests must pass.

### 2. Unit tests

```text
./gradlew testDebugUnitTest
```

### 3. Full connected instrumentation suite

```text
./gradlew connectedDebugAndroidTest
```

Expected result based on the current analysis:

- the seven Group A failures are gone;
- Group B may still fail;
- Group C may still fail.

Do not modify Group B or Group C even if they remain red.

Do not claim the full connected suite passed unless it actually finishes green.

### 4. Debug build

```text
./gradlew assembleDebug
```

## Failure handling

If any of the seven Group A tests still fail after this exact change:

- do not make a second fix;
- do not change assertions;
- do not add sleeps;
- do not touch production code;
- report the exact failure and STOP.

If any unexpected test outside Group B/Group C fails in the full suite:

- report it exactly;
- do not fix it in this iteration.

If the change would require another file, STOP before modifying it.

## Required final report

Return:

1. Exact modified file.
2. Exact modified method.
3. Exact change made.
4. Confirmation that no other file was modified.
5. Result of the targeted seven Group A tests.
6. Result of `./gradlew testDebugUnitTest`.
7. Result of full `./gradlew connectedDebugAndroidTest`.
8. Result of `./gradlew assembleDebug`.
9. Exact remaining failures, if any.
10. Confirmation that Group B and Group C were not modified.
11. Whether any real-device follow-up is still required.
12. Any deviation from the approved scope.

Do not make any additional fixes.

STOP after implementation and verification.
