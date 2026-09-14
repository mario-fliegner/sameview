# Claude Prompt — DeinWackelbild Redundant Subtitle Removal — STEP 3 IMPLEMENTATION

## Approval

STEP 2 scope is approved exactly as analyzed.

Implement **only** the isolated removal of the redundant subtitle:

`See your lenticular print in action.`

Do not expand scope.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

The implementation-plan note from STEP 2 is accepted as-is:
- `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` remains unchanged because that row is historical implementation documentation.
- Do not add it as a sixth modified file.

---

# Exact approved files

Modify exactly these five files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/main/res/values/strings.xml`
3. `app/src/main/res/values-de/strings.xml`
4. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
5. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

If any sixth tracked file becomes necessary, STOP and report before modifying it.

Untracked prompt archive files must remain untouched and uncommitted.

---

# Exact implementation

## 1. `WackelbildScreen.kt`

Inside `WackelbildInteractionHint`:

- remove only the `Spacer(modifier = Modifier.height(4.dp))` between title and subtitle;
- remove only the subtitle `Text(...)` block that uses:
  - `R.string.wackelbild_hint_subtitle`
  - `testTag("wackelbild_hint_subtitle")`

Leave unchanged:
- the surrounding `Column`;
- the primary title `Text`;
- title typography;
- title color;
- title alignment;
- title test tag;
- the position of the entire interaction-hint block;
- the existing external 16.dp spacers around the block.

Do not simplify or refactor the composable.

## 2. `values/strings.xml`

Remove only:

`wackelbild_hint_subtitle`

with value:

`See your lenticular print in action.`

No other EN string changes.

## 3. `values-de/strings.xml`

Remove only:

`wackelbild_hint_subtitle`

with value:

`Sieh dir dein Wackelbild an.`

No other DE string changes.

## 4. `WackelbildScreenTest.kt`

Inside:

`interactionHint_isDisplayedWithoutScrolling_forTallPortraitImage`

remove only the assertion for:

`wackelbild_hint_subtitle`

Keep the primary-hint assertion and all other assertions unchanged.

Do not add unrelated tests.

## 5. `DEINWACKELBILD_INTEGRATION_V1.md`

Amend only:

### §8.6
Remove the supporting/subtitle-copy requirement and the equivalent swipe-fallback supporting-copy clause.

Preserve the primary interaction hints exactly as currently specified:
- sensor branch primary hint;
- swipe fallback primary hint.

### §45
Remove only the approved subtitle-copy bullet:
- `Sieh dir dein Wackelbild an.`

Keep every other approved-copy entry unchanged.

Do not touch unrelated sections.

---

# Explicitly out of scope

Do not change:

- `Tilt your phone` placement;
- `Tilt your phone` typography;
- `Tilt your phone` color;
- interaction-hint alignment;
- preview→hint spacing;
- hint→Show-date spacing;
- swipe fallback wording;
- ridge/date-badge z-order;
- ridge opacity/spacing;
- preview border/radius;
- transfer disclosure;
- Privacy Policy;
- upload/API/handoff;
- sensor math/lifecycle;
- blend behavior;
- perspective;
- date badge;
- preview sizing;
- navigation/storage/permissions;
- implementation plan;
- unrelated code formatting/refactoring.

---

# Verification

After implementation, run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

The user explicitly approves running `connectedDebugAndroidTest` on the currently connected Samsung for this iteration.

Do not:
- suppress failures;
- disable tests;
- add lint baselines;
- hide failures;
- fix unrelated failures.

If any verification failure requires changing a sixth file or expanding scope, STOP and report.

Report exact instrumentation counts:
- total;
- passed;
- failed;
- errors;
- skipped.

---

# Manual validation

Real-device visual validation is still required after automated verification.

The expected visual result is only:

- the primary interaction hint remains;
- the subtitle is gone;
- the 4.dp internal gap is gone with it;
- no extra blank line/gap remains inside the hint block.

Do not claim the later hint-reposition/restyle task is complete.

---

# Commit

Do **not** commit automatically in this prompt unless the user explicitly asked you to do so outside this file.

For this STEP 3:
- implement;
- verify;
- report;
- leave changes unstaged/uncommitted for user visual confirmation.

Do not push.

---

# Required final report

Return:

1. initial `git status --short`;
2. exact five modified files;
3. exact change made in each;
4. confirmation no sixth tracked file changed;
5. `testDebugUnitTest` result;
6. `assembleDebug` result;
7. `lintDebug` result;
8. `connectedDebugAndroidTest` result with exact counts;
9. confirmation real-device visual validation is still required;
10. confirmation no commit was created;
11. confirmation no push occurred;
12. final `git status --short`;
13. confirmation that the next separate task remains:
   - reposition/restyle the now-single-line `Tilt your phone` hint.

Do not start that next task.
