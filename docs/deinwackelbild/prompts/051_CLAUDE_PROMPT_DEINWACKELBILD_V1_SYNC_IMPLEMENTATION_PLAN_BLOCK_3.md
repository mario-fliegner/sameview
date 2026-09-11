# Claude Prompt --- Synchronize DeinWackelbild V1 Implementation Plan Block 3 with Updated Preview Spec

## Objective

Synchronize the existing DeinWackelbild V1 implementation plan with the
now-updated Source-of-Truth specification for the local lenticular
preview.

This is a **documentation / implementation-plan synchronization
iteration only**.

Do **not** modify production code. Do **not** modify tests. Do **not**
modify the Source-of-Truth integration specification. Do **not**
implement the new preview behavior yet. Do **not** work on later
DeinWackelbild blocks. Do **not** make unrelated documentation changes.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

------------------------------------------------------------------------

# 1. Current situation

The authoritative Source-of-Truth specification has already been
intentionally amended:

`docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

The existing local preview behavior governed by **Block 3 ---
"Tilt/swipe interaction"** is no longer specified as a hard
Reference/Capture switch.

The updated Source-of-Truth now requires, in summary:

-   approximately 50/50 Reference/Capture at calibrated neutral
    orientation;
-   continuous tilt-driven change in Reference/Capture dominance;
-   progression toward the corresponding full-image endpoint as useful
    tilt increases;
-   no hard A/B visual switch during normal tilt interaction;
-   stable visual behavior without visible sensor flicker;
-   manual swipe/accessibility interaction selecting deterministic
    full-image endpoints;
-   preservation of the existing sensor/manual arbitration principle;
-   a subtle perspective / physical-print tilt effect;
-   a subtle vertical lenticular ridge surface;
-   no viewing-angle / `Blickwinkel` slider or bar;
-   all new visual effects remain preview-only and must never alter
    persisted or uploaded image data.

The Source-of-Truth acceptance criteria in §47 have also already been
synchronized with this behavior.

------------------------------------------------------------------------

# 2. Known implementation-plan inconsistency

The previous analysis established that:

`docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

still describes the old hard-switch architecture in its **§7 Tilt /
Swipe Architecture** and still marks the corresponding Block 3 work as
`✅ implemented`.

That description is now stale relative to the updated Source-of-Truth.

The purpose of this iteration is to synchronize the implementation plan
so that it accurately describes:

1.  what remains implemented and reusable from the original Block 3;
2.  what behavior is now obsolete;
3.  what additional / revised work Block 3 requires before it can again
    be considered fully implemented against the current Source-of-Truth.

------------------------------------------------------------------------

# 3. Preserve the existing block identity

Do NOT create a new feature block for this UX change unless the existing
plan structure makes that absolutely necessary.

Preferred model:

**Block 3 remains Block 3 --- "Tilt/swipe interaction".**

The block should be updated to reflect that its original implementation
is now only partially compliant with the amended Source-of-Truth.

Do not renumber later blocks.

Do not move Partner Key, networking, upload, or other later work into
Block 3.

------------------------------------------------------------------------

# 4. Required implementation-plan content

The synchronized plan must distinguish clearly between **existing
reusable implementation** and **new work required**.

## 4.1 Existing behavior / infrastructure to preserve

Based on the previous code analysis, the following existing architecture
is expected to remain reusable and should be documented accurately if
confirmed against the current repository:

-   `Sensor.TYPE_ROTATION_VECTOR`;
-   existing `TiltProvider`;
-   existing orientation/remapping behavior;
-   current sensor lifecycle registration/unregistration;
-   neutral-roll calibration;
-   existing device-rotation handling;
-   existing manual swipe fallback;
-   existing accessibility toggle;
-   existing manual/sensor arbitration and neutral/re-arm principle;
-   already-loaded Reference/Capture preview painters;
-   existing preview sizing/aspect-ratio behavior;
-   date-overlay preview behavior.

Do not claim these are preserved without re-checking the current
implementation and the updated Source-of-Truth.

------------------------------------------------------------------------

## 4.2 Obsolete Block 3 behavior

The implementation plan must no longer present the following as the
target architecture:

-   hard visual Reference/Capture switching as the primary tilt
    behavior;
-   a design where no continuous progress value exists by definition;
-   fixed threshold/hysteresis behavior as the sole mechanism
    controlling visual image presentation;
-   mutually exclusive rendering of only one preview image as the
    intended final behavior;
-   any statement that fading/blending is prohibited;
-   any statement that the preview must have no perspective/3D effect.

Historical implementation facts may be retained only if clearly
identified as the **current pre-update implementation that must be
revised**, not as the desired final architecture.

------------------------------------------------------------------------

## 4.3 New Block 3 work to document

The implementation plan must describe the required implementation work
for the amended preview contract without prematurely implementing it.

The plan should cover:

### Continuous preview value

The existing sensor delta must feed a continuous preview value suitable
for rendering Reference/Capture dominance.

The implementation plan should require:

-   a normalized/clamped continuous value;
-   stable behavior around neutral orientation;
-   suppression/damping of visible sensor noise;
-   approximately 50/50 at calibrated neutral;
-   progression toward the appropriate endpoint with increasing useful
    tilt.

Do not hard-code tuning constants in the implementation plan unless the
plan's existing conventions require concrete provisional values. If
tuning values are not yet validated, identify them explicitly as
real-device tuning parameters rather than normative UX values.

### Preserve discrete semantics where still required

The continuous rendering value must not automatically eliminate existing
discrete state where discrete state is still required for:

-   manual swipe/accessibility selection;
-   accessibility semantic identity;
-   sensor/manual arbitration;
-   neutral/re-arm behavior.

The plan should describe the smallest architecture that allows
continuous rendering while preserving these existing deterministic
behaviors.

Do not redesign the entire state machine unless the current
implementation demonstrably requires it.

### Rendering both images

The implementation plan should state that the preview must render both
already-loaded image painters in the same preview area and control their
visual dominance continuously.

It must explicitly avoid:

-   decoding/resizing bitmaps on every sensor update;
-   writing the blended result to disk;
-   routing the visual blend through the print/upload renderer.

### Lenticular ridge overlay

Document the new static/subtle vertical ridge surface as a preview
rendering layer.

It must remain:

-   lightweight;
-   preview-only;
-   independent of persisted image data;
-   subordinate to the photographs.

### Perspective tilt

Document the new subtle physical-print perspective effect as a
preview-layer transform driven by the same continuous tilt
direction/value.

The implementation plan may discuss appropriate Compose rendering
mechanisms if that matches the document's existing technical level, but
it must not unnecessarily lock the implementation to a specific API if
multiple minimal solutions remain viable.

The perspective effect must remain visual only and must not alter
layout/source geometry used by the upload/output pipeline.

### Manual interaction

Document the amended manual behavior:

-   manual Reference selection → full Reference endpoint;
-   manual Capture selection → full Capture endpoint;
-   no ambiguous intermediate manual state;
-   sensor control must not immediately override the manual endpoint due
    to noise;
-   preserve the existing neutral/re-arm arbitration principle unless
    implementation analysis proves a minimal adjustment is required.

### No Blickwinkel control

Explicitly preserve the Source-of-Truth non-goal:

-   no viewing-angle / `Blickwinkel` slider/bar;
-   no second manual angle-control mechanism.

------------------------------------------------------------------------

# 5. Testing / verification plan

Synchronize the Block 3 verification section so later implementation can
be validated against the amended Source-of-Truth.

At minimum, plan for:

## Unit tests

-   continuous mapping at neutral;
-   monotonic progression in both directions;
-   clamping/endpoints;
-   noise behavior around neutral;
-   interaction with manual override / neutral re-arm;
-   preservation of discrete accessibility/manual semantics where
    applicable.

## UI / instrumentation verification

-   both images can coexist in the preview;
-   correct dominance at controlled preview-state values;
-   manual endpoint behavior remains deterministic;
-   ridge layer remains preview-only;
-   perspective application does not crash or break preview bounds.

## Real-device verification

Must include:

-   calibrated neutral ≈ 50/50;
-   smooth continuous transition in both directions;
-   no hard visual switch during normal tilt;
-   no visible flicker while holding the phone still;
-   correct tilt direction;
-   perspective direction feels physically natural;
-   perspective remains subtle;
-   lenticular ridges are visible but unobtrusive;
-   portrait;
-   landscape / display rotation;
-   leave and re-enter screen;
-   manual swipe/accessibility fallback;
-   sensor/manual re-arm behavior;
-   date overlay remains correct;
-   uploaded/prepared image bytes remain unaffected by preview-only
    effects.

## Build / test commands

Document the repository-appropriate verification commands expected after
implementation, including the relevant unit, instrumentation, and
debug-build tasks.

Do not execute them in this documentation-only iteration unless
repository policy explicitly requires tests for MD-only changes.

------------------------------------------------------------------------

# 6. Block status

The implementation plan must no longer misleadingly state that Block 3
is fully `✅ implemented` against the current Source-of-Truth if
production code still implements the obsolete hard-switch behavior.

Use the plan's existing status vocabulary / formatting.

Choose the smallest accurate status representation that communicates:

-   the original Block 3 infrastructure exists;
-   the Source-of-Truth changed;
-   implementation work is now required to bring Block 3 back into
    compliance.

Do not invent a new project-wide status convention merely for this
block.

------------------------------------------------------------------------

# 7. Hard scope boundaries

This synchronization must NOT alter the planned behavior or status of
unrelated blocks.

In particular, do not change:

-   Block 4 date-overlay behavior;
-   later partner-key injection work;
-   networking;
-   `INTERNET` permission;
-   upload activation;
-   API client;
-   retry/error handling;
-   image preparation/output behavior;
-   navigation;
-   session storage;
-   Compare;
-   Share Comparison;
-   Video Export;
-   Camera workflow.

Do not reorder the remaining DeinWackelbild roadmap.

------------------------------------------------------------------------

# 8. STEP 2 --- scope confirmation before editing

Before modifying the implementation plan, inspect:

-   the current updated `DEINWACKELBILD_INTEGRATION_V1.md`;
-   the current `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`;
-   the current Block 3 production implementation only as needed to
    ensure the plan accurately distinguishes existing infrastructure
    from missing work.

Then provide a scope confirmation containing:

1.  exact file(s) proposed for modification;
2.  exact implementation-plan sections proposed for modification;
3.  current Block 3 status/text that is stale;
4.  exact categories of plan content to be changed;
5.  confirmation that Source-of-Truth spec, production code, and tests
    will not be modified;
6.  confirmation that unrelated blocks will not be modified;
7.  any newly discovered documentation inconsistency or required scope
    expansion;
8.  risks.

Then STOP.

Wait for explicit approval before editing any file.

------------------------------------------------------------------------

# 9. After explicit approval

Only after approval:

-   modify exactly the approved implementation-plan file and sections;
-   synchronize them with the current Source-of-Truth;
-   preserve existing structure and terminology where possible;
-   mark Block 3 accurately according to the plan's existing status
    convention;
-   do not implement code;
-   do not modify tests;
-   do not modify the Source-of-Truth spec;
-   do not touch unrelated blocks;
-   do not reformat unrelated content.

After editing, report:

-   exact file modified;
-   exact sections modified;
-   old Block 3 status;
-   new Block 3 status;
-   reusable existing infrastructure documented;
-   obsolete hard-switch assumptions removed/reclassified;
-   new implementation work documented;
-   verification plan updated;
-   confirmation that no other file changed.

Finally, perform a documentation consistency check between the updated
Block 3 implementation plan and the authoritative preview rules in:

-   §7;
-   §8.1;
-   §8.3;
-   §8.4;
-   §8.5;
-   §8.7;
-   §8.8;
-   §8.9;
-   §8.10;
-   §47 Acceptance Criteria --- UX;
-   §55 Hard Non-Goals.

If another contradiction is discovered outside the approved scope,
report it and STOP rather than silently expanding scope.

Do not begin production implementation after this synchronization.
