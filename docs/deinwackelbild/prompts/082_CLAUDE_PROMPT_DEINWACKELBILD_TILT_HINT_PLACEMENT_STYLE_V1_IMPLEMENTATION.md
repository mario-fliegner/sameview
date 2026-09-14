# Claude Prompt — DeinWackelbild Tilt Hint Placement & Style — STEP 3 IMPLEMENTATION

## Approval

STEP 2 is approved.

Implement exactly one isolated UI fix:

> Move the now-single-line interaction hint directly below the preview and make it visually subordinate.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

Do not expand scope.

---

# Approved UX

The final vertical order must be:

1. Preview
2. `8.dp`
3. primary interaction hint (`Tilt your phone` or the existing swipe fallback)
4. `16.dp`
5. `Show date` row
6. optional existing date-unavailable helper
7. existing transfer disclosure
8. existing CTA

The hint must use:

- `MaterialTheme.typography.bodySmall`
- `SameViewSettingsSecondaryText`
- centered alignment
- existing wording
- existing sensor/swipe branching
- existing `wackelbild_hint_title` test tag
- no icon / arrow / glyph
- no subtitle

---

# Known pending working-tree changes

The working tree already contains the previously implemented subtitle-removal changes plus the isolated spacing-test expectation correction.

Do not revert or disturb those changes.

In particular, `WackelbildScreenTest.kt` already contains independent pending hunks. Add only the newly approved hint-placement/style test changes.

---

# Exact approved file scope

Modify exactly these three files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
3. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

If a fourth tracked file becomes necessary, STOP and report before modifying it.

---

# 1. `WackelbildScreen.kt`

Relocate `WackelbildInteractionHint(...)` from its current position inside the scrollable lower-controls `Column`.

Place it in the outer non-scrolling `Column`, immediately after `WackelbildPreview(...)`.

Use this hierarchy:

```text
WackelbildPreview(...)
Spacer(8.dp)
WackelbildInteractionHint(...)
Spacer(16.dp)
scrollable lower-controls Column
    WackelbildDateToggleRow(...)
    existing lower content...
```

At the old hint location:

- remove the old `Spacer(16.dp)` that separated the date group from the hint;
- remove the old hint call;
- preserve the existing `Spacer(16.dp)` that then separates the date/date-helper group from the transfer disclosure.

Do not alter preview sizing formulas, `weight(1f, fill = false)`, scrolling behavior, date helper behavior, transfer disclosure, or CTA.

## Hint styling

Inside `WackelbildInteractionHint`, change only the primary `Text` styling from:

```kotlin
style = MaterialTheme.typography.titleMedium
```

to:

```kotlin
style = MaterialTheme.typography.bodySmall,
color = SameViewSettingsSecondaryText
```

Preserve its centered alignment and existing branch logic.

At the relocated call site, use:

```kotlin
modifier = Modifier.widthIn(max = contentMaxWidth)
```

as confirmed in STEP 2, so the hint remains in the same responsive content lane.

Do not introduce custom measurement, `SubcomposeLayout`, device-specific branches, or new state.

---

# 2. `WackelbildScreenTest.kt`

Update only tests whose geometry contract changes because the hint moves.

Replace the obsolete tests:

- `dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperPresent`
- `dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent`

with tests for the new hierarchy:

1. `previewToHintGap_isApproximatelyEightDp`
   - measure bottom of `wackelbild_reference_preview_container`
   - measure top of `wackelbild_hint_title`
   - assert approximately `8.dp`
   - choose a tight, justified tolerance consistent with existing real-device geometry tests.

2. `hintToDateGroup_spacingIsApproximatelySixteenDp`
   - measure bottom of `wackelbild_hint_title`
   - measure top of `wackelbild_date_toggle`
   - assert approximately `16.dp`
   - use a similarly justified tolerance.

Do not change:

`interactionHint_isDisplayedWithoutScrolling_forTallPortraitImage`

unless compilation proves a mechanical update is required. Its remaining primary-hint assertion is still valid.

Do not modify unrelated tests.

The existing CTA/no-scroll tests remain unchanged and will be exercised by the class-scoped run.

---

# 3. `DEINWACKELBILD_INTEGRATION_V1.md`

Amend only §9.9.

Update the hierarchy so it no longer says the date row is directly below the preview or that the interaction hint follows the date row.

Document qualitatively:

- the interaction hint is directly below the preview;
- the date row follows below the interaction hint;
- the interaction hint is visually subordinate: centered, small body text, secondary text color.

**Do not put literal `8.dp` / `16.dp` implementation constants into the Source-of-Truth document.**

This resolves the minor STEP-2 choice: use qualitative wording for consistency with the rest of the UX contract.

Do not touch §8.6 or §45; they already reflect the subtitle removal.

---

# Explicitly out of scope

Do not change:

- ridge/date-badge z-order;
- ridge opacity/spacing;
- preview border/radius;
- preview sizing;
- subtitle removal beyond preserving the existing pending change;
- sensor logic;
- swipe fallback wording;
- blend behavior;
- perspective;
- date badge styling;
- date-toggle behavior;
- transfer disclosure;
- Privacy Policy;
- upload/API/handoff;
- navigation/storage/permissions;
- unrelated strings;
- unrelated docs;
- unrelated formatting/refactoring.

---

# Verification — SMALL-SCOPE RULE

Do **not** run the full project test suite.

Do **not** run full:

- `connectedDebugAndroidTest`
- `testDebugUnitTest`
- `assembleDebug`
- `lintDebug`

For this isolated screen-level UI change, run only the relevant instrumentation class on the connected Samsung:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.wackelbild.WackelbildScreenTest
```

This is the approved verification scope.

If this class-scoped run fails:

- report the exact failing test(s);
- only fix a failure if it is directly caused by this approved hint-placement/style change and remains inside the approved three-file scope;
- otherwise STOP and report;
- do not escalate automatically to the full suite.

Report exact class-run totals: started, passed, failed, errors, skipped.

---

# Real-device validation

After the class-scoped automated run, manual validation on the Samsung is required.

Verify visually:

- hint is directly below preview;
- preview → hint gap looks like the intended subtle 8dp separation;
- hint → Show date gap is 16dp;
- hint is visibly smaller/subordinate rather than heading-like;
- secondary text color is correct;
- hint remains centered;
- portrait CTA remains visible without routine scrolling;
- landscape layout remains sensible.

Also verify TalkBack reading order if practical:

preview → interaction hint → date controls.

Do not claim the fix is visually accepted until the user confirms it.

---

# Commit handling

Do not stage.
Do not commit.
Do not push.

Leave the implementation available for user visual confirmation.

---

# Required final report

Return:

1. initial `git status --short`;
2. exact three files modified by this iteration;
3. exact production-code move;
4. exact styling change;
5. exact tests replaced and their new assertions/tolerances;
6. exact §9.9 documentation amendment;
7. confirmation existing subtitle-removal and spacing-test pending changes were preserved;
8. class-scoped `WackelbildScreenTest` result with exact counts;
9. confirmation no full project suite was run;
10. confirmation no fourth tracked file was modified;
11. manual real-device checks still required;
12. final `git status --short`;
13. confirmation nothing was staged;
14. confirmation no commit was created;
15. confirmation no push occurred.

Then STOP.

Do not start the ridge/date-badge z-order fix.
