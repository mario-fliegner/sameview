# Claude Prompt — Restore a Stable Baseline, Resolve the Blocking Instrumentation Failures, Verify, and Commit Once

## Goal

We have spent too long branching into individual test investigations. The goal of this prompt is to get the repository back to a **stable, committed baseline** so development can immediately return to the DeinWackelbild preview/UI and then continue the DeinWackelbild workflow.

This prompt is therefore a controlled stabilization task for the currently blocking instrumentation failures.

Do **not** work on Wackelbild UI behavior itself in this prompt.

Do **not** add unrelated improvements.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and the project Source-of-Truth documents.

---

# Current state

There has been **no commit since prompt 048**.

The working tree currently contains successful, intended work that must be preserved, including the already-approved Wackelbild Block 3 work and subsequent successful fixes.

Known intended changes include at least:

## Wackelbild Block 3
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
- `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapper.kt`
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapperTest.kt`

## CompareScreen locale fix
- `app/src/main/java/com/isardomains/sameview/ui/compare/CompareScreen.kt`

## EditSession Group A instrumentation-host fix
- `app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`
- the successful `setCountryPickerSheetContent()` wake/show-window flags must remain

## Approved strings/docs
There are also approved Wackelbild/spec/string changes in the current working tree from the work since prompt 048. Audit them against the actual diff and preserve only the approved project changes.

The failed prompt-064 Group B `onChildren()` synchronization experiment has already been removed and must **not** be reintroduced.

Prompt-archive `.md` files are conversation artifacts and must **not** be committed unless they are actual repository documentation already intended by the project.

---

# Current blocking instrumentation behavior

Recent full real-device suites have shown unstable failures.

Known unresolved EditSession tests:

- `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`
  - intermittent `RootViewWithoutFocusException` around `Espresso.pressBack()`
  - prior evidence showed transient loss of Activity window focus on the Samsung real device

- `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de`
  - recurrent failure where the expected validation error is not displayed
  - prior evidence implicated IME/window/lifecycle timing

- `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_en`
  - failed in the latest full suite with the same user-visible behavior class (`is not displayed`)
  - previously passed, so determine whether it shares the same synchronization/lifecycle cause

A previous VideoExport ~50-minute hang disappeared on the next full run:
- the same test then passed in ~33 seconds
- 1079/1079 results were received
Therefore do not change VideoExport unless it independently reproduces during this stabilization work.

The latest full run completed with 1079/1079 tests and exactly:
- DE reference-date error test failing
- EN reference-date error test failing

Group B happened to pass that run but is known intermittent.

---

# User-approved stabilization scope

For this prompt, you are authorized to:

1. inspect the three blocking EditSession instrumentation tests above and the directly relevant test-host/synchronization code;
2. establish their actual root cause(s);
3. make the **smallest test-side stabilization changes necessary** to make those existing tests reliable on the real connected device;
4. modify production code **only if concrete evidence proves a real production defect** rather than a test synchronization/lifecycle problem;
5. run targeted repeated tests and the full verification suite;
6. if verification is clean, create **one commit** containing all intended successful work since prompt 048.

This authorization does **not** permit unrelated cleanup, refactoring, UI redesign, architecture changes, naming changes, or Wackelbild feature changes.

Keep all changes surgical.

---

# Source of Truth

Before changing anything, inspect the relevant current specifications, especially:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- the active session metadata/editor specification
- `SESSION_METADATA_V1.md` or its current superseding document
- relevant localization specification
- Wackelbild integration/implementation-plan docs for auditing the existing Block 3 diff

The expected product contracts must remain unchanged:

- cancelling/dismissing the country picker preserves the original country value;
- a reference date later than the capture date is rejected and shows the correct localized validation error;
- EN and DE behavior must be equivalent apart from localized text;
- no Wackelbild behavior is to be changed in this stabilization task.

If code and Source of Truth conflict, report it before implementation.

---

# Phase 1 — Audit and diagnose

First capture:

```text
git status --short
git diff --stat
git diff
```

Confirm the current tree and ensure no successful work is lost.

Then diagnose the three EditSession tests as one **instrumentation-stability blocker** because they prevent establishing the stable baseline needed to continue feature work.

Use the existing evidence, current source, test logs, and targeted real-device reproduction.

For each failing test determine the exact failing mechanism.

You may run targeted tests repeatedly before editing.

For the two reference-date tests, compare EN and DE directly and determine whether they share the same lifecycle/IME/render synchronization cause.

For Group B, use the existing window-focus evidence and determine the smallest reliable test-side way to preserve the existing native Back intent without arbitrary sleeps.

Do not guess.

---

# Phase 2 — Implement only the minimum proven stabilization

Once the cause is established from evidence, implement only the minimum required stabilization in the directly affected instrumentation test file(s).

Preferred direction if supported by evidence:

- synchronize with the actual condition the test depends on;
- preserve `Espresso.pressBack()` if it can be made reliable by waiting for the Activity/root to regain focus;
- for date-validation tests, synchronize on the actual post-IME/lifecycle/UI condition rather than using arbitrary delays;
- keep assertions and product behavior unchanged.

Forbidden:

- `Thread.sleep`
- arbitrary fixed delays
- retry-until-pass wrappers around assertions
- ignoring tests
- disabling tests
- weakening assertions
- changing expected strings to hide failures
- suppressions/baselines
- changing production behavior merely to satisfy instrumentation
- unrelated refactoring

If one of the three failures proves to be a real production defect, stop before changing production code and report the evidence and exact required production scope. Do not silently broaden into production changes.

---

# Phase 3 — Targeted real-device verification

After the minimal stabilization, run each affected test repeatedly on the connected real device.

At minimum:

## Group B
Run:
`countryPicker_cancelViaBackPress_preservesOriginalValue`

**10 consecutive times.**

## Reference-date EN
Run:
`referenceDate_laterThanCapture_showsOrderErrorText_en`

**5 consecutive times.**

## Reference-date DE
Run:
`referenceDate_laterThanCapture_showsOrderErrorText_de`

**5 consecutive times.**

Every targeted run must pass.

If any targeted run fails:
- inspect the failure;
- if the same proven mechanism was not actually addressed, refine only that stabilization;
- do not branch into unrelated issues;
- do not commit until the targeted tests are reliable.

Do not use arbitrary retries merely to obtain a green result.

---

# Phase 4 — Full verification

Once all targeted repetitions pass, run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

For `connectedDebugAndroidTest`, the required result is now:

- full suite completes normally;
- expected test count equals received test count;
- **zero failures**;
- no missing-result anomaly;
- no VideoExport hang;
- no new unexpected failure.

If the full suite produces a clearly unrelated one-off infrastructure/device anomaly, rerun the **specific anomalous test once** to establish whether it reproduces. Do not modify unrelated code. If it reproduces, stop and report it. If it passes and the evidence clearly identifies a runner/device anomaly, rerun the full suite once. Do not enter an endless rerun loop.

The objective is a genuinely stable green baseline, not a lucky pass.

---

# Phase 5 — Final diff audit

If verification is green, run:

```text
git status --short
git diff --stat
git diff
```

Audit every file.

The final commit may contain:

- the approved successful work since prompt 048;
- the minimal instrumentation stabilization implemented in this prompt;
- approved project Source-of-Truth documentation already changed as part of the Wackelbild work.

It must not contain:

- prompt-archive conversation `.md` files;
- failed experimental changes;
- unrelated files;
- generated build/test artifacts;
- accidental formatting changes.

If anything unexpected remains, clean only the accidental/unapproved change and re-run whatever verification is invalidated by that cleanup.

---

# Phase 6 — Commit once

When and only when the final baseline is green:

```text
git add <only intended project files>
git status --short
git diff --cached --stat
git diff --cached
```

Verify the staged diff.

Then create **exactly one commit** for the successful work since prompt 048.

Use a concise message accurately describing the final diff, for example:

```text
Implement lenticular preview and stabilize instrumentation tests
```

Do not amend.

Do not create multiple commits.

Do not push.

After commit:

```text
git status --short
git rev-parse --short HEAD
git show --stat --oneline --summary HEAD
```

The tracked project working tree should be clean. Untracked prompt-archive files may remain, but they must not be part of the commit.

---

# Required final report

Report:

1. initial working-tree state;
2. exact root cause found for Group B;
3. exact root cause found for EN reference-date test;
4. exact root cause found for DE reference-date test;
5. whether EN/DE shared one mechanism;
6. exact files modified during stabilization;
7. exact stabilization changes;
8. confirmation no product behavior/assertion was weakened;
9. Group B targeted results — all 10 runs;
10. EN targeted results — all 5 runs;
11. DE targeted results — all 5 runs;
12. `testDebugUnitTest` result;
13. `assembleDebug` result;
14. `lintDebug` result;
15. full `connectedDebugAndroidTest` result, count and runtime;
16. any infrastructure anomaly encountered and how it was proven non-code-related;
17. complete final staged file list;
18. confirmation prompt-archive files were excluded;
19. commit message;
20. commit hash;
21. `git show --stat --oneline --summary HEAD`;
22. final `git status --short`;
23. confirmation no push occurred;
24. whether the repository is now ready to resume DeinWackelbild preview/UI work.

If a real production defect is proven or a stable green baseline cannot be achieved without broadening scope, **do not commit**. Stop and report the exact blocker and smallest next required scope.

The purpose of this prompt is to finish stabilization and return development to the DeinWackelbild workflow, not to create another chain of unrelated investigations.
