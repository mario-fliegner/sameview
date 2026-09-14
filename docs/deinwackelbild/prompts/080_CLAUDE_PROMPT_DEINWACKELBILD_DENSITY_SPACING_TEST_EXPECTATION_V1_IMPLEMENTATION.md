# Claude Prompt — DeinWackelbild Density Spacing Test Expectation — STEP 3 IMPLEMENTATION

## Approval

STEP 1 + STEP 2 are approved exactly as analyzed.

Implement **only** the isolated correction of the false spacing expectation in:

`dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent`

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

Do not expand scope.

---

# Critical working-tree constraint

A separate subtitle-removal STEP 3 is still pending in the working tree.

The following existing uncommitted changes must remain untouched:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/main/res/values/strings.xml`
3. `app/src/main/res/values-de/strings.xml`
4. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
5. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Within `WackelbildScreenTest.kt`, the existing subtitle-removal hunk is the deletion of:

`composeRule.onNodeWithTag("wackelbild_hint_subtitle").assertIsDisplayed()`

inside:

`interactionHint_isDisplayedWithoutScrolling_forTallPortraitImage`

Do not alter, revert, restage, or otherwise modify that existing hunk as part of this test-only correction.

The new fix must be an independent second hunk in the same file.

---

# Exact approved file scope

Modify exactly one tracked file:

`app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`

No production files.
No string files.
No docs.
No implementation-plan changes.

If any second tracked file appears necessary, STOP and report before modifying it.

---

# Exact implementation

Inside:

`dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent`

make exactly these changes:

1. Replace the incorrect assertion:

```kotlin
assertEquals(24f, hintTop - groupBottom, 4f)
```

with:

```kotlin
assertEquals(16f, hintTop - groupBottom, 3f)
```

Rationale:
- production code contains one unconditional `Spacer(height = 16.dp)`;
- the real Samsung measured exactly `16.0`;
- the structurally parallel helper-present test already uses `16f` with `3f` tolerance and passes on the same device;
- there is no valid hidden-padding reason for a 24dp expectation.

2. Rewrite only the immediately preceding explanatory comment that currently describes the disproven 24dp / hidden-padding theory.

The replacement comment should be concise and factual:
- both helper-present and helper-absent paths are intended to have the same 16dp group spacing;
- the tagged `SettingsSwitchRow` bounds on the real device include the row height as measured;
- therefore the assertion directly checks the intended 16dp gap.

Do not add speculative Compose behavior claims.

Do not change the measurement nodes or test structure.

---

# Explicitly out of scope

Do not change:

- any production layout code;
- any Spacer value;
- interaction-hint placement/style;
- subtitle-removal implementation;
- any other assertion;
- any other test function;
- strings;
- docs;
- ridges;
- border;
- transfer disclosure;
- accessibility;
- preview sizing;
- sensor/blend/perspective behavior;
- navigation/storage/permissions;
- unrelated formatting or cleanup.

---

# Verification

First run the affected test in isolation if supported by the current Gradle/device setup:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.wackelbild.WackelbildScreenTest#dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent
```

Then run the full required sequence:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

The user explicitly authorizes `connectedDebugAndroidTest` on the connected Samsung.

Do not:
- suppress failures;
- disable tests;
- loosen unrelated assertions;
- add lint baselines;
- fix unrelated failures.

If any failure requires a second tracked file or broader scope, STOP and report.

Report exact instrumentation totals:
- total started;
- passed;
- failed;
- errors;
- skipped.

---

# Commit handling

Do **not** commit automatically in this prompt.

Reason: `WackelbildScreenTest.kt` currently contains two independent uncommitted hunks:
- the earlier subtitle-removal hunk;
- this spacing-test correction hunk.

For this prompt:
- implement the new hunk;
- verify;
- report;
- leave everything unstaged/uncommitted.

Do not push.

A later controlled commit step can separate the test-only correction from the subtitle-removal work if desired.

---

# Real-device validation

No additional visual validation is required for this specific fix because it changes no production code or pixels.

The full connected instrumentation run is the relevant validation.

Do not claim the subtitle-removal iteration is complete merely because the suite is green; that is still a separate pending change requiring its own closure.

---

# Required final report

Return:

1. initial `git status --short`;
2. confirmation that only one new tracked-file hunk was added;
3. exact before/after assertion;
4. exact rewritten comment;
5. confirmation the existing subtitle-removal hunk remained untouched;
6. isolated affected-test result;
7. `testDebugUnitTest` result;
8. `assembleDebug` result;
9. `lintDebug` result;
10. full `connectedDebugAndroidTest` result with exact counts;
11. confirmation no production/doc/string file was changed by this fix;
12. final `git diff -- app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`, clearly distinguishing the two independent hunks;
13. confirmation no staging occurred;
14. confirmation no commit was created;
15. confirmation no push occurred;
16. remaining next step: return to the already-implemented subtitle-removal iteration and close it separately.

Do not start any other fix.
