# Claude Prompt — EditSession Group B Back-Press Focus Failure — STEP 1 Root-Cause Analysis

## STEP 1 — Analysis Only

The full real-device instrumentation suite has now been re-run after the Group A test-host fix and the walkthrough flake verification.

Current verified state:

- `./gradlew connectedDebugAndroidTest`
- 1079 tests total
- exactly 2 failures
- no unexpected failures
- walkthrough test passed
- no file was modified during the verification run

The only remaining failures are:

1. `com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`
2. `com.isardomains.sameview.ui.compare.EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de`

This prompt concerns **Group B only**:

- `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`

Do **not** analyze or modify Group C in this iteration.

Do **not** implement any fix yet.

Do **not** modify any file.

## Known prior observations

The earlier root-cause analysis found:

- the Group B test fails with `RootViewPicker$RootViewWithoutFocusException`
- the failure occurs at `Espresso.pressBack()`
- the test uses `CountryPickerSheet`
- `CountryPickerSheet` is implemented with Material3 `ModalBottomSheet`
- the failure reproduced in isolation on the real SM-S911B
- therefore it is not dependent on cross-test contamination
- the Group A host-window flag fix did not resolve this test
- the latest full suite still fails in exactly this Group B test with the same signature

The previous analysis suggested a possible test-side synchronization gap around `Espresso.pressBack()` and the modal sheet window, but this was **not proven to implementation-ready certainty**.

The purpose of this prompt is to establish the exact cause and the smallest justified fix.

## Source-of-Truth / project rules

Before analysis, re-read:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- the active Edit Session / session-metadata specification
- any active localization specification covering the country picker
- any UI/UX specification that governs dismissing the country picker via system Back

If no specification explicitly covers system Back behavior, state that clearly.

Do not infer or change product behavior merely to make the test pass.

## Required analysis

Perform a causal investigation of this one test only.

### 1. Inspect the exact failing test path

Read:

- `EditSessionScreenTest.kt`
- the full `countryPicker_cancelViaBackPress_preservesOriginalValue` test
- all helpers it calls
- `CountryPickerSheet.kt`
- the production state/callback path that preserves the original country value on cancel/dismiss

Trace the exact sequence:

- host/activity setup
- current/original country value setup
- opening the country picker
- sheet composition
- any animation or focus transition
- `Espresso.pressBack()`
- sheet dismissal callback
- post-dismiss assertion that the original value is preserved

Identify the exact line throwing `RootViewWithoutFocusException`.

### 2. Determine which window owns focus

Use the existing real-device environment and, if necessary, targeted logging/inspection commands that do not require modifying source files.

Determine at the moment immediately before `Espresso.pressBack()`:

- which root/window exists
- whether the host Activity window has focus
- whether the ModalBottomSheet window has focus
- whether the IME is open
- whether another system window owns focus
- whether focus changes after `composeRule.waitForIdle()`

Do not stop at "ModalBottomSheet uses another window". Establish the actual observed window/focus state in this test.

### 3. Reproduce and characterize timing

Run the single test repeatedly on the connected real device.

At minimum:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.compare.EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue
```

Run enough repetitions to determine whether the failure is:

- deterministic,
- timing-dependent,
- or intermittent.

Record exact outcomes.

If every isolated run fails identically, treat it as deterministic.

### 4. Compare with other back-dismiss tests

Search the instrumentation suite for tests that successfully dismiss:

- `ModalBottomSheet`
- dialogs
- popups
- country pickers
- other Compose modal surfaces

especially tests that use:

- `Espresso.pressBack()`
- Compose back semantics
- `ActivityScenario.onActivity { ... }`
- `UiDevice.pressBack()`
- `onDismissRequest`

Determine whether this codebase already has an established reliable pattern.

Do not introduce a new testing approach if an existing project convention already solves the same case.

### 5. Test the smallest diagnostic alternatives

Without modifying committed source files, use the smallest diagnostic experiments available to establish what would actually solve the failure.

Examples, only if appropriate and possible in the environment:

- wait for a specific sheet node to be displayed before back
- `composeRule.waitForIdle()` immediately before back
- wait for root/window focus before back
- use the project's existing alternative back-action mechanism, if one already exists elsewhere
- confirm that dismissal invokes `onDismissRequest`
- confirm that original value preservation works once dismissal succeeds

Do **not** add arbitrary sleeps as a proposed fix.

Do **not** weaken the assertion.

Do **not** bypass testing Back behavior if the spec/product contract requires system Back dismissal.

### 6. Separate product behavior from test-harness behavior

Determine conclusively whether:

- production dismissal/preservation behavior is wrong,
- or production behavior is correct and only the test's Back injection/synchronization is defective.

Verify what happens when the sheet is dismissed successfully.

The required functional contract is:

- cancelling/dismissing the picker via Back must preserve the original country value, if that is what the active product specification requires.

If the specification does not explicitly require Back dismissal, determine whether the current test still represents intended behavior based on code/history.

### 7. Git/history provenance

Use `git log` / `git blame` for:

- `countryPicker_cancelViaBackPress_preservesOriginalValue`
- the relevant helper(s)
- `CountryPickerSheet.kt`
- any back/dismiss handling involved

Determine:

- when the test was introduced
- when ModalBottomSheet implementation was introduced
- whether the test ever had a known-green real-device implementation state
- whether any later Material3/Compose dependency upgrade may plausibly have changed window/focus behavior

Do not blame a dependency upgrade without evidence.

### 8. Minimal-fix decision

At the end, identify the smallest fix that is actually supported by evidence.

Potential classes of fix include:

- test-side synchronization change
- test-side back-injection mechanism change
- production `onDismissRequest` handling fix
- fixture/lifecycle fix

Do not prescribe more than one unless the evidence proves multiple independent defects.

## Scope discipline

Do not touch:

- Group C: `referenceDate_laterThanCapture_showsOrderErrorText_de`
- Group A helper fix
- Walkthrough tests
- Wackelbild files
- CompareScreen locale fix
- unrelated country-picker tests

Do not refactor test infrastructure broadly.

## Required output

Return only:

1. **Exact failing line and exception**
2. **Observed window/focus state at failure**
3. **Whether the failure is deterministic**
4. **Exact root cause, if proven**
5. **Evidence**
6. **Production defect vs. test defect classification**
7. **Whether system Back dismissal is required by Source of Truth**
8. **Minimal fix strategy**
9. **Exact file(s) that would need modification**
10. **Whether documentation must change**
11. **Diagnostic commands run and exact results**
12. **Remaining uncertainty**

Do **not** provide implementation code.

Do **not** modify files.

Do **not** apply any fix.

STOP after the analysis and wait for explicit scope approval.
