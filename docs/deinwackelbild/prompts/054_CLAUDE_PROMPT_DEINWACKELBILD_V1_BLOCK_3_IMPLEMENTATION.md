# Claude Prompt --- DeinWackelbild V1 Block 3 STEP 3 Implementation

## Approval

**Approved for STEP 3 implementation.**

Implement exactly the final approved six-file scope and the corrected
delta-space seed-and-freeze design.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly and re-check the current
Source-of-Truth and implementation plan before editing. Do not expand
scope.

## Approved files

### Existing files to modify

-   `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
-   `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
-   `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
-   `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`

### New files to create

-   `app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapper.kt`
-   `app/src/test/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapperTest.kt`

Exactly these six files are approved. Do not modify any other file.

## Implementation requirements

-   Use **wrapped-delta EMA smoothing**, never raw-roll EMA smoothing.
-   Compute
    `wrappedDelta = TiltHysteresisStateMachine.wrapAngleDegrees(rawRollDegrees - neutralRollDegrees)`.
-   Keep `TiltProvider.kt` unchanged.
-   Keep `TiltHysteresisStateMachine.kt` unchanged.
-   Preserve sensor lifecycle, neutral calibration, discrete
    `visibleImage`, accessibility semantics, and existing manual
    neutral/re-arm arbitration.
-   Implement the approved **seed-and-freeze** behavior.
-   While manual override is active, do not feed live sensor readings
    into the blend mapper.
-   Manual Reference pins the fraction to `0f`; manual Capture pins it
    to `1f`.
-   Seed the mapper's `smoothedDelta` to the matching endpoint on manual
    selection.
-   When the existing re-arm transition releases override, resume mapper
    updates from that seeded delta state.
-   The first resumed event may produce a bounded first step; do not
    claim mathematical zero-discontinuity.
-   `reset()` clears smoothing history whenever existing neutral
    calibration is reset.

## Continuous blend convention

-   `0f` = full Reference
-   `0.5f` = calibrated neutral / approximately 50/50
-   `1f` = full Capture

Mapping must be monotonic, symmetric, angle-wrap-safe, and clamped to
`[0f, 1f]`.

## Candidate initial tuning values

Use these as **candidate starting values for mandatory real-device
tuning**, not normative UX values:

-   `maxUsefulTiltDegrees = 24f`
-   `emaAlpha = 0.2f`
-   perspective maximum rotation = `6f`
-   `cameraDistance = 8f * density`

Treat all as real-device tuning values where appropriate. Do not
introduce additional tuning systems.

## Compose preview

In `WackelbildScreen.kt`:

-   Render both already-loaded preview images simultaneously.
-   Use complementary alpha values from the continuous fraction.
-   Preserve existing bounds, aspect ratio, `ContentScale`, alignment,
    and sizing.
-   No bitmap decoding/resizing/re-rendering per sensor update.
-   Add only the approved subtle vertical lenticular ridge overlay,
    preview-only and confined to image bounds.
-   Add only the approved subtle perspective transform, driven from the
    same continuous state.
-   No card flip or exaggerated trapezoid effect.
-   No `Blickwinkel` slider/bar/control.

## Date badge

Keep existing date-overlay behavior discrete:

-   one badge only;
-   no badge crossfade;
-   no second badge;
-   sensor control follows existing discrete `visibleImage`;
-   manual selection follows the manually selected discrete side;
-   no other Block 4 changes.

## Accessibility

-   Keep TalkBack identity driven by existing discrete state.
-   Do not announce continuous blend percentages.
-   Keep accessibility toggle as a deterministic manual endpoint action.
-   No Reduce Motion system.
-   If the previously scoped custom semantics property is needed solely
    for robust UI-test observability, keep it non-user-facing and
    minimal.

## Preview/output isolation

Blend, ridges, and perspective remain exclusively on the Compose preview
path and must not alter `reference.jpg`, `capture.jpg`, originals,
session storage, print-transfer images, shared images, video output, or
upload bytes.

Do not modify print renderer, temp-file, upload/API, handoff, share,
video, storage, or original-image code.

## Tests

### `TiltBlendMapperTest.kt`

Cover at least neutral, both directions, monotonicity,
clamping/endpoints, angle-wrap behavior, EMA initialization, `reset()`,
`seedToFraction(0f/0.5f/1f)`, bounded first resumed step, convergence/no
overshoot for held input, and representative noisy-input stability.

### `WackelbildViewModelTest.kt`

Cover initial fraction, calibration-only first reading, live sensor
updates, manual endpoint pinning, mapper freeze during override,
existing neutral/re-arm sequence, release on the existing transition,
bounded first resumed step, subsequent convergence, and preservation of
discrete `visibleImage`.

Do not rewrite unrelated tests.

### `WackelbildScreenTest.kt`

Update only assertions made obsolete by dual-image rendering. Both image
nodes may coexist. Replace old hard-switch `assertDoesNotExist()`
assumptions only where required. Verify blend state through the approved
minimal test-observability mechanism if necessary. Keep date badge
discrete. Cover ridge/perspective only where robustly testable. No
brittle pixel-perfect tests and no unrelated test rewrites.

## Change discipline

Do not refactor, rename, clean up, or reformat unrelated code. Do not
alter navigation, permissions, storage, networking, upload behavior,
camera behavior, unrelated Compose state, documentation, or later
DeinWackelbild blocks. Do not add telemetry, analytics, dependencies, or
unrelated hardening.

If implementation proves a seventh file is required, **STOP before
modifying it** and report why approval must be expanded.

## Verification after implementation

Run:

``` text
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug
```

Do not suppress, disable, or hide failures.

Real-device validation remains mandatory for calibrated neutral ≈50/50,
smooth bidirectional blend, flicker, first-step behavior after manual
re-arm, endpoint pinning, perspective direction/subtlety/clipping, ridge
subtlety, Portrait/Landscape, lifecycle recalibration, date overlay,
TalkBack, and output/upload isolation.

## Required final report

After implementation and verification, report:

1.  Modified/created files --- all six with full paths.
2.  Exact implementation performed, file by file.
3.  Tests changed/added.
4.  Commands run and exact results.
5.  Commands not run.
6.  Remaining mandatory real-device validation.
7.  Any deviation from approved scope.
8.  Whether documentation follow-up is required.

Do not claim physical-device validation unless actually performed. Do
not modify documentation in this iteration even if implementation
succeeds; report the follow-up instead.

Implement nothing outside the approved six-file scope.
