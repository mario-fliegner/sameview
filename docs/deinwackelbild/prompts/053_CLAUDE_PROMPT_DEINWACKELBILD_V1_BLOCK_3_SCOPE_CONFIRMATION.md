# Claude Prompt — DeinWackelbild V1 Block 3 STEP 2 Scope Confirmation

## Objective

Perform **STEP 2 — scope confirmation only** for the approved DeinWackelbild V1 Block 3 preview revision.

Do not modify files.
Do not output production code.
Do not output test code.
Do not update documentation.
Do not implement anything.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

The Source-of-Truth and implementation plan are already synchronized. STEP 1 implementation analysis is complete. This prompt exists only to turn that analysis into an exact, reviewable implementation scope before any code change.

---

# 1. Source of truth to re-check

Before producing the scope confirmation, re-read the current repository versions of:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

Relevant Source-of-Truth areas include at least:

- Preview Image Presentation
- §8.1 Core behavior
- §8.3 Tilt interaction
- §8.4 Swipe fallback
- §8.5 Sensor/swipe arbitration
- §8.7 Accessibility
- §8.8 Sensor lifecycle and privacy
- §8.9 Perspective tilt
- §8.10 Lenticular ridge overlay
- §47 Acceptance Criteria — UX
- §55 Hard Non-Goals

Relevant implementation-plan areas include at least:

- §7 Tilt / Swipe Architecture
- §7.7 Preview contract amendment — required Block 3 revision
- Block 3
- related §21 / §24 / §25 / §29 / §30 entries

If the repository differs from the STEP 1 analysis, report that explicitly and do not silently reuse stale assumptions.

---

# 2. Fixed product decisions for this scope

The following decisions are now approved constraints for STEP 2 and must not be re-opened unless they conflict with the current Source-of-Truth.

## 2.1 Date badge behavior

The existing date-overlay feature remains **discrete**.

Do not introduce date-badge crossfading.
Do not render two date badges.
Do not change Block 4 behavior beyond what is mechanically necessary to keep it working with the revised preview.

Rules:

- during sensor control, the date badge continues to follow the existing discrete `visibleImage` / semantic-side state;
- during manual swipe/accessibility selection, the badge follows the manually selected discrete side;
- the continuous image blend does not create a third/intermediate date-badge state.

This is intentionally minimal and preserves the existing date-overlay contract.

---

## 2.2 Manual override release / continuous blend gating

This must be defined precisely in the implementation scope.

The preferred behavior is:

- when the user manually selects Reference or Capture, the continuous blend is pinned to the corresponding full endpoint;
- while manual override is active, live sensor blend updates must not overwrite that endpoint;
- the existing neutral-observed / re-arm principle remains authoritative;
- sensor control resumes only through the same existing arbitration concept, not because of incidental sensor noise;
- when sensor control resumes, the transition back to live continuous tilt must not create an avoidable hard visual jump.

Claude must inspect the current `swipeOverrideActive` / `neutralObservedSinceOverride` / hysteresis transition flow and state exactly:

1. which existing transition releases manual override;
2. when the continuous blend value stops being pinned;
3. what value is applied at the exact moment live sensor control resumes;
4. whether the current flow can satisfy this without modifying `TiltHysteresisStateMachine.kt`;
5. whether any narrowly scoped ViewModel logic is required to avoid a discontinuity.

Do not leave this as an implementation-time guess.

Do not redesign the arbitration architecture unless strictly necessary.

---

# 3. Preferred architecture from STEP 1

Use the STEP 1 analysis as the baseline, but verify every item against current code before including it in scope.

Expected preferred structure:

- `TiltProvider.kt` remains unchanged;
- `TiltHysteresisStateMachine.kt` remains unchanged if current arbitration can support the revised behavior;
- one small pure Kotlin mapper/helper is likely justified for continuous mapping;
- `WackelbildViewModel.kt` gains the continuous preview state and orchestrates mapper + existing hysteresis/arbitration;
- `WackelbildScreen.kt` renders both already-loaded images, adds preview-only ridge rendering, and adds preview-only subtle perspective;
- `WackelbildViewModelTest.kt` is extended;
- a dedicated mapper unit test is added if a new mapper/helper file is approved;
- `WackelbildScreenTest.kt` is updated because current hard-switch `assertDoesNotExist()` assumptions are incompatible with dual-image rendering.

Do not broaden beyond Block 3.

---

# 4. Continuous mapper scope

Resolve the new helper/file now.

Claude must decide and state:

- exact proposed class name;
- exact proposed file path;
- exact responsibility;
- exact input;
- exact output convention;
- whether it owns angle wrapping;
- whether it owns smoothing/damping state;
- whether it is stateless or stateful;
- how it is reset on screen activation / recalibration if stateful;
- why placing this logic in a separate pure Kotlin file is preferable to inlining it in the ViewModel.

The output convention should remain simple and explicit, for example:

- `0f` = full Reference
- `0.5f` = calibrated neutral
- `1f` = full Capture

If current direction/sign behavior requires the inverse, report that instead of assuming it.

Do not hard-code final tuning values unless already required by the Source-of-Truth.

Classify any proposed constants as either:

- fixed algorithmic constants, or
- real-device tuning parameters.

---

# 5. Smoothing / dead-zone scope

The STEP 1 analysis identified smoothing as likely necessary for visible stability, while a dead zone may or may not be required.

For scope confirmation, Claude must choose the minimum intended approach.

State explicitly:

- whether an EMA or equivalent minimal smoothing method will be implemented;
- where the smoothing state lives;
- how smoothing is initialized/reset;
- whether a neutral dead zone will be included;
- if no dead zone is included, why continuous mapping plus smoothing is sufficient;
- whether any quantization/rate-limiting is in scope.

Do not add speculative performance machinery.

No new sensor framework.
No coroutine sampling pipeline unless current architecture proves it necessary.

---

# 6. Exact ViewModel changes to scope

For `WackelbildViewModel.kt`, identify the exact methods/state likely to change.

At minimum resolve:

- new continuous `StateFlow<Float>` or equivalent state holder;
- initialization value;
- update path from calibrated roll delta;
- interaction with existing `hysteresisStateMachine.onDeltaDegrees(...)`;
- manual endpoint pinning;
- gating while manual override is active;
- release/resume behavior after neutral/re-arm;
- lifecycle/reset behavior;
- whether `visibleImage` remains unchanged as the discrete semantic/manual state;
- whether `applySensorState()` changes;
- whether `manualToggle()` changes;
- whether `onRawRollChanged()` changes;
- whether any new private helper methods are needed.

Do not rename existing public state/classes unless strictly required.

---

# 7. Exact Compose rendering scope

For `WackelbildScreen.kt`, identify the exact composable/function area to change.

Scope the minimum required behavior:

## Dual-image blend

- keep both existing painters;
- render both in the same preview bounds;
- use complementary alpha values;
- preserve `ContentScale`, aspect ratio, alignment, existing sizing, and current layout bounds;
- do not decode/resize images per sensor update.

## Date badge

Preserve the fixed decision from §2.1:
- one badge only;
- discrete side semantics only;
- no continuous badge crossfade.

## Ridge overlay

Scope:
- preview-only;
- vertical;
- subtle;
- stateless if practical;
- confined to image bounds;
- no decorative frame;
- no output persistence.

## Perspective

Scope:
- preview-only container transform;
- driven by the same continuous tilt state;
- subtle;
- no exaggerated card flip;
- no strong trapezoid effect;
- no output geometry change;
- define the exact clipping/bounds strategy proposed for implementation.

Do not introduce an animation subsystem unless strictly necessary.

---

# 8. Accessibility scope

Confirm exactly what changes, if any, are required in existing semantics.

Preferred behavior:

- semantics continue to read discrete `visibleImage`;
- no blend percentage is announced;
- accessibility toggle remains a deterministic manual endpoint action;
- continuous visual StateFlow must not drive noisy semantic updates.

If no semantics code change is required, state that explicitly.

Do not introduce a global Reduce Motion setting.

---

# 9. Test scope

Identify every test file to be modified or created.

For each test file, state the exact categories of tests to add/change.

At minimum address:

## Mapper tests

If a new mapper/helper is created:
- neutral;
- both directions;
- monotonicity;
- clamping/endpoints;
- wrap behavior;
- smoothing initialization/reset;
- noise stability expectations.

## ViewModel tests

- continuous state initialization;
- sensor-driven updates;
- manual endpoint pinning;
- override gating;
- neutral/re-arm release;
- exact resume behavior;
- preservation of discrete `visibleImage`;
- accessibility/manual semantics where currently covered.

## UI/instrumentation tests

The current `WackelbildScreenTest.kt` contains hard-switch assumptions such as `assertDoesNotExist()` on the non-visible image.

Scope the exact replacement strategy:
- both image nodes may coexist;
- tests should verify controlled dominance/alpha or equivalent observable rendering state;
- date badge remains discrete;
- perspective/ridge behavior is covered only where robustly testable;
- do not write brittle pixel-perfect instrumentation tests unless the project already uses that approach.

If alpha/perspective cannot be robustly asserted with current semantics/test tags, state the minimal testability hook needed, if any, and include it in the prospective production scope.

Do not add debug-only production behavior solely for tests unless necessary and minimal.

---

# 10. Expected files — resolve exactly

Produce the exact final expected file list for implementation.

The current expectation is approximately:

## Production
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- one new pure mapper/helper file under the same package

## Unit tests
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
- one new mapper/helper test file

## Instrumentation/UI
- `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`

Expected unchanged:
- `TiltProvider.kt`
- `TiltHysteresisStateMachine.kt`
- `TiltHysteresisStateMachineTest.kt`
- `TiltProviderTest.kt`
- print/upload/temp-file/API classes
- Source-of-Truth docs
- implementation plan

But Claude must verify and correct this list if the current repository requires something different.

No documentation status update in this implementation iteration. Returning Block 3 to `✅ implemented` is a later separate documentation iteration after successful implementation/verification.

---

# 11. Risk review

Before approval, explicitly identify risks for:

- discontinuity when manual override returns to sensor control;
- sensor noise / flicker;
- excessive recomposition;
- perspective clipping;
- date badge regression;
- TalkBack semantic churn;
- portrait/landscape;
- lifecycle recalibration;
- output/upload isolation;
- UI test brittleness.

For each risk, give the narrow mitigation within the proposed scope.

Do not add unrelated hardening.

---

# 12. Verification commands to plan

State which commands will be required after implementation.

At minimum evaluate:

- `./gradlew testDebugUnitTest`
- `./gradlew connectedDebugAndroidTest`
- `./gradlew assembleDebug`

Also state whether project/release policy requires any of:

- `./gradlew clean`
- `./gradlew assembleRelease`
- `./gradlew bundleRelease`
- lint task(s)

Do not execute them in STEP 2.

State explicitly that real-device validation remains mandatory for:
- neutral calibration;
- smooth bidirectional blend;
- flicker;
- natural perspective direction;
- subtle perspective magnitude;
- ridge subtlety;
- rotation/orientation;
- manual override/re-arm feel.

---

# 13. STEP 2 required output

Return a scope-confirmation report with exactly these headings:

1. Files to modify
2. New files to create
3. Files confirmed unchanged
4. Exact ViewModel changes
5. Exact continuous-mapper design
6. Manual override release behavior
7. Exact Compose rendering changes
8. Date badge behavior
9. Accessibility impact
10. Exact test changes
11. Risks and mitigations
12. Verification plan
13. Documentation impact
14. Scope confirmation

Under **14. Scope confirmation**, explicitly state:

- no production/test/doc file will be changed before approval;
- no unrelated code will be touched;
- no refactoring outside the agreed lines;
- no permission/network/storage/navigation changes;
- no Block 4 or later-block behavior changes;
- any uncertainty that still prevents safe implementation.

Then STOP.

Wait for explicit approval before editing anything.

Do not provide code.
Do not modify files.
Do not implement.
