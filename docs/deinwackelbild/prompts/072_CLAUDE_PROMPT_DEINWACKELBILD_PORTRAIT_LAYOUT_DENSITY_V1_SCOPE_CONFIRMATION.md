# Claude Prompt — DeinWackelbild Portrait Layout Density / CTA Visibility — STEP 2 SCOPE CONFIRMATION

## Context

Continue from the completed STEP 1 analysis for the Wackelbild preview-screen vertical density issue.

The user has approved the following product/implementation direction:

1. The root cause is the current preview height cap being based on a flat fraction of the full content height without reserving room for the controls stack below.
2. The fix must be **remaining-height-aware**, not a device-specific fixed-height hack.
3. The preview must preserve the full image and aspect ratio. No crop, no distortion.
4. The current **62% preview ceiling remains as a secondary upper bound** so previews do not become excessively large when plenty of vertical space exists.
5. Add a **16.dp group gap** between the complete date-control group and the interaction-hint group.
6. Keep all existing text for this iteration:
   - `Add a reference date to show the date.`
   - `Tilt your phone`
   - `See your lenticular print in action.`
   - transfer/privacy disclosure
7. On normal compact phone layouts, the primary interaction stack including the order CTA should fit without routine scrolling, including portrait source images.
8. Scrolling must remain as a safe fallback for large font scale, accessibility, genuinely short-height windows, or other cases where fitting everything would be unreasonable.
9. Landscape-source-image behavior is already good and must remain effectively unchanged apart from the new intentional 16.dp date-group → interaction-group spacing.
10. The solution must also remain correct on tablets / Expanded layouts:
    - no phone-only hardcoding;
    - no fixed portrait-image height tied to Samsung dimensions;
    - no artificial shrinking on large screens merely to force all content into a compact-looking block;
    - existing responsive width constraints and max-width behavior remain authoritative;
    - the new available-height cap should only constrain the preview when the natural preview size would displace required controls.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

Do NOT implement anything yet.

---

# Source of Truth

Before scoping, re-check the current committed versions of:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `RESPONSIVE_LAYOUT_SYSTEM_V1.md`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`

Also inspect relevant EN/DE string resources only to confirm no string change is required.

If code/spec conflict exists, report it explicitly.

---

# STEP 2 — SCOPE CONFIRMATION ONLY

Provide the exact implementation scope and STOP.

## Approved conceptual behavior

The intended sizing logic is:

- start from the preview height implied by the available width and source-image aspect ratio;
- retain the existing preview-size ceiling (currently 62% of the available content height) as a secondary upper bound;
- additionally constrain the preview by the vertical space remaining after reserving the natural height required by the lower interaction stack;
- use the minimum of those constraints;
- preserve `effectiveWidth = effectiveHeight * referenceRatio` or equivalent invariant so no crop/distortion is introduced;
- keep the lower controls column scrollable as an accessibility/extreme-height fallback.

Conceptually:

`previewHeight = min(heightFromWidth, existingPreviewCeiling, remainingHeightAfterRequiredControls)`

Do not treat this formula as permission to invent arbitrary constants. Confirm the actual implementation mechanism from the current code.

---

# Tablet / Expanded-layout constraint

This is mandatory for the scope confirmation.

Explicitly verify that the proposed implementation:

- is not keyed to phone width only;
- uses current layout constraints rather than Samsung-specific numbers;
- respects current `contentMaxWidth` / width-class / responsive behavior;
- leaves a landscape source image on a tablet at its current natural/responsive size unless vertical space genuinely becomes limiting;
- does not shrink a tablet preview simply because the phone requirement says CTA should fit;
- remains valid in portrait and landscape screen orientation;
- does not conflict with `RESPONSIVE_LAYOUT_SYSTEM_V1.md`.

If the current Wackelbild screen is not formally listed in the responsive system matrix, state that fact and define the minimal safe consistency rule rather than inventing a new tablet design.

---

# Expected file scope to verify

The STEP 1 analysis indicated these likely files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
3. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Potentially:
4. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

Do not assume #4. Include it only if current committed wording would become inconsistent.

No string-resource file should be changed unless inspection proves otherwise.

---

# For each proposed modified file

Report:

- full path;
- exact reason it must change;
- exact conceptual change;
- exact sections/functions/tests affected;
- why no unrelated file is needed.

---

# Required production scope details

For `WackelbildScreen.kt`, identify precisely:

1. where the current 62% height ceiling is calculated;
2. how the natural lower-stack height will be obtained or reserved;
3. how the remaining height will be calculated;
4. how the existing 62% ceiling remains as a secondary cap;
5. where the new `16.dp` spacer will be inserted;
6. why preview aspect ratio and full-image visibility remain unchanged;
7. why the existing scroll fallback remains intact;
8. why landscape-source behavior remains effectively unchanged;
9. why tablet/Expanded behavior remains responsive rather than phone-hardcoded.

Do not provide implementation code in this STEP 2 response.

---

# Required test scope details

For `WackelbildScreenTest.kt`, scope only tests genuinely needed for the approved behavior.

At minimum analyze and propose exact updates for:

- the current test that hard-asserts the old 62% invariant;
- a CTA-without-routine-scroll test for a tall portrait source image;
- preservation of existing no-scroll visibility for the date row and interaction hint;
- no-reference-date state;
- landscape source regression;
- tablet / Expanded regression if the current test infrastructure supports a meaningful layout-size override;
- portrait and landscape screen-orientation coverage if already supported by this suite.

Do not add pixel-perfect screenshot tests.

Do not remove useful existing coverage merely because the old 62% assertion must change.

---

# Required documentation scope

For `DEINWACKELBILD_INTEGRATION_V1.md`:

Identify the exact section (STEP 1 found §44) and propose the minimal amendment conceptually:

- normal compact-phone layout should height-bound the preview so the complete primary interaction stack, including the CTA, fits without routine scrolling for portrait source images;
- full image remains visible;
- aspect ratio preserved;
- no crop;
- existing responsive width constraints remain;
- scrolling remains allowed as a fallback when required by large text, accessibility, short windows, or other genuinely constrained layouts;
- larger/Expanded layouts should not artificially shrink the preview when sufficient space exists.

Do not edit the doc yet.

For `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`, state explicitly whether a change is required or not.

---

# Explicitly unchanged

Confirm that the following are outside scope and remain untouched:

- `WackelbildViewModel.kt`
- `TiltBlendMapper.kt`
- sensor calibration
- blend behavior
- smoothing
- ridge rendering
- white rounded preview border
- perspective tilt amount
- image crop/content scale
- date badge rendering
- manual Reference/Capture behavior
- upload/API flow
- transfer image bytes
- print renderer
- temp files
- order backend/handoff logic
- navigation
- storage
- permissions
- EN/DE copy text
- accessibility semantics and touch-target sizes

No refactor.
No cleanup.
No renaming.
No unrelated formatting.

---

# Risks to report

Explicitly assess:

- measurement/layout-loop risk if lower-stack natural height is measured dynamically;
- recomposition risk;
- large-font behavior;
- very short windows;
- portrait vs landscape source;
- portrait vs landscape screen orientation;
- tablet / Expanded layouts;
- scroll fallback;
- CTA visibility;
- accidental preview over-shrinking;
- regression to current landscape-source appearance.

If the proposed measuring mechanism could cause unstable or circular measurement constraints, call that out before implementation.

---

# Verification plan for STEP 3

Propose the exact minimum verification commands:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

Also require manual validation on the Samsung:

- portrait source, with date
- portrait source, without date
- landscape source, with date
- landscape source, without date
- CTA visible without routine scroll on normal font size
- large font scale still usable via scroll fallback
- portrait screen orientation
- landscape screen orientation
- preview fully visible / no crop
- 16.dp grouping gap visually correct

And require an Expanded/tablet validation path using the current project test infrastructure or emulator/managed-device setup if available.

---

# Required STEP 2 response

Return:

1. exact files to modify;
2. exact files confirmed unchanged;
3. exact production-code scope;
4. exact test scope;
5. exact documentation scope;
6. explicit tablet/Expanded compatibility analysis;
7. risks;
8. verification plan;
9. confirmation that no unrelated code will be touched;
10. confirmation that no files were modified yet.

Then STOP and wait for explicit user approval before STEP 3 implementation.
