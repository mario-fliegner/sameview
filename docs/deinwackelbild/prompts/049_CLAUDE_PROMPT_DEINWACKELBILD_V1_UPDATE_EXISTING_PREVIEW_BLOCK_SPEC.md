# Claude Prompt --- Update DeinWackelbild V1 Spec for Existing Preview Block

## Objective

Update the existing DeinWackelbild V1 Source-of-Truth specification so
that **Block 3 --- "Tilt/swipe interaction"** authoritatively defines
the newly approved lenticular preview UX.

This is a **specification-only iteration**.

Do **not** implement production code. Do **not** modify tests. Do
**not** modify the implementation plan unless the existing
Source-of-Truth rules explicitly require it as part of keeping
documentation consistent; if that is required, STOP and report it before
modifying any additional file. Do **not** work on any later
DeinWackelbild block. Do **not** make unrelated documentation changes.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

------------------------------------------------------------------------

# 1. Source of truth and existing block

The previous analysis established:

-   Existing implementation block: **Block 3 --- "Tilt/swipe
    interaction"**
-   Status: currently implemented
-   Authoritative Source-of-Truth specification:
    `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
-   Relevant existing sections:
    -   §7 `Preview Image Presentation`
    -   §8 `Lenticular Preview Interaction`
    -   especially §8.1 `Core behavior`
    -   §8.3 `Tilt interaction`
    -   §8.7 `Accessibility`
    -   §8.8 `Sensor lifecycle and privacy`

The current specification intentionally defines a hard Reference/Capture
switch and explicitly prohibits fading/blending and a 3D effect.

That existing behavior is now being **intentionally superseded for the
local preview UX** by the product decision below.

This is therefore a deliberate Source-of-Truth amendment, not an attempt
to reinterpret the old wording.

------------------------------------------------------------------------

# 2. Approved product behavior

Update the existing specification so the local DeinWackelbild preview
behaves like a subtle simulation of a physical lenticular print.

The feature consists of exactly three related visual behaviors.

## 2.1 Continuous tilt-driven image blend

The preview MUST render both Reference and Capture simultaneously and
vary their visual dominance continuously according to the existing
relevant device-tilt direction.

Required behavior:

-   At the calibrated neutral orientation, Reference and Capture MUST be
    visible at approximately 50/50.
-   Tilting toward Reference MUST continuously increase Reference
    dominance and decrease Capture dominance.
-   Tilting toward Capture MUST continuously increase Capture dominance
    and decrease Reference dominance.
-   At the maximum useful preview tilt in either direction, the
    corresponding image SHOULD be visually dominant to the point that
    the opposite image is effectively or nearly invisible.
-   Returning toward neutral MUST continuously return toward
    approximately 50/50.
-   The transition MUST be continuous.
-   The preview MUST NOT use the current hard A/B visual switch as its
    primary tilt behavior.
-   Sensor noise around a stable orientation MUST NOT cause visible
    flicker or nervous oscillation.
-   The exact mapping curve, useful maximum tilt, filtering constants,
    and implementation mechanism are implementation details and MUST NOT
    be unnecessarily hard-coded into the UX Source-of-Truth
    specification unless an existing project convention requires such
    values to be normative.

Conceptually:

-   useful maximum toward Reference → approximately Reference 100% /
    Capture 0%
-   calibrated neutral → approximately Reference 50% / Capture 50%
-   useful maximum toward Capture → approximately Reference 0% / Capture
    100%

This is a local preview behavior only.

------------------------------------------------------------------------

## 2.2 Subtle vertical lenticular ridge overlay

The preview MUST include a very subtle vertical surface structure that
visually suggests the ridges/lenses of a physical lenticular print.

Requirements:

-   ridges/lines MUST be vertical;
-   they MUST remain visually subtle;
-   they MUST be distributed consistently across the visible preview;
-   they MUST NOT significantly darken or obscure either photograph;
-   they MUST NOT resemble a strong grid, barcode, technical alignment
    overlay, or distracting moiré pattern;
-   they MUST be treated as a preview-only visual surface effect.

The ridge effect MUST NOT be rendered into:

-   `reference.jpg`;
-   `capture.jpg`;
-   session originals;
-   transfer/upload images;
-   Share Comparison output;
-   video output;
-   any persisted image asset.

Clarify the existing §7 wording about decorative or simulated
physical-product frames so it does not conflict with this approved
surface effect:

-   SameView still MUST NOT add a decorative physical-product frame
    around the preview.
-   The subtle lenticular ridge surface itself is an explicitly allowed
    exception because it communicates the behavior/material of the
    intended lenticular product rather than adding a decorative frame.

------------------------------------------------------------------------

## 2.3 Subtle perspective / physical tilt effect

The complete visible preview surface MUST react subtly to the same
relevant device tilt so it appears as though a small physical print is
being tilted in space.

Required perceptual behavior:

-   the side rotating away from the viewer MUST appear slightly smaller
    / compressed;
-   reversing the tilt MUST mirror the effect;
-   the effect MUST correspond naturally to the direction used for the
    image blend;
-   the effect MUST remain deliberately subtle;
-   it MUST support the physical-print impression rather than become a
    prominent animation.

The preview MUST NOT:

-   use a dramatic card-flip animation;
-   use a large or exaggerated 3D rotation;
-   become strongly trapezoidal;
-   introduce a free-form manipulation gesture;
-   change the stored image geometry;
-   change image alignment used by the actual transfer/upload pipeline.

Remove or replace the existing §7 rule that categorically states that
the preview has "no 3D effect", because that rule directly conflicts
with this newly approved behavior.

The specification SHOULD define the perceptual result, not mandate a
specific Compose implementation such as `graphicsLayer`, `rotationY`,
`cameraDistance`, Canvas transforms, or any other rendering API.

------------------------------------------------------------------------

# 3. No viewing-angle / "Blickwinkel" bar

Explicitly record this as a non-goal.

SameView MUST NOT add the viewing-angle / `Blickwinkel` slider/bar used
by the DeinWackelbild website.

There MUST NOT be:

-   a manual viewing-angle slider;
-   a second angle-control UI;
-   a website-style angle indicator whose purpose is to control the
    preview.

The phone tilt remains the primary interactive preview mechanism.

Do not redesign the surrounding screen.

------------------------------------------------------------------------

# 4. Swipe and accessibility behavior

The existing swipe fallback and accessibility toggle MUST remain usable
and deterministic.

Update the specification so their behavior remains meaningful with
continuous visual blending.

Approved behavior:

-   A manual swipe/toggle MUST select the corresponding full image
    endpoint rather than an intermediate blend.
-   Selecting Reference manually MUST result in the Reference endpoint
    (approximately 100% Reference / 0% Capture).
-   Selecting Capture manually MUST result in the Capture endpoint
    (approximately 0% Reference / 100% Capture).
-   Manual interaction MUST NOT leave the preview at an ambiguous
    intermediate blend.
-   The existing sensor/manual arbitration principle MUST be preserved:
    manual interaction must not immediately be overridden by sensor
    noise.
-   Preserve the existing neutral/re-arm concept unless the current
    specification requires different wording to express the same
    behavior with the continuous preview.
-   Do NOT redesign sensor lifecycle or manual-override architecture in
    this specification update.

For accessibility semantics:

-   Preserve a deterministic discrete semantic identity of the currently
    dominant/selected side for accessibility rather than exposing
    continuously changing percentage announcements.
-   The specification MUST NOT require TalkBack to announce every
    continuous blend change.
-   Existing accessibility interaction must remain available without
    requiring physical tilt.

Do not introduce a new global Reduce Motion feature as part of this
change. If the existing project already contains a binding accessibility
rule that conflicts with this behavior, report it before making an
unrelated accessibility-system change.

------------------------------------------------------------------------

# 5. Existing behavior that must remain unchanged

The specification amendment MUST preserve all unaffected DeinWackelbild
contracts.

In particular, do not change the documented behavior for:

-   date overlay preview;
-   date overlay toggle;
-   navigation;
-   session selection;
-   partner-key handling;
-   API/network behavior;
-   `INTERNET` permission;
-   upload behavior;
-   image preparation;
-   image ordering;
-   metadata;
-   retry/error behavior;
-   session storage;
-   Compare rendering;
-   Share Comparison;
-   Video Export;
-   camera workflow;
-   sensor lifecycle/privacy except wording strictly necessary to
    describe the new preview behavior.

The three new effects are **preview-only**.

They MUST NOT alter the actual image bytes prepared for DeinWackelbild
transfer/upload.

------------------------------------------------------------------------

# 6. Documentation structure

Prefer updating the existing sections rather than creating a parallel
specification.

Expected approach:

-   amend §7 `Preview Image Presentation` where its existing
    prohibitions conflict with the approved ridge/perspective behavior;
-   amend §8.1 `Core behavior` to replace the hard-switch/no-fade
    contract with the continuous blend contract;
-   amend §8.3 `Tilt interaction` as needed to define how tilt
    conceptually drives the continuous preview;
-   amend §8.7 `Accessibility` only as needed to preserve deterministic
    manual/accessibility behavior;
-   preserve §8.8 `Sensor lifecycle and privacy` unless wording must be
    adjusted for consistency;
-   add narrowly scoped subsections for perspective tilt and lenticular
    ridge behavior if this produces clearer normative documentation than
    overloading an existing subsection.

Do NOT renumber unrelated sections unnecessarily.

Do NOT rewrite unrelated prose.

Do NOT create a new V2 document.

This remains `DEINWACKELBILD_INTEGRATION_V1.md`.

------------------------------------------------------------------------

# 7. Scope confirmation before editing

Before changing the file, perform the required STEP 2 scope
confirmation.

State:

1.  every file that will be modified;
2.  the exact sections that will change;
3.  what will change in each section;
4.  that no production code or tests will be touched;
5.  whether any other Source-of-Truth document would become
    inconsistent;
6.  risks of the documentation change.

Expected file scope is currently:

`docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

If you determine that any additional file MUST be changed in the same
iteration for documentation consistency, do **not** silently expand
scope. Report it and STOP for approval.

After the scope confirmation, STOP and wait for explicit approval before
editing the MD file.

------------------------------------------------------------------------

# 8. After explicit approval

Only after explicit approval:

-   edit exactly the approved Source-of-Truth file;
-   make only the agreed amendments;
-   preserve existing section structure where possible;
-   remove/replace obsolete contradictory rules rather than leaving both
    old and new behavior in the document;
-   do not implement code;
-   do not update tests;
-   do not modify unrelated documentation;
-   do not reformat unrelated sections.

After editing, report:

-   exact file modified;
-   exact sections modified;
-   obsolete rules removed/replaced;
-   new normative behavior added;
-   confirmation that no other files changed;
-   whether implementation-plan synchronization is required as the
    **next separate iteration**.

------------------------------------------------------------------------

# 9. Validation of the specification edit

After the edit, re-read the affected sections and explicitly verify that
the resulting Source-of-Truth has no internal contradiction regarding:

-   hard switch vs. continuous blend;
-   no fade vs. 50/50 neutral blend;
-   no 3D effect vs. subtle perspective effect;
-   no physical-product frame vs. allowed subtle lenticular ridge
    surface;
-   tilt behavior vs. manual swipe/accessibility behavior;
-   preview-only effects vs. upload/output image isolation;
-   presence vs. explicit absence of a `Blickwinkel` control.

No code/build/test execution is required for a documentation-only edit
unless repository policy explicitly requires it.

Do not start implementation after completing the specification update.
