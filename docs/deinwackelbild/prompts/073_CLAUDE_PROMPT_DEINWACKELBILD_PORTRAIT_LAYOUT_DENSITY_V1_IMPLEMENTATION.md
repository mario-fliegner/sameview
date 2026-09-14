# Claude Prompt — DeinWackelbild Portrait Layout Density / CTA Visibility — STEP 3 IMPLEMENTATION

## Approved baseline and workflow

Continue from the current committed DeinWackelbild implementation and the completed STEP 1 + STEP 2 analysis.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

This is **STEP 3 — implementation only after explicit approval**.

Implement exactly the approved scope and nothing else.

Do not refactor unrelated code.
Do not rename unrelated symbols.
Do not reformat unrelated files.
Do not touch any file outside the approved list.
Do not commit or push in this prompt.

---

# Approved product decision

Fix the Wackelbild screen's vertical density so portrait source images do not push the order CTA below the fold on normal compact-phone layouts.

The approved behavior is:

1. Preserve the full preview image and its aspect ratio.
2. No crop and no distortion.
3. Keep the current 62% preview-height ceiling as a secondary upper bound.
4. Make the preview additionally constrained by the vertical space left after the lower controls/content stack claims its natural height.
5. Use the already-planned `weight(1f, fill = false)` mechanism on the preview call site instead of introducing a second measurement pass, `SubcomposeLayout`, `onGloballyPositioned`, or new state.
6. Remove the current `weight(1f)` from the lower scrollable controls column so that it reports its natural height first.
7. Keep `verticalScroll(...)` on the lower controls column unchanged as the fallback for extreme font scaling / very short windows.
8. Add exactly one **16.dp** spacer between the complete date-control group and the interaction-hint group.
9. Keep all existing copy unchanged:
   - `Add a reference date to show the date.`
   - `Tilt your phone`
   - `See your lenticular print in action.`
   - transfer/privacy disclosure
10. Landscape-source-image behavior should remain visually equivalent apart from the intentionally added 16.dp group separation.
11. Tablet/Expanded layouts must remain responsive and must not be artificially shrunk simply because the phone CTA-fit requirement exists.
12. Do **not** add a minimum preview-height floor in this iteration. If extreme font scaling makes the preview visually too small, that is a separate follow-up issue after real-device validation.

---

# Exact files approved for modification

Modify exactly these three files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
3. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

No fourth file is approved.

If implementation unexpectedly requires another file, STOP and report why instead of modifying it.

---

# File 1 — `WackelbildScreen.kt`

## A. Outer layout weight swap

Current structure:
- `WackelbildPreview(...)` is a fixed/non-weighted child
- lower controls `Column(...)` carries `.weight(1f)`

Approved change:
- move the weight responsibility to the preview call site using:

`Modifier.weight(1f, fill = false)`

- remove `.weight(1f)` from the lower controls column
- keep all other lower-column modifiers and behavior intact:
  - `fillMaxWidth()`
  - `verticalScroll(rememberScrollState())`
  - `widthIn(max = contentMaxWidth)`
  - horizontal padding
  - alignment

Do not introduce new layout state or measurement callbacks.

The lower stack must therefore claim its natural height first; the preview receives only the remaining height.

---

## B. Preview height calculation

Keep the existing constant:

`PREVIEW_HEIGHT_FRACTION_OF_CONTENT = 0.62f`

Keep its numerical value unchanged.

Keep the existing width/aspect-derived height calculation.

Update `effectiveHeight` so it is bounded by all three constraints:

1. width-derived natural height
2. incoming `maxHeight` from the weighted preview slot (remaining-height term)
3. existing 62% ceiling

Conceptually:

`effectiveHeight = min(heightFromWidth, maxHeight, maxPreviewHeight)`

Use the smallest/local implementation consistent with current code style.

Keep:

`effectiveWidth = effectiveHeight * referenceRatio`

or the exact existing equivalent unchanged in meaning.

No crop.
No content-scale change.
No aspect-ratio change.

Update only the nearby existing comment for the 62% constant so it accurately describes the constant as a **secondary ceiling**, not the sole sizing driver.

---

## C. Date-group → interaction-group spacing

Insert exactly one:

`Spacer(modifier = Modifier.height(16.dp))`

between:

- `WackelbildDateToggleRow(...)`

and:

- `WackelbildInteractionHint(...)`

No other spacing changes.

Do not alter the helper text spacing internally.
Do not alter transfer-disclosure spacing.
Do not alter CTA spacing.
Do not globally compress the layout.

---

## D. Tablet / Expanded behavior

Do not add any width-class-specific branch for this fix.

Preserve current `contentMaxWidth` handling, including the existing Expanded 680.dp behavior.

The new logic must derive solely from the actual Compose constraints and existing responsive width lane.

No Samsung-specific dp constants.
No phone-model assumptions.
No portrait-source fixed height.
No `LocalConfiguration.orientation` branching.

---

# File 2 — `WackelbildScreenTest.kt`

Modify only the tests needed to encode the approved behavior.

## Keep existing tests unchanged where still valid

In particular, keep and re-run:

- `preview_tallPortraitImage_heightDoesNotExceed62PercentOfAvailableContent`
- `dateToggleRow_isDisplayedWithoutScrolling_forTallPortraitImage`
- `interactionHint_isDisplayedWithoutScrolling_forTallPortraitImage`

The 62% upper-bound test remains valid because the 62% ceiling still exists.

## Add focused regression coverage

Add tests for:

1. **Tall portrait source, no reference date**
   - CTA is displayed without calling `performScrollTo()`
   - this is the worst-case lower stack because the helper text is present

2. **Tall portrait source, with reference date**
   - CTA is displayed without calling `performScrollTo()`

3. **Landscape source**
   - CTA remains displayed without routine scrolling

4. **16.dp grouping gap**
   - verify the date-control group → interaction-hint group separation is approximately 16.dp
   - cover both:
     - helper visible
     - helper absent
   - use existing bounds/tolerance conventions already present in this test file
   - do not introduce pixel-perfect screenshot tests

5. **Expanded-width regression**
   - extend or add focused coverage using the existing `WindowWidthSizeClass.Expanded` test path
   - use a tall portrait source and assert the screen/preview/CTA still compose correctly
   - be explicit in the test naming/comments that this verifies the Expanded width lane only, not a physically large tablet height

Do not build new orientation-test infrastructure in this iteration.

Do not remove the existing scroll-tolerant CTA test unless it becomes genuinely redundant and removal is strictly necessary. Prefer leaving useful existing coverage intact.

---

# File 3 — `DEINWACKELBILD_INTEGRATION_V1.md`

Update **§44 Responsive Layout Requirements only**.

Append the narrow normative rule that:

- on normal compact-phone layouts, preview height must be bounded so the complete primary interaction stack, including the order CTA, fits within the viewport without routine scrolling for portrait source images;
- the full preview image remains visible;
- aspect ratio is preserved;
- no crop is introduced;
- existing responsive width constraints continue to apply;
- scrolling remains available as a fallback for:
  - large font scale
  - accessibility needs
  - genuinely short-height windows
  - otherwise physically constrained layouts
- on larger/Expanded layouts, the preview must not be artificially shrunk merely to force a compact-looking layout when sufficient vertical space is available.

Do not rewrite unrelated parts of §44.
Do not change the implementation plan; STEP 2 confirmed that the implementation plan already describes the intended `weight(1f, fill = false)` behavior.

---

# Explicitly unchanged

Do not modify:

- `WackelbildViewModel.kt`
- `TiltBlendMapper.kt`
- `TiltProvider.kt`
- any sensor logic
- blend behavior
- smoothing
- ridge rendering
- white rounded preview border
- perspective tilt
- image `ContentScale`
- image crop behavior
- date badge rendering
- manual Reference/Capture behavior
- upload/API flow
- transfer image generation
- print renderer
- temp files
- handoff/order backend logic
- navigation
- storage
- permissions
- EN strings
- DE strings
- accessibility semantics
- touch-target sizes
- `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

No unrelated cleanup or formatting.

---

# Verification

After implementation, run exactly:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

Do not suppress failures.
Do not disable tests.
Do not add baselines.
Do not broaden scope to fix unrelated failures.

If a command fails due to this change:
- report the exact failure;
- fix only within the approved three-file scope if possible;
- if a fourth file becomes necessary, STOP and report instead.

---

# Required manual validation after automated tests

Real-device Samsung validation remains mandatory:

- portrait source, no reference date
- portrait source, with reference date
- landscape source, no reference date
- landscape source, with reference date
- CTA visible without routine scrolling at normal font size
- portrait screen orientation
- landscape screen orientation
- full image visible
- no crop/distortion
- 16.dp date-group → hint-group spacing looks visually correct
- existing tilt/blend interaction unchanged
- white rounded border unchanged
- date badge unchanged
- order flow unchanged

Large-font validation:
- verify layout remains usable
- lower content can scroll if necessary
- note whether preview becomes visually too small
- do NOT add a minimum preview-height floor in this iteration

Tablet/Expanded validation:
- existing automated Expanded-width test coverage must pass
- because the repo has no real tablet managed device, actual large-viewport height validation remains manual/emulator validation outside the connected phone suite
- do not claim full tablet-height validation from `connectedDebugAndroidTest` alone

---

# Commit behavior

Do not commit.
Do not push.

The user will first inspect the result on the real device.

---

# Required final report

Return:

1. exact modified files
2. exact outer-layout modifier changes
3. exact preview-height formula before/after
4. confirmation 62% remains as secondary ceiling
5. confirmation no crop/distortion introduced
6. exact new 16.dp spacer location
7. exact tests added/changed
8. exact §44 documentation change
9. confirmation implementation plan untouched
10. confirmation no strings changed
11. confirmation no fourth file modified
12. `testDebugUnitTest` result
13. `assembleDebug` result
14. `lintDebug` result
15. `connectedDebugAndroidTest` result and test count
16. confirmation accessibility semantics unchanged
17. confirmation upload/print/order/storage paths unchanged
18. confirmation no commit/push
19. remaining manual Samsung validation items
20. tablet/Expanded validation caveat

Implement exactly this scope and nothing else.
