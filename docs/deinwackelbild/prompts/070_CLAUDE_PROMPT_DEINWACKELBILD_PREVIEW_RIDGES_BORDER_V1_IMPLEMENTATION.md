# Claude Prompt — DeinWackelbild Preview UI Tuning: Stronger Ridges + Rounded Light Outer Edge — STEP 3 IMPLEMENTATION

## Approved baseline

Work from the committed green baseline:

`9f5e4f4` — `Implement lenticular preview and stabilize instrumentation tests`

Verified baseline before this change:
- `testDebugUnitTest` PASS
- `assembleDebug` PASS
- `lintDebug` PASS
- `connectedDebugAndroidTest` PASS, 1079/1079, 0 failures

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Approved product decision

The real-device tilt/blend behavior is good and must remain unchanged.

This iteration changes only the preview appearance:

1. Make the lenticular vertical ridge lines slightly more visible.
2. Add a thin neutral white/light outer edge around the preview.
3. The preview should now have a small rounded corner radius.
4. The outer edge and rounded preview must move/tilt together with the existing perspective transform.
5. No SameView accent blue border.
6. No shadow, glow, elevation, gradient, extra texture, or other UI changes.

The rounded corners are now explicitly approved. They are not an accidental extension of scope: the requested visual target is a subtle white rounded edge similar to the physical Wackelbild presentation.

---

# Exact approved tuning values

Use these values for this iteration:

- `RIDGE_LINE_ALPHA`: change from `0.06f` to **`0.10f`**
- ridge spacing: unchanged at `6.dp`
- ridge stroke width: unchanged at current `1f`
- outer border width: **`1.dp`**
- outer border color: **`Color.White.copy(alpha = 0.85f)`**
- preview corner radius: **`6.dp`**

Treat the ridge alpha and border visual constants as real-device-tunable values if the existing code convention uses `TODO(real-device tuning)` comments.

Do not change any tilt/blend/smoothing/perspective constants.

---

# Exact implementation scope

Modify exactly these two files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Do not modify the implementation-plan document in this iteration; the STEP 1/2 analysis established that its current summary wording does not become contradictory.

No test file changes are approved unless compilation proves one is strictly required. If a third file appears necessary, STOP and report instead of modifying it.

---

# File 1 — WackelbildScreen.kt

## A. Ridge strength

Change only:

`RIDGE_LINE_ALPHA = 0.06f`

to:

`RIDGE_LINE_ALPHA = 0.10f`

Do not alter:
- ridge spacing
- ridge stroke width
- ridge color basis
- ridge drawing pattern
- ridge placement

---

## B. Rounded preview surface + border

Use one shared preview shape:

`RoundedCornerShape(6.dp)`

Use the same shape consistently for both content clipping and border rendering.

Conceptual structure:

- outer preview container remains the node carrying the existing perspective `graphicsLayer`
- apply a `1.dp` border with `Color.White.copy(alpha = 0.85f)` and the shared rounded shape
- clip the inner preview content (both stacked images + ridge overlay) to the same rounded shape
- the border must remain part of the same transformed preview surface, so it rotates/scales together with the existing `rotationY`
- do not change preview width/height or aspect-ratio calculations
- do not move or resize the date badge
- do not change pointer/swipe hit targets
- do not change accessibility semantics

Do not add padding for the border. The border must draw within the existing bounds.

Avoid double-rounding drift: one shared shape value, reused for clipping and border.

Do not change `graphicsLayer` rotation, camera distance, or modifier semantics beyond what is necessary to add the shape/border.

---

# File 2 — DEINWACKELBILD_INTEGRATION_V1.md

Update the Source of Truth before or together with the code change so implementation and documentation remain consistent.

## A. §7 decorative-frame rule

Amend the current absolute:

`has no decorative frame;`

with the smallest explicit exception allowing the new subtle outer preview edge.

The wording must make clear:

- this is a thin neutral edge
- it exists solely to make the perspective tilt visually legible against dark content/background
- it is not a thick decorative card frame
- it is not branding
- it is preview-only

## B. New §8.11 — Preview outer edge

Add a new subsection after current §8.10 and before §9.

It must define:

- thin neutral white/light outline
- small rounded corners
- visual-only purpose: making the existing perspective tilt easier to perceive
- border and rounded content move/tilt as one preview surface
- no shadow/elevation/glow
- no SameView accent-color border
- no output/print/upload effect
- must never be rendered into:
  - reference/capture transfer images
  - originals
  - partner upload images
  - Share Image output
  - Video Export output
  - persisted session media

Do not rewrite unrelated sections.

Do not alter the existing §8.10 ridge exception wording except where absolutely needed for cross-reference consistency.

---

# Explicitly unchanged

Do not modify:

- `WackelbildViewModel.kt`
- `TiltBlendMapper.kt`
- sensor calibration
- smoothing
- max useful tilt
- perspective rotation amount
- camera distance
- blend direction
- manual Reference/Capture behavior
- date overlay behavior
- upload/API flow
- print renderer
- temp-file handling
- storage
- navigation
- strings
- permissions
- accessibility semantics

No refactoring, renaming, cleanup, or unrelated formatting.

---

# Verification

After implementation run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

Expected connected result:
- 1079/1079
- 0 failures

If any command fails:
- do not suppress
- do not baseline
- do not disable tests
- do not introduce unrelated fixes
- report exact failure and stop

---

# Real-device validation still required after automated verification

Do not mark the visual tuning fully accepted until manually checked on the Samsung device.

Manual checklist:

- dark photo on dark SameView background
- light photo
- neutral orientation
- tilt left
- tilt right
- white border moves with the tilted preview
- rounded corners remain clean
- no clipping at either tilt extreme
- ridge lines are perceptibly stronger but still subtle
- portrait
- landscape
- date badge unaffected
- manual Reference/Capture endpoints unaffected

---

# Commit behavior

Do not commit and do not push in this prompt.

The user will first perform the real-device visual check.

---

# Required final report

Return:

1. exact modified files
2. exact ridge alpha before/after
3. exact border width/color
4. exact corner radius
5. exact modifier/render placement used
6. confirmation images + ridges are clipped to the same shape
7. confirmation border is inside the transformed preview surface
8. exact documentation changes
9. confirmation no third file was modified
10. `testDebugUnitTest` result
11. `assembleDebug` result
12. `lintDebug` result
13. `connectedDebugAndroidTest` result and test count
14. confirmation output/upload/print paths unchanged
15. confirmation accessibility semantics unchanged
16. confirmation no commit/push
17. real-device validation still pending

Implement exactly this approved scope and nothing else.
