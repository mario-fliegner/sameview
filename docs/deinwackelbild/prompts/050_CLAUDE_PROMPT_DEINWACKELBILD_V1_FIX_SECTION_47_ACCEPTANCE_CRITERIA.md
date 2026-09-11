# Claude Prompt --- Fix Remaining DeinWackelbild V1 §47 Acceptance-Criteria Contradiction

## Objective

Perform one **minimal documentation-consistency fix** in the existing
DeinWackelbild V1 Source-of-Truth specification.

The previous approved specification amendment changed the existing Block
3 preview behavior from a hard Reference/Capture switch to a continuous
tilt-driven lenticular preview blend.

During the required post-edit consistency check, one remaining
contradictory acceptance criterion was correctly identified:

`§47 Acceptance Criteria — UX`, point 4 currently states:

> "Tilt switches directly between Reference and Capture without
> fade/animation."

This now directly contradicts the newly authoritative behavior in §8.1 /
§8.3.

This iteration exists **only to remove that single contradiction**.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

------------------------------------------------------------------------

# 1. Source of truth

File:

`docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

The newly updated authoritative preview contract already defines:

-   approximately 50/50 Reference/Capture at calibrated neutral
    orientation;
-   continuous tilt-driven change in image dominance;
-   progression toward approximately 100/0 or 0/100 at the useful tilt
    endpoints;
-   no hard A/B visual switching as the primary tilt behavior;
-   stable behavior without visible sensor flicker;
-   manual swipe/accessibility selection using deterministic full-image
    endpoints;
-   subtle perspective tilt;
-   subtle lenticular ridge overlay;
-   no viewing-angle / `Blickwinkel` control.

Do not redesign or reinterpret those already-approved rules in this
iteration.

------------------------------------------------------------------------

# 2. Exact problem

`§47 Acceptance Criteria — UX`, point 4 still validates the obsolete
behavior:

> "Tilt switches directly between Reference and Capture without
> fade/animation."

That acceptance criterion is no longer valid because §8.1 and §8.3 now
require continuous blending.

The acceptance criteria must validate the current Source-of-Truth
behavior rather than the superseded hard-switch behavior.

------------------------------------------------------------------------

# 3. Required minimal change

Update **only §47 Acceptance Criteria --- UX, point 4**.

Replace the obsolete hard-switch acceptance criterion with a concise
acceptance criterion that verifies the new normative behavior already
defined in §8.1 / §8.3.

The replacement MUST express that:

-   calibrated neutral orientation presents approximately 50/50
    Reference/Capture;
-   tilting continuously changes image dominance toward the
    corresponding endpoint;
-   there is no hard A/B visual switch during normal tilt interaction;
-   the behavior is consistent with §8.1 and §8.3.

Keep the acceptance criterion concise and appropriate for an
acceptance-criteria list.

Do **not** duplicate the complete §8 specification inside §47.

Prefer referencing §8.1 / §8.3 where useful so that detailed tuning
rules remain defined in one authoritative place.

Do not introduce implementation-specific values, Compose APIs, mapping
algorithms, smoothing constants, or sensor thresholds.

------------------------------------------------------------------------

# 4. Hard scope

Expected modified file:

`docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Expected modified location:

**§47 Acceptance Criteria --- UX, point 4 only.**

Do NOT modify:

-   §7;
-   §8;
-   §55;
-   any other acceptance criterion;
-   `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`;
-   `IMPLEMENTATION_NOTES.md`;
-   production code;
-   tests;
-   any other file.

Do not perform unrelated wording cleanup or formatting.

If inspection shows that fixing §47 point 4 necessarily requires another
change, do not expand scope silently. Report it and STOP.

------------------------------------------------------------------------

# 5. STEP 2 --- scope confirmation

Before editing, confirm:

1.  the exact file to be modified;
2.  the exact current §47 point 4 wording found in the repository;
3.  the exact proposed replacement wording;
4.  that no other section or file will be modified;
5.  any risk or remaining known documentation inconsistency.

Then STOP and wait for explicit approval.

Do not edit the file before approval.

------------------------------------------------------------------------

# 6. After explicit approval

After approval:

-   modify only the approved §47 point 4;
-   preserve all surrounding content and formatting;
-   do not change any other line unless mechanically unavoidable;
-   do not modify the implementation plan;
-   do not implement code;
-   do not modify tests.

After the edit, re-read:

-   §8.1;
-   §8.3;
-   §47 Acceptance Criteria --- UX point 4;

and verify that §47 point 4 now accurately tests the normative preview
behavior without introducing a new contradiction.

Report:

-   exact file modified;
-   exact old wording;
-   exact new wording;
-   confirmation that no other files changed;
-   confirmation that no production code/tests changed;
-   remaining known next step: separate synchronization of
    `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
    before production implementation.

No Gradle/build/test execution is required for this documentation-only
correction unless repository policy explicitly requires it.

Stop after completing this documentation consistency fix.
