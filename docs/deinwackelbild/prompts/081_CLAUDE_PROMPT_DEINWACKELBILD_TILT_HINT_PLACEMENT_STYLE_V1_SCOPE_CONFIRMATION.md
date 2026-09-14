# Claude Prompt — DeinWackelbild Tilt Hint Placement & Style — STEP 2 SCOPE CONFIRMATION

## Goal

Prepare **STEP 2 — SCOPE CONFIRMATION ONLY** for exactly one UI fix:

> Reposition and restyle the now-single-line `Tilt your phone` / swipe-fallback interaction hint.

Do not implement anything yet.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

The redundant subtitle has already been removed in the current working tree. This prompt must work from that new single-line-hint baseline.

---

# Mandatory pre-check

Before proposing scope:

1. Read and follow:
   - `CLAUDE_PROJECT_INSTRUCTION.md`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
   - any directly relevant responsive-layout specification.
2. Run `git status --short`.
3. Inspect current diffs carefully.
4. Confirm:
   - `wackelbild_hint_subtitle` is no longer rendered in the current working tree;
   - the primary hint remains;
   - the ridge/date-badge z-order fix is still a separate, unfinished task and must NOT be included here.
5. Do not modify or revert any existing pending subtitle-removal/test-correction hunks.

If unrelated tracked changes exist beyond the known pending Wackelbild work, report and STOP.

---

# Exact target UX

The final vertical order must become:

1. Preview
2. **8.dp**
3. Primary interaction hint:
   - sensor-capable branch: existing `Tilt your phone`
   - fallback branch: existing swipe fallback wording
4. **16.dp**
5. `Show date` row
6. optional existing date-unavailable helper text
7. existing lower content, including transfer disclosure and CTA

The primary interaction hint must be:

- single-line;
- centered;
- `bodySmall`;
- `SameViewSettingsSecondaryText`;
- permanently visible;
- no icon;
- no arrow / `↔`;
- no new string;
- no wording change;
- same sensor-vs-swipe fallback logic as today.

This iteration must only change the primary hint's **placement and visual prominence**.

---

# Current problem to analyze

The current screen still renders the interaction hint below the `Show date` group and with overly prominent styling.

Analyze exactly:

1. where `WackelbildInteractionHint(...)` is currently inserted;
2. which parent currently owns it;
3. which current spacers create the existing layout;
4. which typography/color/alignment are currently applied;
5. how to move the hint directly below the preview without altering preview sizing or the lower-scroll behavior;
6. how the existing portrait-density solution interacts with this move;
7. whether the hint should remain inside or move outside the scrollable lower-controls column;
8. whether the exact target `8.dp` preview→hint and `16.dp` hint→date-group can be implemented with existing simple Compose layout primitives, without custom measurement;
9. whether tablet / Expanded layout behavior remains correct.

Prefer the smallest possible Compose change.

Do not introduce:
- `SubcomposeLayout`;
- custom measurement state;
- device-specific branches;
- new responsive architecture.

---

# Source-of-Truth consistency

The previous analysis identified a likely conflict in `DEINWACKELBILD_INTEGRATION_V1.md`:

- §9.9 currently places the interaction hint after the date row / date area.

Inspect the current file and confirm exactly which section(s) contradict the newly approved UX.

The Source of Truth should be updated only where needed to document:

- preview first;
- 8.dp to the interaction hint;
- 16.dp from hint to date controls;
- subtle styling (`bodySmall`, secondary text, centered);
- unchanged sensor/swipe wording and behavior.

Do not touch unrelated copy or sections.

Also inspect whether §8.6 / §45 already reflect the subtitle removal and therefore need no further change here.

---

# Test scope

Inspect `WackelbildScreenTest.kt` and identify only the tests that must change because the hint physically moves above the date group.

At minimum, inspect:

- the existing interaction-hint visibility test;
- both `dateGroupToHintGroup_spacingIsApproximatelySixteenDp_*` tests;
- any test that assumes the old date-before-hint order;
- any geometry/order assertion involving preview, hint, date row, or CTA.

Determine the minimal test changes needed to assert the new contract.

Preferred geometry assertions:

- preview bottom → hint top ≈ 8.dp;
- hint bottom → date-group top ≈ 16.dp;
- primary hint visible;
- CTA remains visible in the already validated portrait case if an existing test already covers that outcome.

Do not add broad snapshot tests or unrelated coverage.

---

# Verification policy — IMPORTANT

For this isolated UI change, **do not propose the full project test suite**.

The later STEP 3 verification should be limited to the smallest relevant scope:

1. `WackelbildScreenTest` instrumentation class, or filtered affected tests if reliable;
2. real-device visual validation on the connected Samsung.

Do NOT require:
- full `connectedDebugAndroidTest`;
- full `testDebugUnitTest`;
- full `lintDebug`;
- full `assembleDebug`;

unless the analysis finds a concrete technical reason why this specific change cannot be verified safely without one of them. If so, explain that reason explicitly before proposing it.

The user has explicitly established this project rule:
> Small isolated changes use the smallest relevant test scope, not the full suite by default.

---

# Explicitly out of scope

Do not include:

- subtitle removal;
- ridge/date-badge z-order;
- ridge alpha/spacing;
- border/radius;
- preview sizing;
- blend behavior;
- perspective;
- sensor math/lifecycle;
- date badge styling;
- date toggle behavior;
- transfer disclosure;
- Privacy Policy;
- upload/API/handoff;
- navigation/storage/permissions;
- unrelated strings;
- unrelated docs;
- refactors/cleanup.

---

# Expected likely file scope

Confirm against the current repository. Expected files are likely:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
3. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

If any fourth file is genuinely required, explain why and STOP for approval.

---

# Required STEP 2 output

Return:

1. current `git status --short`;
2. confirmation that the subtitle is already absent in the current working tree;
3. current interaction-hint placement and styling;
4. exact root cause of the wrong hierarchy/prominence;
5. exact proposed Compose move;
6. exact spacer changes;
7. exact typography/color/alignment changes;
8. exact sensor/swipe behavior preserved;
9. exact files to modify;
10. exact change in each file;
11. exact tests to change/add;
12. exact Source-of-Truth sections to amend;
13. tablet / Expanded-layout impact;
14. regression risks;
15. exact minimal verification commands for STEP 3;
16. real-device validation requirements;
17. confirmation that ridge/date-badge z-order remains out of scope;
18. any blocker requiring user input.

Then STOP and wait for explicit approval.

Do not modify files.
Do not stage.
Do not commit.
Do not push.
