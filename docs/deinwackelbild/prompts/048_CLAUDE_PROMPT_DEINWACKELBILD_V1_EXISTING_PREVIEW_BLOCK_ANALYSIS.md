# Claude Prompt --- Analyze Existing DeinWackelbild Preview Block and Prepare Spec Amendment

## Task

We want to improve **only the already existing interactive
DeinWackelbild / lenticular preview** in SameView.

**IMPORTANT: This is STEP 1 --- ANALYSIS ONLY.**

-   Do NOT modify any files yet.
-   Do NOT edit any MD specification yet.
-   Do NOT implement code.
-   Do NOT refactor.
-   Do NOT make unrelated improvements.
-   Do NOT pull forward or combine any other DeinWackelbild blocks with
    this change.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and all existing Source-of-Truth
specifications strictly.

------------------------------------------------------------------------

## 1. First identify the existing DeinWackelbild preview block

The current DeinWackelbild specification / implementation plan is
already organized into **blocks**.

First inspect the current DeinWackelbild documentation and identify the
**existing block that introduced or governs the local interactive
lenticular / tilt / shake preview**.

IMPORTANT:

-   Do not invent a new block before checking the existing block
    structure.
-   State the exact existing block number and title.
-   State the MD file and exact section.
-   Determine what that block already specifies as mandatory preview
    behavior.
-   Then inspect the actual current code belonging to that block.
-   If code and specification differ, report the inconsistency
    explicitly.

This UX change should be treated as a **targeted extension of the
existing preview block**, provided the existing documentation structure
supports that.

Later DeinWackelbild blocks --- especially partner-key, networking,
permission, and upload work --- are outside this change and must not be
mixed into it.

------------------------------------------------------------------------

# 2. Desired new preview UX

The current SameView preview already reacts to phone tilt.

It should now communicate much more clearly how a **real physical
lenticular / "Wackelbild" print** behaves when tilted.

The improvement consists of exactly three related visual effects.

## 2.1 Continuous tilt-controlled image transition

At a neutral / approximately centered device orientation:

-   both images should be visible at approximately **50 / 50**;
-   the preview should not already clearly select image A or image B.

When tilting:

-   toward direction A, image A continuously becomes more dominant;
-   image B simultaneously becomes correspondingly weaker / more
    transparent;
-   toward direction B, the behavior is reversed;
-   stronger relevant tilt should progressively approach an almost 100 /
    0 state;
-   returning toward neutral should continuously return toward
    approximately 50 / 50.

Conceptually:

-   maximum useful tilt A → approximately A 100% / B 0%
-   neutral → approximately A 50% / B 50%
-   maximum useful tilt B → approximately A 0% / B 100%

There must be **no hard A/B switching threshold**.

Small sensor movements / sensor noise must not create visible flickering
or nervous alpha changes.

Do **not invent** exact thresholds, dead zones, smoothing, or mapping
curves during this analysis. First determine which normalized / filtered
values the existing preview block already provides and how they can be
reused with the smallest possible change.

------------------------------------------------------------------------

## 2.2 Subtle vertical lenticular ridges

The preview should contain very subtle **vertical lines / ridges**.

They should visually suggest that a real physical lenticular print has a
vertically structured lens surface.

Requirements:

-   vertical orientation;
-   very subtle;
-   distributed consistently across the preview;
-   only slightly dark/light, or otherwise as restrained as appropriate
    for the existing SameView UI;
-   photographs must remain clearly visible;
-   must not look like a strong grid, barcode, technical overlay, or
    moiré pattern;
-   purely a visual effect of the local preview.

These lines must specifically **not**:

-   be rendered into session images;
-   become part of the image data generated or uploaded to
    DeinWackelbild;
-   affect Compare or export rendering.

------------------------------------------------------------------------

## 2.3 Subtle physical 3D tilt / perspective effect

In addition, the **entire preview surface** should react to phone tilt
as though the user were tilting a small physical print in space.

The intended behavior:

When the phone is tilted in one direction, the side of the preview
rotating away from the viewer should appear **slightly smaller /
compressed**.

Tilting in the opposite direction should mirror the effect.

This should create a subtle spatial impression:

**The user appears to be tilting a real lenticular print in their
hand.**

Important:

-   subtle only;
-   naturally coupled to the same relevant tilt direction used for the
    image transition;
-   the receding side becomes slightly shortened / smaller;
-   no strong trapezoidal distortion;
-   no obvious card-flip animation;
-   no large 3D rotation;
-   no complex 3D model;
-   no free manipulation gesture;
-   no modification of the underlying images or their stored geometry.

Analyze which **smallest possible Compose / rendering solution** within
the existing preview structure can create this impression.

Do not implement it yet.

------------------------------------------------------------------------

# 3. Explicit non-goal: no viewing-angle bar

The preview on `deinwackelbild.de` also shows a viewing-angle /
"Blickwinkel" bar below the image.

We do **NOT** want this in SameView.

The interaction remains:

**Tilt the phone → preview reacts.**

Do not add:

-   a viewing-angle bar;
-   a manual angle slider;
-   an alternative manual preview control;
-   additional UI merely to reproduce the website control.

The existing tilt instruction in the SameView screen remains the basic
interaction concept.

------------------------------------------------------------------------

# 4. Hard scope boundaries

This change affects only the visual / interactive preview of the
**already existing DeinWackelbild preview block**.

Do not change:

-   partner-key injection;
-   API-key handling;
-   networking code;
-   `INTERNET` permission;
-   upload logic;
-   API client;
-   request / response handling;
-   retry / error classification;
-   date-overlay rendering;
-   date-overlay toggle;
-   session storage;
-   session metadata;
-   `capture.jpg`;
-   `reference.jpg`;
-   original files;
-   CompareScreen;
-   Compare rendering;
-   Share Comparison;
-   Video Export;
-   CameraScreen;
-   camera overlay;
-   capture workflow;
-   navigation;
-   other screens.

If the current architecture technically couples the preview to any of
these areas, do not redesign it automatically. Report the coupling and
resulting risk explicitly.

------------------------------------------------------------------------

# 5. Technical analysis

Inspect the actual current code of the existing preview block.

Determine exactly:

1.  Which existing block introduced / governs the preview.
2.  Which production files belong to that block.
3.  Which screen / composable renders the preview.
4.  Where sensors are registered and unregistered.
5.  Which sensor and sensor values are used.
6.  Which axis drives the visual transition.
7.  Whether the value is raw, normalized, smoothed, clamped,
    thresholded, or otherwise transformed.
8.  How that value currently determines image A versus image B.
9.  Whether the current behavior is a hard switch, alpha blend,
    animation, or something else.
10. How preview bounds, aspect ratio, clipping, shape, and transforms
    are currently implemented.
11. Whether the same existing tilt value can safely drive all three
    desired visual effects:
    -   alpha blend;
    -   lenticular ridges;
    -   perspective tilt.
12. Whether existing smoothing / dead-zone behavior is sufficient.
13. What minimal adjustment would be required if it is not sufficient.
14. Whether rapid sensor updates could cause unnecessary Compose
    recompositions, allocations, bitmap processing, or jank.
15. Whether sensor lifecycle and registration can remain completely
    unchanged.
16. Whether the perspective effect can fit cleanly into the current clip
    / preview structure or requires additional bounds handling.
17. How the current behavior works in portrait and landscape.

Do not guess. Reference concrete files, classes, composables, functions,
and current logic.

------------------------------------------------------------------------

# 6. Source-of-Truth check

Before evaluating the change, inspect at minimum:

-   `CLAUDE_PROJECT_INSTRUCTION.md`;
-   the current DeinWackelbild specification;
-   its implementation plan / block structure;
-   other MD specifications only where they actually govern this screen.

Explicitly answer:

-   Which MD file is authoritative for the existing preview block?
-   What is the exact block number and title?
-   What rules does it currently define for the preview?
-   Which rules remain unchanged?
-   Which rules need to be extended or replaced for this UX?
-   Is there any conflict with another Source-of-Truth specification?

If an existing rule conflicts with this new product decision, identify
the exact conflict. Do not silently override it.

------------------------------------------------------------------------

# 7. Required analysis output

Return the analysis in this structure:

## A. Existing preview block

-   exact block number;
-   exact block title;
-   MD file;
-   relevant sections;
-   current block status;
-   what the specification says should already be implemented.

## B. Current implementation

Describe the actual data flow:

**Sensor → processing / normalization → preview state → rendering**

Include full file paths, classes / composables, and functions.

## C. Current vs. desired behavior

For each of the three effects:

1.  continuous alpha blending;
2.  lenticular ridges;
3.  perspective 3D tilt;

describe:

-   current state;
-   desired state;
-   exact functional gap.

## D. Minimal fix strategy

Describe the smallest technical strategy.

Priorities:

-   reuse existing sensor logic;
-   preserve the existing preview structure;
-   no new architecture;
-   no bitmap regeneration per sensor update;
-   no changes outside the preview.

Do not provide code.

## E. Exact prospective file scope

List **all files** that would likely need modification during the later
implementation.

Separate them into:

-   Source-of-Truth specification;
-   production code;
-   tests.

If a file is uncertain, explain why rather than adding it speculatively.

## F. Risks / regression boundaries

Assess specifically:

-   sensor lifecycle;
-   Compose recomposition;
-   performance;
-   preview clipping;
-   perspective transform;
-   portrait / landscape;
-   accessibility / Reduce Motion if existing project rules apply;
-   isolation from upload / output images;
-   regression risk to the existing DeinWackelbild flow.

## G. Concrete specification amendment proposal

Draft the **exact content change for the already existing preview
block**.

Requirements:

-   preserve the existing block number and structure where possible;
-   do not create a parallel specification;
-   define precise MUST / MUST NOT rules;
-   fully define the three new effects;
-   explicitly record the viewing-angle bar as a non-goal;
-   record the scope boundaries;
-   add verification / testing requirements for the improved preview.

But:

**Do NOT edit the MD file in this iteration.**

## H. Verification plan

State the later required:

-   unit tests;
-   Compose / UI / instrumentation tests;
-   Gradle / build commands;
-   real-device checks.

For real-device verification specifically include:

-   neutral orientation ≈ 50 / 50;
-   continuous transition in both directions;
-   no hard switch;
-   no visible sensor flicker;
-   subtle lenticular ridges;
-   correct direction of the perspective effect;
-   perspective remains subtle;
-   portrait and landscape;
-   leave screen / return to screen;
-   no modification of image data later uploaded.

------------------------------------------------------------------------

# STOP CONDITION

Stop after this analysis.

Do not modify any file. Do not update the specification. Do not write
code. Do not change tests. Do not work on any other block.

We will review the analysis first and explicitly approve the next step
before the specification is changed.
