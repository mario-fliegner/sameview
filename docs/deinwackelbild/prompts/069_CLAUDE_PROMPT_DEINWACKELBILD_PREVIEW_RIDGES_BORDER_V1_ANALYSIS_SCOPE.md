# Claude Prompt — DeinWackelbild Preview UI Tuning: Stronger Lenticular Ridges + Light Outer Border — STEP 1 + STEP 2 ONLY

## Goal

Continue directly from the now-stable committed baseline:

- commit: `9f5e4f4`
- unit tests green
- `assembleDebug` green
- `lintDebug` green
- full connected instrumentation green: 1079/1079, 0 failures

The current DeinWackelbild preview behavior has now been manually validated on the real Samsung device.

The user feedback is:

1. the continuous tilt/blend behavior is good;
2. the lenticular vertical ridge lines should be **a little stronger/more visible**;
3. on dark photos against SameView's dark background, the perspective/tilt effect is not immediately obvious because the image edge visually disappears into the background;
4. deinwackelbild.de uses a light/white rounded outer edge around the preview, which makes the physical-card/tilt effect easier to perceive;
5. the requested direction is therefore:
   - slightly stronger lenticular ridge visibility;
   - a **thin neutral white/light outer border**, not SameView blue;
   - the border should follow the existing rounded preview shape;
   - the border should visually move/tilt with the preview, i.e. belong to the transformed preview/card surface rather than remain fixed to the screen;
   - no other UI change.

This is a **visual tuning iteration only**.

Do not implement anything yet.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Important Source-of-Truth issue to inspect

Previous Source-of-Truth wording around the DeinWackelbild preview explicitly rejected a decorative/simulated physical-product frame, while later amendments introduced only the lenticular-ridge exception.

The new user decision now intentionally adds a subtle outer edge because it materially improves perception of the perspective tilt on dark content.

Therefore:

- inspect the current committed `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`;
- inspect `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`;
- inspect `CLAUDE_PROJECT_INSTRUCTION.md`;
- inspect the current committed `WackelbildScreen.kt`;
- identify the exact current Source-of-Truth wording that conflicts with this new approved visual decision.

Do **not** silently implement against the existing documentation.

If the spec currently says no decorative frame / no simulated physical-product frame, propose the smallest wording amendment needed to permit **only** this subtle visual edge as part of the on-screen preview effect.

The intended distinction is:

- this is **not** a new thick decorative card frame;
- this is **not** a print/output border;
- this is **not** branding;
- this is a thin neutral edge used to make the existing perspective tilt legible against dark backgrounds;
- it affects on-screen preview rendering only.

---

# STEP 1 — Analysis only

Analyze the exact current implementation.

## A. Lenticular ridge tuning

Find the current ridge implementation in `WackelbildScreen.kt`.

Report:

- current spacing;
- current line thickness;
- current alpha/opacity;
- current color;
- whether changing only alpha is sufficient for "a little stronger";
- whether thickness/spacing should remain unchanged;
- the smallest recommended value change for real-device tuning.

Prefer the smallest single-value adjustment if possible.

Do not redesign the ridge effect.

Do not add gradients, shaders, highlights, shadows, or extra texture.

---

## B. Outer border / edge

Inspect the current preview composition and perspective transform.

Determine the smallest correct place to render a thin light border so that:

- it follows the exact existing rounded preview shape;
- it encloses the complete composed Reference/Capture preview;
- it also encloses the ridge overlay visually;
- it rotates/scales together with the existing perspective `graphicsLayer`;
- it does not remain fixed while the image tilts;
- it does not alter image crop/content scale;
- it does not alter preview dimensions;
- it does not affect manual toggle hit targets;
- it does not affect accessibility semantics;
- it cannot enter the print/upload/output pipeline.

Analyze whether the border should be implemented with the existing shape via a Compose border modifier or an equivalent minimal draw operation.

Do not write code in this step.

---

## C. Visual values

The intended appearance is:

- neutral white / slightly softened white;
- visually clear on dark images and dark SameView background;
- still subtle;
- roughly in the **1–2 dp** range;
- use the preview's existing corner radius/shape rather than inventing another radius;
- no SameView accent blue;
- no shadow/elevation/glow.

Determine the smallest concrete candidate values based on the existing implementation.

Do not introduce a new color token unless genuinely required.

Prefer an existing neutral/white value or an inline existing Material/Compose color if consistent with current project conventions.

---

## D. Perspective/clipping interaction

Verify that adding the border inside the transformed preview does not:

- clip incorrectly during `rotationY`;
- extend into surrounding controls;
- create double rounding;
- create a border that gets cut off by an existing clip order.

Identify the exact modifier/render ordering required conceptually.

Do not change the current perspective amount, blend mapping, smoothing, or tilt direction in this iteration.

---

## E. Preview/output isolation

Explicitly confirm that both requested visual changes remain strictly within the Compose preview and cannot modify:

- `reference.jpg`
- `capture.jpg`
- originals
- generated partner upload images
- print renderer output
- session storage
- Share Image output
- Video Export output

If any code path would cross this boundary, reject it.

---

## F. Accessibility

Confirm that:

- no new TalkBack announcement is required for the purely visual border/ridge tuning;
- existing semantics remain unchanged;
- no hit target changes are required.

---

# STEP 2 — Scope confirmation

After completing the analysis, provide the exact proposed scope.

List **all** files that would need modification.

Expected minimal scope is likely:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Possibly:
3. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

But do not assume this. Inspect the current committed docs and include the implementation plan only if its current wording/status would otherwise become inconsistent.

Tests should only be modified if the existing structural UI tests genuinely require a narrow update for the new border. Do not add pixel-perfect tests.

For every proposed file state:

- exact reason it must change;
- exact conceptual change;
- why no other file is required.

Confirm explicitly that the following remain untouched:

- `WackelbildViewModel.kt`
- `TiltBlendMapper.kt`
- sensor calibration
- smoothing constants
- blend thresholds
- manual Reference/Capture behavior
- date overlay behavior
- upload/API flow
- print renderer
- storage
- navigation
- strings, unless an actual user-visible string is added (none is expected)
- permissions

---

# Risks to report

Explicitly assess:

- border clipping under perspective;
- visual over-emphasis if ridge alpha is too strong;
- dark/light photo contrast;
- portrait/landscape behavior;
- regression risk to preview sizing;
- output isolation.

This should remain low-risk, preview-only Compose work.

---

# Verification plan for later implementation

Propose the minimum verification required after implementation.

At minimum expect:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

Real-device manual verification on the Samsung remains required:

- dark image against dark app background;
- light image;
- neutral orientation;
- tilt left/right;
- border moves with the preview;
- border remains thin and rounded;
- ridges are slightly more visible but still subtle;
- portrait and landscape;
- no clipping;
- date overlay unaffected;
- manual Reference/Capture endpoints unaffected.

---

# Explicitly forbidden

Do not:

- implement code;
- modify files;
- change the tilt/blend behavior;
- change smoothing or thresholds;
- add a Blickwinkel slider;
- add shadows/elevation/glow;
- add a thick decorative frame;
- use SameView accent blue for the border;
- add new animation;
- refactor unrelated code;
- touch upload/output rendering;
- change accessibility behavior;
- commit or push.

---

# Required response

Return:

1. current ridge implementation values;
2. smallest recommended ridge-strength change;
3. exact current preview/perspective composition relevant to border placement;
4. recommended border thickness/color/shape;
5. exact conceptual modifier/render order;
6. Source-of-Truth conflict found, with exact section(s);
7. minimal documentation amendment required;
8. exact files proposed for modification;
9. exact files confirmed unchanged;
10. risks;
11. verification plan;
12. whether any real-device tuning value remains intentionally adjustable.

Then STOP and wait for explicit user approval before implementation.
