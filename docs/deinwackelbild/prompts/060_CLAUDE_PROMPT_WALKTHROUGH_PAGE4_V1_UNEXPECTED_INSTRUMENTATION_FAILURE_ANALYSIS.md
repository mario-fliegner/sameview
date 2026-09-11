# Claude Prompt — WalkthroughScreen Unexpected Instrumentation Failure — STEP 1 Root-Cause Analysis

## STEP 1 — Analysis Only

A new, unexpected instrumentation failure appeared in the latest full real-device suite after the previously approved Group A test-host fix.

Do **not** implement any fix.

Do **not** modify any file.

Do **not** combine this with the still-open EditSession Group B or Group C issues.

This prompt concerns exactly one failing test:

- `com.isardomains.sameview.guide.WalkthroughScreenTest#page4_hasBackAndStartNoSkip`

## Current verified baseline

The immediately preceding Group A iteration modified exactly one method in exactly one file:

- `app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`
- method: `setCountryPickerSheetContent()`

The change only added the same test-host window flags already used by another helper:

- `FLAG_KEEP_SCREEN_ON`
- `setShowWhenLocked(true)`
- `setTurnScreenOn(true)`

Verification results from that iteration:

- targeted seven Group A tests → **BUILD SUCCESSFUL**, all 7 passed
- `./gradlew testDebugUnitTest` → **BUILD SUCCESSFUL**
- `./gradlew assembleDebug` → **BUILD SUCCESSFUL**
- full `./gradlew connectedDebugAndroidTest` → **BUILD FAILED**, with 3 failures total

The three remaining full-suite failures were:

1. `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue` — known Group B
2. `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de` — known Group C
3. `WalkthroughScreenTest#page4_hasBackAndStartNoSkip` — **new/unexpected**

The Walkthrough failure was **not present** in the earlier 13-failure baseline and was **not present** in the subsequent 9-failure baseline.

Therefore it must be treated as a fresh investigation target. Do not assume it is caused by the Group A change, and do not assume it is unrelated either — establish causality.

## Source-of-Truth / project rules

Before analysis, re-read:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `FIRST_RUN_WALKTHROUGH_GUIDE_V1.md`
- any active localization or responsive-layout specification that governs the walkthrough screen
- any implementation notes that cover onboarding / first-run walkthrough behavior

If code and Source-of-Truth documentation conflict, report the conflict explicitly. The active MD specification is authoritative unless clearly superseded.

## Required analysis

Perform a causal root-cause analysis for this one failing test only.

Do not guess.

### 1. Inspect the exact failure

Read the latest real-device instrumentation XML/report and per-test logcat for:

- `WalkthroughScreenTest#page4_hasBackAndStartNoSkip`

Report:

- exact assertion or exception
- exact failing source line
- expected UI state
- actual UI state
- whether the host Activity/composition existed
- whether the failure is missing node, wrong text, visibility, focus, lifecycle, timing, semantics, or something else

Do not proceed from the test name alone.

### 2. Reproduce in isolation

Run this single test alone on the connected real device:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.guide.WalkthroughScreenTest#page4_hasBackAndStartNoSkip
```

If the project's runner requires a different exact syntax, use the smallest supported equivalent.

Record whether it:

- passes in isolation,
- fails identically,
- fails differently,
- or is flaky across repeated runs.

If practical, run it at least twice in isolation so we can distinguish a deterministic defect from a one-off full-suite flake.

Do not modify code to enable this diagnosis.

### 3. Compare against nearby Walkthrough tests

Inspect the full `WalkthroughScreenTest` class and identify:

- tests immediately before and after `page4_hasBackAndStartNoSkip`
- whether they use the same test host
- whether they alter page index, orientation, locale, activity/window state, or persistent first-run state
- whether one preceding test can leak state into page 4
- whether `page4_hasBackAndStartNoSkip` depends on test order

If useful, run:

- the full `WalkthroughScreenTest` class alone
- the failing test after a fresh process
- a short sequence containing the test immediately before it plus the failing test

Record exact commands and outcomes.

### 4. Inspect test-host lifecycle and shared state

Audit `WalkthroughScreenTest.kt` for:

- Compose test rule type
- `ActivityScenario`
- `setContent`
- scenario close / teardown
- persistent preferences / DataStore
- saved instance state
- remembered page state
- static/global state
- orientation changes
- locale changes
- screen wake / lock flags
- `waitForIdle` / `waitUntil`
- animations / pager transitions
- node lookup semantics
- test ordering assumptions

Determine whether the newly introduced Group A window-flag change in another test class could plausibly affect this test through global device/window state.

Do not claim causality without a concrete mechanism and evidence.

### 5. Inspect production Walkthrough behavior

Trace the current implementation for page 4.

Verify against `FIRST_RUN_WALKTHROUGH_GUIDE_V1.md`:

- which controls page 4 must show
- whether Back must be present
- whether Start must be present
- whether Skip must be absent
- exact labels / localization keys
- whether the final page index is still page 4
- whether responsive layout can hide or relocate one of these controls
- whether state transitions or animations delay their composition

Identify whether the production UI is correct and the test is stale, or whether production behavior violates the active spec.

### 6. Git provenance

Use `git log` / `git blame` for:

- the failing test
- relevant Walkthrough production code
- relevant string resources
- any recent walkthrough/layout/localization commits

Determine:

- when the test was introduced
- when the relevant production behavior last changed
- whether any commit since the last green connected baseline could explain the failure
- whether the Group A iteration touched anything in this path indirectly (expected: likely no, but verify)

### 7. Determine failure classification

Classify the failure as one of:

- production defect
- test defect
- test fixture defect
- lifecycle/window-state defect
- timing/flakiness issue
- stale assertion after intended behavior change
- spec/code mismatch
- unresolved

Do not force a classification if evidence is insufficient.

## Scope discipline

Do not touch:

- EditSession Group B
- EditSession Group C
- Wackelbild files
- CompareScreen files
- Group A test-host fix
- unrelated walkthrough tests

Do not propose a broad walkthrough refactor.

## Required output

Return only:

1. **Exact observed failure**
2. **Whether it reproduces in isolation**
3. **Exact root cause, if proven**
4. **Evidence**
5. **Failure classification**
6. **Whether the latest Group A change is causal, non-causal, or unresolved**
7. **Relevant Source-of-Truth result**
8. **Git/history provenance**
9. **Minimal fix strategy**
10. **Exact file(s) that would need modification**
11. **Diagnostic commands run and exact results**
12. **Remaining uncertainty**

Do **not** provide code.

Do **not** modify files.

Do **not** apply any fix.

STOP after the analysis and wait for explicit scope approval.
