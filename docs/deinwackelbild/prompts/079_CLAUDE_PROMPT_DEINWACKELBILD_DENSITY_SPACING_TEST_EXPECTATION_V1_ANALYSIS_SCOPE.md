# Claude Prompt — DeinWackelbild Density Spacing Test Expectation — STEP 1 + STEP 2 ONLY

## Goal

Handle exactly one isolated test-regression issue discovered during the subtitle-removal verification:

`WackelbildScreenTest.dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent`

failed on the real connected Samsung with:

```text
java.lang.AssertionError: expected:<24.0> but was:<16.0>
```

1085 instrumentation tests were started, 1 was skipped, and exactly 1 failed.

This prompt is **STEP 1 ANALYSIS + STEP 2 SCOPE CONFIRMATION ONLY**.

Do not implement anything.
Do not modify files.
Do not stage, commit, or push.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

## Critical working-tree constraint

A separate subtitle-removal STEP 3 has already modified five files and those changes must remain untouched while this test issue is analyzed:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/main/res/values/strings.xml`
3. `app/src/main/res/values-de/strings.xml`
4. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
5. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Important: `WackelbildScreenTest.kt` therefore already contains an unrelated uncommitted subtitle-removal change. Do not overwrite, revert, stage, or alter that existing change during this prompt.

The purpose here is to determine whether the failing spacing expectation itself can later be corrected as one surgically isolated additional hunk in that same test file.

---

# STEP 1 — Analysis

Inspect:

- current `git status --short`;
- current diff of `WackelbildScreenTest.kt`;
- committed baseline around the failing test;
- current `WackelbildScreen.kt` layout relevant to the measured positions;
- the portrait-density commit/history if needed;
- relevant Source-of-Truth layout documentation.

Analyze the exact failing test:

`dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent`

Also compare it with:

`dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperPresent`

Determine precisely:

1. What two coordinates/nodes each test measures.
2. Why the helper-absent test currently expects `24f`.
3. Why its tolerance is currently `4f` (or whatever the current code actually contains).
4. Why the real Samsung reports exactly `16.0`.
5. Why the helper-present test passes.
6. Whether there is any legitimate layout reason for helper-absent and helper-present cases to have different intended group spacing.
7. Whether the production implementation and current Source of Truth intend exactly 16.dp between the date group and interaction-hint group.
8. Whether the failure is definitely a stale/incorrect test expectation rather than a production-layout defect.

Do not assume the proposed fix is `24 → 16` until the code/spec analysis proves it.

In particular, determine the **smallest justified tolerance** for a Compose instrumentation assertion of this geometry. Do not mechanically preserve `4f` if that tolerance was only compensating for a mistaken assumption. Conversely, do not make the assertion unrealistically brittle if density/layout-coordinate rounding genuinely warrants tolerance.

The test must verify the intended 16.dp contract meaningfully rather than merely be loosened until it passes.

---

# Source-of-Truth check

Inspect the relevant current section of:

`docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

and any directly relevant responsive/layout contract.

Determine whether any documentation change is needed.

Expected direction: if the existing documentation already correctly specifies the intended 16.dp spacing, **do not modify documentation** merely because a test expectation was wrong.

---

# Scope discipline

This issue must not change production behavior.

Do not propose changes to:

- `WackelbildScreen.kt`;
- actual Spacer values;
- preview sizing;
- interaction-hint placement/style;
- subtitle removal;
- ridge/date-badge z-order;
- strings;
- transfer disclosure;
- accessibility semantics;
- any other tests unless directly necessary to correct this exact false expectation;
- documentation unless the Source of Truth is actually wrong.

Do not revert or modify the currently pending subtitle-removal changes.

---

# STEP 2 — Scope confirmation

If and only if STEP 1 proves that the test expectation is wrong, propose the minimal correction.

Preferred scope should be exactly one file:

`app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`

Within that file, identify the exact assertion values that need changing and explain why.

Because this file already contains an unrelated subtitle-removal hunk, explicitly explain how the spacing-test correction will later be kept logically isolated and how no existing subtitle-removal lines will be altered as part of this fix.

If any second file is required, explain why and STOP for user approval.

---

# Verification plan for later STEP 3

Specify the exact verification sequence.

At minimum it should include:

1. the affected instrumentation test if Gradle/test filtering supports running it reliably on the connected device;
2. `./gradlew testDebugUnitTest`
3. `./gradlew assembleDebug`
4. `./gradlew lintDebug`
5. `./gradlew connectedDebugAndroidTest`

The user explicitly authorizes `connectedDebugAndroidTest` on the connected Samsung for the later implementation iteration.

Do not suppress failures, loosen assertions merely to obtain green tests, add baselines, or fix unrelated failures.

Real-device visual validation is not expected to be necessary for a test-only correction that changes no production code, but confirm this from the analysis.

---

# Required output

Return:

1. current `git status --short`;
2. exact current unrelated subtitle-removal hunk in `WackelbildScreenTest.kt`;
3. exact failing spacing-test code and measurement method;
4. root cause of `expected 24.0 / actual 16.0`;
5. comparison with the helper-present test;
6. intended spacing according to production code and Source of Truth;
7. whether production code is correct;
8. exact justified expected value and tolerance for the corrected assertion;
9. whether documentation needs any change;
10. exact STEP 2 file scope;
11. confirmation no existing subtitle-removal change will be touched;
12. risks;
13. exact later verification commands;
14. whether real-device visual validation is required;
15. any blocker requiring user input.

Then STOP and wait for explicit approval.

No implementation.
No file modifications.
No staging.
No commit.
No push.
