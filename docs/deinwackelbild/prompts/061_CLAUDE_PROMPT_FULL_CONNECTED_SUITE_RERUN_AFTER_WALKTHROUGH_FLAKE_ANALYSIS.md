# Claude Prompt — Full Connected Suite Re-Run After Walkthrough Flake Analysis

## Verification Only — No Code Changes

The one-off failure in:

- `com.isardomains.sameview.guide.WalkthroughScreenTest#page4_hasBackAndStartNoSkip`

was analyzed and **did not reproduce**:

- isolated run #1 → BUILD SUCCESSFUL
- isolated run #2 → BUILD SUCCESSFUL
- full `WalkthroughScreenTest` class (46 tests) → BUILD SUCCESSFUL

No production or test defect was proven, so **no file change is approved** for the walkthrough issue.

The latest known full-suite state before this verification step was:

- Group A country-picker host fix: implemented and targeted 7/7 tests green
- `testDebugUnitTest`: green
- `assembleDebug`: green
- full `connectedDebugAndroidTest`: 3 failures
  - Group B: `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`
  - Group C: `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de`
  - one-off walkthrough flake: `WalkthroughScreenTest#page4_hasBackAndStartNoSkip`

## Task

Run the full connected instrumentation suite again on the real connected device:

```text
./gradlew connectedDebugAndroidTest
```

Do **not** modify any file before, during, or after this run.

Do **not** attempt any fix.

Do **not** change test assertions.

Do **not** add retries, sleeps, waits, suppressions, ignores, or annotations.

Do **not** touch Group B or Group C in this step.

## Required evaluation

After the run, report exactly:

1. Overall Gradle result.
2. Total test count.
3. Exact failure count.
4. Exact failing test names.
5. Whether `WalkthroughScreenTest#page4_hasBackAndStartNoSkip` failed again or passed.
6. Whether the two known EditSession failures (Group B and Group C) remain unchanged.
7. Whether any new unexpected failure appeared.
8. Exact runtime of the full suite.
9. Confirmation that no file was modified.

## Decision rule

- If the walkthrough test passes and only Group B + Group C remain, classify the walkthrough incident as a non-reproducing one-off flake for now. Do not change walkthrough code/tests.
- If the walkthrough test fails again, STOP and report it as a recurring intermittent failure requiring a separate new analysis iteration.
- If any different unexpected failure appears, STOP and report it exactly; do not investigate or fix it in this verification step.
- If the full suite is green except for Group B + Group C, the next development step should return to those remaining EditSession issues, one at a time.

Do not make any code or test changes.

STOP after reporting the full-suite result.
