# Claude Prompt — DeinWackelbild V1 Block 3 Implementation Analysis

## Objective

Perform **STEP 1 — analysis only** for the now-approved DeinWackelbild V1 Block 3 preview revision.

Do not modify any file.
Do not output production code.
Do not output test code.
Do not change documentation.
Do not implement anything.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

The Source-of-Truth and implementation plan have already been synchronized. This task is now to analyze the smallest safe production implementation that brings Block 3 back into compliance.

---

# 1. Source of truth to inspect first

Before analyzing implementation, read and treat as authoritative:

- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `CLAUDE_PROJECT_INSTRUCTION.md`

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
- related file-scope, test-strategy, real-device-validation, risk, and open-tuning sections

If code conflicts with these MDs, the MDs are authoritative. Report the conflict explicitly.

---

# 2. Exact implementation problem to analyze

The currently shipped/pre-update Block 3 architecture still uses a discrete hard-switch presentation:

- tilt input is derived from the existing rotation-vector sensor pipeline;
- neutral roll is calibrated after activation;
- current discrete hysteresis logic produces Reference / Capture / Neutral-style state transitions;
- manual swipe/accessibility behavior is built around deterministic discrete image selection;
- the preview currently renders only one of the two images at a time;
- no continuous visual blend value currently drives rendering;
- no lenticular ridge overlay is rendered;
- no subtle perspective tilt effect is rendered.

The approved UX now requires:

- approximately 50/50 Reference/Capture at calibrated neutral;
- continuous tilt-driven dominance in both directions;
- progression toward approximately full Reference or full Capture at useful tilt endpoints;
- no hard A/B visual switch during normal tilt;
- stable behavior without visible sensor flicker;
- manual Reference/Capture selection still ending at deterministic full-image endpoints;
- existing sensor/manual arbitration and neutral/re-arm principle preserved unless a minimal change is proven necessary;
- subtle preview-only perspective;
- subtle preview-only vertical lenticular ridge structure;
- no `Blickwinkel` slider/bar;
- no impact on uploaded/persisted/share/video/print image bytes.

---

# 3. Files/code to inspect

Inspect the current implementation relevant to Block 3, including at least:

- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltProvider.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltHysteresisStateMachine.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`

Also inspect the existing Block 3 tests, including at least:

- `WackelbildViewModelTest.kt`
- `WackelbildScreenTest.kt`
- any existing direct tests for `TiltHysteresisStateMachine`

Use the repository to resolve exact paths instead of guessing if necessary.

Inspect only the additional files needed to understand the current Block 3 flow and dependencies.

Do not modify anything.

---

# 4. Analysis questions that must be answered

## 4.1 Exact current data flow

Trace the current path from:

sensor event  
→ normalized/remapped device orientation  
→ calibrated neutral roll  
→ roll delta  
→ hysteresis/discrete state  
→ ViewModel state  
→ Compose preview rendering

Identify exact classes/functions/state holders involved.

Also trace:

manual swipe/accessibility action  
→ manual selection  
→ override/arbitration  
→ neutral/re-arm behavior  
→ rendered image

Do not rely only on the previous analysis. Verify against the current repository.

---

## 4.2 Smallest architecture for continuous rendering

Determine the smallest implementation that can add a continuous preview value without destabilizing existing working behavior.

Analyze whether the continuous value should be:

- computed directly in `WackelbildViewModel`,
- produced by a small pure mapper/helper,
- or derived elsewhere.

Compare these options briefly and choose one preferred minimal design.

Important constraints:

- avoid architectural redesign;
- avoid replacing `TiltProvider` unless strictly necessary;
- avoid replacing the existing discrete hysteresis/arbitration system unless strictly necessary;
- preserve lifecycle behavior;
- preserve rotation handling;
- keep logic testable without Android dependencies where practical;
- keep changes reversible and localized.

Explicitly answer:

**Is a new mapper/helper file actually warranted, or is that unnecessary scope?**

Do not create it yet.

---

## 4.3 Continuous mapping behavior

Analyze the required mapping from calibrated roll delta to a continuous preview value.

The result must support:

- neutral ≈ 50/50;
- monotonic shift toward Reference in one direction;
- monotonic shift toward Capture in the other;
- clamped useful endpoints;
- no wrap discontinuity near ±180°;
- stable appearance around neutral;
- no visible sensor flicker.

Do not treat the old `9f`/`6f` hysteresis values as automatically correct for the blend mapping.

Determine which aspects should remain:

- fixed algorithmic rules,
- implementation constants,
- real-device tuning parameters.

Analyze whether a small neutral dead zone and/or smoothing/filtering is necessary, and if so, what minimal technique is appropriate.

Do not over-engineer with a new sensor/filtering framework.

---

## 4.4 Coexistence with existing discrete state

This is critical.

Analyze how the new continuous visual value can coexist with the existing discrete state required for:

- manual Reference/Capture endpoint selection;
- accessibility semantics;
- sensor/manual arbitration;
- neutral/re-arm logic.

Determine whether:

- the existing hysteresis machine can remain unchanged,
- it needs a narrowly scoped change,
- or part of its current responsibility should move elsewhere.

Explain the exact reason.

Avoid introducing parallel competing sources of truth for preview state.

The final design should make clear which state is responsible for:

1. continuous visual dominance;
2. discrete semantic/manual selection;
3. arbitration between sensor and manual interaction.

---

## 4.5 Manual interaction behavior

Analyze the minimum changes needed so that:

- manual Reference selection gives the full Reference endpoint;
- manual Capture selection gives the full Capture endpoint;
- the sensor does not instantly override that selection because of small movement/noise;
- the current neutral/re-arm behavior remains understandable and deterministic.

Identify whether the current `swipeOverrideActive` / neutral-observed flow can remain as-is or needs a minimal adjustment.

---

## 4.6 Compose rendering strategy

Analyze the smallest safe Compose change for rendering both images continuously in the same preview area.

Address:

- how both already-loaded painters should be layered;
- alpha relationship between the two images;
- preserving existing content scale, aspect ratio, sizing, alignment, clipping, and date-overlay positioning;
- avoiding bitmap decode/re-render work on every sensor update;
- avoiding unnecessary recomposition of unrelated screen content.

Do not implement code.

---

## 4.7 Lenticular ridge overlay

Analyze a minimal preview-only rendering strategy for the subtle vertical ridge effect.

The analysis must cover:

- whether it should be a stateless drawing layer;
- placement relative to the two images and date overlay;
- density-independent or pixel-aware spacing considerations;
- avoiding excessive contrast/visual obstruction;
- performance implications;
- ensuring it never reaches persisted/uploaded/output bitmaps.

Do not invent a decorative frame.

Do not add a `Blickwinkel` control.

---

## 4.8 Perspective tilt effect

Analyze a minimal preview-only perspective strategy.

The perceptual result must be:

- slight depth/tilt impression;
- side rotating away appears subtly compressed/smaller;
- no dramatic card flip;
- no strong trapezoid distortion;
- no change to transfer-image geometry;
- no effect on upload/output bytes.

Discuss whether a visual-layer transform on the preview container is sufficient and how it should relate to the same continuous tilt value.

Identify clipping/layout risks.

Do not prescribe an excessive animation system.

---

## 4.9 StateFlow / Compose performance risk

The old discrete state changed infrequently.

The new continuous value may emit on many sensor events.

Analyze:

- expected recomposition surface;
- whether update-rate reduction, deduplication, quantization, smoothing, or `distinctUntilChanged`-style handling is needed;
- whether these are necessary for correctness/performance or should be deferred until measured.

Follow the project performance rule: only isolate an actual likely bottleneck; no speculative architecture work.

---

## 4.10 Accessibility

Verify current semantics and analyze the minimal change required so that:

- TalkBack still exposes deterministic Reference/Capture identity;
- no continuous percentage is announced on every sensor update;
- manual accessibility action remains meaningful;
- continuous rendering does not cause noisy semantic updates.

Do not introduce a new global Reduce Motion system in this change.

If perspective or continuous visual movement creates an accessibility concern, identify it as a risk and propose the narrowest compliant mitigation.

---

## 4.11 Preview/output isolation

Verify the current separation between the Compose preview and the print/upload/output path.

Explicitly confirm whether the proposed:

- continuous blend,
- ridges,
- perspective

can remain entirely within preview rendering without altering:

- `reference.jpg`;
- `capture.jpg`;
- originals;
- print-transfer images;
- share images;
- video output;
- session storage.

If any proposed implementation could accidentally cross that boundary, reject it.

---

# 5. Root cause

State the exact root cause of the current mismatch in implementation terms.

The answer should distinguish between:

- working reusable infrastructure;
- obsolete presentation/state assumptions;
- missing continuous rendering state;
- missing visual surface effects.

Do not describe the whole subsystem as broken if only the presentation contract changed.

---

# 6. Preferred minimal fix strategy

Provide one preferred implementation strategy.

It must be:

- minimal;
- targeted;
- reversible;
- compatible with existing lifecycle/arbitration behavior;
- aligned with the updated Source-of-Truth and implementation plan;
- limited to Block 3.

For the preferred strategy, describe the sequence of changes conceptually, but do not provide code.

Explicitly identify:

- which existing classes likely remain unchanged;
- which existing classes likely need modification;
- whether any new file is truly justified;
- how continuous and discrete state coexist;
- how manual endpoint selection works;
- how the preview layers are composed;
- how perspective and ridges remain preview-only.

---

# 7. Alternatives rejected

Briefly identify any obvious broader designs you considered and rejected, such as:

- replacing the whole sensor pipeline;
- deleting the hysteresis machine entirely;
- creating a new animation architecture;
- routing preview through the print renderer;
- adding a manual angle slider.

Explain why each is unnecessary or conflicts with scope.

Keep this section short.

---

# 8. Prospective file scope

At the end of the analysis, list **all files that you currently expect would need modification if the preferred strategy is approved**.

Separate into:

- production files;
- unit-test files;
- instrumentation/UI-test files;
- documentation files, if any.

For every file, state exactly why it would need to change.

Also list relevant inspected files expected to remain unchanged.

This is still analysis only — do not edit anything.

If exact paths differ from the names above, report the repository-resolved paths.

---

# 9. Verification plan

Propose the verification required after implementation.

At minimum cover:

- mapper/continuous-state unit tests;
- ViewModel/manual arbitration tests;
- Compose/UI instrumentation;
- real-device sensor behavior;
- portrait/landscape;
- lifecycle leave/re-enter;
- date overlay regression;
- accessibility;
- output/upload-byte isolation.

List the exact Gradle commands that should be run after implementation according to project policy/repository capabilities.

Explicitly distinguish:

- commands required after implementation;
- commands not required during this analysis-only step;
- real-device validation that cannot be replaced by automated tests.

Do not run implementation verification commands in this analysis-only step unless repository policy explicitly mandates them for analysis.

---

# 10. Release-safety review

Briefly assess whether the proposed change affects:

- permissions;
- privacy;
- offline behavior;
- sensor lifecycle;
- navigation;
- storage;
- upload/network behavior;
- Play Store compliance;
- release build stability.

Flag any real risk.

Do not introduce telemetry, analytics, permissions, or network changes.

---

# 11. Required output format

Return only an analysis report with these headings:

1. Source-of-Truth check
2. Current implementation data flow
3. Exact root cause
4. Continuous-value design options
5. Preferred minimal architecture
6. Discrete-state / manual-arbitration coexistence
7. Compose rendering analysis
8. Ridge-overlay analysis
9. Perspective analysis
10. Performance analysis
11. Accessibility analysis
12. Preview/output isolation
13. Prospective file scope
14. Verification plan
15. Release-safety risks
16. Open questions / tuning decisions

Do not output code.
Do not modify files.
Do not provide an implementation patch.

After the analysis report, STOP.

The next step will be a separate STEP 2 scope-confirmation iteration after review of this analysis.
