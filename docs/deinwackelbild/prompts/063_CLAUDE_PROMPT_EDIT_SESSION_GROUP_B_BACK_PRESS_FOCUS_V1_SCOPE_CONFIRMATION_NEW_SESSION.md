# Claude Prompt — EditSession Group B Back-Press Focus Failure — STEP 2 Scope Confirmation

## Context for a New Claude Session

This is a fresh session. Continue from the completed root-cause analysis summarized below.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

The project uses a controlled workflow:

1. STEP 1 — analysis only
2. STEP 2 — scope confirmation
3. STEP 3 — implementation only after explicit approval

STEP 1 for this issue is complete. This prompt is **STEP 2 only**.

Do **not** modify any file.

Do **not** provide implementation code.

Do **not** implement the fix yet.

---

# Current verified project/test state

The full real-device connected instrumentation suite currently has exactly **2 failures** out of **1079 tests**:

1. `com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`
2. `com.isardomains.sameview.ui.compare.EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de`

This prompt concerns **only the first failure (Group B)**.

Group C must remain untouched.

Recent unrelated issues were already resolved:

- CompareScreen empty-locale NPE fixed
- 7 CountryPicker Group A test-host failures fixed
- walkthrough page-4 failure was verified as a one-off flake and not changed
- Wackelbild Block 3 files are not implicated here

---

# Completed STEP 1 analysis for Group B

## Exact failing test

`EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`

Failure:

```text
androidx.test.espresso.base.RootViewPicker$RootViewWithoutFocusException:
Waited for the root of the view hierarchy to have window focus and not request layout for 10 seconds.

at androidx.test.espresso.Espresso.pressBack(Espresso.java:237)
at com.isardomains.sameview.ui.compare.EditSessionScreenTest.countryPicker_cancelViaBackPress_preservesOriginalValue(EditSessionScreenTest.kt:219)
```

Relevant test sequence:

```kotlin
setEditSessionContent(createSession(locationCountry = "Germany", locationCountryCode = "DE"))
composeRule.onNodeWithTag("edit_session_country_field").performScrollTo().performClick()
composeRule.waitForIdle()
composeRule.onNodeWithTag("edit_session_country_picker_list").assertIsDisplayed()

androidx.test.espresso.Espresso.pressBack()
composeRule.waitForIdle()

composeRule.onNodeWithTag("edit_session_country_field").performScrollTo().assert(hasText("Germany"))
composeRule.onNodeWithTag("edit_session_save_button").assertIsNotEnabled()
```

The sheet/list is confirmed displayed before `pressBack()`.

The failure is specifically Espresso's native window-focus requirement.

---

# Proven / observed behavior

Real-device focus polling during an isolated run captured:

```text
mFocusedWindow=Window{... com.isardomains.sameview/androidx.activity.ComponentActivity}
mFocusedWindow=<no window reported>
mFocusedWindow=Window{... u0 NotificationShade}
```

So the host Activity can genuinely lose focus transiently to a Samsung system window.

The failure is intermittent/timing-dependent:

- 2 isolated runs in the latest analysis session: PASS
- 1 isolated run in the earlier analysis session: FAIL
- every known full 1079-test suite run: FAIL for this test

This strongly indicates a full-suite/device-load-sensitive synchronization race.

---

# Existing project precedent

A directly relevant precedent exists in:

`SettingsScreenTest.kt`

Commit:

`49a499a` — `"Fix flaky branding symbol picker test"`

That existing test documents that:

> `waitForIdle()` alone races the `ModalBottomSheet` enter animation under full-suite device load.

It uses `composeRule.waitUntil(...)` to poll until sheet content is actually present before continuing.

This is the established project pattern for stabilizing `ModalBottomSheet` timing in instrumentation.

---

# Production behavior assessment

`CountryPickerSheet.kt` uses standard Material3:

```kotlin
ModalBottomSheet(onDismissRequest = onDismiss)
```

No production defect was identified.

The active session-metadata spec requires that cancelling/dismissing the picker leaves the original value unchanged.

It does **not** require that this contract specifically be tested via a raw Espresso system-back injection mechanism.

The current test is still valid in intent: dismissing via Back should preserve the value.

The issue is the test synchronization before invoking native `Espresso.pressBack()`.

---

# STEP 2 task

Confirm the smallest implementation scope for fixing **Group B only**.

The intended fix is test-side synchronization in:

`app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

specifically inside:

`countryPicker_cancelViaBackPress_preservesOriginalValue`

The candidate minimal change is:

- retain the existing Back-based behavior check
- retain `Espresso.pressBack()`
- do not weaken or remove the preservation assertion
- before `Espresso.pressBack()`, replace or supplement the current bare `composeRule.waitForIdle()` with an explicit `composeRule.waitUntil(...)` condition that confirms the country-picker sheet/list is truly present/settled
- model this on the already-established `SettingsScreenTest.kt` ModalBottomSheet stabilization pattern
- no arbitrary sleep
- no retry wrapper around the test
- no production code change
- no broad test-infrastructure change

Do not assume this scope blindly. Verify it against the current source and project rules.

---

# Required scope confirmation

Confirm whether the fix can be limited to exactly:

`app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

and exactly one test method:

`countryPicker_cancelViaBackPress_preservesOriginalValue`

Describe:

1. the exact synchronization behavior to add
2. whether the existing `composeRule.waitForIdle()` should remain, be replaced, or be supplemented
3. the exact sheet/list node or condition that should be polled
4. why this follows the existing `SettingsScreenTest.kt` precedent
5. why `Espresso.pressBack()` should remain
6. why production code does not need modification
7. why Group C remains unaffected

Do not provide code.

---

# Explicitly out of scope

Do not modify or analyze further:

- `referenceDate_laterThanCapture_showsOrderErrorText_de` (Group C)
- Group A country-picker tests
- `CountryPickerSheet.kt`
- `EditSessionScreen.kt`
- `EditSessionViewModel.kt`
- `CompareLabelLogic.kt`
- resources
- CompareScreen files
- Walkthrough files
- Wackelbild files

Do not introduce sleeps, retries, ignores, suppressions, or relaxed assertions.

---

# Risk assessment required

State the risk of this test-only synchronization change with respect to:

- production behavior
- system Back behavior
- ModalBottomSheet behavior
- test runtime
- full-suite flakiness
- false-positive passing behavior

Also state whether the chosen wait condition could accidentally pass before the sheet is truly ready for native back injection.

If that risk exists, explain the smallest stronger condition that still stays within the one-test scope.

---

# Verification plan required for later STEP 3

If the one-file/one-test scope is valid, propose exact verification commands for the later implementation step.

At minimum:

### Targeted Group B test

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue
```

Because this issue is intermittent, recommend an appropriate number of repeated targeted runs to gain confidence.

### Unit tests

```text
./gradlew testDebugUnitTest
```

### Full connected suite

```text
./gradlew connectedDebugAndroidTest
```

Expected full-suite result after a successful Group B fix:

- Group B disappears
- only Group C remains
- no new failures appear

### Debug build

```text
./gradlew assembleDebug
```

Do not consider the fix successful merely because one isolated run passes.

---

# Required output

Return only:

1. **Exact file to modify**
2. **Exact test method to modify**
3. **Exact synchronization behavior to add**
4. **Whether `waitForIdle()` stays, is replaced, or supplemented**
5. **Exact node/condition to poll**
6. **Why `Espresso.pressBack()` remains**
7. **Confirmation that no production file is required**
8. **Confirmation that Group C remains untouched**
9. **Risk assessment**
10. **Documentation impact**
11. **Exact STEP 3 verification plan**
12. **Any uncertainty or reason scope expansion would be required**

Do **not** modify files.

Do **not** provide implementation code.

Do **not** implement the fix.

STOP after scope confirmation and wait for explicit approval.
