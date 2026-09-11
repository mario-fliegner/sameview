# Claude Prompt — EditSession Group B Back-Press Focus Failure — STEP 3 Implementation

## Context

This is STEP 3 of the controlled implementation workflow.

STEP 1 root-cause analysis is complete.

STEP 2 scope confirmation is complete and explicitly approved.

Implement exactly the approved minimal Group B stabilization fix.

Do **not** expand scope.

Do **not** touch Group C.

Do **not** modify production code.

---

# Approved issue

Failing instrumentation test:

`com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`

Known failure:

```text
androidx.test.espresso.base.RootViewPicker$RootViewWithoutFocusException:
Waited for the root of the view hierarchy to have window focus and not request layout for 10 seconds.

at androidx.test.espresso.Espresso.pressBack(Espresso.java:237)
at com.isardomains.sameview.ui.compare.EditSessionScreenTest.countryPicker_cancelViaBackPress_preservesOriginalValue(EditSessionScreenTest.kt:219)
```

The failure is intermittent and device-load-sensitive.

Production behavior is not implicated.

The established project precedent in `SettingsScreenTest.kt` shows that `waitForIdle()` alone can race a Material3 `ModalBottomSheet` enter transition under full-suite load and uses `composeRule.waitUntil(...)` polling to stabilize sheet interaction.

---

# Exact approved modification scope

Modify exactly one file:

`app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

Modify exactly one test method:

`countryPicker_cancelViaBackPress_preservesOriginalValue`

Do not modify any other method.

Do not modify any helper.

Do not modify any production file.

Do not modify any resource file.

Do not modify any documentation file.

---

# Exact implementation requirement

Inside `countryPicker_cancelViaBackPress_preservesOriginalValue`:

1. Keep the existing picker-opening click.
2. Keep the existing `composeRule.waitForIdle()`.
3. Immediately after that existing `waitForIdle()`, add a bounded `composeRule.waitUntil(timeoutMillis = 5_000) { ... }`.
4. The wait condition must poll for a **concrete country-picker row node**, not only the list container.
5. Use the existing row test-tag convention already present in the current codebase/tests.
6. Because this test seeds `locationCountryCode = "DE"`, poll for the concrete DE row node if that is the current established tag.
7. Use the same `onAllNodesWithTag(...).fetchSemanticsNodes().isNotEmpty()` style already used by the existing `SettingsScreenTest.kt` ModalBottomSheet stabilization precedent.
8. Keep the existing `edit_session_country_picker_list` displayed assertion after the wait.
9. Keep the existing `Espresso.pressBack()` call unchanged.
10. Keep all existing post-dismiss assertions unchanged:
   - country field still shows `"Germany"`
   - save button remains disabled

Do not add:

- arbitrary sleeps
- retries around `Espresso.pressBack()`
- retry loops inside the test
- `Thread.sleep`
- `UiDevice.pressBack()`
- direct window-focus polling
- new helper methods
- production changes
- assertion weakening
- ignored tests
- suppressions

This iteration intentionally implements only the minimal semantics-row settle wait.

If the concrete row tag differs from the expected DE naming convention, use the actual existing tag from the source. Do not invent a new test tag.

---

# Explicitly out of scope

Do not touch:

- `referenceDate_laterThanCapture_showsOrderErrorText_de` (Group C)
- Group A picker tests
- `CountryPickerSheet.kt`
- `EditSessionScreen.kt`
- `EditSessionViewModel.kt`
- `CompareLabelLogic.kt`
- CompareScreen files
- Walkthrough files
- Wackelbild files
- SettingsScreen tests
- shared instrumentation infrastructure

No refactor.

No cleanup.

No reformatting unrelated code.

---

# Verification requirements

After the exact one-method change, run all of the following.

## 1. Repeated targeted Group B test

Run the exact Group B test **10 times** on the connected real device.

Use the environment's supported shell syntax.

Conceptually:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue
```

Repeat 10 times.

Report each run individually as PASS/FAIL.

A single isolated pass is not sufficient.

If any of the 10 targeted runs fail:

- do **not** add a second fix
- do **not** add window-focus polling
- do **not** change back-injection mechanism
- report the failure exactly
- STOP after completing only the verification/reporting needed to document the result

## 2. Unit tests

Run:

```text
./gradlew testDebugUnitTest
```

## 3. Full connected instrumentation suite

Run:

```text
./gradlew connectedDebugAndroidTest
```

Expected successful outcome for this iteration:

- Group B no longer fails
- Group C may still fail
- no new failures appear

Do not fix Group C even if it is the only remaining failure.

If Group B still fails in the full suite despite 10 targeted passes:

- report that fact exactly
- do not implement another stabilization mechanism
- STOP

If any unexpected new failure appears:

- report it
- do not investigate or fix it in this iteration

## 4. Debug build

Run:

```text
./gradlew assembleDebug
```

---

# Success criteria

This STEP 3 iteration is successful only if:

- exactly one file was modified
- exactly one test method was modified
- the only behavioral change is the added row-level `waitUntil` synchronization before `Espresso.pressBack()`
- all 10 targeted runs pass
- `testDebugUnitTest` passes
- `assembleDebug` passes
- full `connectedDebugAndroidTest` no longer contains Group B
- no unexpected new failures appear
- Group C remains untouched

---

# Required final report

Return:

1. **Exact modified file**
2. **Exact modified method**
3. **Exact synchronization change implemented**
4. **Exact row tag used**
5. **Confirmation that `waitForIdle()` remained**
6. **Confirmation that `Espresso.pressBack()` remained unchanged**
7. **Confirmation that no other file/method changed**
8. **Results of all 10 targeted runs, individually**
9. **Result of `./gradlew testDebugUnitTest`**
10. **Result of full `./gradlew connectedDebugAndroidTest`**
11. **Exact remaining failure list from the full suite**
12. **Result of `./gradlew assembleDebug`**
13. **Confirmation that Group C was not modified**
14. **Whether real-device follow-up is still required**
15. **Any deviation from approved scope**

Do not make any additional fix if verification exposes another problem.

STOP after the report.
